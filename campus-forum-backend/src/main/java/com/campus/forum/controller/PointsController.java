package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.result.Result;
import com.campus.forum.entity.PointsRecord;
import com.campus.forum.mapper.PointsRecordMapper;
import com.campus.forum.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分接口
 * 提供积分明细记录的分页查询
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Tag(name = "积分接口", description = "积分明细记录查询")
public class PointsController {

    private final PointsRecordMapper pointsRecordMapper;

    /**
     * 查询积分明细分页（按创建时间倒序）
     *
     * @param current 当前页码，默认 1
     * @param size    每页条数，默认 10
     * @return 积分记录分页
     */
    @Operation(summary = "积分明细", description = "分页查询当前用户的积分变更记录，按 create_time 倒序")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/records")
    public Result<Page<PointsRecord>> records(@Parameter(description = "当前页码，默认 1")
                                              @RequestParam(value = "current", defaultValue = "1") long current,
                                              @Parameter(description = "每页条数，默认 10")
                                              @RequestParam(value = "size", defaultValue = "10") long size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<PointsRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreateTime);
        // TODO: 后续统一为 PageResult
        return Result.success(pointsRecordMapper.selectPage(page, wrapper));
    }

}
