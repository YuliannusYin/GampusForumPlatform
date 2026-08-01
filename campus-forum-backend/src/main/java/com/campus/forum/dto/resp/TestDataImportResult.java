package com.campus.forum.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 测试数据导入结果视图对象
 * 记录一键导入后各类测试数据的实际生成数量
 *
 * @author campus
 */
@Data
@Schema(description = "测试数据导入结果")
public class TestDataImportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 导入的用户数量 */
    @Schema(description = "导入的用户数量", example = "100")
    private long users;

    /** 导入的帖子数量 */
    @Schema(description = "导入的帖子数量", example = "500")
    private long posts;

    /** 导入的评论数量 */
    @Schema(description = "导入的评论数量", example = "1000")
    private long comments;

    /** 导入的社团数量 */
    @Schema(description = "导入的社团数量", example = "10")
    private long clubs;

    /** 导入的社团成员数量 */
    @Schema(description = "导入的社团成员数量", example = "100")
    private long clubMembers;

    /** 导入的社团帖子关联数量 */
    @Schema(description = "导入的社团帖子关联数量", example = "100")
    private long clubPosts;

    /** 导入的点赞记录数量 */
    @Schema(description = "导入的点赞记录数量", example = "800")
    private long likes;

    /** 导入的收藏记录数量 */
    @Schema(description = "导入的收藏记录数量", example = "500")
    private long favorites;

    /** 导入的关注关系数量 */
    @Schema(description = "导入的关注关系数量", example = "300")
    private long follows;

    /** 导入的私信会话数量 */
    @Schema(description = "导入的私信会话数量", example = "50")
    private long chatSessions;

    /** 导入的私信消息数量 */
    @Schema(description = "导入的私信消息数量", example = "200")
    private long chatMessages;

    /** 导入的通知数量 */
    @Schema(description = "导入的通知数量", example = "150")
    private long notifications;

}
