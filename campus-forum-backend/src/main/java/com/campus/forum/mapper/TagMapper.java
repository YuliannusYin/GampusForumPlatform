package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
