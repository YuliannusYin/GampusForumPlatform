package com.campus.forum.service;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.resp.ChatMessageVO;
import com.campus.forum.dto.resp.ChatSessionVO;

import java.util.List;

/**
 * 私信服务接口
 * 封装发送私信、会话列表、历史消息、未读数、标记已读、在线状态等业务逻辑
 *
 * @author campus
 */
public interface ChatService {

    /**
     * 发送私信
     * - 校验接收者存在且不等于发送者
     * - 计算或创建会话（约定 user1_id < user2_id）
     * - 保存消息并更新会话最后消息信息
     * - 若接收者在线，通过 WebSocket 实时推送
     * - 创建通知（type=3 私信）
     *
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @return 消息视图
     */
    ChatMessageVO sendMessage(Long receiverId, String content);

    /**
     * 查询当前用户的会话列表
     *
     * @param userId 当前用户ID
     * @return 会话视图列表
     */
    List<ChatSessionVO> listSessions(Long userId);

    /**
     * 分页查询会话历史消息（按 create_time 正序）
     *
     * @param sessionId 会话ID
     * @param page      当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    PageResult<ChatMessageVO> listMessages(Long sessionId, long page, long size);

    /**
     * 查询当前用户未读私信总数
     *
     * @param userId 当前用户ID
     * @return 未读私信总数
     */
    int countUnread(Long userId);

    /**
     * 标记某会话中发送给当前用户的所有未读消息为已读
     *
     * @param sessionId 会话ID
     * @param userId    当前用户ID
     */
    void markRead(Long sessionId, Long userId);

    /**
     * 判断用户是否在线（Redis 中是否存在 online:{userId}）
     *
     * @param userId 用户ID
     * @return 在线返回 true
     */
    boolean isOnline(Long userId);

}
