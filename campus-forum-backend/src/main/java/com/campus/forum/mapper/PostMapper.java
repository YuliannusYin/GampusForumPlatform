package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 帖子 Mapper 接口
 * 提供基础的 BaseMapper 方法以及自定义的联表查询（实际 SQL 在 PostMapper.xml 中实现）
 *
 * @author campus
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子列表（联表 user 与 section）
     * 排序规则：
     *   - 置顶帖 is_top=1 始终优先
     *   - latest：按 create_time 倒序
     *   - hot：按 (like_count + comment_count*2 + view_count/10) 倒序
     * 仅查询 status=0（已发布）的帖子
     *
     * @param page      分页对象
     * @param sectionId 板块ID（可选，为 null 时不限制）
     * @param sort      排序方式 latest / hot
     * @return 分页结果
     */
    IPage<PostListVO> selectPostList(IPage<PostListVO> page,
                                     @Param("sectionId") Long sectionId,
                                     @Param("sort") String sort);

    /**
     * 查询帖子详情（联表 user 与 section，包含正文 content）
     *
     * @param id 帖子ID
     * @return 帖子详情视图，不存在返回 null
     */
    PostDetailVO selectPostDetail(@Param("id") Long id);

    /**
     * 关键词搜索帖子（联表 user 与 section）
     * 匹配标题、正文、作者用户名；仅查询 status=0（已发布）且 deleted=0 的帖子
     * 可选筛选：板块ID、起始时间、截止时间
     * 排序：默认按 create_time 倒序
     *
     * @param page      分页对象
     * @param keyword   关键词（匹配标题/正文/作者用户名）
     * @param sectionId 板块ID（可选，为 null 时不限制）
     * @param startTime 起始时间（可选，为 null 时不限制）
     * @param endTime   截止时间（可选，为 null 时不限制）
     * @return 分页结果
     */
    IPage<PostListVO> selectPostsByKeyword(IPage<PostListVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("sectionId") Long sectionId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 按标签分页查询帖子列表（联表 user 与 section，通过 post_tag 联查）
     * - 仅查询 status=0（已发布）且 deleted=0 的帖子
     * - 排序按 create_time 倒序
     *
     * @param page  分页对象
     * @param tagId 标签ID
     * @return 分页结果
     */
    IPage<PostListVO> selectPostsByTag(IPage<PostListVO> page,
                                       @Param("tagId") Long tagId);

    /**
     * 原子自增浏览数 +1
     *
     * @param id 帖子ID
     * @return 受影响行数
     */
    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrViewCount(@Param("id") Long id);

    /**
     * 原子更新帖子点赞数（delta 为正数加、负数减，且不会变为负数）
     * WHERE 条件中通过 like_count + #{delta} >= 0 保证不为负
     *
     * @param id    帖子ID
     * @param delta 变化值（+1 或 -1）
     * @return 受影响行数（0 表示目标不存在或会变为负数）
     */
    @Update("UPDATE post SET like_count = like_count + #{delta} WHERE id = #{id} AND deleted = 0 AND like_count + #{delta} >= 0")
    int updateLikeCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 原子更新帖子收藏数（delta 为正数加、负数减，且不会变为负数）
     * WHERE 条件中通过 favorite_count + #{delta} >= 0 保证不为负
     *
     * @param id    帖子ID
     * @param delta 变化值（+1 或 -1）
     * @return 受影响行数（0 表示目标不存在或会变为负数）
     */
    @Update("UPDATE post SET favorite_count = favorite_count + #{delta} WHERE id = #{id} AND deleted = 0 AND favorite_count + #{delta} >= 0")
    int updateFavoriteCount(@Param("id") Long id, @Param("delta") int delta);

}
