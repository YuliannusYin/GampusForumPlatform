package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.UserSimpleVO;
import com.campus.forum.entity.Follow;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 关注关系接口
 * 提供关注/取关、关注状态查询及关注/粉丝列表
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "关注关系接口", description = "关注/取关、关注状态、关注与粉丝列表")
public class FollowController {

    private final FollowService followService;
    private final UserMapper userMapper;

    /**
     * 关注指定用户
     * 登录用户关注 userId，幂等，不能关注自己
     *
     * @param userId 被关注用户ID
     * @return 操作结果
     */
    @Operation(summary = "关注用户", description = "登录用户关注指定用户，幂等，不能关注自己")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{userId}/follow")
    public Result<String> follow(@Parameter(description = "被关注用户ID", required = true)
                                 @PathVariable("userId") Long userId) {
        Long followerId = SecurityUtils.getCurrentUserId();
        followService.follow(followerId, userId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注指定用户
     *
     * @param userId 被取关用户ID
     * @return 操作结果
     */
    @Operation(summary = "取消关注", description = "登录用户取消关注指定用户")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{userId}/follow")
    public Result<String> unfollow(@Parameter(description = "被取关用户ID", required = true)
                                   @PathVariable("userId") Long userId) {
        Long followerId = SecurityUtils.getCurrentUserId();
        followService.unfollow(followerId, userId);
        return Result.success("取关成功");
    }

    /**
     * 查询当前登录用户是否已关注指定用户
     *
     * @param userId 目标用户ID
     * @return true 表示已关注
     */
    @Operation(summary = "查询是否已关注", description = "查询当前登录用户是否已关注指定用户")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{userId}/is-following")
    public Result<Boolean> isFollowing(@Parameter(description = "目标用户ID", required = true)
                                       @PathVariable("userId") Long userId) {
        Long followerId = SecurityUtils.getCurrentUserId();
        return Result.success(followService.isFollowing(followerId, userId));
    }

    /**
     * 分页查询指定用户关注的用户列表，对游客可见
     *
     * @param userId 目标用户ID
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @return 分页结果
     */
    @Operation(summary = "关注列表", description = "分页查询指定用户关注的用户列表，对游客可见")
    @GetMapping("/{userId}/followings")
    public Result<PageResult<UserSimpleVO>> followings(@Parameter(description = "目标用户ID", required = true)
                                                       @PathVariable("userId") Long userId,
                                                       @Parameter(description = "当前页码，默认 1")
                                                       @RequestParam(value = "page", defaultValue = "1") long page,
                                                       @Parameter(description = "每页条数，默认 10")
                                                       @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<Follow> p = new Page<>(page, size);
        IPage<Follow> result = followService.page(p, new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, userId)
                .orderByDesc(Follow::getCreateTime));
        List<UserSimpleVO> vos = buildUserSimpleVOList(result.getRecords(), Follow::getFollowingId);
        return Result.success(new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 分页查询指定用户的粉丝列表，对游客可见
     *
     * @param userId 目标用户ID
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @return 分页结果
     */
    @Operation(summary = "粉丝列表", description = "分页查询指定用户的粉丝列表，对游客可见")
    @GetMapping("/{userId}/followers")
    public Result<PageResult<UserSimpleVO>> followers(@Parameter(description = "目标用户ID", required = true)
                                                      @PathVariable("userId") Long userId,
                                                      @Parameter(description = "当前页码，默认 1")
                                                      @RequestParam(value = "page", defaultValue = "1") long page,
                                                      @Parameter(description = "每页条数，默认 10")
                                                      @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<Follow> p = new Page<>(page, size);
        IPage<Follow> result = followService.page(p, new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowingId, userId)
                .orderByDesc(Follow::getCreateTime));
        List<UserSimpleVO> vos = buildUserSimpleVOList(result.getRecords(), Follow::getFollowerId);
        return Result.success(new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据 Follow 记录列表与目标用户ID提取函数，批量查询 User 并组装 UserSimpleVO
     * 顺序与 records 保持一致
     *
     * @param records  关注记录列表
     * @param idGetter 从 Follow 提取目标用户ID的函数（followingId 或 followerId）
     * @return 用户简要信息列表
     */
    private List<UserSimpleVO> buildUserSimpleVOList(List<Follow> records, Function<Follow, Long> idGetter) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = records.stream().map(idGetter).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return records.stream()
                .map(f -> toUserSimpleVO(userMap.get(idGetter.apply(f))))
                .collect(Collectors.toList());
    }

    /**
     * 将 User 实体转换为 UserSimpleVO
     *
     * @param user 用户实体
     * @return 用户简要信息
     */
    private UserSimpleVO toUserSimpleVO(User user) {
        UserSimpleVO vo = new UserSimpleVO();
        if (user != null) {
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }

}
