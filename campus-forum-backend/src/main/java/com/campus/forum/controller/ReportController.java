package com.campus.forum.controller;

import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.CreateReportRequest;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户举报接口
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "举报接口", description = "举报表白墙帖子与评论")
public class ReportController {

    private final ReportService reportService;

    /**
     * 提交举报
     *
     * @param request 举报请求
     * @return 操作结果
     */
    @Operation(summary = "提交举报", description = "登录用户可举报表白墙帖子及其评论")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public Result<String> createReport(@Parameter(description = "举报请求体", required = true)
                                       @Valid @RequestBody CreateReportRequest request) {
        reportService.createReport(request, SecurityUtils.getCurrentUserId());
        return Result.success("举报已提交，管理员将尽快处理");
    }

}
