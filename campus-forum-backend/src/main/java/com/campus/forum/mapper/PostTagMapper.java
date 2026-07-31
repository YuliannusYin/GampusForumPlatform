package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子标签关联 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    /**
     * 根据帖子ID查询其关联的标签ID列表
     *
     * @param postId 帖子ID
     * @return 标签ID列表
     */
    @Select("SELECT tag_id FROM post_tag WHERE post_id = #{postId} AND deleted = 0")
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);

    /**
     * 统计某标签下已发布且未删除的帖子数（联表 post 过滤）
     *
     * @param tagId 标签ID
     * @return 帖子数
     */
    @Select("SELECT COUNT(*) FROM post_tag pt " +
            "INNER JOIN post p ON pt.post_id = p.id " +
            "WHERE pt.tag_id = #{tagId} AND pt.deleted = 0 AND p.deleted = 0 AND p.status = 0")
    Long countPostsByTagId(@Param("tagId") Long tagId);

}
