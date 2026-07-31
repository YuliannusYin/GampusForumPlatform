package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信会话视图对象
 * 用于会话列表展示，包含另一用户信息、最后消息内容与未读数
 *
 * @author campus
 */
@Data
@Schema(description = "私信会话视图")
public class ChatSessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话ID */
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /** 另一用户ID */
    @Schema(description = "另一用户ID", example = "2")
    private Long otherUserId;

    /** 另一用户名 */
    @Schema(description = "另一用户名", example = "alice")
    private String otherUsername;

    /** 另一用户头像URL */
    @Schema(description = "另一用户头像URL", example = "/uploads/avatar/2.png")
    private String otherAvatar;

    /** 最后消息内容 */
    @Schema(description = "最后消息内容", example = "在吗？")
    private String lastMessageContent;

    /** 最后消息时间 */
    @Schema(description = "最后消息时间", example = "2024-01-01 12:00:00")
    private Date lastMessageTime;

    /** 未读消息数 */
    @Schema(description = "未读消息数", example = "3")
    private Integer unreadCount;

}
