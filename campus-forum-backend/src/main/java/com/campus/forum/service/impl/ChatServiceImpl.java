package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.ChatMessageVO;
import com.campus.forum.dto.resp.ChatSessionVO;
import com.campus.forum.entity.ChatMessage;
import com.campus.forum.entity.ChatSession;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.ChatMessageMapper;
import com.campus.forum.mapper.ChatSessionMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.ChatService;
import com.campus.forum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 私信服务实现类
 * 封装发送私信、会话列表、历史消息、未读数、标记已读、在线状态等业务逻辑
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /** Redis 在线状态 key 前缀，完整 key 为 online:{userId} */
    private static final String ONLINE_KEY_PREFIX = "online:";

    /** WebSocket 推送目的地（用户级） */
    private static final String QUEUE_DESTINATION = "/queue/chat";

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 发送私信
     *
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @return 消息视图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVO sendMessage(Long receiverId, String content) {
        Long senderId = com.campus.forum.security.SecurityUtils.getCurrentUserId();

        // 校验接收者存在且不等于发送者
        if (receiverId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "接收者不能为空");
        }
        if (receiverId.equals(senderId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能给自己发送私信");
        }
        User receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "接收者不存在");
        }

        // 计算会话双方：user1=min，user2=max
        long user1Id = Math.min(senderId, receiverId);
        long user2Id = Math.max(senderId, receiverId);

        // 查询或创建会话
        ChatSession session = chatSessionMapper.selectByUsers(user1Id, user2Id);
        if (session == null) {
            session = new ChatSession();
            session.setUser1Id(user1Id);
            session.setUser2Id(user2Id);
            chatSessionMapper.insert(session);
        }

        // 保存消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);
        chatMessageMapper.insert(message);

        // 更新会话最后消息信息
        Date now = message.getCreateTime() != null ? message.getCreateTime() : new Date();
        ChatSession sessionUpdate = new ChatSession();
        sessionUpdate.setId(session.getId());
        sessionUpdate.setLastMessageId(message.getId());
        sessionUpdate.setLastMessageTime(now);
        chatSessionMapper.updateById(sessionUpdate);

        // 构建消息视图（联查发送者用户名与头像）
        User sender = userMapper.selectById(senderId);
        ChatMessageVO vo = toMessageVO(message, sender);

        // 若接收者在线，通过 WebSocket 实时推送
        if (isOnline(receiverId)) {
            try {
                messagingTemplate.convertAndSendToUser(receiverId.toString(), QUEUE_DESTINATION, vo);
            } catch (Exception e) {
                log.warn("WebSocket 推送私信失败，receiverId={}，messageId={}：{}",
                        receiverId, message.getId(), e.getMessage());
            }
        }

        // 通知接收者（type=3 私信，targetType=3）
        notificationService.createNotification(receiverId, senderId, 3, "收到一条私信", message.getId(), 3);

        return vo;
    }

    /**
     * 查询当前用户的会话列表
     *
     * @param userId 当前用户ID
     * @return 会话视图列表
     */
    @Override
    public List<ChatSessionVO> listSessions(Long userId) {
        return chatSessionMapper.selectSessionList(userId);
    }

    /**
     * 分页查询会话历史消息（按 create_time 正序）
     *
     * @param sessionId 会话ID
     * @param page      当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<ChatMessageVO> listMessages(Long sessionId, long page, long size) {
        Page<ChatMessageVO> p = new Page<>(page, size);
        IPage<ChatMessageVO> result = chatMessageMapper.selectMessagePage(p, sessionId);
        return PageResult.of(result);
    }

    /**
     * 查询当前用户未读私信总数
     *
     * @param userId 当前用户ID
     * @return 未读私信总数
     */
    @Override
    public int countUnread(Long userId) {
        Long count = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 标记某会话中发送给当前用户的所有未读消息为已读
     *
     * @param sessionId 会话ID
     * @param userId    当前用户ID
     */
    @Override
    public void markRead(Long sessionId, Long userId) {
        chatMessageMapper.markRead(sessionId, userId);
    }

    /**
     * 判断用户是否在线（Redis 中是否存在 online:{userId}）
     *
     * @param userId 用户ID
     * @return 在线返回 true
     */
    @Override
    public boolean isOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Boolean exists = redisTemplate.hasKey(ONLINE_KEY_PREFIX + userId);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 将消息实体与发送者用户信息组装为消息视图
     *
     * @param message 消息实体
     * @param sender  发送者用户实体
     * @return 消息视图
     */
    private ChatMessageVO toMessageVO(ChatMessage message, User sender) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        if (sender != null) {
            vo.setSenderUsername(sender.getUsername());
            vo.setSenderAvatar(sender.getAvatar());
        }
        vo.setReceiverId(message.getReceiverId());
        vo.setContent(message.getContent());
        vo.setIsRead(message.getIsRead());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

}
