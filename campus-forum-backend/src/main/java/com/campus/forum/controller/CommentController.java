package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.req.CreateCommentRequest;
import com.campus.forum.dto.resp.CommentVO;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论接口
 * 提供发表评论/回复、评论列表、回复列表、删除评论等功能
 *
 * @author campus
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
@Tag(name = "评论接口", description = "发表评论/回复、评论列表、回复列表、删除评论")
public class CommentController {

    /** 管理员角色编码 */
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final CommentService commentService;

    /**
     * 发表评论或回复
     * parentId 为 0 或空时表示顶级评论，非 0 时表示回复指定评论
     *
     * @param postId   帖子ID
     * @param request  评论请求体
     * @return 创建后的评论视图（含作者信息）
     */
    @Operation(summary = "发表评论/回复", description = "登录用户可发表评论或回复，评论奖励 2 积分，并通知帖子作者与父评论作者")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{postId}/comments")
    public Result<CommentVO> createComment(@Parameter(description = "帖子ID", required = true)
                                           @PathVariable("postId") Long postId,
                                           @Parameter(description = "评论请求体", required = true)
                                           @Valid @RequestBody CreateCommentRequest request) {
        CommentVO vo = commentService.createComment(postId, request);
        return Result.success(vo);
    }

    /**
     * 分页查询某帖子的顶级评论
     * 每条评论带 replyCount（子回复数）
     *
     * @param postId 帖子ID
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @return 分页结果
     */
    @Operation(summary = "评论分页列表", description = "分页查询某帖子的顶级评论（parent_id=0），每条带 replyCount，按创建时间倒序")
    @GetMapping("/posts/{postId}/comments")
    public Result<PageResult<CommentVO>> listComments(@Parameter(description = "帖子ID", required = true)
                                                      @PathVariable("postId") Long postId,
                                                      @Parameter(description = "当前页码，默认 1")
                                                      @RequestParam(value = "page", defaultValue = "1") long page,
                                                      @Parameter(description = "每页条数，默认 10")
                                                      @RequestParam(value = "size", defaultValue = "10") long size) {
        PageResult<CommentVO> result = commentService.listComments(postId, page, size);
        return Result.success(result);
    }

    /**
     * 分页查询某评论的回复列表
     *
     * @param commentId 父评论ID
     * @param page      当前页码，默认 1
     * @param size      每页条数，默认 5
     * @return 分页结果
     */
    @Operation(summary = "回复分页列表", description = "分页查询某评论的回复列表（parent_id=commentId），按创建时间正序")
    @GetMapping("/comments/{commentId}/replies")
    public Result<PageResult<CommentVO>> listReplies(@Parameter(description = "父评论ID", required = true)
                                                     @PathVariable("commentId") Long commentId,
                                                     @Parameter(description = "当前页码，默认 1")
                                                     @RequestParam(value = "page", defaultValue = "1") long page,
                                                     @Parameter(description = "每页条数，默认 5")
                                                     @RequestParam(value = "size", defaultValue = "5") long size) {
        PageResult<CommentVO> result = commentService.listReplies(commentId, page, size);
        return Result.success(result);
    }

    /**
     * 删除评论（逻辑删除）
     * 仅作者或管理员可操作，帖子 commentCount -1（不减为负）
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @Operation(summary = "删除评论", description = "逻辑删除，仅作者或管理员可操作，帖子评论数 -1")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/comments/{id}")
    public Result<String> deleteComment(@Parameter(description = "评论ID", required = true)
                                        @PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = isAdmin();
        commentService.deleteComment(id, userId, isAdmin);
        return Result.success("删除成功");
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
