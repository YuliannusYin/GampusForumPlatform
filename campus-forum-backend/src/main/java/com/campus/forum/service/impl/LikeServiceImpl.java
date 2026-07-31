package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.LikeRecord;
import com.campus.forum.entity.Post;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.LikeRecordMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.LikeService;
import com.campus.forum.service.NotificationService;
import com.campus.forum.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞服务实现类
 * 实现帖子与评论的点赞/取消点赞逻辑，以及点赞状态查询
 * 通过 like_record 表的 deleted 字段切换实现幂等点赞，避免唯一索引冲突
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    /** 目标类型：帖子 */
    private static final int TARGET_TYPE_POST = 1;

    /** 目标类型：评论 */
    private static final int TARGET_TYPE_COMMENT = 2;

    /** 通知类型：点赞 */
    private static final int NOTIFICATION_TYPE_LIKE = 2;

    /** 积分类型：被赞 */
    private static final int POINTS_TYPE_LIKED = 4;

    /** 被赞奖励积分值 */
    private static final int LIKED_REWARD_POINTS = 2;

    /** 逻辑删除：未删除 */
    private static final int NOT_DELETED = 0;

    /** 逻辑删除：已删除 */
    private static final int DELETED = 1;

    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    private final PointsService pointsService;

    /**
     * 切换当前用户对帖子的点赞状态
     *
     * @param postId 帖子ID
     * @return 包含 liked 与 likeCount 的 Map
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLikePost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 校验帖子存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 查询点赞记录（忽略 deleted 标记，避免唯一索引冲突）
        LikeRecord record = likeRecordMapper.selectByUserAndTargetIgnoreDeleted(userId, postId, TARGET_TYPE_POST);

        Map<String, Object> result = new HashMap<>(2);
        if (record != null && record.getDeleted() != null && record.getDeleted() == NOT_DELETED) {
            // 已点赞 -> 取消点赞：逻辑删除记录，post.like_count -1（不为负）
            likeRecordMapper.updateDeletedById(record.getId(), DELETED);
            postMapper.updateLikeCount(postId, -1);
            result.put("liked", false);
        } else {
            // 未点赞 -> 点赞：新建或恢复记录，post.like_count +1
            if (record == null) {
                LikeRecord newRecord = new LikeRecord();
                newRecord.setUserId(userId);
                newRecord.setTargetId(postId);
                newRecord.setTargetType(TARGET_TYPE_POST);
                likeRecordMapper.insert(newRecord);
            } else {
                // 恢复已逻辑删除的记录
                likeRecordMapper.updateDeletedById(record.getId(), NOT_DELETED);
            }
            postMapper.updateLikeCount(postId, 1);

            // 通知帖子作者并给作者加积分（避免自己赞自己）
            Long authorId = post.getUserId();
            if (!userId.equals(authorId)) {
                notificationService.createNotification(authorId, userId, NOTIFICATION_TYPE_LIKE,
                        "赞了你的帖子", postId, TARGET_TYPE_POST);
                try {
                    pointsService.addPoints(authorId, LIKED_REWARD_POINTS, POINTS_TYPE_LIKED, "被点赞奖励");
                } catch (Exception e) {
                    // 积分奖励失败不影响点赞主流程
                    log.warn("被点赞奖励积分失败，userId={}", authorId, e);
                }
            }
            result.put("liked", true);
        }

        // 重新查询帖子获取最新点赞数
        Post latest = postMapper.selectById(postId);
        result.put("likeCount", latest != null && latest.getLikeCount() != null ? latest.getLikeCount() : 0);
        return result;
    }

    /**
     * 切换当前用户对评论的点赞状态
     *
     * @param commentId 评论ID
     * @return 包含 liked 与 likeCount 的 Map
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggleLikeComment(Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 校验评论存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }

        // 查询点赞记录（忽略 deleted 标记，避免唯一索引冲突）
        LikeRecord record = likeRecordMapper.selectByUserAndTargetIgnoreDeleted(userId, commentId, TARGET_TYPE_COMMENT);

        Map<String, Object> result = new HashMap<>(2);
        if (record != null && record.getDeleted() != null && record.getDeleted() == NOT_DELETED) {
            // 已点赞 -> 取消点赞：逻辑删除记录，comment.like_count -1（不为负）
            likeRecordMapper.updateDeletedById(record.getId(), DELETED);
            commentMapper.updateLikeCount(commentId, -1);
            result.put("liked", false);
        } else {
            // 未点赞 -> 点赞：新建或恢复记录，comment.like_count +1
            if (record == null) {
                LikeRecord newRecord = new LikeRecord();
                newRecord.setUserId(userId);
                newRecord.setTargetId(commentId);
                newRecord.setTargetType(TARGET_TYPE_COMMENT);
                likeRecordMapper.insert(newRecord);
            } else {
                // 恢复已逻辑删除的记录
                likeRecordMapper.updateDeletedById(record.getId(), NOT_DELETED);
            }
            commentMapper.updateLikeCount(commentId, 1);

            // 通知评论作者并给作者加积分（避免自己赞自己）
            Long authorId = comment.getUserId();
            if (!userId.equals(authorId)) {
                notificationService.createNotification(authorId, userId, NOTIFICATION_TYPE_LIKE,
                        "赞了你的评论", commentId, TARGET_TYPE_COMMENT);
                try {
                    pointsService.addPoints(authorId, LIKED_REWARD_POINTS, POINTS_TYPE_LIKED, "被点赞奖励");
                } catch (Exception e) {
                    // 积分奖励失败不影响点赞主流程
                    log.warn("被点赞奖励积分失败，userId={}", authorId, e);
                }
            }
            result.put("liked", true);
        }

        // 重新查询评论获取最新点赞数
        Comment latest = commentMapper.selectById(commentId);
        result.put("likeCount", latest != null && latest.getLikeCount() != null ? latest.getLikeCount() : 0);
        return result;
    }

    /**
     * 查询当前用户是否已点赞某帖子
     *
     * @param postId 帖子ID
     * @return true 表示当前用户已点赞该帖子
     */
    @Override
    public boolean isLikedPost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long count = likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, postId)
                .eq(LikeRecord::getTargetType, TARGET_TYPE_POST));
        return count != null && count > 0;
    }

    /**
     * 查询当前用户是否已点赞某评论
     *
     * @param commentId 评论ID
     * @return true 表示当前用户已点赞该评论
     */
    @Override
    public boolean isLikedComment(Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long count = likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetId, commentId)
                .eq(LikeRecord::getTargetType, TARGET_TYPE_COMMENT));
        return count != null && count > 0;
    }

}
