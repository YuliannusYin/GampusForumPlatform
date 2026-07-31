package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.CreatePostRequest;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子接口
 * 提供发帖、编辑、删除、分页列表、详情查询等功能
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
@Tag(name = "帖子接口", description = "发帖、编辑、删除、列表、详情")
public class PostController {

    /** 管理员角色编码 */
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final PostService postService;

    /**
     * 发帖
     *
     * @param request 发帖请求
     * @return 创建后的帖子详情
     */
    @Operation(summary = "发帖", description = "登录用户可发帖，发帖奖励 5 积分（不写积分记录）")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public Result<PostDetailVO> createPost(@Parameter(description = "发帖请求体", required = true)
                                           @Valid @RequestBody CreatePostRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PostDetailVO vo = postService.createPost(request, userId);
        return Result.success(vo);
    }

    /**
     * 编辑帖子
     * 仅作者或管理员可操作
     *
     * @param id      帖子ID
     * @param request 编辑请求
     * @return 更新后的帖子详情
     */
    @Operation(summary = "编辑帖子", description = "仅作者或管理员可操作，会更新标签关联")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public Result<PostDetailVO> updatePost(@Parameter(description = "帖子ID", required = true)
                                           @PathVariable("id") Long id,
                                           @Parameter(description = "编辑请求体", required = true)
                                           @Valid @RequestBody CreatePostRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = isAdmin();
        PostDetailVO vo = postService.updatePost(id, request, userId, isAdmin);
        return Result.success(vo);
    }

    /**
     * 删除帖子（逻辑删除）
     * 仅作者或管理员可操作
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @Operation(summary = "删除帖子", description = "逻辑删除，仅作者或管理员可操作")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@Parameter(description = "帖子ID", required = true)
                                     @PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = isAdmin();
        postService.deletePost(id, userId, isAdmin);
        return Result.success("删除成功");
    }

    /**
     * 分页查询帖子列表
     *
     * @param page      当前页码，默认 1
     * @param size      每页条数，默认 10
     * @param sectionId 板块ID（可选）
     * @param sort      排序方式 latest（最新，默认）/ hot（最热）
     * @return 分页结果
     */
    @Operation(summary = "帖子分页列表", description = "支持按板块筛选与 latest/hot 排序，置顶帖优先，仅返回已发布帖子")
    @GetMapping("")
    public Result<PageResult<PostListVO>> listPosts(@Parameter(description = "当前页码，默认 1")
                                                    @RequestParam(value = "page", defaultValue = "1") long page,
                                                    @Parameter(description = "每页条数，默认 10")
                                                    @RequestParam(value = "size", defaultValue = "10") long size,
                                                    @Parameter(description = "板块ID（可选）")
                                                    @RequestParam(value = "sectionId", required = false) Long sectionId,
                                                    @Parameter(description = "排序方式 latest（最新，默认）/ hot（最热）")
                                                    @RequestParam(value = "sort", defaultValue = "latest") String sort) {
        PageResult<PostListVO> result = postService.listPosts(page, size, sectionId, sort);
        return Result.success(result);
    }

    /**
     * 查询帖子详情，浏览数 +1
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    @Operation(summary = "帖子详情", description = "查询帖子详情（含正文），浏览数 +1")
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPost(@Parameter(description = "帖子ID", required = true)
                                        @PathVariable("id") Long id) {
        PostDetailVO vo = postService.getPostDetail(id);
        return Result.success(vo);
    }

    /**
     * 关键词搜索帖子
     * 匹配标题、正文、作者用户名；支持按板块与时间范围筛选；按创建时间倒序
     *
     * @param keyword   关键词（必填，匹配标题/正文/作者用户名）
     * @param sectionId 板块ID（可选）
     * @param startTime 起始时间，格式 yyyy-MM-dd（可选）
     * @param endTime   截止时间，格式 yyyy-MM-dd（可选）
     * @param page      当前页码，默认 1
     * @param size      每页条数，默认 10
     * @return 分页结果
     */
    @Operation(summary = "关键词搜索帖子", description = "按标题/正文/作者用户名匹配，支持板块与时间范围筛选，按创建时间倒序")
    @GetMapping("/search")
    public Result<PageResult<PostListVO>> search(@Parameter(description = "关键词，必填", required = true)
                                                 @RequestParam(value = "keyword") @NotBlank(message = "关键词不能为空") String keyword,
                                                 @Parameter(description = "板块ID（可选）")
                                                 @RequestParam(value = "sectionId", required = false) Long sectionId,
                                                 @Parameter(description = "起始时间，格式 yyyy-MM-dd（可选）")
                                                 @RequestParam(value = "startTime", required = false) String startTime,
                                                 @Parameter(description = "截止时间，格式 yyyy-MM-dd（可选）")
                                                 @RequestParam(value = "endTime", required = false) String endTime,
                                                 @Parameter(description = "当前页码，默认 1")
                                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                                 @Parameter(description = "每页条数，默认 10")
                                                 @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<PostListVO> result = postService.search(keyword, sectionId, startTime, endTime, page, size);
        return Result.success(result);
    }

    /**
     * 判断当前登录用户是否具有 ROLE_ADMIN 角色
     *
     * @return true 表示是管理员
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (ROLE_ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}
