package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注关系 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

}
