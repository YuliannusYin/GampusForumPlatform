package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.LoginRequest;
import com.campus.forum.dto.req.RefreshTokenRequest;
import com.campus.forum.dto.req.RegisterRequest;
import com.campus.forum.dto.resp.LoginResponse;
import com.campus.forum.dto.resp.UserInfoVO;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserRole;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.RoleService;
import com.campus.forum.service.UserService;
import com.campus.forum.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证接口
 * 提供注册、登录、token 刷新、登出等接口
 *
 * @author campus
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "注册、登录、Token 刷新、登出")
public class AuthController {

    /** Redis 中存储 refreshToken 的 key 前缀 */
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 用户注册
     * 校验用户名/邮箱唯一性，密码 BCrypt 加密保存，并分配 ROLE_USER 角色
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "校验用户名/邮箱唯一性后创建用户并分配 ROLE_USER 角色，不自动登录")
    @PostMapping("/register")
    public Result<String> register(@Parameter(description = "注册请求体", required = true)
                                   @Valid @RequestBody RegisterRequest request) {
        // 校验用户名唯一性
        long usernameCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        // 校验邮箱唯一性
        long emailCount = userService.count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // 构造用户实体，密码 BCrypt 加密
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getUsername());
        user.setGender(0);
        user.setPoints(0);
        user.setLevel(1);
        user.setStatus(0);
        userService.save(user);

        // 查询 ROLE_USER 角色并分配给新用户
        Role roleUser = roleService.getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_USER"));
        if (roleUser != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleUser.getId());
            userRoleMapper.insert(userRole);
        } else {
            log.warn("未找到 code=ROLE_USER 的角色，用户 {} 未分配角色", user.getId());
        }

        return Result.success("注册成功");
    }

    /**
     * 用户登录
     * 校验账号密码与账号状态，生成 accessToken 与 refreshToken，并将 refreshToken 存入 Redis
     *
     * @param request 登录请求
     * @return 登录响应（包含 token 与用户信息）
     */
    @Operation(summary = "用户登录", description = "根据账号（用户名或邮箱）与密码登录，返回 accessToken/refreshToken 及用户信息")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Parameter(description = "登录请求体", required = true)
                                       @Valid @RequestBody LoginRequest request) {
        // 根据账号查询用户
        User user = userService.getByAccount(request.getAccount());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        // BCrypt 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 查询用户角色
        List<Role> roles = userService.getUserRoles(user.getId());

        // 生成 accessToken 与 refreshToken
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());

        // refreshToken 存入 Redis，过期时间为 refresh-token-expire 秒
        stringRedisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY_PREFIX + user.getId(),
                refreshToken,
                jwtUtils.getRefreshTokenExpire(),
                TimeUnit.SECONDS);

        // 构造登录响应
        LoginResponse response = new LoginResponse(accessToken, refreshToken, buildUserInfoVO(user, roles));
        return Result.success(response);
    }

    /**
     * 刷新 token
     * 校验 refreshToken 有效性与 Redis 一致性，重新生成 accessToken 返回
     *
     * @param request 刷新请求
     * @return 包含新 accessToken 的响应
     */
    @Operation(summary = "刷新 Token", description = "校验 refreshToken 后重新签发 accessToken")
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@Parameter(description = "刷新请求体", required = true)
                                               @Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        // 校验 refreshToken 格式与签名
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        // 从 token 中取 userId
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        // 校验 Redis 中存的 refreshToken 是否一致
        String stored = stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + userId);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        // 重新生成 accessToken
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtUtils.generateAccessToken(userId, username);

        Map<String, String> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        return Result.success(data);
    }

    /**
     * 登出
     * 从 SecurityContext 获取当前登录用户，删除 Redis 中的 refreshToken
     *
     * @return 登出结果
     */
    @Operation(summary = "登出", description = "删除当前用户在 Redis 中的 refreshToken")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public Result<String> logout() {
        Long userId = SecurityUtils.getCurrentUserId();
        stringRedisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + userId);
        return Result.success("登出成功");
    }

    /**
     * 根据用户与角色列表构造 UserInfoVO
     *
     * @param user  用户实体
     * @param roles 角色列表
     * @return 用户信息视图对象
     */
    private UserInfoVO buildUserInfoVO(User user, List<Role> roles) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPoints(user.getPoints());
        vo.setLevel(user.getLevel());
        vo.setRoles(roles.stream().map(Role::getCode).collect(Collectors.toList()));
        return vo;
    }

}
