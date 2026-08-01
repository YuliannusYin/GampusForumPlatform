package com.campus.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.forum.common.constant.TestDataConstants;
import com.campus.forum.common.exception.BusinessException;
import com.campus.forum.common.result.ResultCode;
import com.campus.forum.dto.resp.TestDataImportResult;
import com.campus.forum.dto.resp.TestDataRemoveResult;
import com.campus.forum.dto.resp.TestDataStatus;
import com.campus.forum.entity.ChatMessage;
import com.campus.forum.entity.ChatSession;
import com.campus.forum.entity.Club;
import com.campus.forum.entity.ClubMember;
import com.campus.forum.entity.ClubPost;
import com.campus.forum.entity.Comment;
import com.campus.forum.entity.Favorite;
import com.campus.forum.entity.Follow;
import com.campus.forum.entity.LikeRecord;
import com.campus.forum.entity.Notification;
import com.campus.forum.entity.Post;
import com.campus.forum.entity.PostTag;
import com.campus.forum.entity.Role;
import com.campus.forum.entity.User;
import com.campus.forum.entity.UserRole;
import com.campus.forum.entity.UserSetting;
import com.campus.forum.mapper.ChatMessageMapper;
import com.campus.forum.mapper.ChatSessionMapper;
import com.campus.forum.mapper.ClubMapper;
import com.campus.forum.mapper.ClubMemberMapper;
import com.campus.forum.mapper.ClubPostMapper;
import com.campus.forum.mapper.CommentMapper;
import com.campus.forum.mapper.FavoriteMapper;
import com.campus.forum.mapper.FollowMapper;
import com.campus.forum.mapper.LikeRecordMapper;
import com.campus.forum.mapper.NotificationMapper;
import com.campus.forum.mapper.PostMapper;
import com.campus.forum.mapper.PostTagMapper;
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.mapper.UserSettingMapper;
import com.campus.forum.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 测试数据管理服务实现类
 * 实现测试数据的一键导入、移除与状态查询，仅供超级管理员调用
 *
 * @author campus
 */
@Service
@RequiredArgsConstructor
public class TestDataServiceImpl implements TestDataService {

    // ==================== 数据模板池 ====================

    /** 昵称池（10个），循环使用 */
    private static final String[] NICKNAME_POOL = {
            "alice", "bob", "charlie", "david", "emma",
            "frank", "grace", "henry", "ivy", "jack"
    };

    /** 个人简介池（10条） */
    private static final String[] BIO_POOL = {
            "热爱校园生活", "喜欢编程", "摄影爱好者", "读书破万卷", "运动健将",
            "音乐发烧友", "旅行达人", "美食家", "动漫迷", "科技极客"
    };

    /** 社团名称池（10个） */
    private static final String[] CLUB_NAME_POOL = {
            "摄影社", "编程社", "音乐社", "篮球社", "读书社",
            "动漫社", "舞蹈社", "电影社", "志愿者协会", "辩论社"
    };

    /** 社团简介模板池（10条） */
    private static final String[] CLUB_DESC_POOL = {
            "用镜头记录校园美好瞬间", "探索代码的无限可能", "奏响青春的乐章", "在球场上挥洒汗水", "品味书香，分享阅读",
            "二次元爱好者的聚集地", "舞动青春，跳出自我", "光影世界，电影人生", "用爱心温暖校园", "思辨之美，语言之力"
    };

    /** 帖子标题模板池（20条） */
    private static final String[] POST_TITLE_POOL = {
            "校园日常分享", "求助一个学习问题", "推荐一本好书", "今天的心情记录", "期末复习心得",
            "食堂美食测评", "宿舍生活趣事", "社团活动回顾", "编程学习笔记", "电影观后感",
            "运动打卡记录", "旅行见闻分享", "读书笔记整理", "课程选择建议", "校园风景摄影",
            "考试经验总结", "实习求职经历", "技术分享讨论", "生活小技巧", "周末活动推荐"
    };

