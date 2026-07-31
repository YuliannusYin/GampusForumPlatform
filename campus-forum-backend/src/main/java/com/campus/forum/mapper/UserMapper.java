package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名或邮箱查询用户（用于登录）
     *
     * @param account 用户名或邮箱
     * @return 用户实体，不存在返回 null
     */
    @Select("SELECT * FROM user WHERE (username = #{account} OR email = #{account}) AND deleted = 0")
    User selectByUsernameOrEmail(@Param("account") String account);

}
