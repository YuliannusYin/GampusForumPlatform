package com.campus.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.dto.req.CreateReportRequest;
import com.campus.forum.dto.req.HandleReportRequest;
import com.campus.forum.dto.resp.ReportVO;
import com.campus.forum.entity.Report;

/**
 * 举报服务
 *
 * @author campus
 */
public interface ReportService extends IService<Report> {

    /**
     * 提交举报（仅表白墙帖及其评论）
     *
     * @param request    举报请求
     * @param reporterId 举报人ID
     */
    void createReport(CreateReportRequest request, Long reporterId);

    /**
     * 管理端分页查询举报
     *
     * @param page       页码
     * @param size       每页条数
     * @param status     状态（可选）
     * @param targetType 目标类型（可选）
     * @return 分页结果
     */
    PageResult<ReportVO> listReports(long page, long size, Integer status, Integer targetType);

    /**
     * 处理举报
     *
     * @param id        举报ID
     * @param request   处理请求
     * @param handlerId 处理人ID
     */
    void handleReport(Long id, HandleReportRequest request, Long handlerId);

}