    /** 帖子正文 Markdown 模板池（5条），随机组合 2~3 条 */
    private static final String[] POST_CONTENT_TEMPLATES = {
            "## 引言\n\n今天想和大家分享一下最近的感悟。\n\n## 主要内容\n\n- 第一点：保持好奇心\n- 第二点：坚持学习\n- 第三点：善于总结\n\n## 代码示例\n\n```java\npublic class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, Campus!\");\n    }\n}\n```\n\n> 学习是一个持续的过程，共勉！\n",
            "## 背景\n\n最近在做一个项目，遇到了一些问题。\n\n## 问题描述\n\n在实现用户认证时，遇到了 token 过期的处理逻辑问题。\n\n## 解决方案\n\n1. 使用刷新 token 机制\n2. 前端拦截 401 状态码\n3. 自动跳转登录页\n\n```javascript\naxios.interceptors.response.use(\n  response => response,\n  error => {\n    if (error.status === 401) {\n      router.push('/login');\n    }\n  }\n);\n```\n\n> 技术问题要善于查阅文档和社区。\n",
            "## 推荐理由\n\n这本书非常值得一读，作者用通俗易懂的语言解释了复杂的概念。\n\n## 核心观点\n\n- 刻意练习的重要性\n- 反馈机制的作用\n- 心理表征的建立\n\n## 读后感\n\n> 阅读是性价比最高的投资。\n\n希望大家都能抽出时间多读书！\n",
            "## 活动回顾\n\n周末参加了社团组织的活动，收获满满。\n\n## 活动内容\n\n1. 破冰游戏，认识新朋友\n2. 主题分享，学习新知识\n3. 小组讨论，碰撞思想火花\n\n## 感悟\n\n- 团队合作很重要\n- 要敢于表达自己的想法\n- 多倾听他人的意见\n\n> 每一次参与都是一次成长。\n",
            "## 今日心情\n\n今天是一个特别的日子，想记录一下。\n\n## 日常清单\n\n- [x] 早起跑步\n- [x] 完成作业\n- [ ] 读半小时书\n- [ ] 给家人打电话\n\n## 感悟\n\n生活就是由这些小事组成的，珍惜每一天。\n\n> 认真生活，温柔以待。\n"
    };

    /** 评论内容模板池（15条） */
    private static final String[] COMMENT_POOL = {
            "支持一下！", "学到了，感谢分享", "同问，有大神解答吗", "写得很好，赞一个", "楼主辛苦了",
            "学到了很多", "感谢楼主的分享", "我也遇到了同样的问题", "很有帮助，收藏了", "期待更多分享",
            "观点很独特，受教了", "哈哈，太有意思了", "这个想法不错", "学习了，谢谢", "顶一下，让更多人看到"
    };

    /** 私信消息模板池（10条） */
    private static final String[] CHAT_MESSAGE_POOL = {
            "你好呀", "在吗？", "看到你发的帖子了", "这个问题我也感兴趣", "有空聊聊吗",
            "谢谢你的回复", "周末一起出去玩吗", "这门课你觉得怎么样", "你在哪个社团呀", "晚安啦"
    };

    // ==================== Mapper 注入 ====================

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final FavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    private final NotificationMapper notificationMapper;
    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final ClubPostMapper clubPostMapper;
    private final PostTagMapper postTagMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserSettingMapper userSettingMapper;
    private final PasswordEncoder passwordEncoder;

    /** 随机数生成器 */
    private final Random random = new Random();

    // ==================== importTestData ====================

