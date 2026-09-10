package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.SectionVO;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Section;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.service.SectionService;
import com.campus.forum.utils.PostAnonymityHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 板块接口
 * 提供板块的查询、创建、更新、删除（逻辑删除）功能
 * 创建/更新/删除仅管理员可操作
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "板块接口", description = "板块查询、创建、更新、删除")
public class SectionController {

    private final SectionService sectionService;
    private final PostMapper postMapper;

    /**
     * 查询所有板块（按 sort 升序），返回包含帖子数的视图列表
     *
     * @return 板块视图列表
     */
    @Operation(summary = "查询所有板块", description = "按 sort 升序返回所有板块，包含每个板块的帖子数")
    @GetMapping("")
    public Result<List<SectionVO>> listSections() {
        List<Section> sections = sectionService.list(new LambdaQueryWrapper<Section>()
                .orderByAsc(Section::getSort));
        List<SectionVO> voList = sections.stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 查询单个板块
     *
     * @param id 板块ID
     * @return 板块视图
     */
    @Operation(summary = "查询单个板块", description = "根据板块ID查询板块详情，包含帖子数")
    @GetMapping("/{id}")
    public Result<SectionVO> getSection(@Parameter(description = "板块ID", required = true)
                                        @PathVariable("id") Long id) {
        Section section = sectionService.getById(id);
        if (section == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }
        return Result.success(toVO(section));
    }

    /**
     * 创建板块（仅管理员）
     *
     * @param section 板块信息
     * @return 创建后的板块
     */
    @Operation(summary = "创建板块", description = "仅管理员可操作")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping("")
    public Result<SectionVO> createSection(@Parameter(description = "板块信息", required = true)
                                           @Valid @RequestBody Section section) {
        sectionService.save(section);
        return Result.success(toVO(sectionService.getById(section.getId())));
    }

    /**
     * 更新板块（仅管理员）
     *
     * @param id      板块ID
     * @param section 板块信息
     * @return 更新后的板块
     */
    @Operation(summary = "更新板块", description = "仅管理员可操作")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public Result<SectionVO> updateSection(@Parameter(description = "板块ID", required = true)
                                           @PathVariable("id") Long id,
                                           @Parameter(description = "板块信息", required = true)
                                           @Valid @RequestBody Section section) {
        Section exist = sectionService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }
        section.setId(id);
        // 表白墙编码不可被普通编辑覆盖，避免匿名能力失效
        if (PostAnonymityHelper.CONFESSION_SECTION_CODE.equals(exist.getCode())) {
            section.setCode(exist.getCode());
        }
        sectionService.updateById(section);
        return Result.success(toVO(sectionService.getById(id)));
    }

    /**
     * 删除板块（逻辑删除，仅管理员）
     *
     * @param id 板块ID
     * @return 操作结果
     */
    @Operation(summary = "删除板块", description = "逻辑删除，仅管理员可操作")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<String> deleteSection(@Parameter(description = "板块ID", required = true)
                                        @PathVariable("id") Long id) {
        Section exist = sectionService.getById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }
        sectionService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 将 Section 实体转换为 SectionVO，并统计该板块下的帖子数
     *
     * @param section 板块实体
     * @return 板块视图对象
     */
    private SectionVO toVO(Section section) {
        SectionVO vo = new SectionVO();
        BeanUtils.copyProperties(section, vo);
        // 统计该板块下已发布且未删除的帖子数
        Long count = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getSectionId, section.getId())
                .eq(Post::getStatus, 0));
        vo.setPostCount(count);
        return vo;
    }

}
