package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.entity.UserSetting;
import com.campus.forum.mapper.UserSettingMapper;
import com.campus.forum.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户设置服务实现类
 * 封装用户通知偏好的查询、初始化与判断逻辑
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl extends ServiceImpl<UserSettingMapper, UserSetting> implements UserSettingService {

    /**
     * 获取用户设置，不存在则创建默认设置（全部开启）
     *
     * @param userId 用户ID
     * @return 用户设置实体
     */
    @Override
    public UserSetting getOrCreate(Long userId) {
        UserSetting setting = this.getOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, userId));
        if (setting == null) {
            setting = new UserSetting();
            setting.setUserId(userId);
            setting.setNotifyComment(1);
            setting.setNotifyLike(1);
            setting.setNotifyMessage(1);
            this.save(setting);
        }
        return setting;
    }

    /**
     * 判断某类型通知是否开启
     * type: 1评论 2点赞 3私信 4系统（系统通知始终返回 true）
     * 设置记录不存在时默认返回 true（兼容老用户）
     *
     * @param userId 用户ID
     * @param type   通知类型 1评论 2点赞 3私信 4系统
     * @return true 开启 false 关闭
     */
    @Override
    public boolean isNotifyEnabled(Long userId, int type) {
        UserSetting setting = this.getOne(new LambdaQueryWrapper<UserSetting>()
                .eq(UserSetting::getUserId, userId));
        // 老用户无设置记录，默认所有通知开启
        if (setting == null) {
            return true;
        }
        switch (type) {
            case 1:
                return setting.getNotifyComment() != null && setting.getNotifyComment() == 1;
            case 2:
                return setting.getNotifyLike() != null && setting.getNotifyLike() == 1;
            case 3:
                return setting.getNotifyMessage() != null && setting.getNotifyMessage() == 1;
            case 4:
                return true;
            default:
                return true;
        }
    }

}
