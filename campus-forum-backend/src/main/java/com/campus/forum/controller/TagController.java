package com.campus.forum.controller;

import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.dto.resp.TagVO;
import com.campus.forum.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签接口
 * 提供标签列表查询、按标签检索帖子等功能
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "标签接口", description = "标签列表、按标签检索帖子")
public class TagController {

    private final TagService tagService;

    /**
     * 获取所有标签列表（含每个标签的帖子数）
     *
     * @return 标签视图列表
     */
    @Operation(summary = "获取所有标签", description = "返回所有标签列表，包含每个标签下的帖子数")
    @GetMapping("")
    public Result<List<TagVO>> listTags() {
        List<TagVO> tags = tagService.listAllTags();
        return Result.success(tags);
    }

    /**
     * 按标签检索帖子
     *
     * @param tagId 标签ID
     * @param page  当前页码，默认 1
     * @param size  每页条数，默认 10
     * @return 帖子分页结果
     */
    @Operation(summary = "按标签检索帖子", description = "根据标签ID分页查询关联帖子，按创建时间倒序")
    @GetMapping("/{tagId}/posts")
    public Result<PageResult<PostListVO>> getPostsByTag(@Parameter(description = "标签ID", required = true)
                                                        @PathVariable("tagId") Long tagId,
                                                        @Parameter(description = "当前页码，默认 1")
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @Parameter(description = "每页条数，默认 10")
                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<PostListVO> result = tagService.getPostsByTag(tagId, page, size);
        return Result.success(result);
    }

}
