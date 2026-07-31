package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.CreatePostRequest;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.dto.resp.TagVO;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostTag;
import com.campus.forum.entity.Section;
import com.campus.forum.entity.Tag;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.PostTagMapper;
import com.campus.forum.mapper.SectionMapper;
import com.campus.forum.mapper.TagMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 * 封装发帖、编辑、删除、分页列表、详情查询等业务逻辑
 *
 * @author campus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    /** 发帖奖励积分（后续 Task 14 由 PointsService 统一管理） */
    private static final int CREATE_POST_REWARD_POINTS = 5;

    /** 搜索接口日期参数格式（yyyy-MM-dd） */
    private static final DateTimeFormatter SEARCH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final TagMapper tagMapper;
    private final SectionMapper sectionMapper;
    private final UserMapper userMapper;

    /**
     * 发帖：
     * 1. 校验板块存在
     * 2. 构造 Post 实体并保存（status=0 已发布）
     * 3. 若 tagIds 非空，批量插入 post_tag 关联
     * 4. 给作者增加积分（user.points += 5，不创建 points_record）
     *
     * @param request 发帖请求
     * @param userId  当前用户ID
     * @return 创建后的帖子详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createPost(CreatePostRequest request, Long userId) {
        // 校验板块存在
        Section section = sectionMapper.selectById(request.getSectionId());
        if (section == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }

        // 构造 Post 实体
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setUserId(userId);
        post.setSectionId(request.getSectionId());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(0);
        postMapper.insert(post);

        // 保存标签关联
        savePostTags(post.getId(), request.getTagIds());

        // 发帖奖励积分（后续 Task 14 将由 PointsService.addPoints 统一处理）
        addPointsForCreatePost(userId);

        // 返回详情（创建时不计入浏览数）
        return loadPostDetail(post.getId());
    }

    /**
     * 编辑帖子：
     * 1. 校验帖子存在
     * 2. 校验权限（作者或管理员）
     * 3. 更新帖子字段
     * 4. 更新标签关联（先删旧再插新）
     *
     * @param id        帖子ID
     * @param request   编辑请求
     * @param userId    当前用户ID
     * @param isAdmin   当前用户是否为管理员
     * @return 更新后的帖子详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO updatePost(Long id, CreatePostRequest request, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 权限校验：作者或管理员
        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权编辑他人帖子");
        }

        // 校验板块存在
        Section section = sectionMapper.selectById(request.getSectionId());
        if (section == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "板块不存在");
        }

        // 更新帖子字段
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        post.setSectionId(request.getSectionId());
        postMapper.updateById(post);

        // 更新标签关联（先删旧再插新）
        savePostTags(id, request.getTagIds());

        // 返回详情（编辑时不计入浏览数）
        return loadPostDetail(id);
    }

    /**
     * 删除帖子（逻辑删除）：
     * 1. 校验帖子存在
     * 2. 校验权限（作者或管理员）
     * 3. 逻辑删除帖子
     *
     * @param id      帖子ID
     * @param userId  当前用户ID
     * @param isAdmin 当前用户是否为管理员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id, Long userId, boolean isAdmin) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 权限校验：作者或管理员
        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除他人帖子");
        }
        // 逻辑删除（MyBatis-Plus @TableLogic 自动处理）
        postMapper.deleteById(id);
    }

    /**
     * 分页查询帖子列表
     *
     * @param page      当前页码
     * @param size      每页条数
     * @param sectionId 板块ID（可选）
     * @param sort      排序方式 latest / hot
     * @return 分页结果
     */
    @Override
    public PageResult<PostListVO> listPosts(long page, long size, Long sectionId, String sort) {
        // 规范化排序参数，默认 latest
        String order = (sort == null || sort.isBlank()) ? "latest" : sort.trim().toLowerCase();
        if (!"hot".equals(order)) {
            order = "latest";
        }

        Page<PostListVO> p = new Page<>(page, size);
        IPage<PostListVO> result = postMapper.selectPostList(p, sectionId, order);

        // 批量填充标签
        fillTags(result.getRecords());
        return PageResult.of(result);
    }

    /**
     * 查询帖子详情，并将浏览数 +1
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    @Override
    public PostDetailVO getPostDetail(Long id) {
        PostDetailVO vo = loadPostDetail(id);
        // 浏览数 +1（原子更新）
        postMapper.incrViewCount(id);
        return vo;
    }

    /**
     * 关键词搜索帖子
     * 1. 将 startTime/endTime 字符串（yyyy-MM-dd）解析为 LocalDateTime（起始为当天 00:00:00，截止为当天 23:59:59）
     * 2. 调用 mapper 执行联表分页查询
     * 3. 批量填充标签后返回
     *
     * @param keyword   关键词
     * @param sectionId 板块ID（可选）
     * @param startTime 起始时间字符串 yyyy-MM-dd（可选）
     * @param endTime   截止时间字符串 yyyy-MM-dd（可选）
     * @param page      当前页码
     * @param size      每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<PostListVO> search(String keyword, Long sectionId, String startTime, String endTime, int page, int size) {
        // 解析起始时间字符串为当天 00:00:00
        LocalDateTime startLdt = parseStartLocalDateTime(startTime);
        // 解析截止时间字符串为当天 23:59:59
        LocalDateTime endLdt = parseEndLocalDateTime(endTime);

        Page<PostListVO> p = new Page<>(page, size);
        IPage<PostListVO> result = postMapper.selectPostsByKeyword(p, keyword, sectionId, startLdt, endLdt);

        // 批量填充标签
        fillTags(result.getRecords());
        return PageResult.of(result);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 加载帖子详情（不增加浏览数），并填充标签
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    private PostDetailVO loadPostDetail(Long id) {
        PostDetailVO vo = postMapper.selectPostDetail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND);
        }
        // 填充标签
        fillTags(Collections.singletonList(vo));
        return vo;
    }

    /**
     * 将 yyyy-MM-dd 字符串解析为当天的起始时间（00:00:00）
     * 用于搜索的起始时间过滤
     *
     * @param dateStr 日期字符串，为空或空白则返回 null
     * @return 当天 00:00:00 对应的 LocalDateTime
     */
    private LocalDateTime parseStartLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, SEARCH_DATE_FORMATTER).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "startTime 格式应为 yyyy-MM-dd");
        }
    }

    /**
     * 将 yyyy-MM-dd 字符串解析为当天的结束时间（23:59:59）
     * 用于搜索的截止时间过滤
     *
     * @param dateStr 日期字符串，为空或空白则返回 null
     * @return 当天 23:59:59 对应的 LocalDateTime
     */
    private LocalDateTime parseEndLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, SEARCH_DATE_FORMATTER).atTime(23, 59, 59);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "endTime 格式应为 yyyy-MM-dd");
        }
    }

    /**
     * 保存帖子标签关联：先删除该 postId 的旧 post_tag 记录，再批量插入新关联
     *
     * @param postId  帖子ID
     * @param tagIds  标签ID列表（为空或 null 则仅清理）
     */
    private void savePostTags(Long postId, List<Long> tagIds) {
        // 删除旧关联（逻辑删除）
        postTagMapper.delete(new LambdaQueryWrapper<PostTag>()
                .eq(PostTag::getPostId, postId));

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        // 去重并批量插入
        List<Long> distinctIds = tagIds.stream().distinct().collect(Collectors.toList());
        // 校验标签存在
        List<Tag> existTags = tagMapper.selectBatchIds(distinctIds);
        if (existTags.size() != distinctIds.size()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "存在不合法的标签ID");
        }
        for (Long tagId : distinctIds) {
            PostTag pt = new PostTag();
            pt.setPostId(postId);
            pt.setTagId(tagId);
            postTagMapper.insert(pt);
        }
    }

    /**
     * 批量为 VO 列表填充标签信息
     *
     * @param records VO 列表
     */
    private void fillTags(List<? extends PostListVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> postIds = records.stream().map(PostListVO::getId).collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return;
        }
        // 一次性查询所有帖子对应的标签（联表 post_tag 与 tag）
        List<PostTag> postTags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>()
                .in(PostTag::getPostId, postIds));
        if (postTags.isEmpty()) {
            records.forEach(vo -> vo.setTags(Collections.emptyList()));
            return;
        }
        List<Long> tagIds = postTags.stream().map(PostTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, Tag> tagMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));

        // 按 postId 分组
        Map<Long, List<TagVO>> postIdToTags = postTags.stream()
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
                        Collectors.mapping(
                                pt -> {
                                    Tag tag = tagMap.get(pt.getTagId());
                                    TagVO vo = new TagVO();
                                    if (tag != null) {
                                        vo.setId(tag.getId());
                                        vo.setName(tag.getName());
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

    /**
     * 发帖奖励积分：user.points += 5（原子更新，不创建 points_record）
     * TODO: Task 14 由 PointsService.addPoints 统一处理，并写入 points_record
     *
     * @param userId 用户ID
     */
    private void addPointsForCreatePost(Long userId) {
        try {
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, userId)
                    .setSql("points = points + " + CREATE_POST_REWARD_POINTS));
        } catch (Exception e) {
            // 积分奖励失败不应影响发帖主流程
            log.warn("发帖奖励积分失败，userId={}", userId, e);
        }
    }

}
