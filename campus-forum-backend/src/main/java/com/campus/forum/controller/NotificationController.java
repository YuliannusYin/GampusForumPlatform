package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.NotificationVO;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知接口
 * 提供通知列表查询、未读数量、标记已读、删除等功能
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知接口", description = "通知列表、未读数量、标记已读、删除")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询当前用户的通知列表（按 create_time 倒序）
     *
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @param type   通知类型（可选，1评论 2点赞 3私信 4系统）
     * @param isRead 是否已读（可选，0未读 1已读）
     * @return 分页结果
     */
    @Operation(summary = "通知分页列表", description = "按 create_time 倒序，支持按类型与已读状态筛选，联查发送者用户名与头像")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public Result<PageResult<NotificationVO>> listNotifications(@Parameter(description = "当前页码，默认 1")
                                                                @RequestParam(value = "page", defaultValue = "1") long page,
                                                                @Parameter(description = "每页条数，默认 10")
                                                                @RequestParam(value = "size", defaultValue = "10") long size,
                                                                @Parameter(description = "通知类型（可选，1评论 2点赞 3私信 4系统）")
                                                                @RequestParam(value = "type", required = false) Integer type,
                                                                @Parameter(description = "是否已读（可选，0未读 1已读）")
                                                                @RequestParam(value = "isRead", required = false) Integer isRead) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResult<NotificationVO> result = notificationService.listNotifications(userId, page, size, type, isRead);
        return Result.success(result);
    }

    /**
     * 查询当前用户未读通知数量
     *
     * @return 未读通知数量
     */
    @Operation(summary = "未读通知数量", description = "查询当前用户的未读通知总数")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread/count")
    public Result<Integer> countUnread() {
        Long userId = SecurityUtils.getCurrentUserId();
        int count = notificationService.countUnread(userId);
        return Result.success(count);
    }

    /**
     * 标记单条通知为已读
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "标记单条通知已读", description = "仅可操作自己的通知")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@Parameter(description = "通知ID", required = true)
                                   @PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 标记当前用户所有未读通知为已读
     *
     * @return 操作结果
     */
    @Operation(summary = "标记全部通知已读", description = "将当前用户所有未读通知标记为已读")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/read/all")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除单条通知（逻辑删除）
     *
     * @param id 通知ID
     * @return 操作结果
     */
    @Operation(summary = "删除通知", description = "逻辑删除，仅可操作自己的通知")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@Parameter(description = "通知ID", required = true)
                                           @PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }

}
