package com.campus.forum.service;

import java.util.Map;

/**
 * 点赞服务接口
 * 封装帖子与评论的点赞/取消点赞逻辑，以及当前用户的点赞状态查询
 * 点赞幂等：基于 like_record 表唯一索引 (user_id, target_id, target_type)，
 * 逻辑删除后再次点赞通过切换 deleted 字段恢复，避免唯一索引冲突
 *
 * @author campus
 */
public interface LikeService {

    /**
     * 切换当前用户对帖子的点赞状态
     * 已点赞则取消（逻辑删除记录，post.like_count -1，不为负）；
     * 未点赞则新建/恢复记录（post.like_count +1），并通知帖子作者、给作者加 2 积分（被赞奖励）
     *
     * @param postId 帖子ID
     * @return 包含 liked（当前是否已赞）与 likeCount（帖子最新点赞数）的 Map
     */
    Map<String, Object> toggleLikePost(Long postId);

    /**
     * 切换当前用户对评论的点赞状态
     * 已点赞则取消（逻辑删除记录，comment.like_count -1，不为负）；
     * 未点赞则新建/恢复记录（comment.like_count +1），并通知评论作者、给作者加 2 积分（被赞奖励）
     *
     * @param commentId 评论ID
     * @return 包含 liked（当前是否已赞）与 likeCount（评论最新点赞数）的 Map
     */
    Map<String, Object> toggleLikeComment(Long commentId);

    /**
     * 查询当前用户是否已点赞某帖子
     *
     * @param postId 帖子ID
     * @return true 表示当前用户已点赞该帖子
     */
    boolean isLikedPost(Long postId);

    /**
     * 查询当前用户是否已点赞某评论
     *
     * @param commentId 评论ID
     * @return true 表示当前用户已点赞该评论
     */
    boolean isLikedComment(Long commentId);

}
