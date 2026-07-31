package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.SignInStatusVO;
import com.campus.forum.dto.resp.SignInVO;
import com.campus.forum.entity.SignInRecord;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.SignInRecordMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.SignInService;
import com.campus.forum.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 签到接口
 * 提供每日签到、签到状态查询、签到记录分页查询
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/signin")
@RequiredArgsConstructor
@Tag(name = "签到接口", description = "每日签到、签到状态、签到记录")
public class SignInController {

    private final SignInService signInService;
    private final SignInRecordMapper signInRecordMapper;
    private final UserService userService;

    /**
     * 每日签到
     *
     * @return 签到结果
     */
    @Operation(summary = "每日签到", description = "当前用户进行每日签到，返回连续天数、获得积分与总积分")
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public Result<SignInVO> signIn() {
        return Result.success(signInService.signIn());
    }

    /**
     * 查询今日签到状态
     *
     * @return 签到状态
     */
    @Operation(summary = "签到状态", description = "查询今日是否已签到、连续天数、总积分与等级")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/status")
    public Result<SignInStatusVO> status() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();

        // 查询今日签到记录
        SignInRecord todayRecord = signInRecordMapper.selectByUserAndDate(userId, today);
        boolean signedToday = todayRecord != null;

        // 计算连续天数：今日已签到则取今日记录，否则取昨日记录（断签则为 0）
        int continuousDays = 0;
        if (signedToday) {
            continuousDays = todayRecord.getContinuousDays();
        } else {
            SignInRecord yesterdayRecord = signInRecordMapper.selectByUserAndDate(userId, today.minusDays(1));
            if (yesterdayRecord != null) {
                continuousDays = yesterdayRecord.getContinuousDays();
            }
        }

        // 查询用户积分与等级
        User user = userService.getById(userId);
        int totalPoints = (user != null && user.getPoints() != null) ? user.getPoints() : 0;
        int level = (user != null && user.getLevel() != null) ? user.getLevel() : 1;

        SignInStatusVO vo = new SignInStatusVO();
        vo.setSignedToday(signedToday);
        vo.setContinuousDays(continuousDays);
        vo.setTotalPoints(totalPoints);
        vo.setLevel(level);
        return Result.success(vo);
    }

    /**
     * 查询签到记录分页（按签到日期倒序）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 签到记录分页
     */
    @Operation(summary = "签到记录", description = "分页查询当前用户的签到记录，按 sign_date 倒序")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/records")
    public Result<Page<SignInRecord>> records(@Parameter(description = "当前页码，默认 1")
                                               @RequestParam(value = "current", defaultValue = "1") long current,
                                               @Parameter(description = "每页条数，默认 10")
                                               @RequestParam(value = "size", defaultValue = "10") long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<SignInRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<SignInRecord> wrapper = new LambdaQueryWrapper<SignInRecord>()
                .eq(SignInRecord::getUserId, userId)
                .orderByDesc(SignInRecord::getSignDate);
        // TODO: 后续统一为 PageResult
        return Result.success(signInRecordMapper.selectPage(page, wrapper));
    }

}
