package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.PostEssenceRequest;
import com.campus.forum.dto.req.PostTopRequest;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.Post;
import com.campus.forum.mapper.AdminMapper;
import com.campus.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-帖子管理接口
 * 提供帖子分页查询（含已删除/草稿）、删除、置顶/取消置顶、加精/取消加精等功能
 * 全部接口仅管理员（ROLE_ADMIN）可访问
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理员-帖子管理", description = "帖子分页、删除、置顶、加精")
public class AdminPostController {

    private final AdminMapper adminMapper;
    private final PostService postService;

    /**
     * 分页查询所有帖子（含已删除/草稿）
     *
     * @param page      当前页码，默认 1
     * @param size      每页条数，默认 10
     * @param keyword   标题关键词（可选）
     * @param sectionId 板块ID（可选）
     * @param status    帖子状态（可选，0已发布 1草稿 2已删除）
     * @return 帖子分页结果
     */
    @Operation(summary = "分页查询所有帖子", description = "含已删除/草稿，支持按标题/板块/状态筛选")
    @GetMapping("")
    public Result<PageResult<PostListVO>> listPosts(@Parameter(description = "当前页码，默认 1")
                                                     @RequestParam(value = "page", defaultValue = "1") long page,
                                                     @Parameter(description = "每页条数，默认 10")
                                                     @RequestParam(value = "size", defaultValue = "10") long size,
                                                     @Parameter(description = "标题关键词（可选）")
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @Parameter(description = "板块ID（可选）")
                                                     @RequestParam(value = "sectionId", required = false) Long sectionId,
                                                     @Parameter(description = "帖子状态（可选，0已发布 1草稿 2已删除）")
                                                     @RequestParam(value = "status", required = false) Integer status) {
        Page<PostListVO> p = new Page<>(page, size);
        IPage<PostListVO> result = adminMapper.selectAdminPostList(p, keyword, sectionId, status);
        return Result.success(PageResult.of(result));
    }

    /**
     * 删除帖子（逻辑删除，status=2）
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @Operation(summary = "删除帖子", description = "逻辑删除，将帖子 status 置为 2（已删除）")
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@Parameter(description = "帖子ID", required = true)
                                      @PathVariable("id") Long id) {
        Post exist = postService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        Post update = new Post();
        update.setId(id);
        update.setStatus(2);
        postService.updateById(update);
        return Result.success("删除成功");
    }

    /**
     * 置顶/取消置顶帖子
     *
     * @param id      帖子ID
     * @param request 置顶请求
     * @return 操作结果
     */
    @Operation(summary = "置顶/取消置顶帖子", description = "isTop=1 置顶，0 取消置顶")
    @PutMapping("/{id}/top")
    public Result<String> updateTop(@Parameter(description = "帖子ID", required = true)
                                    @PathVariable("id") Long id,
                                    @Parameter(description = "置顶请求体", required = true)
                                    @Valid @RequestBody PostTopRequest request) {
        Post exist = postService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        Post update = new Post();
        update.setId(id);
        update.setIsTop(request.getIsTop());
        postService.updateById(update);
        return Result.success(request.getIsTop() == 1 ? "置顶成功" : "取消置顶成功");
    }

    /**
     * 加精/取消加精帖子
     *
     * @param id      帖子ID
     * @param request 加精请求
     * @return 操作结果
     */
    @Operation(summary = "加精/取消加精帖子", description = "isEssence=1 加精，0 取消加精")
    @PutMapping("/{id}/essence")
    public Result<String> updateEssence(@Parameter(description = "帖子ID", required = true)
                                         @PathVariable("id") Long id,
                                         @Parameter(description = "加精请求体", required = true)
                                         @Valid @RequestBody PostEssenceRequest request) {
        Post exist = postService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        Post update = new Post();
        update.setId(id);
        update.setIsEssence(request.getIsEssence());
        postService.updateById(update);
        return Result.success(request.getIsEssence() == 1 ? "加精成功" : "取消加精成功");
    }

}
