package com.campus.forum.service;

import java.util.Map;

/**
 * 收藏服务接口
 * 封装帖子的收藏/取消收藏逻辑
 * 收藏幂等：基于 favorite 表唯一索引 (user_id, post_id)，
 * 逻辑删除后再次收藏通过切换 deleted 字段恢复，避免唯一索引冲突
 *
 * @author campus
 */
public interface FavoriteService {

    /**
     * 切换当前用户对帖子的收藏状态
     * 已收藏则取消（逻辑删除记录，post.favorite_count -1，不为负）；
     * 未收藏则新建/恢复记录（post.favorite_count +1）
     *
     * @param postId 帖子ID
     * @return 包含 favorited（当前是否已收藏）与 favoriteCount（帖子最新收藏数）的 Map
     */
    Map<String, Object> toggleFavorite(Long postId);

    /**
     * 查询当前用户是否已收藏某帖子
     *
     * @param postId 帖子ID
     * @return true 表示当前用户已收藏该帖子
     */
    boolean isFavorited(Long postId);

}
