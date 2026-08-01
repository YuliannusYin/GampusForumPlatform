package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.ClubVO;
import com.campus.forum.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-社团审核接口
 * 提供待审核社团列表、审核通过、审核拒绝等功能
 * 全部接口仅管理员（ROLE_ADMIN / ROLE_SUPER_ADMIN）可访问
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/clubs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = "管理员-社团审核", description = "待审核社团列表、审核通过、审核拒绝")
public class AdminClubController {

    private final ClubService clubService;

    /**
     * 待审核社团列表（status=0）
     *
     * @param page 当前页码，默认 1
     * @param size 每页条数，默认 10
     * @return 待审核社团分页结果
     */
    @Operation(summary = "待审核社团列表", description = "查询 status=0 的待审核社团，按创建时间倒序")
    @GetMapping("")
    public Result<PageResult<ClubVO>> listPendingClubs(@Parameter(description = "当前页码，默认 1")
                                                       @RequestParam(value = "page", defaultValue = "1") long page,
                                                       @Parameter(description = "每页条数，默认 10")
                                                       @RequestParam(value = "size", defaultValue = "10") long size) {
        PageResult<ClubVO> result = clubService.listClubs(page, size, null, 0);
        return Result.success(result);
    }

    /**
     * 审核通过（status=0→1）
     *
     * @param clubId 社团ID
     * @return 操作结果
     */
    @Operation(summary = "审核通过", description = "将待审核社团置为正常，已通过的不重复处理")
    @PutMapping("/{clubId}/approve")
    public Result<String> approveClub(@Parameter(description = "社团ID", required = true)
                                      @PathVariable("clubId") Long clubId) {
        clubService.approveClub(clubId);
        return Result.success("审核通过");
    }

    /**
     * 审核拒绝（status=0→2 禁用）
     *
     * @param clubId 社团ID
     * @return 操作结果
     */
    @Operation(summary = "审核拒绝", description = "将待审核社团置为禁用")
    @PutMapping("/{clubId}/reject")
    public Result<String> rejectClub(@Parameter(description = "社团ID", required = true)
                                     @PathVariable("clubId") Long clubId) {
        clubService.rejectClub(clubId);
        return Result.success("审核已拒绝");
    }

}
