package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.NotificationVO;
import com.campus.forum.entity.Notification;
import com.campus.forum.mapper.NotificationMapper;
import com.campus.forum.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知服务实现类
 * 封装通知生成、查询、标记已读、删除等业务逻辑
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 创建通知（公共服务，供其他模块调用）
     * 若 userId == fromUserId（自己给自己的操作，如自己评论自己帖子），则不创建通知
     *
     * @param userId     接收者ID
     * @param fromUserId 发送者ID
     * @param type       通知类型 1评论 2点赞 3私信 4系统
     * @param content    通知内容
     * @param targetId   目标对象ID
     * @param targetType 目标类型
     */
    @Override
    public void createNotification(Long userId, Long fromUserId, Integer type, String content, Long targetId, Integer targetType) {
        // 接收者为空或自己给自己的操作不产生通知
        if (userId == null || userId.equals(fromUserId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setContent(content);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    /**
     * 分页查询当前用户的通知列表（按 create_time 倒序）
     *
     * @param userId 当前用户ID
     * @param page   当前页码
     * @param size   每页条数
     * @param type   通知类型（可选，1评论 2点赞 3私信 4系统）
     * @param isRead 是否已读（可选，0未读 1已读）
     * @return 分页结果
     */
    @Override
    public PageResult<NotificationVO> listNotifications(Long userId, long page, long size, Integer type, Integer isRead) {
        Page<NotificationVO> p = new Page<>(page, size);
        IPage<NotificationVO> result = notificationMapper.selectNotificationList(p, userId, type, isRead);
        return PageResult.of(result);
    }

    /**
     * 查询当前用户未读通知数量
     *
     * @param userId 当前用户ID
     * @return 未读通知数量
     */
    @Override
    public int countUnread(Long userId) {
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 标记单条通知为已读
     * 校验：通知必须属于当前用户
     *
     * @param id     通知ID
     * @param userId 当前用户ID
     */
    @Override
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        // 校验通知归属
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人通知");
        }
        // 已读则无需重复更新
        if (notification.getIsRead() != null && notification.getIsRead() == 1) {
            return;
        }
        // 仅更新 is_read 字段，updateTime 由 MetaObjectHandler 自动填充
        Notification update = new Notification();
        update.setId(id);
        update.setIsRead(1);
        notificationMapper.updateById(update);
    }

    /**
     * 标记当前用户所有未读通知为已读
     * SQL 语义：UPDATE notification SET is_read=1 WHERE user_id=? AND is_read=0 AND deleted=0
     *
     * @param userId 当前用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    /**
     * 删除单条通知（逻辑删除）
     * 校验：通知必须属于当前用户
     *
     * @param id     通知ID
     * @param userId 当前用户ID
     */
    @Override
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        // 校验通知归属
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人通知");
        }
        // 逻辑删除（MyBatis-Plus @TableLogic 自动处理）
        notificationMapper.deleteById(id);
    }

}
