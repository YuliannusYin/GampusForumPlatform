package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 测试数据状态视图对象
 * 表示当前系统中各类测试数据的现存数量
 *
 * @author campus
 */
@Data
@Schema(description = "测试数据状态")
public class TestDataStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 测试用户数量 */
    @Schema(description = "测试用户数量", example = "100")
    private long users;

    /** 测试帖子数量 */
    @Schema(description = "测试帖子数量", example = "500")
    private long posts;

    /** 测试评论数量 */
    @Schema(description = "测试评论数量", example = "1000")
    private long comments;

    /** 测试社团数量 */
    @Schema(description = "测试社团数量", example = "10")
    private long clubs;

    /** 测试社团成员数量 */
    @Schema(description = "测试社团成员数量", example = "100")
    private long clubMembers;

    /** 测试社团帖子关联数量 */
    @Schema(description = "测试社团帖子关联数量", example = "100")
    private long clubPosts;

    /** 测试点赞记录数量 */
    @Schema(description = "测试点赞记录数量", example = "800")
    private long likes;

    /** 测试收藏记录数量 */
    @Schema(description = "测试收藏记录数量", example = "500")
    private long favorites;

    /** 测试关注关系数量 */
    @Schema(description = "测试关注关系数量", example = "300")
    private long follows;

    /** 测试私信会话数量 */
    @Schema(description = "测试私信会话数量", example = "50")
    private long chatSessions;

    /** 测试私信消息数量 */
    @Schema(description = "测试私信消息数量", example = "200")
    private long chatMessages;

    /** 测试通知数量 */
    @Schema(description = "测试通知数量", example = "150")
    private long notifications;

}
