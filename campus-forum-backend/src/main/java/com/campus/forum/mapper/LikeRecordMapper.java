package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 点赞记录 Mapper 接口（完整版）
 * 提供基础的 BaseMapper 方法以及绕过 @TableLogic 的自定义查询与更新方法，
 * 用于支持唯一索引下的幂等切换（取消后再次点赞恢复 deleted=0 的记录）
 *
 * @author campus
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {

    /**
     * 查询用户对某目标的点赞记录（忽略逻辑删除标记）
     * 由于 like_record 表的唯一索引为 (user_id, target_id, target_type) 不含 deleted，
     * 因此需通过自定义 SQL 绕过 @TableLogic 自动追加的 deleted=0 条件，
     * 以查找已逻辑删除的记录并恢复，避免唯一索引冲突
     *
     * @param userId     点赞用户ID
     * @param targetId   目标对象ID
     * @param targetType 目标类型 1帖子 2评论
     * @return 点赞记录（可能为已逻辑删除状态），不存在返回 null
     */
    @Select("SELECT * FROM like_record WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType}")
    LikeRecord selectByUserAndTargetIgnoreDeleted(@Param("userId") Long userId,
                                                  @Param("targetId") Long targetId,
                                                  @Param("targetType") Integer targetType);

    /**
     * 直接更新 deleted 字段（绕过 @TableLogic 拦截）
     * 用于在取消点赞（deleted=1）与恢复点赞（deleted=0）之间切换
     *
     * @param id      记录ID
     * @param deleted 目标删除标记 0未删除 1已删除
     * @return 受影响行数
     */
    @Update("UPDATE like_record SET deleted = #{deleted}, update_time = NOW() WHERE id = #{id}")
    int updateDeletedById(@Param("id") Long id, @Param("deleted") Integer deleted);

}
