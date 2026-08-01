package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 测试数据移除结果视图对象
 * 记录一键移除后各类测试数据的实际删除数量
 *
 * @author campus
 */
@Data
@Schema(description = "测试数据移除结果")
public class TestDataRemoveResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 移除的用户数量 */
    @Schema(description = "移除的用户数量", example = "100")
    private long users;

    /** 移除的帖子数量 */
    @Schema(description = "移除的帖子数量", example = "500")
    private long posts;

    /** 移除的评论数量 */
    @Schema(description = "移除的评论数量", example = "1000")
    private long comments;

    /** 移除的社团数量 */
    @Schema(description = "移除的社团数量", example = "10")
    private long clubs;

    /** 移除的社团成员数量 */
    @Schema(description = "移除的社团成员数量", example = "100")
    private long clubMembers;

    /** 移除的社团帖子关联数量 */
    @Schema(description = "移除的社团帖子关联数量", example = "100")
    private long clubPosts;

    /** 移除的点赞记录数量 */
    @Schema(description = "移除的点赞记录数量", example = "800")
    private long likes;

    /** 移除的收藏记录数量 */
    @Schema(description = "移除的收藏记录数量", example = "500")
    private long favorites;

    /** 移除的关注关系数量 */
    @Schema(description = "移除的关注关系数量", example = "300")
    private long follows;

    /** 移除的私信会话数量 */
    @Schema(description = "移除的私信会话数量", example = "50")
    private long chatSessions;

    /** 移除的私信消息数量 */
    @Schema(description = "移除的私信消息数量", example = "200")
    private long chatMessages;

    /** 移除的通知数量 */
    @Schema(description = "移除的通知数量", example = "150")
    private long notifications;

}