    /**
     * 一键导入全量测试数据
     * 按顺序生成用户→社团→帖子→评论→点赞→收藏→关注→社团帖子→私信→通知
     * 若已存在测试数据则抛出业务异常
     *
     * @return 导入结果（各类数据实际生成数量）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestDataImportResult importTestData() {
        // 1. 重复导入检测
        long existCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .likeRight(User::getUsername, TestDataConstants.USERNAME_PREFIX));
        if (existCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "已存在测试数据，请先移除后再导入");
        }

        // 2. 查询 ROLE_USER 角色
        Role roleUser = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_USER"));
        if (roleUser == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "系统未配置 ROLE_USER 角色");
        }

        // 3. 密码只加密一次，所有用户复用
        String encodedPassword = passwordEncoder.encode(TestDataConstants.DEFAULT_PASSWORD);

        TestDataImportResult result = new TestDataImportResult();

        // ==================== 用户生成 ====================
        List<Long> testUserIds = generateUsers(encodedPassword, roleUser.getId());
        result.setUsers(testUserIds.size());

        // ==================== 社团生成 ====================
        int clubMemberCount = generateClubs(testUserIds);
        result.setClubs(TestDataConstants.CLUB_COUNT);
        result.setClubMembers(clubMemberCount);

        // ==================== 帖子生成 ====================
        List<Long> testPostIds = generatePosts(testUserIds);
        result.setPosts(testPostIds.size());

        // ==================== 评论生成 ====================
        List<Long> testCommentIds = generateComments(testUserIds, testPostIds);
        result.setComments(testCommentIds.size());

        // ==================== 点赞生成 ====================
        int likeCount = generateLikes(testUserIds, testPostIds, testCommentIds);
        result.setLikes(likeCount);

        // ==================== 收藏生成 ====================
        int favoriteCount = generateFavorites(testUserIds, testPostIds);
        result.setFavorites(favoriteCount);

        // ==================== 关注生成 ====================
        int followCount = generateFollows(testUserIds);
        result.setFollows(followCount);

        // ==================== 社团帖子关联 ====================
        List<Long> testClubIds = clubMapper.selectList(new LambdaQueryWrapper<Club>()
                .likeRight(Club::getName, TestDataConstants.CLUB_NAME_PREFIX))
                .stream().map(Club::getId).collect(Collectors.toList());
        int clubPostCount = generateClubPosts(testClubIds, testPostIds);
        result.setClubPosts(clubPostCount);

        // ==================== 私信会话与消息 ====================
        int[] chatCounts = generateChats(testUserIds);
        result.setChatSessions(chatCounts[0]);
        result.setChatMessages(chatCounts[1]);

        // ==================== 通知生成 ====================
        int notificationCount = generateNotifications(testUserIds, testPostIds, testCommentIds);
        result.setNotifications(notificationCount);

        return result;
    }

    // ==================== removeTestData ====================

    /**
     * 一键移除所有测试数据
     * 根据 test_ 前缀与【测试】前缀识别并清理全部测试数据
     * 按依赖顺序删除以保持逻辑清晰
     *
     * @return 移除结果（各类数据实际删除数量）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestDataRemoveResult removeTestData() {
        TestDataRemoveResult result = new TestDataRemoveResult();

        // 查询测试用户ID
        List<Long> testUserIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                .select(User::getId)
                .likeRight(User::getUsername, TestDataConstants.USERNAME_PREFIX))
                .stream().map(User::getId).collect(Collectors.toList());

        // 查询测试社团ID
        List<Long> testClubIds = clubMapper.selectList(new LambdaQueryWrapper<Club>()
                .select(Club::getId)
                .likeRight(Club::getName, TestDataConstants.CLUB_NAME_PREFIX))
                .stream().map(Club::getId).collect(Collectors.toList());

        // 查询测试帖子ID（作者为测试用户且标题以【测试】开头）
        List<Long> testPostIds = new ArrayList<>();
        if (!testUserIds.isEmpty()) {
            testPostIds = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .select(Post::getId)
                    .in(Post::getUserId, testUserIds)
                    .likeRight(Post::getTitle, TestDataConstants.POST_TITLE_PREFIX))
                    .stream().map(Post::getId).collect(Collectors.toList());
        }

        // 如果测试用户和社团都为空，直接返回空结果
        if (testUserIds.isEmpty() && testClubIds.isEmpty()) {
            return result;
        }

        // 1. 删除社团帖子关联
        if (!testClubIds.isEmpty()) {
            result.setClubPosts(clubPostMapper.delete(new LambdaQueryWrapper<ClubPost>()
                    .in(ClubPost::getClubId, testClubIds)));
        }

        // 2. 删除社团成员
        if (!testClubIds.isEmpty() && !testUserIds.isEmpty()) {
            result.setClubMembers(clubMemberMapper.delete(new LambdaQueryWrapper<ClubMember>()
                    .and(w -> w.in(ClubMember::getClubId, testClubIds)
                            .or().in(ClubMember::getUserId, testUserIds))));
        } else if (!testClubIds.isEmpty()) {
            result.setClubMembers(clubMemberMapper.delete(new LambdaQueryWrapper<ClubMember>()
                    .in(ClubMember::getClubId, testClubIds)));
        } else if (!testUserIds.isEmpty()) {
            result.setClubMembers(clubMemberMapper.delete(new LambdaQueryWrapper<ClubMember>()
                    .in(ClubMember::getUserId, testUserIds)));
        }

        // 3. 删除社团
        if (!testClubIds.isEmpty()) {
            result.setClubs(clubMapper.delete(new LambdaQueryWrapper<Club>()
                    .in(Club::getId, testClubIds)));
        }

        // 4. 删除帖子标签关联
        if (!testPostIds.isEmpty()) {
            postTagMapper.delete(new LambdaQueryWrapper<PostTag>()
                    .in(PostTag::getPostId, testPostIds));
        }

        // 5. 删除评论
        if (!testUserIds.isEmpty()) {
            result.setComments(commentMapper.delete(new LambdaQueryWrapper<Comment>()
                    .in(Comment::getUserId, testUserIds)));
        }

        // 6. 删除点赞记录
        if (!testUserIds.isEmpty()) {
            result.setLikes(likeRecordMapper.delete(new LambdaQueryWrapper<LikeRecord>()
                    .in(LikeRecord::getUserId, testUserIds)));
        }

        // 7. 删除收藏
        if (!testUserIds.isEmpty()) {
            result.setFavorites(favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                    .in(Favorite::getUserId, testUserIds)));
        }

        // 8. 删除关注关系
        if (!testUserIds.isEmpty()) {
            result.setFollows(followMapper.delete(new LambdaQueryWrapper<Follow>()
                    .and(w -> w.in(Follow::getFollowerId, testUserIds)
                            .or().in(Follow::getFollowingId, testUserIds))));
        }

        // 9. 删除私信消息
        if (!testUserIds.isEmpty()) {
            result.setChatMessages(chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                    .and(w -> w.in(ChatMessage::getSenderId, testUserIds)
                            .or().in(ChatMessage::getReceiverId, testUserIds))));
        }

        // 10. 删除私信会话
        if (!testUserIds.isEmpty()) {
            result.setChatSessions(chatSessionMapper.delete(new LambdaQueryWrapper<ChatSession>()
                    .and(w -> w.in(ChatSession::getUser1Id, testUserIds)
                            .or().in(ChatSession::getUser2Id, testUserIds))));
        }

        // 11. 删除通知
        if (!testUserIds.isEmpty()) {
            result.setNotifications(notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                    .and(w -> w.in(Notification::getUserId, testUserIds)
                            .or().in(Notification::getFromUserId, testUserIds))));
        }

        // 12. 删除帖子
        if (!testUserIds.isEmpty()) {
            result.setPosts(postMapper.delete(new LambdaQueryWrapper<Post>()
                    .in(Post::getUserId, testUserIds)));
        }

        // 13. 删除用户角色关联
        if (!testUserIds.isEmpty()) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                    .in(UserRole::getUserId, testUserIds));
        }

        // 14. 删除用户设置
        if (!testUserIds.isEmpty()) {
            userSettingMapper.delete(new LambdaQueryWrapper<UserSetting>()
                    .in(UserSetting::getUserId, testUserIds));
        }

        // 15. 删除用户
        if (!testUserIds.isEmpty()) {
            result.setUsers(userMapper.delete(new LambdaQueryWrapper<User>()
                    .in(User::getId, testUserIds)));
        }

        return result;
    }

    // ==================== status ====================

    /**
     * 查询当前系统中各类测试数据的数量
     *
     * @return 测试数据状态
     */
    @Override
    public TestDataStatus status() {
        TestDataStatus status = new TestDataStatus();

        // 查询测试用户ID
        List<Long> testUserIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                .select(User::getId)
                .likeRight(User::getUsername, TestDataConstants.USERNAME_PREFIX))
                .stream().map(User::getId).collect(Collectors.toList());

