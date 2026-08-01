package com.campus.forum.controller;

import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.UpdateSettingRequest;
import com.campus.forum.dto.resp.UserSettingVO;
import com.campus.forum.entity.UserSetting;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.UserSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户设置接口
 * 提供当前用户通知偏好的查询与修改
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/user/settings")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "用户设置接口", description = "通知偏好查询与修改")
public class UserSettingController {

    private final UserSettingService userSettingService;

    /**
     * 查询当前用户通知偏好设置
     * 不存在则初始化为默认值（全部开启）
     *
     * @return 用户设置视图对象
     */
    @Operation(summary = "查询通知偏好", description = "获取当前登录用户的通知偏好设置，不存在则初始化为默认值")
    @GetMapping("")
    public Result<UserSettingVO> getSetting() {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSetting setting = userSettingService.getOrCreate(userId);
        return Result.success(buildVO(setting));
    }

    /**
     * 修改当前用户通知偏好设置
     * 仅更新请求中非空字段
     *
     * @param request 修改设置请求
     * @return 最新用户设置视图对象
     */
    @Operation(summary = "修改通知偏好", description = "更新当前登录用户的通知偏好，仅更新请求中非空字段")
    @PutMapping("")
    public Result<UserSettingVO> updateSetting(@RequestBody UpdateSettingRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        // 确保设置记录存在
        UserSetting setting = userSettingService.getOrCreate(userId);
        // 仅更新非空字段（MyBatis-Plus updateById 默认忽略 null 字段）
        UserSetting update = new UserSetting();
        update.setId(setting.getId());
        update.setNotifyComment(request.getNotifyComment());
        update.setNotifyLike(request.getNotifyLike());
        update.setNotifyMessage(request.getNotifyMessage());
        userSettingService.updateById(update);
        // 返回最新设置
        UserSetting latest = userSettingService.getById(setting.getId());
        return Result.success(buildVO(latest));
    }

    /**
     * 根据用户设置实体构造 UserSettingVO
     *
     * @param setting 用户设置实体
     * @return 用户设置视图对象
     */
    private UserSettingVO buildVO(UserSetting setting) {
        UserSettingVO vo = new UserSettingVO();
        vo.setNotifyComment(setting.getNotifyComment());
        vo.setNotifyLike(setting.getNotifyLike());
        vo.setNotifyMessage(setting.getNotifyMessage());
        return vo;
    }

}
