package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.UserSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户设置 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface UserSettingMapper extends BaseMapper<UserSetting> {

}
