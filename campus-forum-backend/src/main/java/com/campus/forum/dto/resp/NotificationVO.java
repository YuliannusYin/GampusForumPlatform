package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知视图对象
 * 用于通知列表展示，联查 user 表获取发送者用户名和头像
 *
 * @author campus
 */
@Data
@Schema(description = "通知视图")
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知ID */
    @Schema(description = "通知ID", example = "1")
    private Long id;

    /** 接收者ID */
    @Schema(description = "接收者ID", example = "2")
    private Long userId;

    /** 发送者ID */
    @Schema(description = "发送者ID", example = "1")
    private Long fromUserId;

    /** 发送者用户名 */
    @Schema(description = "发送者用户名", example = "admin")
    private String fromUsername;

    /** 发送者头像URL */
    @Schema(description = "发送者头像URL", example = "/uploads/avatar/1.png")
    private String fromUserAvatar;

    /** 类型 1评论 2点赞 3私信 4系统 */
    @Schema(description = "通知类型 1评论 2点赞 3私信 4系统", example = "1")
    private Integer type;

    /** 通知内容 */
    @Schema(description = "通知内容", example = "admin 评论了你的帖子")
    private String content;

    /** 目标对象ID */
    @Schema(description = "目标对象ID", example = "10")
    private Long targetId;

    /** 目标类型 */
    @Schema(description = "目标类型", example = "1")
    private Integer targetType;

    /** 是否已读 0未读 1已读 */
    @Schema(description = "是否已读 0未读 1已读", example = "0")
    private Integer isRead;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

}
