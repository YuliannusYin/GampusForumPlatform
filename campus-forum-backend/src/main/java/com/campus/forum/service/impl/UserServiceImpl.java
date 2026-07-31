package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 角色 Mapper，用于查询用户角色 */
    private final RoleMapper roleMapper;

    @Override
    public User getByAccount(String account) {
        if (account == null || account.isEmpty()) {
            return null;
        }
        return baseMapper.selectByUsernameOrEmail(account);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return roleMapper.selectRolesByUserId(userId);
    }

}
