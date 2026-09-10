package com.campus.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.forum.dto.resp.ReportVO;
import com.campus.forum.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 举报 Mapper
 *
 * @author campus
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 管理端分页查询举报
     *
     * @param page       分页
     * @param status     状态（可选）
     * @param targetType 目标类型（可选）
     * @return 分页结果
     */
    IPage<ReportVO> selectAdminReportList(IPage<ReportVO> page,
                                          @Param("status") Integer status,
                                          @Param("targetType") Integer targetType);

}