        // 查询测试社团ID
        List<Long> testClubIds = clubMapper.selectList(new LambdaQueryWrapper<Club>()
                .select(Club::getId)
                .likeRight(Club::getName, TestDataConstants.CLUB_NAME_PREFIX))
                .stream().map(Club::getId).collect(Collectors.toList());

        // 用户数
        status.setUsers(testUserIds.size());

        // 社团数
        status.setClubs(testClubIds.size());

        // 社团成员数
        if (!testClubIds.isEmpty()) {
            status.setClubMembers(clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                    .in(ClubMember::getClubId, testClubIds)));
        }

        // 社团帖子关联数
        if (!testClubIds.isEmpty()) {
            status.setClubPosts(clubPostMapper.selectCount(new LambdaQueryWrapper<ClubPost>()
                    .in(ClubPost::getClubId, testClubIds)));
        }

        if (testUserIds.isEmpty()) {
            return status;
        }

        // 帖子数
        status.setPosts(postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .in(Post::getUserId, testUserIds)
                .likeRight(Post::getTitle, TestDataConstants.POST_TITLE_PREFIX)));

        // 评论数
        status.setComments(commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .in(Comment::getUserId, testUserIds)));

        // 点赞数
        status.setLikes(likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .in(LikeRecord::getUserId, testUserIds)));

        // 收藏数
        status.setFavorites(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .in(Favorite::getUserId, testUserIds)));

        // 关注数
        status.setFollows(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .and(w -> w.in(Follow::getFollowerId, testUserIds)
                        .or().in(Follow::getFollowingId, testUserIds))));

        // 私信消息数
        status.setChatMessages(chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .and(w -> w.in(ChatMessage::getSenderId, testUserIds)
                        .or().in(ChatMessage::getReceiverId, testUserIds))));

        // 私信会话数
        status.setChatSessions(chatSessionMapper.selectCount(new LambdaQueryWrapper<ChatSession>()
                .and(w -> w.in(ChatSession::getUser1Id, testUserIds)
                        .or().in(ChatSession::getUser2Id, testUserIds))));

        // 通知数
        status.setNotifications(notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .and(w -> w.in(Notification::getUserId, testUserIds)
                        .or().in(Notification::getFromUserId, testUserIds))));

        return status;
    }

    // ==================== 私有生成方法 ====================

    /**
     * 生成测试用户（含 user_role 关联与 user_setting 默认记录）
     *
     * @param encodedPassword BCrypt 加密后的密码（所有用户复用）
     * @param roleUserId      ROLE_USER 角色 ID
     * @return 生成的用户 ID 列表
     */
    private List<Long> generateUsers(String encodedPassword, Long roleUserId) {
        List<Long> testUserIds = new ArrayList<>(TestDataConstants.USER_COUNT);
        for (int i = 1; i <= TestDataConstants.USER_COUNT; i++) {
            String nickname = NICKNAME_POOL[(i - 1) % NICKNAME_POOL.length];
            String username = TestDataConstants.USERNAME_PREFIX + nickname + "_" + String.format("%03d", i);

            User user = new User();
            user.setUsername(username);
            user.setEmail("test+" + username + "@" + TestDataConstants.EMAIL_DOMAIN);
            user.setPassword(encodedPassword);
            user.setNickname(nickname);
            user.setGender(random.nextInt(3));
            user.setAvatar("https://picsum.photos/seed/test" + i + "/100/100");
            user.setBio(BIO_POOL[random.nextInt(BIO_POOL.length)]);
            user.setPoints(random.nextInt(501));
            user.setLevel(1 + random.nextInt(10));
            user.setStatus(0);
            userMapper.insert(user);
            testUserIds.add(user.getId());

            // 插入 user_role 关联
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleUserId);
            userRoleMapper.insert(userRole);

            // 插入 user_setting 默认记录
            UserSetting setting = new UserSetting();
            setting.setUserId(user.getId());
            setting.setNotifyComment(1);
            setting.setNotifyLike(1);
            setting.setNotifyMessage(1);
            userSettingMapper.insert(setting);
        }
        return testUserIds;
    }

    /**
     * 生成测试社团（含社团成员记录）
     *
     * @param testUserIds 测试用户 ID 列表
     * @return 实际生成的社团成员总数（含社长）
     */
    private int generateClubs(List<Long> testUserIds) {
        int totalMembers = 0;
        for (int i = 0; i < TestDataConstants.CLUB_COUNT; i++) {
            Long creatorId = testUserIds.get(i);

            Club club = new Club();
            club.setName(TestDataConstants.CLUB_NAME_PREFIX + CLUB_NAME_POOL[i]);
            club.setDescription(CLUB_DESC_POOL[i]);
            club.setCreatorId(creatorId);
            club.setStatus(1);
            club.setMemberCount(1);
            club.setPostCount(0);
            clubMapper.insert(club);

            // 社长自动加入
            ClubMember president = new ClubMember();
            president.setClubId(club.getId());
            president.setUserId(creatorId);
            president.setRole(1);
            president.setJoinedTime(new Date());
            clubMemberMapper.insert(president);
            totalMembers++;

            // 随机添加 5~15 个普通成员（排除社长）
            int memberCount = 5 + random.nextInt(11);
            Set<Long> addedMemberIds = new HashSet<>();
            addedMemberIds.add(creatorId);
            for (int j = 0; j < memberCount; j++) {
                Long memberId = testUserIds.get(random.nextInt(testUserIds.size()));
                if (addedMemberIds.contains(memberId)) {
                    continue;
                }
                addedMemberIds.add(memberId);
                ClubMember member = new ClubMember();
                member.setClubId(club.getId());
                member.setUserId(memberId);
                member.setRole(0);
                member.setJoinedTime(new Date());
                clubMemberMapper.insert(member);
                totalMembers++;
            }

            // 更新社团成员数
            clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                    .eq(Club::getId, club.getId())
                    .set(Club::getMemberCount, addedMemberIds.size()));
        }
        return totalMembers;
    }

    /**
     * 生成测试帖子
     *
     * @param testUserIds 测试用户 ID 列表
     * @return 生成的帖子 ID 列表
     */
    private List<Long> generatePosts(List<Long> testUserIds) {
        List<Long> testPostIds = new ArrayList<>(TestDataConstants.POST_COUNT);
        for (int i = 0; i < TestDataConstants.POST_COUNT; i++) {
            String title = TestDataConstants.POST_TITLE_PREFIX
                    + POST_TITLE_POOL[random.nextInt(POST_TITLE_POOL.length)];
            String content = generatePostContent();
            String summary = generateSummary(content);

            Post post = new Post();
            post.setTitle(title);
            post.setContent(content);
            post.setSummary(summary);
            post.setUserId(testUserIds.get(random.nextInt(testUserIds.size())));
            post.setSectionId(1L + random.nextInt(5));
            post.setViewCount(random.nextInt(501));
            post.setLikeCount(0);
            post.setCommentCount(0);
            post.setFavoriteCount(0);
            post.setIsTop(random.nextDouble() < 0.05 ? 1 : 0);
            post.setIsEssence(random.nextDouble() < 0.10 ? 1 : 0);
            post.setStatus(0);
            postMapper.insert(post);
            testPostIds.add(post.getId());
        }
        return testPostIds;
    }

    /**
     * 生成测试评论（含二级回复），并回填帖子评论数
     *
     * @param testUserIds  测试用户 ID 列表
     * @param testPostIds  测试帖子 ID 列表
     * @return 生成的评论 ID 列表
     */
    private List<Long> generateComments(List<Long> testUserIds, List<Long> testPostIds) {
        List<Long> testCommentIds = new ArrayList<>(TestDataConstants.COMMENT_COUNT);
        List<Long> topLevelCommentIds = new ArrayList<>();
        Map<Long, Integer> postCommentCount = new HashMap<>();

        for (int i = 0; i < TestDataConstants.COMMENT_COUNT; i++) {
            Long postId = testPostIds.get(random.nextInt(testPostIds.size()));
            Comment comment = new Comment();
            comment.setPostId(postId);
            comment.setUserId(testUserIds.get(random.nextInt(testUserIds.size())));
            comment.setContent(COMMENT_POOL[random.nextInt(COMMENT_POOL.length)]);
            comment.setLikeCount(0);
            comment.setStatus(0);

            // 70% 顶级评论，30% 二级回复（若无顶级评论则强制顶级）
            if (random.nextDouble() < 0.7 || topLevelCommentIds.isEmpty()) {
                comment.setParentId(0L);
                commentMapper.insert(comment);
                topLevelCommentIds.add(comment.getId());
            } else {
                comment.setParentId(topLevelCommentIds.get(random.nextInt(topLevelCommentIds.size())));
                commentMapper.insert(comment);
            }
            testCommentIds.add(comment.getId());
            postCommentCount.merge(postId, 1, Integer::sum);
        }

        // 回填帖子评论数
        for (Map.Entry<Long, Integer> entry : postCommentCount.entrySet()) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, entry.getKey())
                    .set(Post::getCommentCount, entry.getValue()));
        }
        return testCommentIds;
    }

    /**
     * 生成测试点赞记录（帖子与评论各50%），并回填点赞数
     *
     * @param testUserIds    测试用户 ID 列表
     * @param testPostIds    测试帖子 ID 列表
     * @param testCommentIds 测试评论 ID 列表
     * @return 实际生成的点赞记录数
     */
    private int generateLikes(List<Long> testUserIds, List<Long> testPostIds, List<Long> testCommentIds) {
        Set<String> dedup = new HashSet<>();
        Map<Long, Integer> postLikeCount = new HashMap<>();
        Map<Long, Integer> commentLikeCount = new HashMap<>();
        int count = 0;

        while (count < TestDataConstants.LIKE_COUNT) {
            Long userId = testUserIds.get(random.nextInt(testUserIds.size()));
            int targetType = random.nextDouble() < 0.5 ? 1 : 2;
            Long targetId = (targetType == 1)
                    ? testPostIds.get(random.nextInt(testPostIds.size()))
                    : testCommentIds.get(random.nextInt(testCommentIds.size()));
            String key = userId + ":" + targetId + ":" + targetType;
            if (dedup.contains(key)) {
                continue;
            }
            dedup.add(key);

            LikeRecord like = new LikeRecord();
            like.setUserId(userId);
            like.setTargetId(targetId);
            like.setTargetType(targetType);
            likeRecordMapper.insert(like);
            count++;

            if (targetType == 1) {
                postLikeCount.merge(targetId, 1, Integer::sum);
            } else {
                commentLikeCount.merge(targetId, 1, Integer::sum);
            }
        }

        // 回填帖子点赞数
        for (Map.Entry<Long, Integer> entry : postLikeCount.entrySet()) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, entry.getKey())
                    .set(Post::getLikeCount, entry.getValue()));
        }
        // 回填评论点赞数
        for (Map.Entry<Long, Integer> entry : commentLikeCount.entrySet()) {
            commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, entry.getKey())
                    .set(Comment::getLikeCount, entry.getValue()));
        }
        return count;
    }

    /**
     * 生成测试收藏记录，并回填帖子收藏数
     *
     * @param testUserIds 测试用户 ID 列表
     * @param testPostIds 测试帖子 ID 列表
     * @return 实际生成的收藏记录数
     */
    private int generateFavorites(List<Long> testUserIds, List<Long> testPostIds) {
        Set<String> dedup = new HashSet<>();
        Map<Long, Integer> postFavoriteCount = new HashMap<>();
        int count = 0;

        while (count < TestDataConstants.FAVORITE_COUNT) {
            Long userId = testUserIds.get(random.nextInt(testUserIds.size()));
            Long postId = testPostIds.get(random.nextInt(testPostIds.size()));
            String key = userId + ":" + postId;
            if (dedup.contains(key)) {
                continue;
            }
            dedup.add(key);

            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setPostId(postId);
            favoriteMapper.insert(favorite);
            count++;

            postFavoriteCount.merge(postId, 1, Integer::sum);
        }

        // 回填帖子收藏数
        for (Map.Entry<Long, Integer> entry : postFavoriteCount.entrySet()) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, entry.getKey())
                    .set(Post::getFavoriteCount, entry.getValue()));
        }
        return count;
    }

    /**
     * 生成测试关注关系
     *
     * @param testUserIds 测试用户 ID 列表
     * @return 实际生成的关注关系数
     */
    private int generateFollows(List<Long> testUserIds) {
        Set<String> dedup = new HashSet<>();
        int count = 0;

        while (count < TestDataConstants.FOLLOW_COUNT) {
            Long followerId = testUserIds.get(random.nextInt(testUserIds.size()));
            Long followingId = testUserIds.get(random.nextInt(testUserIds.size()));
            if (followerId.equals(followingId)) {
                continue;
            }
            String key = followerId + ":" + followingId;
            if (dedup.contains(key)) {
                continue;
            }
            dedup.add(key);

            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            followMapper.insert(follow);
            count++;
        }
        return count;
    }

    /**
     * 生成测试社团帖子关联
     *
     * @param testClubIds 测试社团 ID 列表
     * @param testPostIds 测试帖子 ID 列表
     * @return 实际生成的社团帖子关联数
     */
    private int generateClubPosts(List<Long> testClubIds, List<Long> testPostIds) {
        Set<String> dedup = new HashSet<>();
        int count = 0;

        while (count < TestDataConstants.CLUB_POST_COUNT) {
            Long clubId = testClubIds.get(random.nextInt(testClubIds.size()));
            Long postId = testPostIds.get(random.nextInt(testPostIds.size()));
            String key = clubId + ":" + postId;
            if (dedup.contains(key)) {
                continue;
            }
            dedup.add(key);

            ClubPost clubPost = new ClubPost();
            clubPost.setClubId(clubId);
            clubPost.setPostId(postId);
            clubPostMapper.insert(clubPost);
            count++;
        }
        return count;
    }

    /**
     * 生成测试私信会话与消息
     *
     * @param testUserIds 测试用户 ID 列表
     * @return int数组，[0]=会话数，[1]=消息数
     */
    private int[] generateChats(List<Long> testUserIds) {
        Set<String> sessionKeys = new HashSet<>();
        int sessionCount = 0;
        int messageCount = 0;

        while (sessionCount < TestDataConstants.CHAT_SESSION_COUNT) {
            Long u1 = testUserIds.get(random.nextInt(testUserIds.size()));
            Long u2 = testUserIds.get(random.nextInt(testUserIds.size()));
            if (u1.equals(u2)) {
                continue;
            }
            Long minId = Math.min(u1, u2);
            Long maxId = Math.max(u1, u2);
            String key = minId + ":" + maxId;
            if (sessionKeys.contains(key)) {
                continue;
            }
            sessionKeys.add(key);

            ChatSession session = new ChatSession();
            session.setUser1Id(minId);
            session.setUser2Id(maxId);
            chatSessionMapper.insert(session);
            sessionCount++;

            // 每个会话随机 1~10 条消息
            int msgCount = 1 + random.nextInt(10);
            Long lastMessageId = null;
            for (int j = 0; j < msgCount; j++) {
                ChatMessage msg = new ChatMessage();
                msg.setSessionId(session.getId());
                if (random.nextBoolean()) {
                    msg.setSenderId(minId);
                    msg.setReceiverId(maxId);
                } else {
                    msg.setSenderId(maxId);
                    msg.setReceiverId(minId);
                }
                msg.setContent(CHAT_MESSAGE_POOL[random.nextInt(CHAT_MESSAGE_POOL.length)]);
                msg.setIsRead(random.nextInt(2));
                chatMessageMapper.insert(msg);
                lastMessageId = msg.getId();
                messageCount++;
            }

            // 更新会话的最后消息ID与时间
            if (lastMessageId != null) {
                chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                        .eq(ChatSession::getId, session.getId())
                        .set(ChatSession::getLastMessageId, lastMessageId)
                        .set(ChatSession::getLastMessageTime, new Date()));
            }
        }
        return new int[]{sessionCount, messageCount};
    }

    /**
     * 生成测试通知
     *
     * @param testUserIds    测试用户 ID 列表
     * @param testPostIds    测试帖子 ID 列表
     * @param testCommentIds 测试评论 ID 列表
     * @return 实际生成的通知数
     */
    private int generateNotifications(List<Long> testUserIds, List<Long> testPostIds, List<Long> testCommentIds) {
        int count = 0;
        for (int i = 0; i < TestDataConstants.NOTIFICATION_COUNT; i++) {
            int type = 1 + random.nextInt(4);
            Long userId = testUserIds.get(random.nextInt(testUserIds.size()));
            Long fromUserId = testUserIds.get(random.nextInt(testUserIds.size()));
            while (fromUserId.equals(userId)) {
                fromUserId = testUserIds.get(random.nextInt(testUserIds.size()));
            }

            Notification notif = new Notification();
            notif.setUserId(userId);
            notif.setFromUserId(fromUserId);
            notif.setType(type);
            notif.setIsRead(random.nextInt(2));

            switch (type) {
                case 1: // 评论通知
                    notif.setContent("评论了你的帖子");
                    notif.setTargetId(testPostIds.get(random.nextInt(testPostIds.size())));
                    notif.setTargetType(1);
                    break;
                case 2: // 点赞通知
                    if (random.nextBoolean()) {
                        notif.setContent("赞了你的帖子");
                        notif.setTargetId(testPostIds.get(random.nextInt(testPostIds.size())));
                        notif.setTargetType(1);
                    } else {
                        notif.setContent("赞了你的评论");
                        notif.setTargetId(testCommentIds.get(random.nextInt(testCommentIds.size())));
                        notif.setTargetType(2);
                    }
                    break;
                case 3: // 关注通知
                    notif.setContent("关注了你");
                    notif.setTargetId(fromUserId);
                    notif.setTargetType(3);
                    break;
                case 4: // 系统通知
                    notif.setContent("系统通知：欢迎使用校园论坛");
                    notif.setTargetId(null);
                    notif.setTargetType(null);
                    break;
            }
            notificationMapper.insert(notif);
            count++;
        }
        return count;
    }

    // ==================== 辅助方法 ====================

    /**
     * 随机组合 2~3 个 Markdown 模板生成帖子正文
     *
     * @return Markdown 格式的帖子正文
     */
    private String generatePostContent() {
        int count = 2 + random.nextInt(2);
        StringBuilder sb = new StringBuilder();
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < count; i++) {
            int idx;
            do {
                idx = random.nextInt(POST_CONTENT_TEMPLATES.length);
            } while (used.contains(idx));
            used.add(idx);
            sb.append(POST_CONTENT_TEMPLATES[idx]);
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    /**
     * 从 Markdown 正文中提取摘要（去除 Markdown 符号后截取前100字符）
     *
     * @param content Markdown 正文
     * @return 纯文本摘要
     */
    private String generateSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String plain = content.replaceAll("[#*`>\\[\\]()!|\\-]", "")
                .replaceAll("\\s+", " ")
                .trim();
        return plain.length() > 100 ? plain.substring(0, 100) : plain;
    }

}
