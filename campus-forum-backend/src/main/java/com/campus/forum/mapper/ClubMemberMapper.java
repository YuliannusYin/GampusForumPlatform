package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.ClubMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团成员 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface ClubMemberMapper extends BaseMapper<ClubMember> {

}
