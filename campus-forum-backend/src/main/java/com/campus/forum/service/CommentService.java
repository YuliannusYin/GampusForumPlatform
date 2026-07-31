package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.req.CreateCommentRequest;
import com.campus.forum.dto.resp.CommentVO;
import com.campus.forum.entity.Comment;

/**
 * 评论服务接口
 * 封装发表评论/回复、评论列表、回复列表、删除评论等业务逻辑
 *
 * @author campus
 */
public interface CommentService extends IService<Comment> {

    /**
     * 发表评论或回复
     * - 校验帖子存在
     * - 若 parentId 非0，校验父评论存在
     * - 保存评论，帖子 commentCount +1（原子更新）
     * - 评论奖励 2 积分（type=3 评论）
     * - 通知帖子作者（评论者≠作者时）与父评论作者（回复者≠父评论作者时）
     *
     * @param postId 帖子ID
     * @param req    评论请求
     * @return 创建后的评论视图（含作者信息）
     */
    CommentVO createComment(Long postId, CreateCommentRequest req);

    /**
     * 分页查询某帖子的顶级评论（含 replyCount）
     *
     * @param postId 帖子ID
     * @param page   当前页码
     * @param size   每页条数
     * @return 分页结果
     */
    PageResult<CommentVO> listComments(Long postId, long page, long size);

    /**
     * 分页查询某评论的回复列表
     *
     * @param commentId 父评论ID
     * @param page      当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    PageResult<CommentVO> listReplies(Long commentId, long page, long size);

    /**
     * 删除评论（逻辑删除）
     * 校验权限：当前用户是作者或管理员
     * 帖子 commentCount -1（用 GREATEST 避免减为负数）
     *
     * @param id      评论ID
     * @param userId  当前用户ID
     * @param isAdmin 当前用户是否为管理员
     */
    void deleteComment(Long id, Long userId, boolean isAdmin);

}
