package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.entity.Role;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.service.RoleService;
import org.springframework.stereotype.Service;

/**
 * 角色服务实现类
 *
 * @author campus
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
