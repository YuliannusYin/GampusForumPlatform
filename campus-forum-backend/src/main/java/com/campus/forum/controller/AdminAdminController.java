package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.CreateAdminRequest;
import com.campus.forum.dto.req.PasswordResetRequest;
import com.campus.forum.dto.req.UserStatusRequest;
import com.campus.forum.dto.resp.AdminUserVO;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserRole;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 超级管理员接口
 * 管理普通管理员账号（ROLE_ADMIN），仅超级管理员（ROLE_SUPER_ADMIN）可访问
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "超级管理员-管理员管理", description = "管理普通管理员账号")
public class AdminAdminController {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 管理员列表（分页）
     * 查询所有拥有 ROLE_ADMIN 角色的用户：先查 role 表中 code='ROLE_ADMIN' 的角色，
     * 再通过 user_role 关联查出 userId 集合，最后分页查询这些用户
     *
     * @param page 当前页码，默认 1
     * @param size 每页条数，默认 10
     * @return 管理员分页结果
     */
    @Operation(summary = "管理员列表", description = "分页查询所有拥有 ROLE_ADMIN 角色的用户")
    @GetMapping("")
    public Result<PageResult<AdminUserVO>> listAdmins(@Parameter(description = "当前页码，默认 1")
                                                      @RequestParam(value = "page", defaultValue = "1") long page,
                                                      @Parameter(description = "每页条数，默认 10")
                                                      @RequestParam(value = "size", defaultValue = "10") long size) {
        // 查询 ROLE_ADMIN 角色
        Role roleAdmin = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_ADMIN"));
        if (roleAdmin == null) {
            return Result.success(new PageResult<>(Collections.emptyList(), 0L, page, size));
        }
        // 查询拥有该角色的 userId 集合（@TableLogic 自动过滤 deleted=1）
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, roleAdmin.getId()));
        List<Long> userIds = userRoles.stream().map(UserRole::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Result.success(new PageResult<>(Collections.emptyList(), 0L, page, size));
        }
        // 分页查询这些用户，按创建时间倒序
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)
                        .orderByDesc(User::getCreateTime));
        List<AdminUserVO> voList = userPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        // 批量填充角色
        fillRoles(voList);

        PageResult<AdminUserVO> result = new PageResult<>(voList, userPage.getTotal(),
                userPage.getCurrent(), userPage.getSize());
        return Result.success(result);
    }

    /**
     * 创建管理员账号
     * 校验用户名/邮箱唯一性，BCrypt 加密密码，保存 User 并关联 ROLE_ADMIN 角色
     *
     * @param request 创建管理员请求
     * @return 创建后的管理员信息
     */
    @Operation(summary = "创建管理员", description = "创建普通管理员账号并分配 ROLE_ADMIN 角色")
    @PostMapping("")
    @Transactional(rollbackFor = Exception.class)
    public Result<AdminUserVO> createAdmin(@Parameter(description = "管理员创建请求体", required = true)
                                            @Valid @RequestBody CreateAdminRequest request) {
        // 校验用户名唯一性
        long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        // 校验邮箱唯一性
        long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 构造用户实体，密码 BCrypt 加密
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname((request.getNickname() == null || request.getNickname().isBlank())
                ? request.getUsername() : request.getNickname());
        user.setGender(0);
        user.setPoints(0);
        user.setLevel(1);
        user.setStatus(0);
        userMapper.insert(user);

        // 查询 ROLE_ADMIN 角色并关联
        Role roleAdmin = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_ADMIN"));
        if (roleAdmin == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "系统未配置 ROLE_ADMIN 角色");
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(roleAdmin.getId());
        userRoleMapper.insert(userRole);

        // 返回 VO（含角色）
        AdminUserVO vo = toVO(user);
        vo.setRoles(Collections.singletonList(roleAdmin));
        return Result.success(vo);
    }

    /**
     * 启用/禁用管理员
     * status=0 启用，status=1 禁用（用户无法登录）
     *
     * @param id      管理员ID
     * @param request 状态修改请求
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用管理员", description = "status=1 禁用，0 启用")
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@Parameter(description = "管理员ID", required = true)
                                        @PathVariable("id") Long id,
                                        @Parameter(description = "状态修改请求体", required = true)
                                        @Valid @RequestBody UserStatusRequest request) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 不能操作当前登录账号
        if (exist.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作当前登录账号");
        }
        User update = new User();
        update.setId(id);
        update.setStatus(request.getStatus());
        userMapper.updateById(update);
        return Result.success(request.getStatus() == 1 ? "禁用成功" : "启用成功");
    }

    /**
     * 重置管理员密码
     * 由超级管理员重置，无需旧密码，BCrypt 加密后更新
     *
     * @param id      管理员ID
     * @param request 重置密码请求
     * @return 操作结果
     */
    @Operation(summary = "重置管理员密码", description = "BCrypt 加密后更新密码，无需旧密码")
    @PutMapping("/{id}/password-reset")
    public Result<String> resetPassword(@Parameter(description = "管理员ID", required = true)
                                         @PathVariable("id") Long id,
                                         @Parameter(description = "重置密码请求体", required = true)
                                         @Valid @RequestBody PasswordResetRequest request) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 不能操作当前登录账号
        if (exist.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作当前登录账号");
        }
        User update = new User();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.updateById(update);
        return Result.success("密码重置成功");
    }

    /**
     * 删除管理员（逻辑删除）
     * MyBatis-Plus removeById/deleteById 在 @TableLogic 标记下执行逻辑删除
     *
     * @param id 管理员ID
     * @return 操作结果
     */
    @Operation(summary = "删除管理员", description = "逻辑删除管理员账号")
    @DeleteMapping("/{id}")
    public Result<String> deleteAdmin(@Parameter(description = "管理员ID", required = true)
                                       @PathVariable("id") Long id) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 不能删除当前登录账号
        if (exist.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能删除当前登录账号");
        }
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将 User 实体转换为 AdminUserVO
     *
     * @param user 用户实体
     * @return 管理员用户视图对象
     */
    private AdminUserVO toVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setPoints(user.getPoints());
        vo.setLevel(user.getLevel());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setRoles(Collections.emptyList());
        return vo;
    }

    /**
     * 批量为管理员列表填充角色信息
     * 一次性查询所有用户对应的 user_role 与 role，按 userId 分组后回填
     *
     * @param voList 用户视图列表
     */
    private void fillRoles(List<AdminUserVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Long> userIds = voList.stream().map(AdminUserVO::getId).collect(Collectors.toList());
        // 查询这些用户的 user_role 关联（@TableLogic 自动过滤 deleted=1）
        List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                .in(UserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return;
        }
        // 查询涉及的角色详情
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, Role> roleMap = roleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, r -> r));
        // 按 userId 分组（过滤掉角色已不存在的脏数据）
        Map<Long, List<Role>> userIdToRoles = userRoles.stream()
                .filter(ur -> roleMap.containsKey(ur.getRoleId()))
                .collect(Collectors.groupingBy(
                        UserRole::getUserId,
                        Collectors.mapping(ur -> roleMap.get(ur.getRoleId()), Collectors.toList())
                ));
        // 填充到每个 VO
        for (AdminUserVO vo : voList) {
            vo.setRoles(userIdToRoles.getOrDefault(vo.getId(), Collections.emptyList()));
        }
    }

}
