package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.dto.resp.TagVO;
import com.campus.forum.entity.PostTag;
import com.campus.forum.entity.Tag;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.PostTagMapper;
import com.campus.forum.mapper.TagMapper;
import com.campus.forum.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 * 提供标签列表查询、按标签检索帖子等业务逻辑
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TagMapper tagMapper;
    private final PostTagMapper postTagMapper;
    private final PostMapper postMapper;

    /**
     * 查询所有标签，返回包含每个标签帖子数的视图列表
     * - 按 id 升序返回所有标签
     * - 帖子数仅统计已发布（status=0）且未删除的帖子
     *
     * @return 标签视图列表
     */
    @Override
    public List<TagVO> listAllTags() {
        // 查询所有标签，按 id 升序
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                .orderByAsc(Tag::getId));
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        // 为每个标签统计帖子数
        return tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            Long count = postTagMapper.countPostsByTagId(tag.getId());
            vo.setPostCount(count != null ? count : 0L);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 根据标签ID分页查询关联帖子
     * - 校验标签存在
     * - 通过 post_tag 联查 post，并填充 user/section 信息
     * - 批量填充每个帖子的标签列表
     *
     * @param tagId 标签ID
     * @param page  当前页码
     * @param size  每页条数
     * @return 帖子分页结果
     */
    @Override
    public PageResult<PostListVO> getPostsByTag(Long tagId, int page, int size) {
        // 校验标签存在
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }

        // 分页查询关联帖子
        Page<PostListVO> p = new Page<>(page, size);
        IPage<PostListVO> result = postMapper.selectPostsByTag(p, tagId);

        // 批量填充标签
        fillTags(result.getRecords());
        return PageResult.of(result);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 批量为帖子列表填充标签信息
     * 一次性查询所有帖子对应的标签（联表 post_tag 与 tag），按 postId 分组后回填
     *
     * @param records 帖子列表
     */
    private void fillTags(List<PostListVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> postIds = records.stream().map(PostListVO::getId).collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return;
        }
        // 查询这些帖子关联的 post_tag 记录
        List<PostTag> postTags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .in(PostTag::getPostId, postIds));
        if (postTags.isEmpty()) {
            records.forEach(vo -> vo.setTags(Collections.emptyList()));
            return;
        }
        // 查询涉及的标签
        List<Long> tagIds = postTags.stream().map(PostTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        // 按 postId 分组
        Map<Long, List<TagVO>> postIdToTags = postTags.stream()
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
                        Collectors.mapping(
                                pt -> {
                                    Tag t = tagMap.get(pt.getTagId());
                                    TagVO vo = new TagVO();
                                    if (t != null) {
                                        vo.setId(t.getId());
                                        vo.setName(t.getName());
                                    } else {
                                        vo.setId(pt.getTagId());
                                        vo.setName(null);
                                    }
                                    return vo;
                                },
                                Collectors.toList()
                        )
                ));
        // 填充到每个 VO
        for (PostListVO vo : records) {
            List<TagVO> tags = postIdToTags.getOrDefault(vo.getId(), Collections.emptyList());
            vo.setTags(new ArrayList<>(tags));
        }
    }

}
