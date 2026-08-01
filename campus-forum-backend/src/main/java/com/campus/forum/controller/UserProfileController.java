package com.campus.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.Result;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.ClubSimpleVO;
import com.campus.forum.dto.resp.CommentVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.dto.resp.UserProfileVO;
import com.campus.forum.entity.Club;
import com.campus.forum.entity.ClubMember;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Section;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.ClubMapper;
import com.campus.forum.mapper.ClubMemberMapper;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.SectionMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户公开主页接口
 * 提供用户资料、发帖列表、评论历史及加入的社团信息
 * 所有接口对游客可见
 *
 * @author campus
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户主页接口", description = "用户公开主页、发帖/评论/社团列表")
public class UserProfileController {

    /** 帖子状态：已发布 */
    private static final int POST_STATUS_PUBLISHED = 0;

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final ClubMapper clubMapper;
    private final SectionMapper sectionMapper;
    private final FollowService followService;

    /**
     * 获取用户公开主页信息
     * 包含用户资料及关注/粉丝/发帖统计
     *
     * @param userId 用户ID
     * @return 用户公开主页
     */
    @Operation(summary = "用户公开主页", description = "返回用户资料及关注/粉丝/发帖统计，对游客可见")
    @GetMapping("/{userId}")
    public Result<UserProfileVO> getProfile(@Parameter(description = "用户ID", required = true)
                                            @PathVariable("userId") Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setPoints(user.getPoints());
        vo.setLevel(user.getLevel());
        vo.setCreateTime(user.getCreateTime());
        vo.setFollowingCount(followService.countFollowings(userId));
        vo.setFollowerCount(followService.countFollowers(userId));
        // 统计已发布且未删除的发帖数（@TableLogic 自动过滤 deleted=0）
        Long postCount = postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, POST_STATUS_PUBLISHED));
        vo.setPostCount(postCount == null ? 0L : postCount);
        return Result.success(vo);
    }

    /**
     * 分页查询指定用户的发帖列表，按创建时间倒序，对游客可见
     *
     * @param userId 用户ID
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @return 分页结果
     */
    @Operation(summary = "用户发帖列表", description = "分页查询指定用户的已发布帖子，按创建时间倒序，对游客可见")
    @GetMapping("/{userId}/posts")
    public Result<PageResult<PostListVO>> listUserPosts(@Parameter(description = "用户ID", required = true)
                                                        @PathVariable("userId") Long userId,
                                                        @Parameter(description = "当前页码，默认 1")
                                                        @RequestParam(value = "page", defaultValue = "1") long page,
                                                        @Parameter(description = "每页条数，默认 10")
                                                        @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<Post> p = new Page<>(page, size);
        IPage<Post> result = postMapper.selectPage(p, new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .eq(Post::getStatus, POST_STATUS_PUBLISHED)
                .orderByDesc(Post::getCreateTime));
        List<PostListVO> vos = buildPostListVOList(result.getRecords());
        return Result.success(new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 分页查询指定用户的评论历史，按创建时间倒序，对游客可见
     *
     * @param userId 用户ID
     * @param page   当前页码，默认 1
     * @param size   每页条数，默认 10
     * @return 分页结果
     */
    @Operation(summary = "用户评论历史", description = "分页查询指定用户的评论历史，按创建时间倒序，对游客可见")
    @GetMapping("/{userId}/comments")
    public Result<PageResult<CommentVO>> listUserComments(@Parameter(description = "用户ID", required = true)
                                                          @PathVariable("userId") Long userId,
                                                          @Parameter(description = "当前页码，默认 1")
                                                          @RequestParam(value = "page", defaultValue = "1") long page,
                                                          @Parameter(description = "每页条数，默认 10")
                                                          @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<Comment> p = new Page<>(page, size);
        IPage<Comment> result = commentMapper.selectPage(p, new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userId)
                .orderByDesc(Comment::getCreateTime));
        User user = userMapper.selectById(userId);
        List<CommentVO> vos = result.getRecords().stream()
                .map(c -> buildCommentVO(c, user))
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(vos, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 查询指定用户加入的社团列表，对游客可见
     *
     * @param userId 用户ID
     * @return 社团简要信息列表
     */
    @Operation(summary = "用户加入的社团", description = "返回指定用户加入的社团列表，对游客可见")
    @GetMapping("/{userId}/clubs")
    public Result<List<ClubSimpleVO>> listUserClubs(@Parameter(description = "用户ID", required = true)
                                                    @PathVariable("userId") Long userId) {
        List<ClubMember> members = clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getUserId, userId));
        if (members.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> clubIds = members.stream().map(ClubMember::getClubId).distinct().collect(Collectors.toList());
        List<Club> clubs = clubMapper.selectBatchIds(clubIds);
        List<ClubSimpleVO> vos = clubs.stream().map(this::toClubSimpleVO).collect(Collectors.toList());
        return Result.success(vos);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 批量将 Post 实体列表转换为 PostListVO 列表
     * 填充作者用户名/头像与板块名，tags 设为空列表
     *
     * @param posts 帖子实体列表
     * @return 帖子列表视图
     */
    private List<PostListVO> buildPostListVOList(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查询作者
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        // 批量查询板块
        List<Long> sectionIds = posts.stream().map(Post::getSectionId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Section> sectionMap = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : sectionMapper.selectBatchIds(sectionIds).stream()
                .collect(Collectors.toMap(Section::getId, s -> s));

        List<PostListVO> vos = new ArrayList<>(posts.size());
        for (Post post : posts) {
            PostListVO vo = new PostListVO();
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            vo.setSummary(post.getSummary());
            vo.setUserId(post.getUserId());
            User author = userMap.get(post.getUserId());
            if (author != null) {
                vo.setUsername(author.getUsername());
                vo.setUserAvatar(author.getAvatar());
            }
            vo.setSectionId(post.getSectionId());
            Section section = sectionMap.get(post.getSectionId());
            if (section != null) {
                vo.setSectionName(section.getName());
            }
            vo.setTags(Collections.emptyList());
            vo.setViewCount(post.getViewCount());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setFavoriteCount(post.getFavoriteCount());
            vo.setIsTop(post.getIsTop());
            vo.setIsEssence(post.getIsEssence());
            vo.setCreateTime(post.getCreateTime());
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 将 Comment 实体转换为 CommentVO（含评论者信息）
     * replyCount 默认 0
     *
     * @param comment 评论实体
     * @param user    评论者用户实体
     * @return 评论视图
     */
    private CommentVO buildCommentVO(Comment comment, User user) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(0);
        vo.setCreateTime(comment.getCreateTime());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        return vo;
    }

    /**
     * 将 Club 实体转换为 ClubSimpleVO
     *
     * @param club 社团实体
     * @return 社团简要信息
     */
    private ClubSimpleVO toClubSimpleVO(Club club) {
        ClubSimpleVO vo = new ClubSimpleVO();
        vo.setId(club.getId());
        vo.setName(club.getName());
        vo.setLogo(club.getLogo());
        vo.setDescription(club.getDescription());
        vo.setMemberCount(club.getMemberCount());
        return vo;
    }

}
