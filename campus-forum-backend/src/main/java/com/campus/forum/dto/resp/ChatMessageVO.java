package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信消息视图对象
 * 用于消息列表展示与 WebSocket 实时推送，联查 user 表获取发送者用户名和头像
 *
 * @author campus
 */
@Data
@Schema(description = "私信消息视图")
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    @Schema(description = "消息ID", example = "1")
    private Long id;

    /** 会话ID */
    @Schema(description = "会话ID", example = "1")
    private Long sessionId;

    /** 发送者ID */
    @Schema(description = "发送者ID", example = "1")
    private Long senderId;

    /** 发送者用户名 */
    @Schema(description = "发送者用户名", example = "admin")
    private String senderUsername;

    /** 发送者头像URL */
    @Schema(description = "发送者头像URL", example = "/uploads/avatar/1.png")
    private String senderAvatar;

    /** 接收者ID */
    @Schema(description = "接收者ID", example = "2")
    private Long receiverId;

    /** 消息内容 */
    @Schema(description = "消息内容", example = "你好")
    private String content;

    /** 是否已读 0未读 1已读 */
    @Schema(description = "是否已读 0未读 1已读", example = "0")
    private Integer isRead;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

}
