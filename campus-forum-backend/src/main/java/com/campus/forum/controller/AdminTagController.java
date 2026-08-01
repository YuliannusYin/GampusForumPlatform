package com.campus.forum.controller;

import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.TagRequest;
import com.campus.forum.entity.Tag;
import com.campus.forum.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-标签管理接口
 * 提供标签的创建、修改、删除（逻辑删除）功能
 * 全部接口仅管理员（ROLE_ADMIN）可访问
 * 标签查询已在 TagController 实现，此处不重复
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@io.swagger.v3.oas.annotations.tags.Tag(name = "管理员-标签管理", description = "标签创建、修改、删除")
public class AdminTagController {

    private final TagService tagService;

    /**
     * 创建标签
     *
     * @param request 标签请求
     * @return 创建后的标签
     */
    @Operation(summary = "创建标签", description = "创建一个新标签，name 唯一")
    @PostMapping("")
    public Result<Tag> createTag(@Parameter(description = "标签请求体", required = true)
                                 @Valid @RequestBody TagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.getName());
        tagService.save(tag);
        return Result.success(tagService.getById(tag.getId()));
    }

    /**
     * 修改标签
     *
     * @param id      标签ID
     * @param request 标签请求
     * @return 更新后的标签
     */
    @Operation(summary = "修改标签", description = "修改标签名称")
    @PutMapping("/{id}")
    public Result<Tag> updateTag(@Parameter(description = "标签ID", required = true)
                                  @PathVariable("id") Long id,
                                  @Parameter(description = "标签请求体", required = true)
                                  @Valid @RequestBody TagRequest request) {
        Tag exist = tagService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        Tag update = new Tag();
        update.setId(id);
        update.setName(request.getName());
        tagService.updateById(update);
        return Result.success(tagService.getById(id));
    }

    /**
     * 删除标签（逻辑删除）
     *
     * @param id 标签ID
     * @return 操作结果
     */
    @Operation(summary = "删除标签", description = "逻辑删除标签")
    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@Parameter(description = "标签ID", required = true)
                                     @PathVariable("id") Long id) {
        Tag exist = tagService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        tagService.removeById(id);
        return Result.success("删除成功");
    }

}
