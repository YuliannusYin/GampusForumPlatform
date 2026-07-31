package com.campus.forum.service.impl;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.SignInVO;
import com.campus.forum.entity.SignInRecord;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.SignInRecordMapper;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.PointsService;
import com.campus.forum.service.SignInService;
import com.campus.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 签到服务实现类
 * 处理每日签到、连续天数计算、积分奖励等核心逻辑
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class SignInServiceImpl implements SignInService {

    /** 基础签到积分 */
    private static final int BASE_POINTS = 5;

    private final SignInRecordMapper signInRecordMapper;
    private final UserService userService;
    private final PointsService pointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SignInVO signIn() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();

        // 1. 校验今日是否已签到
        SignInRecord todayRecord = signInRecordMapper.selectByUserAndDate(userId, today);
        if (todayRecord != null) {
            throw new BusinessException(ResultCode.ALREADY_SIGNED);
        }

        // 2. 查询昨日签到记录，计算连续天数
        LocalDate yesterday = today.minusDays(1);
        SignInRecord yesterdayRecord = signInRecordMapper.selectByUserAndDate(userId, yesterday);
        int continuousDays = (yesterdayRecord != null) ? yesterdayRecord.getContinuousDays() + 1 : 1;

        // 3. 计算本次签到获得积分
        int earnedPoints = calculatePoints(continuousDays);

        // 4. 保存签到记录
        SignInRecord record = new SignInRecord();
        record.setUserId(userId);
        record.setSignDate(today);
        record.setContinuousDays(continuousDays);
        record.setPoints(earnedPoints);
        signInRecordMapper.insert(record);

        // 5. 更新用户积分与等级
        pointsService.addPoints(userId, earnedPoints, 1, "每日签到");

        // 6. 查询最新用户信息，构造返回对象
        User user = userService.getById(userId);
        SignInVO vo = new SignInVO();
        vo.setSignDate(today);
        vo.setContinuousDays(continuousDays);
        vo.setPoints(earnedPoints);
        vo.setTotalPoints(user != null ? user.getPoints() : earnedPoints);
        vo.setLevel(user != null ? user.getLevel() : 1);
        return vo;
    }

    /**
     * 根据连续签到天数计算本次获得积分
     * 基础 5 分；每满 7 天额外 +10；满 30 天再额外 +40
     *
     * @param continuousDays 连续签到天数
     * @return 本次签到获得积分
     */
    private int calculatePoints(int continuousDays) {
        int points = BASE_POINTS;
        if (continuousDays >= 7 && continuousDays % 7 == 0) {
            points += 10;
        }
        if (continuousDays >= 30 && continuousDays % 30 == 0) {
            points += 40;
        }
        return points;
    }

}
