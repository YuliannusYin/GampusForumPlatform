package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 物理删除指定用户的所有 user_role 记录
     * 用于管理员重置用户角色：先物理清空旧关联，再批量插入新关联，避免唯一约束冲突
     *
     * @param userId 用户ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int physicalDeleteByUserId(@Param("userId") Long userId);

}
