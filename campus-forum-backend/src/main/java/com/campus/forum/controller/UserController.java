package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.ChangePasswordRequest;
import com.campus.forum.dto.req.UpdateProfileRequest;
import com.campus.forum.dto.resp.UserInfoVO;
import com.campus.forum.entity.Favorite;
import com.campus.forum.entity.LikeRecord;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.FavoriteMapper;
import com.campus.forum.mapper.LikeRecordMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 * 提供个人资料查询与修改、修改密码、我的发帖/收藏/点赞记录查询
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "个人资料、修改密码、我的发帖/收藏/点赞")
public class UserController {

    private final UserService userService;
    private final PostMapper postMapper;
    private final FavoriteMapper favoriteMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 查询当前用户个人资料
     *
     * @return 用户信息视图对象
     */
    @Operation(summary = "查询个人资料", description = "获取当前登录用户的资料与角色列表")
    @GetMapping("/profile")
    public Result<UserInfoVO> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        List<Role> roles = userService.getUserRoles(userId);
        return Result.success(buildUserInfoVO(user, roles));
    }

    /**
     * 修改当前用户个人资料
     *
     * @param request 修改资料请求
     * @return 最新用户信息视图对象
     */
    @Operation(summary = "修改个人资料", description = "修改昵称、简介、头像、性别等资料")
    @PutMapping("/profile")
    public Result<UserInfoVO> updateProfile(@Parameter(description = "修改资料请求体", required = true)
                                            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 仅更新非空字段
        User update = new User();
        update.setId(userId);
        update.setNickname(request.getNickname());
        update.setBio(request.getBio());
        update.setAvatar(request.getAvatar());
        update.setGender(request.getGender());
        userService.updateById(update);

        User user = userService.getById(userId);
        List<Role> roles = userService.getUserRoles(userId);
        return Result.success(buildUserInfoVO(user, roles));
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     * @return 操作结果
     */
    @Operation(summary = "修改密码", description = "校验旧密码后更新为新密码（BCrypt 加密）")
    @PutMapping("/password")
    public Result<String> changePassword(@Parameter(description = "修改密码请求体", required = true)
                                         @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 校验旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR, "旧密码不正确");
        }
        // 更新为新密码（BCrypt 加密）
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(update);
        return Result.success("密码修改成功");
    }

    /**
     * 查询当前用户的发帖分页（按创建时间倒序）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 帖子分页
     */
    @Operation(summary = "我的发帖", description = "分页查询当前用户发布的帖子，按 create_time 倒序")
    @GetMapping("/posts")
    public Result<Page<Post>> myPosts(@Parameter(description = "当前页码，默认 1")
                                      @RequestParam(value = "current", defaultValue = "1") long current,
                                      @Parameter(description = "每页条数，默认 10")
                                      @RequestParam(value = "size", defaultValue = "10") long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Post> page = new Page<>(current, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreateTime);
        return Result.success(postMapper.selectPage(page, wrapper));
    }

    /**
     * 查询当前用户的收藏记录分页（按创建时间倒序）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 收藏记录分页
     */
    @Operation(summary = "我的收藏", description = "分页查询当前用户的收藏记录，按 create_time 倒序")
    @GetMapping("/favorites")
    public Result<Page<Favorite>> myFavorites(@Parameter(description = "当前页码，默认 1")
                                              @RequestParam(value = "current", defaultValue = "1") long current,
                                              @Parameter(description = "每页条数，默认 10")
                                              @RequestParam(value = "size", defaultValue = "10") long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Favorite> page = new Page<>(current, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        return Result.success(favoriteMapper.selectPage(page, wrapper));
    }

    /**
     * 查询当前用户的点赞记录分页（按创建时间倒序，仅帖子类型）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 点赞记录分页
     */
    @Operation(summary = "我的点赞", description = "分页查询当前用户对帖子的点赞记录，按 create_time 倒序")
    @GetMapping("/likes")
    public Result<Page<LikeRecord>> myLikes(@Parameter(description = "当前页码，默认 1")
                                            @RequestParam(value = "current", defaultValue = "1") long current,
                                            @Parameter(description = "每页条数，默认 10")
                                            @RequestParam(value = "size", defaultValue = "10") long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<LikeRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, 1)
                .orderByDesc(LikeRecord::getCreateTime);
        return Result.success(likeRecordMapper.selectPage(page, wrapper));
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
