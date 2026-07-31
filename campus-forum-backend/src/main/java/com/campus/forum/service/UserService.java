package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author campus
 */
public interface UserService extends IService<User> {

    /**
     * 根据账号（用户名或邮箱）查询用户
     *
     * @param account 用户名或邮箱
     * @return 用户实体，不存在返回 null
     */
    User getByAccount(String account);

    /**
     * 查询用户拥有的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

}
