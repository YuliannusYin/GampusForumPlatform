package com.campus.forum.service;

import com.campus.forum.dto.resp.SignInVO;

/**
 * 签到服务接口
 *
 * @author campus
 */
public interface SignInService {

    /**
     * 当前用户每日签到
     * 校验今日是否已签到、计算连续天数与获得积分、更新用户积分与等级
     *
     * @return 签到结果视图对象
     */
    SignInVO signIn();

}
