package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.ClubPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团帖子关联 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface ClubPostMapper extends BaseMapper<ClubPost> {

}
