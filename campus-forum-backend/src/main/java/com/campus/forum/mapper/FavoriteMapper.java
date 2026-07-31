package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 收藏 Mapper 接口（完整版）
 * 提供基础的 BaseMapper 方法以及绕过 @TableLogic 的自定义查询与更新方法，
 * 用于支持唯一索引下的幂等切换（取消后再次收藏恢复 deleted=0 的记录）
 *
 * @author campus
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 查询用户对某帖子的收藏记录（忽略逻辑删除标记）
     * 由于 favorite 表的唯一索引为 (user_id, post_id) 不含 deleted，
     * 因此需通过自定义 SQL 绕过 @TableLogic 自动追加的 deleted=0 条件，
     * 以查找已逻辑删除的记录并恢复，避免唯一索引冲突
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 收藏记录（可能为已逻辑删除状态），不存在返回 null
     */
    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND post_id = #{postId}")
    Favorite selectByUserAndPostIgnoreDeleted(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 直接更新 deleted 字段（绕过 @TableLogic 拦截）
     * 用于在取消收藏（deleted=1）与恢复收藏（deleted=0）之间切换
     *
     * @param id      记录ID
     * @param deleted 目标删除标记 0未删除 1已删除
     * @return 受影响行数
     */
    @Update("UPDATE favorite SET deleted = #{deleted}, update_time = NOW() WHERE id = #{id}")
    int updateDeletedById(@Param("id") Long id, @Param("deleted") Integer deleted);

}
