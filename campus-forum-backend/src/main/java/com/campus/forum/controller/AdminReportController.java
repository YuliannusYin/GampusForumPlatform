package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.HandleReportRequest;
import com.campus.forum.dto.resp.ReportVO;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-举报处理接口
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "管理员-举报处理", description = "查看与处理表白墙举报")
public class AdminReportController {

    private final ReportService reportService;

    /**
     * 分页查询举报
     *
     * @param page       页码
     * @param size       每页条数
     * @param status     状态（可选）
     * @param targetType 目标类型（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询举报", description = "支持按状态、目标类型筛选")
    @GetMapping("")
    public Result<PageResult<ReportVO>> listReports(@Parameter(description = "当前页码，默认 1")
                                                    @RequestParam(value = "page", defaultValue = "1") long page,
                                                    @Parameter(description = "每页条数，默认 10")
                                                    @RequestParam(value = "size", defaultValue = "10") long size,
                                                    @Parameter(description = "状态（可选，0待处理 1属实 2驳回）")
                                                    @RequestParam(value = "status", required = false) Integer status,
                                                    @Parameter(description = "目标类型（可选，1帖子 2评论）")
                                                    @RequestParam(value = "targetType", required = false) Integer targetType) {
        return Result.success(reportService.listReports(page, size, status, targetType));
    }

    /**
     * 处理举报
     *
     * @param id      举报ID
     * @param request 处理请求
     * @return 操作结果
     */
    @Operation(summary = "处理举报", description = "属实则删除对应帖/评，驳回仅更新状态")
    @PutMapping("/{id}/handle")
    public Result<String> handleReport(@Parameter(description = "举报ID", required = true)
                                       @PathVariable("id") Long id,
                                       @Parameter(description = "处理请求体", required = true)
                                       @Valid @RequestBody HandleReportRequest request) {
        reportService.handleReport(id, request, SecurityUtils.getCurrentUserId());
        return Result.success(request.getStatus() == 1 ? "已确认违规并处理内容" : "已驳回该举报");
    }

}
