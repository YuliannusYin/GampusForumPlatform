package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.entity.UserSetting;

/**
 * 用户设置服务接口
 * 提供用户通知偏好的查询、初始化与判断
 *
 * @author campus
 */
public interface UserSettingService extends IService<UserSetting> {

    /**
     * 获取用户设置，不存在则创建默认设置（全部开启）
     *
     * @param userId 用户ID
     * @return 用户设置实体
     */
    UserSetting getOrCreate(Long userId);

    /**
     * 判断某类型通知是否开启
     * type: 1评论 2点赞 3私信 4系统（系统通知始终返回 true）
     * 设置记录不存在时默认返回 true（兼容老用户）
     *
     * @param userId 用户ID
     * @param type   通知类型 1评论 2点赞 3私信 4系统
     * @return true 开启 false 关闭
     */
    boolean isNotifyEnabled(Long userId, int type);

}
