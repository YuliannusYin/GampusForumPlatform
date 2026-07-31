package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.UserRoleRequest;
import com.campus.forum.dto.req.UserStatusRequest;
import com.campus.forum.dto.resp.AdminUserVO;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserRole;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
 * 管理员-用户管理接口
 * 提供用户分页查询、封禁/解禁、修改用户角色等功能
 * 全部接口仅管理员（ROLE_ADMIN）可访问
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员-用户管理", description = "用户分页查询、封禁/解禁、修改角色")
public class AdminUserController {

    private final UserService userService;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    /**
     * 分页查询用户列表
     * 支持按用户名/昵称/邮箱关键词搜索与状态筛选
     *
     * @param page    当前页码，默认 1
     * @param size    每页条数，默认 10
     * @param keyword 关键词（可选，匹配用户名/昵称/邮箱）
     * @param status  状态（可选，0正常 1封禁）
     * @return 用户分页结果
     */
    @Operation(summary = "分页查询用户列表", description = "支持按用户名/昵称/邮箱搜索与状态筛选")
    @GetMapping("")
    public Result<PageResult<AdminUserVO>> listUsers(@Parameter(description = "当前页码，默认 1")
                                                      @RequestParam(value = "page", defaultValue = "1") long page,
                                                      @Parameter(description = "每页条数，默认 10")
                                                      @RequestParam(value = "size", defaultValue = "10") long size,
                                                      @Parameter(description = "关键词（可选，匹配用户名/昵称/邮箱）")
                                                      @RequestParam(value = "keyword", required = false) String keyword,
                                                      @Parameter(description = "状态（可选，0正常 1封禁）")
                                                      @RequestParam(value = "status", required = false) Integer status) {
        // 构造查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        // 分页查询
        Page<User> userPage = userService.page(new Page<>(page, size), wrapper);
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
     * 封禁/解禁用户
     * status=1 封禁（用户无法登录），status=0 解禁
     *
     * @param id      用户ID
     * @param request 状态修改请求
     * @return 操作结果
     */
    @Operation(summary = "封禁/解禁用户", description = "status=1 封禁（用户无法登录），status=0 解禁")
    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(@Parameter(description = "用户ID", required = true)
                                            @PathVariable("id") Long id,
                                            @Parameter(description = "状态修改请求体", required = true)
                                            @Valid @RequestBody UserStatusRequest request) {
        User exist = userService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        User update = new User();
        update.setId(id);
        update.setStatus(request.getStatus());
        userService.updateById(update);
        return Result.success(request.getStatus() == 1 ? "封禁成功" : "解禁成功");
    }

    /**
     * 修改用户角色
     * 先物理删除用户所有 user_role 关联，再批量插入新的关联
     *
     * @param id      用户ID
     * @param request 角色修改请求
     * @return 操作结果
     */
    @Operation(summary = "修改用户角色", description = "覆盖用户原有角色，先删除旧关联再插入新关联")
    @PutMapping("/{id}/role")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateUserRoles(@Parameter(description = "用户ID", required = true)
                                           @PathVariable("id") Long id,
                                           @Parameter(description = "角色修改请求体", required = true)
                                           @Valid @RequestBody UserRoleRequest request) {
        User exist = userService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 校验角色ID合法性并去重
        List<Long> roleIds = request.getRoleIds();
        List<Long> distinctIds = (roleIds == null || roleIds.isEmpty())
                ? Collections.emptyList()
                : roleIds.stream().distinct().collect(Collectors.toList());
        if (!distinctIds.isEmpty()) {
            List<Role> existRoles = roleMapper.selectBatchIds(distinctIds);
            if (existRoles.size() != distinctIds.size()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "存在不合法的角色ID");
            }
        }
        // 物理删除用户所有旧角色关联（避免唯一约束 uk_user_role 冲突）
        userRoleMapper.physicalDeleteByUserId(id);
        // 批量插入新角色关联
        for (Long roleId : distinctIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(id);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        return Result.success("角色修改成功");
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
     * 批量为用户列表填充角色信息
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
