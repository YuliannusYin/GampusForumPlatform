package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.entity.Follow;

/**
 * 关注关系服务接口
 * 封装关注/取关、关注状态查询及关注/粉丝数统计
 *
 * @author campus
 */
public interface FollowService extends IService<Follow> {

    /**
     * 关注用户（幂等，不能关注自己）
     * 已存在关注记录则直接返回，否则新增
     *
     * @param followerId  关注者用户ID
     * @param followingId 被关注者用户ID
     */
    void follow(Long followerId, Long followingId);

    /**
     * 取消关注（逻辑删除）
     *
     * @param followerId  关注者用户ID
     * @param followingId 被关注者用户ID
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 查询是否已关注
     *
     * @param followerId  关注者用户ID
     * @param followingId 被关注者用户ID
     * @return true 表示已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 统计用户的关注数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    long countFollowings(Long userId);

    /**
     * 统计用户的粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    long countFollowers(Long userId);

}
