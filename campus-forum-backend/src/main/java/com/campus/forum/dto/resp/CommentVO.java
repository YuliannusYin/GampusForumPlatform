package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论视图对象
 * 用于评论列表与回复列表展示，联查 user 表获取评论者用户名和头像
 *
 * @author campus
 */
@Data
@Schema(description = "评论视图")
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @Schema(description = "评论ID", example = "1")
    private Long id;

    /** 帖子ID */
    @Schema(description = "帖子ID", example = "10")
    private Long postId;

    /** 评论者ID */
    @Schema(description = "评论者ID", example = "1")
    private Long userId;

    /** 评论者用户名 */
    @Schema(description = "评论者用户名", example = "admin")
    private String username;

    /** 评论者头像URL */
    @Schema(description = "评论者头像URL", example = "/uploads/avatar/1.png")
    private String userAvatar;

    /** 父评论ID，0表示顶级评论 */
    @Schema(description = "父评论ID，0表示顶级评论", example = "0")
    private Long parentId;

    /** 评论内容 */
    @Schema(description = "评论内容", example = "写得很不错，学到很多！")
    private String content;

    /** 点赞数 */
    @Schema(description = "点赞数", example = "5")
    private Integer likeCount;

    /** 回复数（子回复数，仅顶级评论有意义） */
    @Schema(description = "回复数", example = "3")
    private Integer replyCount;

    /** 创建时间 */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

}
