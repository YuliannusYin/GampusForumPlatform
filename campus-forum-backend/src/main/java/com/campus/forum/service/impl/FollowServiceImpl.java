package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.entity.Follow;
import com.campus.forum.mapper.FollowMapper;
import com.campus.forum.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关注关系服务实现类
 * 封装关注/取关、关注状态查询及关注/粉丝数统计
 * 关注幂等：已存在关注记录则直接返回，避免重复关注
 * 取关通过 @TableLogic 逻辑删除
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    /**
     * 关注用户（幂等，不能关注自己）
     * 已存在关注记录则直接返回，否则新增
     *
     * @param followerId  关注者用户ID
     * @param followingId 被关注者用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "用户ID不能为空");
        }
        if (followerId.equals(followingId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能关注自己");
        }
        // 查询是否已存在关注记录（@TableLogic 自动过滤已删除）
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFollowingId, followingId));
        if (count != null && count > 0) {
            // 幂等：已关注则直接返回
            return;
        }
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        baseMapper.insert(follow);
    }

    /**
     * 取消关注（逻辑删除）
     *
     * @param followerId  关注者用户ID
     * @param followingId 被关注者用户ID
     */
    @Override
    public void unfollow(Long followerId, Long followingId) {
        baseMapper.delete(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFollowingId, followingId));
    }

    /**
     * 查询是否已关注
     *
     * @param followerId  关注者用户ID
     * @param followingId 被关注者用户ID
     * @return true 表示已关注
     */
    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFollowingId, followingId));
        return count != null && count > 0;
    }

    /**
     * 统计用户的关注数
     *
     * @param userId 用户ID
     * @return 关注数
     */
    @Override
    public long countFollowings(Long userId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, userId));
        return count == null ? 0L : count;
    }

    /**
     * 统计用户的粉丝数
     *
     * @param userId 用户ID
     * @return 粉丝数
     */
    @Override
    public long countFollowers(Long userId) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowingId, userId));
        return count == null ? 0L : count;
    }

}
