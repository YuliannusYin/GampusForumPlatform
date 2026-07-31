package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.forum.entity.SignInRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

/**
 * 签到记录 Mapper 接口
 *
 * @author campus
 */
@Mapper
public interface SignInRecordMapper extends BaseMapper<SignInRecord> {

    /**
     * 根据用户ID与签到日期查询签到记录
     *
     * @param userId   用户ID
     * @param signDate 签到日期
     * @return 签到记录，不存在返回 null
     */
    @Select("SELECT * FROM sign_in_record WHERE user_id = #{userId} AND sign_date = #{signDate} AND deleted = 0")
    SignInRecord selectByUserAndDate(@Param("userId") Long userId, @Param("signDate") LocalDate signDate);

}
