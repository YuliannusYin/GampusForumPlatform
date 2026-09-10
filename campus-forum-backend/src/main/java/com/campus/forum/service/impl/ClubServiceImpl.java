package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.PageResult;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.req.CreateClubRequest;
import com.campus.forum.dto.req.CreatePostRequest;
import com.campus.forum.dto.resp.ClubVO;
import com.campus.forum.dto.resp.PostDetailVO;
import com.campus.forum.dto.resp.PostListVO;
import com.campus.forum.entity.Club;
import com.campus.forum.entity.ClubMember;
import com.campus.forum.entity.ClubPost;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.Section;
import com.campus.forum.entity.User;
import com.campus.forum.mapper.ClubMapper;
import com.campus.forum.mapper.ClubMemberMapper;
import com.campus.forum.mapper.ClubPostMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.SectionMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.service.ClubService;
import com.campus.forum.service.PostService;
import com.campus.forum.security.SecurityUtils;
import com.campus.forum.utils.PostAnonymityHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 社团服务实现类
 * 封装社团创建、审核、加入/退出、成员管理、社团发帖等业务逻辑
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class ClubServiceImpl extends ServiceImpl<ClubMapper, Club> implements ClubService {

    /** 社长角色编码 */
    private static final int ROLE_PRESIDENT = 1;
    /** 普通成员角色编码 */
    private static final int ROLE_MEMBER = 0;
    /** 社团状态：待审核 */
    private static final int STATUS_PENDING = 0;
    /** 社团状态：正常 */
    private static final int STATUS_NORMAL = 1;

    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final ClubPostMapper clubPostMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final SectionMapper sectionMapper;
    private final PostService postService;

    /**
     * 社团分页列表
     * status 为 null 时只查 status=1（正常）的社团，对游客可见
     *
     * @param page    当前页码
     * @param size    每页条数
     * @param keyword 社团名称关键词（可选）
     * @param status  社团状态（可选，为 null 时仅查正常社团）
     * @return 社团分页结果
     */
    @Override
    public PageResult<ClubVO> listClubs(long page, long size, String keyword, Integer status) {
        // status 为 null 时仅查正常社团，否则按指定状态查询
        Integer queryStatus = (status == null) ? STATUS_NORMAL : status;
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<Club>()
                .eq(Club::getStatus, queryStatus)
                .like(keyword != null && !keyword.isBlank(), Club::getName, keyword)
                .orderByDesc(Club::getCreateTime);
        Page<Club> p = new Page<>(page, size);
        IPage<Club> result = clubMapper.selectPage(p, wrapper);

        List<Club> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), result.getTotal(), result.getCurrent(), result.getSize());
        }
        // 批量查询创建者用户名
        List<Long> creatorIds = records.stream()
                .map(Club::getCreatorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = creatorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ClubVO> voList = records.stream()
                .map(club -> toClubVO(club, userMap))
                .collect(Collectors.toList());
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 社团详情
     *
     * @param clubId 社团ID
     * @return 社团视图
     */
    @Override
    public ClubVO getClubDetail(Long clubId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        Map<Long, User> userMap = Collections.emptyMap();
        if (club.getCreatorId() != null) {
            User creator = userMapper.selectById(club.getCreatorId());
            if (creator != null) {
                userMap = Collections.singletonMap(creator.getId(), creator);
            }
        }
        return toClubVO(club, userMap);
    }

    /**
     * 申请创建社团（status=0 待审核）
     * 创建者自动写入 club_member（role=1 社长）
     *
     * @param req    创建社团请求
     * @param userId 当前用户ID
     * @return 创建后的社团视图
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClubVO createClub(CreateClubRequest req, Long userId) {
        Club club = new Club();
        club.setName(req.getName());
        club.setDescription(req.getDescription());
        club.setCreatorId(userId);
        club.setStatus(STATUS_PENDING);
        club.setMemberCount(1);
        club.setPostCount(0);
        clubMapper.insert(club);

        // 创建者自动成为社长
        ClubMember member = new ClubMember();
        member.setClubId(club.getId());
        member.setUserId(userId);
        member.setRole(ROLE_PRESIDENT);
        member.setJoinedTime(new Date());
        clubMemberMapper.insert(member);

        Map<Long, User> userMap = Collections.emptyMap();
        User creator = userMapper.selectById(userId);
        if (creator != null) {
            userMap = Collections.singletonMap(creator.getId(), creator);
        }
        return toClubVO(club, userMap);
    }

    /**
     * 加入社团（幂等，已加入不报错；club.status 必须=1 正常）
     *
     * @param clubId 社团ID
     * @param userId 当前用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinClub(Long clubId, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        if (club.getStatus() != STATUS_NORMAL) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该社团暂不可加入");
        }
        // 幂等：已加入则直接返回
        Long count = clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId));
        if (count != null && count > 0) {
            return;
        }
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(userId);
        member.setRole(ROLE_MEMBER);
        member.setJoinedTime(new Date());
        clubMemberMapper.insert(member);
        // 成员数 +1
        clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                .eq(Club::getId, clubId)
                .setSql("member_count = member_count + 1"));
    }

    /**
     * 退出社团（社长不能退出）
     *
     * @param clubId 社团ID
     * @param userId 当前用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveClub(Long clubId, Long userId) {
        ClubMember member = clubMemberMapper.selectOne(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId));
        if (member == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "您未加入该社团");
        }
        if (member.getRole() != null && member.getRole() == ROLE_PRESIDENT) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "社长不能退出社团，请先转让社长");
        }
        clubMemberMapper.deleteById(member.getId());
        // 成员数 -1（不为负）
        clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                .eq(Club::getId, clubId)
                .ge(Club::getMemberCount, 1)
                .setSql("member_count = member_count - 1"));
    }

    /**
     * 社团帖子列表
     * 简化实现：先查 club_post 全部 postId，再查 Post（status=0 已发布），按 createTime 倒序后内存分页
     *
     * @param clubId 社团ID
     * @param page   当前页码
     * @param size   每页条数
     * @return 帖子分页结果
     */
    @Override
    public PageResult<PostListVO> listClubPosts(Long clubId, long page, long size) {
        // 1. 查询社团关联的全部 postId
        List<ClubPost> clubPosts = clubPostMapper.selectList(new LambdaQueryWrapper<ClubPost>()
                .eq(ClubPost::getClubId, clubId)
                .orderByDesc(ClubPost::getCreateTime));
        if (clubPosts == null || clubPosts.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L, page, size);
        }
        List<Long> postIds = clubPosts.stream()
                .map(ClubPost::getPostId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), 0L, page, size);
        }
        // 2. 查询已发布帖子，按创建时间倒序
        List<Post> posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .in(Post::getId, postIds)
                .eq(Post::getStatus, 0)
                .orderByDesc(Post::getCreateTime));
        // 3. 内存分页
        long total = posts.size();
        long fromIndex = Math.max(0, (page - 1) * size);
        long toIndex = Math.min(total, fromIndex + size);
        List<Post> pageRecords = (fromIndex >= total)
                ? Collections.emptyList()
                : new ArrayList<>(posts.subList((int) fromIndex, (int) toIndex));
        // 4. 转换为 PostListVO
        List<PostListVO> voList = convertToPostListVO(pageRecords);
        return new PageResult<>(voList, total, page, size);
    }

    /**
     * 在社团发帖（需为社团成员）
     * 调用 PostService.createPost 后写入 club_post 关联，并更新 club.postCount+1
     *
     * @param clubId 社团ID
     * @param req    发帖请求
     * @param userId 当前用户ID
     * @return 创建后的帖子详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createClubPost(Long clubId, CreatePostRequest req, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        // 仅社团成员可在社团发帖
        Long count = clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId));
        if (count == null || count == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅社团成员可在社团发帖");
        }
        // 复用帖子服务创建帖子
        PostDetailVO vo = postService.createPost(req, userId);
        // 写入社团帖子关联
        ClubPost clubPost = new ClubPost();
        clubPost.setClubId(clubId);
        clubPost.setPostId(vo.getId());
        clubPostMapper.insert(clubPost);
        // 帖子数 +1
        clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                .eq(Club::getId, clubId)
                .setSql("post_count = post_count + 1"));
        return vo;
    }

    /**
     * 删除社团帖子（社长或管理员可操作）
     * 删除 club_post 关联，club.postCount-1
     *
     * @param clubId  社团ID
     * @param postId  帖子ID
     * @param userId  当前用户ID
     * @param isAdmin 当前用户是否为管理员
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeClubPost(Long clubId, Long postId, Long userId, boolean isAdmin) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        // 校验权限：社长或管理员
        if (!isPresident(clubId, userId) && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅社长或管理员可删除社团帖子");
        }
        // 删除社团帖子关联
        int deleted = clubPostMapper.delete(new LambdaQueryWrapper<ClubPost>()
                .eq(ClubPost::getClubId, clubId)
                .eq(ClubPost::getPostId, postId));
        if (deleted == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团帖子不存在");
        }
        // 帖子数 -1（不为负）
        clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                .eq(Club::getId, clubId)
                .ge(Club::getPostCount, 1)
                .setSql("post_count = post_count - 1"));
    }

    /**
     * 编辑社团信息（仅社长）
     *
     * @param clubId 社团ID
     * @param req    编辑请求
     * @param userId 当前用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClub(Long clubId, CreateClubRequest req, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        if (!isPresident(clubId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅社长可编辑社团信息");
        }
        club.setName(req.getName());
        club.setDescription(req.getDescription());
        clubMapper.updateById(club);
    }

    /**
     * 社长移除成员
     *
     * @param clubId    社团ID
     * @param memberId  成员记录ID
     * @param userId    当前用户ID（操作者）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long clubId, Long memberId, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        if (!isPresident(clubId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅社长可移除成员");
        }
        ClubMember member = clubMemberMapper.selectById(memberId);
        if (member == null || !clubId.equals(member.getClubId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "成员不存在");
        }
        if (userId.equals(member.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能移除自己，请使用退出社团");
        }
        clubMemberMapper.deleteById(memberId);
        // 成员数 -1（不为负）
        clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                .eq(Club::getId, clubId)
                .ge(Club::getMemberCount, 1)
                .setSql("member_count = member_count - 1"));
    }

    /**
     * 成员列表
     *
     * @param clubId 社团ID
     * @return 成员记录列表
     */
    @Override
    public List<ClubMember> listMembers(Long clubId) {
        return clubMemberMapper.selectList(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .orderByDesc(ClubMember::getRole)
                .orderByAsc(ClubMember::getJoinedTime));
    }

    /**
     * 审核通过（status=0→1，已通过的不重复处理）
     *
     * @param clubId 社团ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveClub(Long clubId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        // 已通过或已禁用的不重复处理
        if (club.getStatus() != null && club.getStatus() != STATUS_PENDING) {
            return;
        }
        club.setStatus(STATUS_NORMAL);
        clubMapper.updateById(club);
    }

    /**
     * 审核拒绝（status=0→2 禁用）
     *
     * @param clubId 社团ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectClub(Long clubId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "社团不存在");
        }
        // 非待审核状态不重复处理
        if (club.getStatus() != null && club.getStatus() != STATUS_PENDING) {
            return;
        }
        club.setStatus(2);
        clubMapper.updateById(club);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 判断指定用户是否为某社团的社长
     *
     * @param clubId 社团ID
     * @param userId 用户ID
     * @return true 表示是社长
     */
    private boolean isPresident(Long clubId, Long userId) {
        Long count = clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                .eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId)
                .eq(ClubMember::getRole, ROLE_PRESIDENT));
        return count != null && count > 0;
    }

    /**
     * 将 Club 实体转换为 ClubVO，并填充创建者用户名
     *
     * @param club    社团实体
     * @param userMap 用户ID → 用户实体的映射
     * @return 社团视图
     */
    private ClubVO toClubVO(Club club, Map<Long, User> userMap) {
        ClubVO vo = new ClubVO();
        vo.setId(club.getId());
        vo.setName(club.getName());
        vo.setDescription(club.getDescription());
        vo.setLogo(club.getLogo());
        vo.setCreatorId(club.getCreatorId());
        User creator = (userMap == null || club.getCreatorId() == null) ? null : userMap.get(club.getCreatorId());
        vo.setCreatorName(creator == null ? null : creator.getUsername());
        vo.setStatus(club.getStatus());
        vo.setMemberCount(club.getMemberCount());
        vo.setPostCount(club.getPostCount());
        vo.setCreateTime(club.getCreateTime());
        return vo;
    }

    /**
     * 将 Post 实体列表转换为 PostListVO 列表
     * 批量填充 username/userAvatar（User 表）与 sectionName（Section 表），tags 设为空列表
     *
     * @param posts 帖子实体列表
     * @return 帖子列表视图
     */
    private List<PostListVO> convertToPostListVO(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查询作者信息
        List<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        // 批量查询板块信息
        List<Long> sectionIds = posts.stream()
                .map(Post::getSectionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Section> sectionMap = sectionIds.isEmpty()
                ? Collections.emptyMap()
                : sectionMapper.selectBatchIds(sectionIds).stream()
                .collect(Collectors.toMap(Section::getId, s -> s));

        List<PostListVO> voList = new ArrayList<>(posts.size());
        for (Post post : posts) {
            PostListVO vo = new PostListVO();
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            vo.setSummary(post.getSummary());
            vo.setUserId(post.getUserId());
            User author = post.getUserId() == null ? null : userMap.get(post.getUserId());
            vo.setUsername(author == null ? null : author.getUsername());
            vo.setUserAvatar(author == null ? null : author.getAvatar());
            vo.setSectionId(post.getSectionId());
            Section section = post.getSectionId() == null ? null : sectionMap.get(post.getSectionId());
            vo.setSectionName(section == null ? null : section.getName());
            vo.setTags(Collections.emptyList());
            vo.setViewCount(post.getViewCount());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setFavoriteCount(post.getFavoriteCount());
            vo.setIsTop(post.getIsTop());
            vo.setIsEssence(post.getIsEssence());
            vo.setIsAnonymous(post.getIsAnonymous());
            vo.setCreateTime(post.getCreateTime());
            voList.add(vo);
        }
        PostAnonymityHelper.applyPublicView(voList, SecurityUtils.getCurrentUserIdOrNull());
        return voList;
    }

}
