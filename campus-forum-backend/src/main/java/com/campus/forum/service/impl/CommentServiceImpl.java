package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.CreateCommentRequest;
import com.campus.forum.dto.resp.CommentVO;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.CommentService;
import com.campus.forum.service.NotificationService;
import com.campus.forum.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评论服务实现类
 * 封装发表评论/回复、评论列表、回复列表、删除评论等业务逻辑
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    /** 评论奖励积分（2分） */
    private static final int COMMENT_REWARD_POINTS = 2;

    /** 积分类型 3 评论 */
    private static final int POINTS_TYPE_COMMENT = 3;

    /** 通知类型 1 评论 */
    private static final int NOTIFICATION_TYPE_COMMENT = 1;

    /** 目标类型 1 帖子 */
    private static final int TARGET_TYPE_POST = 1;

    /** 目标类型 2 评论 */
    private static final int TARGET_TYPE_COMMENT = 2;

    /** 顶级评论的 parentId */
    private static final Long TOP_LEVEL_PARENT_ID = 0L;

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final PointsService pointsService;

    /**
     * 发表评论或回复：
     * 1. 校验帖子存在
     * 2. 获取当前用户
     * 3. 若 parentId 非0，校验父评论存在
     * 4. 保存评论（status=0 正常）
     * 5. 帖子 commentCount +1（原子更新）
     * 6. 评论奖励 2 积分（type=3 评论）
     * 7. 通知帖子作者与父评论作者（避免自己通知自己）
     * 8. 返回 CommentVO（含作者信息）
     *
     * @param postId 帖子ID
     * @param req    评论请求
     * @return 创建后的评论视图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO createComment(Long postId, CreateCommentRequest req) {
        // 校验帖子存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }

        // 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();

        // 处理 parentId，null 视为 0（顶级评论）
        Long parentId = (req.getParentId() == null) ? TOP_LEVEL_PARENT_ID : req.getParentId();

        // 若 parentId 非0，校验父评论存在
        Comment parentComment = null;
        if (!TOP_LEVEL_PARENT_ID.equals(parentId)) {
            parentComment = commentMapper.selectById(parentId);
            if (parentComment == null) {
                throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
            }
        }

        // 保存评论
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(req.getContent());
        comment.setLikeCount(0);
        comment.setStatus(0);
        commentMapper.insert(comment);

        // 帖子 commentCount +1（原子更新）
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .setSql("comment_count = comment_count + 1")
                .eq(Post::getId, postId));

        // 评论奖励积分（2分，type=3 评论）
        pointsService.addPoints(userId, COMMENT_REWARD_POINTS, POINTS_TYPE_COMMENT, "评论奖励");

        // 通知帖子作者（评论者≠帖子作者时）
        if (!userId.equals(post.getUserId())) {
            notificationService.createNotification(
                    post.getUserId(), userId, NOTIFICATION_TYPE_COMMENT,
                    "评论了你的帖子", postId, TARGET_TYPE_POST);
        }

        // 通知父评论作者（回复者≠父评论作者时）
        if (parentComment != null && !userId.equals(parentComment.getUserId())) {
            notificationService.createNotification(
                    parentComment.getUserId(), userId, NOTIFICATION_TYPE_COMMENT,
                    "回复了你的评论", parentId, TARGET_TYPE_COMMENT);
        }

        // 构造返回 VO（含作者信息）
        return buildCommentVO(comment, userId);
    }

    /**
     * 分页查询某帖子的顶级评论（含 replyCount）
     *
     * @param postId 帖子ID
     * @param page   当前页码
     * @param size   每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<CommentVO> listComments(Long postId, long page, long size) {
        Page<CommentVO> p = new Page<>(page, size);
        IPage<CommentVO> result = commentMapper.selectCommentsByPostId(p, postId);
        return PageResult.of(result);
    }

    /**
     * 分页查询某评论的回复列表
     *
     * @param commentId 父评论ID
     * @param page      当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<CommentVO> listReplies(Long commentId, long page, long size) {
        Page<CommentVO> p = new Page<>(page, size);
        IPage<CommentVO> result = commentMapper.selectRepliesByParentId(p, commentId);
        return PageResult.of(result);
    }

    /**
     * 删除评论（逻辑删除）：
     * 1. 校验评论存在
     * 2. 校验权限（作者或管理员）
     * 3. 标记 status=1（已删除）并逻辑删除（deleted=1）
     * 4. 帖子 commentCount -1（用 GREATEST 避免减为负数）
     *
     * @param id      评论ID
     * @param userId  当前用户ID
     * @param isAdmin 当前用户是否为管理员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId, boolean isAdmin) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.COMMENT_NOT_FOUND);
        }
        // 权限校验：作者或管理员
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人评论");
        }

        // 标记 status=1（已删除）
        Comment update = new Comment();
        update.setId(id);
        update.setStatus(1);
        commentMapper.updateById(update);

        // 逻辑删除（MyBatis-Plus @TableLogic 自动处理 deleted=1）
        commentMapper.deleteById(id);

        // 帖子 commentCount -1（原子更新，用 GREATEST 避免减为负数）
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .setSql("comment_count = GREATEST(comment_count - 1, 0)")
                .eq(Post::getId, comment.getPostId()));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据评论实体与作者ID构造 CommentVO（含作者用户名与头像）
     * replyCount 默认 0（新评论无回复）
     *
     * @param comment 评论实体
     * @param userId  评论者ID
     * @return 评论视图
     */
    private CommentVO buildCommentVO(Comment comment, Long userId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(userId);
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(0);
        vo.setCreateTime(comment.getCreateTime());

        // 填充作者信息
        User user = userMapper.selectById(userId);
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        return vo;
    }

}
