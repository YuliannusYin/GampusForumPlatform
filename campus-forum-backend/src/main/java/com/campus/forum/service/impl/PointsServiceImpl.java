package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.forum.entity.PointsRecord;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.PointsRecordMapper;
import com.campus.forum.service.PointsService;
import com.campus.forum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分服务实现类
 * 提供积分变更与等级计算的核心逻辑，使用原子 SQL 更新积分避免并发问题
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    /** 等级计算基数：每 100 分升一级 */
    private static final int LEVEL_STEP = 100;

    /** 最高等级 */
    private static final int MAX_LEVEL = 99;

    private final PointsRecordMapper pointsRecordMapper;
    private final UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, int value, int type, String description) {
        // 1. 保存积分变更记录
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeValue(value);
        record.setType(type);
        record.setDescription(description);
        pointsRecordMapper.insert(record);

        // 2. 原子更新用户积分（points = points + value），避免并发问题
        userService.update(new LambdaUpdateWrapper<User>()
                .setSql("points = points + " + value)
                .eq(User::getId, userId));

        // 3. 读取最新积分并更新等级
        User user = userService.getById(userId);
        if (user != null) {
            int newLevel = calculateLevel(user.getPoints());
            if (user.getLevel() == null || !user.getLevel().equals(newLevel)) {
                userService.update(new LambdaUpdateWrapper<User>()
                        .set(User::getLevel, newLevel)
                        .eq(User::getId, userId));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPointsWithLevelCheck(Long userId, int value, int type, String description) {
        // 1. 保存积分变更记录
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeValue(value);
        record.setType(type);
        record.setDescription(description);
        pointsRecordMapper.insert(record);

        // 2. 查询旧等级用于后续升级判断
        User user = userService.getById(userId);
        int oldLevel = (user != null && user.getLevel() != null) ? user.getLevel() : 1;

        // 3. 原子更新用户积分
        userService.update(new LambdaUpdateWrapper<User>()
                .setSql("points = points + " + value)
                .eq(User::getId, userId));

        // 4. 读取最新积分并更新等级
        user = userService.getById(userId);
        if (user != null) {
            int newLevel = calculateLevel(user.getPoints());
            if (newLevel != oldLevel) {
                userService.update(new LambdaUpdateWrapper<User>()
                        .set(User::getLevel, newLevel)
                        .eq(User::getId, userId));
                return newLevel > oldLevel;
            }
        }
        return false;
    }

    /**
     * 根据总积分计算等级
     * 规则：level = points / 100 + 1，最高 99 级
     *
     * @param points 用户总积分
     * @return 用户等级
     */
    private int calculateLevel(int points) {
        return Math.min(points / LEVEL_STEP + 1, MAX_LEVEL);
    }

}
