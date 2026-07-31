package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.dto.resp.CommentVO;
import com.campus.forum.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 评论 Mapper 接口
 * 提供基础的 BaseMapper 方法以及自定义的联表查询（实际 SQL 在 CommentMapper.xml 中实现）
 *
 * @author campus
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询某帖子的顶级评论（parent_id=0）
     * 联表 user 获取评论者用户名（username）与头像（userAvatar）
     * 仅查询 status=0（正常）且 deleted=0 的评论，按 create_time 倒序
     * 每条评论带 replyCount（子回复数，通过子查询统计）
     *
     * @param page   分页对象
     * @param postId 帖子ID
     * @return 分页结果
     */
    IPage<CommentVO> selectCommentsByPostId(IPage<CommentVO> page, @Param("postId") Long postId);

    /**
     * 分页查询某评论的回复（parent_id=#{parentId}）
     * 联表 user 获取回复者用户名（username）与头像（userAvatar）
     * 仅查询 status=0（正常）且 deleted=0 的回复，按 create_time 正序（楼层顺序）
     *
     * @param page     分页对象
     * @param parentId 父评论ID
     * @return 分页结果
     */
    IPage<CommentVO> selectRepliesByParentId(IPage<CommentVO> page, @Param("parentId") Long parentId);

    /**
     * 原子更新评论点赞数（delta 为正数加、负数减，且不会变为负数）
     * WHERE 条件中通过 like_count + #{delta} >= 0 保证不为负
     *
     * @param id    评论ID
     * @param delta 变化值（+1 或 -1）
     * @return 受影响行数（0 表示目标不存在或会变为负数）
     */
    @Update("UPDATE comment SET like_count = like_count + #{delta} WHERE id = #{id} AND deleted = 0 AND like_count + #{delta} >= 0")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

}
