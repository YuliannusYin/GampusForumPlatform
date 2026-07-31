package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.entity.Favorite;
import com.campus.forum.entity.Post;
import com.campus.forum.mapper.FavoriteMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 收藏服务实现类
 * 实现帖子的收藏/取消收藏逻辑以及收藏状态查询
 * 通过 favorite 表的 deleted 字段切换实现幂等收藏，避免唯一索引冲突
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    /** 逻辑删除：未删除 */
    private static final int NOT_DELETED = 0;

    /** 逻辑删除：已删除 */
    private static final int DELETED = 1;

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    /**
     * 切换当前用户对帖子的收藏状态
     *
     * @param postId 帖子ID
     * @return 包含 favorited 与 favoriteCount 的 Map
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleFavorite(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 校验帖子存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 查询收藏记录（忽略 deleted 标记，避免唯一索引冲突）
        Favorite record = favoriteMapper.selectByUserAndPostIgnoreDeleted(userId, postId);

        Map<String, Object> result = new HashMap<>(2);
        if (record != null && record.getDeleted() != null && record.getDeleted() == NOT_DELETED) {
            // 已收藏 -> 取消收藏：逻辑删除记录，post.favorite_count -1（不为负）
            favoriteMapper.updateDeletedById(record.getId(), DELETED);
            postMapper.updateFavoriteCount(postId, -1);
            result.put("favorited", false);
        } else {
            // 未收藏 -> 收藏：新建或恢复记录，post.favorite_count +1
            if (record == null) {
                Favorite newRecord = new Favorite();
                newRecord.setUserId(userId);
                newRecord.setPostId(postId);
                favoriteMapper.insert(newRecord);
            } else {
                // 恢复已逻辑删除的记录
                favoriteMapper.updateDeletedById(record.getId(), NOT_DELETED);
            }
            postMapper.updateFavoriteCount(postId, 1);
            result.put("favorited", true);
        }

        // 重新查询帖子获取最新收藏数
        Post latest = postMapper.selectById(postId);
        result.put("favoriteCount", latest != null && latest.getFavoriteCount() != null ? latest.getFavoriteCount() : 0);
        return result;
    }

    /**
     * 查询当前用户是否已收藏某帖子
     *
     * @param postId 帖子ID
     * @return true 表示当前用户已收藏该帖子
     */
    @Override
    public boolean isFavorited(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getPostId, postId));
        return count != null && count > 0;
    }

}
