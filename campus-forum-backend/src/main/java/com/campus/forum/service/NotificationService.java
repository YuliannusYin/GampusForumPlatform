package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.resp.NotificationVO;
import com.campus.forum.entity.Notification;

/**
 * 通知服务接口
 * 封装通知生成、查询、标记已读、删除等业务逻辑
 * 其中 createNotification 为公共服务，供评论（Task 7）、点赞（Task 8）、私信（Task 13）模块调用
 *
 * @author campus
 */
public interface NotificationService extends IService<Notification> {

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
    void createNotification(Long userId, Long fromUserId, Integer type, String content, Long targetId, Integer targetType);

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
    PageResult<NotificationVO> listNotifications(Long userId, long page, long size, Integer type, Integer isRead);

    /**
     * 查询当前用户未读通知数量
     *
     * @param userId 当前用户ID
     * @return 未读通知数量
     */
    int countUnread(Long userId);

    /**
     * 标记单条通知为已读
     * 校验：通知必须属于当前用户
     *
     * @param id     通知ID
     * @param userId 当前用户ID
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记当前用户所有未读通知为已读
     * SQL 语义：UPDATE notification SET is_read=1 WHERE user_id=? AND is_read=0 AND deleted=0
     *
     * @param userId 当前用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除单条通知（逻辑删除）
     * 校验：通知必须属于当前用户
     *
     * @param id     通知ID
     * @param userId 当前用户ID
     */
    void deleteNotification(Long id, Long userId);

}
