package com.campus.forum.controller;

import com.campus.forum.common.result.Result;
import com.campus.forum.service.FavoriteService;
import com.campus.forum.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞收藏接口
 * 提供帖子/评论的点赞、帖子收藏，以及当前用户对帖子的点赞收藏状态查询
 * 所有接口均需登录
 *
 * @author campus
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "点赞收藏接口", description = "帖子/评论点赞、帖子收藏、互动状态查询")
public class InteractionController {

    private final LikeService likeService;
    private final FavoriteService favoriteService;

    /**
     * 点赞/取消点赞帖子
     *
     * @param postId 帖子ID
     * @return 包含 liked（当前是否已赞）与 likeCount（帖子最新点赞数）的 Map
     */
    @Operation(summary = "点赞/取消点赞帖子", description = "切换当前用户对帖子的点赞状态，返回最新点赞状态与点赞数")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{postId}/like")
    public Result<Map<String, Object>> toggleLikePost(@Parameter(description = "帖子ID", required = true)
                                                      @PathVariable("postId") Long postId) {
        Map<String, Object> data = likeService.toggleLikePost(postId);
        return Result.success(data);
    }

    /**
     * 收藏/取消收藏帖子
     *
     * @param postId 帖子ID
     * @return 包含 favorited（当前是否已收藏）与 favoriteCount（帖子最新收藏数）的 Map
     */
    @Operation(summary = "收藏/取消收藏帖子", description = "切换当前用户对帖子的收藏状态，返回最新收藏状态与收藏数")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/posts/{postId}/favorite")
    public Result<Map<String, Object>> toggleFavorite(@Parameter(description = "帖子ID", required = true)
                                                      @PathVariable("postId") Long postId) {
        Map<String, Object> data = favoriteService.toggleFavorite(postId);
        return Result.success(data);
    }

    /**
     * 点赞/取消点赞评论
     *
     * @param commentId 评论ID
     * @return 包含 liked（当前是否已赞）与 likeCount（评论最新点赞数）的 Map
     */
    @Operation(summary = "点赞/取消点赞评论", description = "切换当前用户对评论的点赞状态，返回最新点赞状态与点赞数")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments/{commentId}/like")
    public Result<Map<String, Object>> toggleLikeComment(@Parameter(description = "评论ID", required = true)
                                                         @PathVariable("commentId") Long commentId) {
        Map<String, Object> data = likeService.toggleLikeComment(commentId);
        return Result.success(data);
    }

    /**
     * 查询当前用户对该帖子的点赞与收藏状态
     *
     * @param postId 帖子ID
     * @return 包含 liked（是否已赞）与 favorited（是否已收藏）的 Map
     */
    @Operation(summary = "查询互动状态", description = "查询当前用户对该帖子的点赞与收藏状态")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/posts/{postId}/interactions")
    public Result<Map<String, Object>> getInteractions(@Parameter(description = "帖子ID", required = true)
                                                       @PathVariable("postId") Long postId) {
        Map<String, Object> data = new HashMap<>(2);
        data.put("liked", likeService.isLikedPost(postId));
        data.put("favorited", favoriteService.isFavorited(postId));
        return Result.success(data);
    }

}
