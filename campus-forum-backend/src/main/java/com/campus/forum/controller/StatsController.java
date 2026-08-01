package com.campus.forum.controller;

import com.campus.forum.common.result.Result;
import com.campus.forum.mapper.AdminMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-数据统计接口
 * 提供总量统计、发帖趋势、板块帖子分布等数据
 * 全部接口仅管理员（ROLE_ADMIN）可访问
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "管理员-数据统计", description = "总量统计、发帖趋势、板块分布")
public class StatsController {

    private final AdminMapper adminMapper;

    /**
     * 总量统计
     * 返回用户总数、帖子总数、评论总数、今日新增用户、今日新增帖子
     *
     * @return 统计数据
     */
    @Operation(summary = "总量统计", description = "返回用户/帖子/评论总数及今日新增数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", adminMapper.countTotalUsers());
        data.put("totalPosts", adminMapper.countTotalPosts());
        data.put("totalComments", adminMapper.countTotalComments());
        data.put("todayNewUsers", adminMapper.countTodayNewUsers());
        data.put("todayNewPosts", adminMapper.countTodayNewPosts());
        return Result.success(data);
    }

    /**
     * 近 N 天发帖趋势
     *
     * @param days 天数，默认 7
     * @return 每日发帖统计列表，每项 {date: "yyyy-MM-dd", count: 数量}
     */
    @Operation(summary = "发帖趋势", description = "近 N 天每日发帖数量统计，默认 7 天")
    @GetMapping("/post/trend")
    public Result<List<Map<String, Object>>> postTrend(@Parameter(description = "天数，默认 7")
                                                        @RequestParam(value = "days", defaultValue = "7") int days) {
        // 规范化天数，最小为 1
        int n = days < 1 ? 7 : days;
        // 计算起始时间：今天往前推 (n-1) 天的零点，共覆盖 n 天（含今天）
        LocalDateTime startTime = LocalDate.now().minusDays(n - 1L).atStartOfDay();
        List<Map<String, Object>> trend = adminMapper.selectPostTrend(startTime);
        return Result.success(trend);
    }

    /**
     * 板块帖子分布
     *
     * @return 板块分布列表，每项 {sectionId, sectionName, count}
     */
    @Operation(summary = "板块帖子分布", description = "统计每个板块下已发布且未删除的帖子数")
    @GetMapping("/section/distribution")
    public Result<List<Map<String, Object>>> sectionDistribution() {
        List<Map<String, Object>> distribution = adminMapper.selectSectionDistribution();
        return Result.success(distribution);
    }

}
