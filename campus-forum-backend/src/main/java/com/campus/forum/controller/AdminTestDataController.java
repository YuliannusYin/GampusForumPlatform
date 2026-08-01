package com.campus.forum.controller;

import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.TestDataImportResult;
import com.campus.forum.dto.resp.TestDataRemoveResult;
import com.campus.forum.dto.resp.TestDataStatus;
import com.campus.forum.service.TestDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超级管理员-测试数据管理接口
 * 提供一键导入/移除系统测试数据的能力，仅超级管理员（ROLE_SUPER_ADMIN）可访问
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/test-data")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "超级管理员-测试数据管理", description = "一键导入/移除系统测试数据")
public class AdminTestDataController {

    private final TestDataService testDataService;

    /**
     * 导入测试数据
     * 一键生成全量测试数据（用户、帖子、评论、社团等）
     *
     * @return 导入结果
     */
    @Operation(summary = "导入测试数据", description = "一键生成全量测试数据（用户、帖子、评论、社团等）")
    @PostMapping("/import")
    public Result<TestDataImportResult> importTestData() {
        return Result.success(testDataService.importTestData());
    }

    /**
     * 移除测试数据
     * 一键移除所有以 test_ 前缀标识的测试数据
     *
     * @return 移除结果
     */
    @Operation(summary = "移除测试数据", description = "一键移除所有以 test_ 前缀标识的测试数据")
    @DeleteMapping("")
    public Result<TestDataRemoveResult> removeTestData() {
        return Result.success(testDataService.removeTestData());
    }

    /**
     * 查询测试数据状态
     * 查询当前系统中各类测试数据的数量
     *
     * @return 测试数据状态
     */
    @Operation(summary = "测试数据状态", description = "查询当前系统中各类测试数据的数量")
    @GetMapping("/status")
    public Result<TestDataStatus> status() {
        return Result.success(testDataService.status());
    }

}
