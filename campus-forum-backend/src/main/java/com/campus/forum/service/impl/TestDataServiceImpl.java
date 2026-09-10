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
import com.campus.forum.mapper.RoleMapper;
import com.campus.forum.mapper.TestDataMapper;
import com.campus.forum.mapper.UserMapper;
import com.campus.forum.mapper.UserRoleMapper;
import com.campus.forum.mapper.UserSettingMapper;
import com.campus.forum.service.TestDataService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
            "考试经验总结", "实习求职经历", "技术分享讨论", "生活小技巧", "周末活动推荐",
            "想对图书馆那个穿白衬衫的你说", "谢谢那个雨天借伞的同学"
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

    // ==================== 依赖注入 ====================

    private final TestDataMapper testDataMapper;
    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final ClubPostMapper clubPostMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final FavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    private final NotificationMapper notificationMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final SqlSessionFactory sqlSessionFactory;
    private final PasswordEncoder passwordEncoder;

    /** 随机数生成器 */
    private final Random random = new Random();

    // ==================== importTestData ====================

    /**
     * 一键导入全量测试数据
     * 按顺序生成用户→社团→帖子→评论→点赞→收藏→关注→社团帖子→私信→通知
     * 若已存在未删除的测试数据则抛出业务异常；逻辑删除残留会先物理清理
     *
     * @return 导入结果（各类数据实际生成数量）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestDataImportResult importTestData() {
        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionFactory, ExecutorType.BATCH, null);
        try {
            ImportSession session = new ImportSession(sqlSession);
            return doImport(session);
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            throw translateImportConflict(e);
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    /**
     * 在 BATCH SqlSession 中执行导入（该方法的首次 Mapper 访问绑定 BATCH 执行器）
     *
     * @param session 导入会话
     * @return 导入结果
     */
    private TestDataImportResult doImport(ImportSession session) {
        long existCount = session.testDataMapper.countActiveTestUsers(TestDataConstants.USERNAME_PREFIX);
        if (existCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "已存在测试数据，请先移除后再导入");
        }

        // 清掉逻辑删除残留，避免唯一索引冲突
        physicalRemoveTestData(session.testDataMapper);
        session.flush();

        Role roleUser = session.roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, "ROLE_USER"));
        if (roleUser == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "系统未配置 ROLE_USER 角色");
        }

        String encodedPassword = passwordEncoder.encode(TestDataConstants.DEFAULT_PASSWORD);
        TestDataImportResult result = new TestDataImportResult();

        List<Long> testUserIds = generateUsers(session, encodedPassword, roleUser.getId());
        result.setUsers(testUserIds.size());

        ClubGenResult clubResult = generateClubs(session, testUserIds);
        result.setClubs(clubResult.clubIds.size());
        result.setClubMembers(clubResult.memberCount);

        List<Long> testPostIds = generatePosts(session, testUserIds);
        result.setPosts(testPostIds.size());

        List<Long> testCommentIds = generateComments(session, testUserIds, testPostIds);
        result.setComments(testCommentIds.size());

        result.setLikes(generateLikes(session, testUserIds, testPostIds, testCommentIds));
        result.setFavorites(generateFavorites(session, testUserIds, testPostIds));
        result.setFollows(generateFollows(session, testUserIds));
        result.setClubPosts(generateClubPosts(session, clubResult.clubIds, testPostIds));

        int[] chatCounts = generateChats(session, testUserIds);
        result.setChatSessions(chatCounts[0]);
        result.setChatMessages(chatCounts[1]);

        result.setNotifications(generateNotifications(session, testUserIds, testPostIds, testCommentIds));
        session.flush();
        return result;
    }

    // ==================== removeTestData ====================

    /**
     * 一键移除所有测试数据（物理删除，含已逻辑删除残留）
     *
     * @return 移除结果（各类数据实际删除数量）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestDataRemoveResult removeTestData() {
        return physicalRemoveTestData(testDataMapper);
    }

    /**
     * 按依赖顺序物理删除测试数据
     *
     * @param mapper 测试数据 Mapper（可来自 BATCH 会话或 Spring 注入）
     * @return 移除数量统计
     */
    private TestDataRemoveResult physicalRemoveTestData(TestDataMapper mapper) {
        TestDataRemoveResult result = new TestDataRemoveResult();

        List<Long> testUserIds = mapper.selectTestUserIds(TestDataConstants.USERNAME_PREFIX);
        List<Long> testClubIds = mapper.selectTestClubIds(TestDataConstants.CLUB_NAME_PREFIX);
        List<Long> testPostIds = new ArrayList<>();
        if (!testUserIds.isEmpty()) {
            testPostIds = mapper.selectTestPostIds(testUserIds, TestDataConstants.POST_TITLE_PREFIX);
        }

        if (testUserIds.isEmpty() && testClubIds.isEmpty()) {
            return result;
        }

        if (!testClubIds.isEmpty()) {
            result.setClubPosts(mapper.physicalDeleteClubPostsByClubIds(testClubIds));
        }
        if (!testPostIds.isEmpty()) {
            mapper.physicalDeleteClubPostsByPostIds(testPostIds);
        }

        long clubMembers = 0;
        if (!testClubIds.isEmpty()) {
            clubMembers += mapper.physicalDeleteClubMembersByClubIds(testClubIds);
        }
        if (!testUserIds.isEmpty()) {
            clubMembers += mapper.physicalDeleteClubMembersByUserIds(testUserIds);
        }
        result.setClubMembers(clubMembers);

        if (!testClubIds.isEmpty()) {
            result.setClubs(mapper.physicalDeleteClubsByIds(testClubIds));
        }

        if (!testPostIds.isEmpty()) {
            mapper.physicalDeletePostTagsByPostIds(testPostIds);
        }

        if (!testUserIds.isEmpty()) {
            mapper.physicalDeleteReportsByUserIds(testUserIds);
            result.setComments(mapper.physicalDeleteCommentsByUserIds(testUserIds));
            result.setLikes(mapper.physicalDeleteLikesByUserIds(testUserIds));
            result.setFavorites(mapper.physicalDeleteFavoritesByUserIds(testUserIds));
            result.setFollows(mapper.physicalDeleteFollowsByUserIds(testUserIds));
            result.setChatMessages(mapper.physicalDeleteChatMessagesByUserIds(testUserIds));
            result.setChatSessions(mapper.physicalDeleteChatSessionsByUserIds(testUserIds));
            result.setNotifications(mapper.physicalDeleteNotificationsByUserIds(testUserIds));
            result.setPosts(mapper.physicalDeletePostsByUserIds(testUserIds));
            mapper.physicalDeleteUserRolesByUserIds(testUserIds);
            mapper.physicalDeleteUserSettingsByUserIds(testUserIds);
            mapper.physicalDeleteSignInRecordsByUserIds(testUserIds);
            mapper.physicalDeletePointsRecordsByUserIds(testUserIds);
            mapper.physicalDeleteFileRecordsByUserIds(testUserIds);
            result.setUsers(mapper.physicalDeleteUsersByIds(testUserIds));
        }

        return result;
    }

    // ==================== status ====================

    /**
     * 查询当前系统中各类测试数据的数量（仅未删除）
     *
     * @return 测试数据状态
     */
    @Override
    public TestDataStatus status() {
        TestDataStatus status = new TestDataStatus();

        List<Long> testUserIds = testDataMapper.selectActiveTestUserIds(TestDataConstants.USERNAME_PREFIX);
        List<Long> testClubIds = testDataMapper.selectActiveTestClubIds(TestDataConstants.CLUB_NAME_PREFIX);

        status.setUsers(testUserIds.size());
        status.setClubs(testClubIds.size());

        if (!testClubIds.isEmpty()) {
            status.setClubMembers(clubMemberMapper.selectCount(new LambdaQueryWrapper<ClubMember>()
                    .in(ClubMember::getClubId, testClubIds)));
            status.setClubPosts(clubPostMapper.selectCount(new LambdaQueryWrapper<ClubPost>()
                    .in(ClubPost::getClubId, testClubIds)));
        }

        if (testUserIds.isEmpty()) {
            return status;
        }

        status.setPosts(postMapper.selectCount(new LambdaQueryWrapper<Post>()
                .in(Post::getUserId, testUserIds)
                .likeRight(Post::getTitle, TestDataConstants.POST_TITLE_PREFIX)));

        status.setComments(commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .in(Comment::getUserId, testUserIds)));
        status.setLikes(likeRecordMapper.selectCount(new LambdaQueryWrapper<LikeRecord>()
                .in(LikeRecord::getUserId, testUserIds)));
        status.setFavorites(favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .in(Favorite::getUserId, testUserIds)));
        status.setFollows(followMapper.selectCount(new LambdaQueryWrapper<Follow>()
                .and(w -> w.in(Follow::getFollowerId, testUserIds)
                        .or().in(Follow::getFollowingId, testUserIds))));
        status.setChatMessages(chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .and(w -> w.in(ChatMessage::getSenderId, testUserIds)
                        .or().in(ChatMessage::getReceiverId, testUserIds))));
        status.setChatSessions(chatSessionMapper.selectCount(new LambdaQueryWrapper<ChatSession>()
                .and(w -> w.in(ChatSession::getUser1Id, testUserIds)
                        .or().in(ChatSession::getUser2Id, testUserIds))));
        status.setNotifications(notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .and(w -> w.in(Notification::getUserId, testUserIds)
                        .or().in(Notification::getFromUserId, testUserIds))));

        return status;
    }

    // ==================== 私有生成方法 ====================

    /**
     * 生成测试用户（含 user_role 关联与 user_setting 默认记录）
     *
     * @param session         导入会话
     * @param encodedPassword BCrypt 加密后的密码（所有用户复用）
     * @param roleUserId      ROLE_USER 角色 ID
     * @return 生成的用户 ID 列表
     */
    private List<Long> generateUsers(ImportSession session, String encodedPassword, Long roleUserId) {
        List<User> users = new ArrayList<>(TestDataConstants.USER_COUNT);
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
            session.userMapper.insert(user);
            users.add(user);
            session.track();
        }
        session.flush();

        List<Long> testUserIds = new ArrayList<>(users.size());
        for (User user : users) {
            testUserIds.add(user.getId());
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleUserId);
            session.userRoleMapper.insert(userRole);
            session.track();

            UserSetting setting = new UserSetting();
            setting.setUserId(user.getId());
            setting.setNotifyComment(1);
            setting.setNotifyLike(1);
            setting.setNotifyMessage(1);
            session.userSettingMapper.insert(setting);
            session.track();
        }
        session.flush();
        return testUserIds;
    }

    /**
     * 生成测试社团（含社团成员记录）
     *
     * @param session     导入会话
     * @param testUserIds 测试用户 ID 列表
     * @return 社团 ID 与成员总数
     */
    private ClubGenResult generateClubs(ImportSession session, List<Long> testUserIds) {
        List<Club> clubs = new ArrayList<>(TestDataConstants.CLUB_COUNT);
        for (int i = 0; i < TestDataConstants.CLUB_COUNT; i++) {
            Club club = new Club();
            club.setName(TestDataConstants.CLUB_NAME_PREFIX + CLUB_NAME_POOL[i]);
            club.setDescription(CLUB_DESC_POOL[i]);
            club.setCreatorId(testUserIds.get(i));
            club.setStatus(1);
            club.setMemberCount(1);
            club.setPostCount(0);
            session.clubMapper.insert(club);
            clubs.add(club);
            session.track();
        }
        session.flush();

        int totalMembers = 0;
        List<Long> clubIds = new ArrayList<>(clubs.size());
        for (int i = 0; i < clubs.size(); i++) {
            Club club = clubs.get(i);
            Long creatorId = testUserIds.get(i);
            clubIds.add(club.getId());

            ClubMember president = new ClubMember();
            president.setClubId(club.getId());
            president.setUserId(creatorId);
            president.setRole(1);
            president.setJoinedTime(new Date());
            session.clubMemberMapper.insert(president);
            session.track();
            totalMembers++;

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
                session.clubMemberMapper.insert(member);
                session.track();
                totalMembers++;
            }

            session.flush();
            session.clubMapper.update(null, new LambdaUpdateWrapper<Club>()
                    .eq(Club::getId, club.getId())
                    .set(Club::getMemberCount, addedMemberIds.size()));
        }
        session.flush();
        return new ClubGenResult(clubIds, totalMembers);
    }

    /**
     * 生成测试帖子
     *
     * @param session     导入会话
     * @param testUserIds 测试用户 ID 列表
     * @return 生成的帖子 ID 列表
     */
    private List<Long> generatePosts(ImportSession session, List<Long> testUserIds) {
        List<Post> posts = new ArrayList<>(TestDataConstants.POST_COUNT);
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
            post.setIsAnonymous(post.getSectionId() != null && post.getSectionId() == 5L ? 1 : 0);
            post.setStatus(0);
            session.postMapper.insert(post);
            posts.add(post);
            session.track();
        }
        session.flush();

        List<Long> testPostIds = new ArrayList<>(posts.size());
        for (Post post : posts) {
            testPostIds.add(post.getId());
        }
        return testPostIds;
    }

    /**
     * 生成测试评论（约 70% 顶级、30% 二级回复），并回填帖子评论数
     *
     * @param session      导入会话
     * @param testUserIds  测试用户 ID 列表
     * @param testPostIds  测试帖子 ID 列表
     * @return 生成的评论 ID 列表
     */
    private List<Long> generateComments(ImportSession session, List<Long> testUserIds, List<Long> testPostIds) {
        int topCount = (int) Math.round(TestDataConstants.COMMENT_COUNT * 0.7);
        int replyCount = TestDataConstants.COMMENT_COUNT - topCount;
        Map<Long, Integer> postCommentCount = new HashMap<>();

        List<Comment> topComments = new ArrayList<>(topCount);
        for (int i = 0; i < topCount; i++) {
            Long postId = testPostIds.get(random.nextInt(testPostIds.size()));
            Comment comment = newComment(testUserIds, postId, 0L);
            session.commentMapper.insert(comment);
            topComments.add(comment);
            session.track();
            postCommentCount.merge(postId, 1, Integer::sum);
        }
        session.flush();

        List<Long> topLevelCommentIds = new ArrayList<>(topComments.size());
        List<Long> testCommentIds = new ArrayList<>(TestDataConstants.COMMENT_COUNT);
        for (Comment comment : topComments) {
            topLevelCommentIds.add(comment.getId());
            testCommentIds.add(comment.getId());
        }

        List<Comment> replies = new ArrayList<>(replyCount);
        for (int i = 0; i < replyCount; i++) {
            Long postId = testPostIds.get(random.nextInt(testPostIds.size()));
            Long parentId = topLevelCommentIds.get(random.nextInt(topLevelCommentIds.size()));
            Comment comment = newComment(testUserIds, postId, parentId);
            session.commentMapper.insert(comment);
            replies.add(comment);
            session.track();
            postCommentCount.merge(postId, 1, Integer::sum);
        }
        session.flush();
        for (Comment comment : replies) {
            testCommentIds.add(comment.getId());
        }

        for (Map.Entry<Long, Integer> entry : postCommentCount.entrySet()) {
            session.postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, entry.getKey())
                    .set(Post::getCommentCount, entry.getValue()));
        }
        session.flush();
        return testCommentIds;
    }

    /**
     * 构造一条评论实体
     *
     * @param testUserIds 测试用户 ID
     * @param postId      帖子 ID
     * @param parentId    父评论 ID，0 表示顶级
     * @return 评论实体
     */
    private Comment newComment(List<Long> testUserIds, Long postId, Long parentId) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(testUserIds.get(random.nextInt(testUserIds.size())));
        comment.setContent(COMMENT_POOL[random.nextInt(COMMENT_POOL.length)]);
        comment.setLikeCount(0);
        comment.setStatus(0);
        comment.setParentId(parentId);
        return comment;
    }

    /**
     * 生成测试点赞记录（帖子与评论各50%），并回填点赞数
     *
     * @param session        导入会话
     * @param testUserIds    测试用户 ID 列表
     * @param testPostIds    测试帖子 ID 列表
     * @param testCommentIds 测试评论 ID 列表
     * @return 实际生成的点赞记录数
     */
    private int generateLikes(ImportSession session, List<Long> testUserIds, List<Long> testPostIds,
                             List<Long> testCommentIds) {
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
            session.likeRecordMapper.insert(like);
            session.track();
            count++;

            if (targetType == 1) {
                postLikeCount.merge(targetId, 1, Integer::sum);
            } else {
                commentLikeCount.merge(targetId, 1, Integer::sum);
            }
        }
        session.flush();

        for (Map.Entry<Long, Integer> entry : postLikeCount.entrySet()) {
            session.postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, entry.getKey())
                    .set(Post::getLikeCount, entry.getValue()));
        }
        for (Map.Entry<Long, Integer> entry : commentLikeCount.entrySet()) {
            session.commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, entry.getKey())
                    .set(Comment::getLikeCount, entry.getValue()));
        }
        session.flush();
        return count;
    }

    /**
     * 生成测试收藏记录，并回填帖子收藏数
     *
     * @param session     导入会话
     * @param testUserIds 测试用户 ID 列表
     * @param testPostIds 测试帖子 ID 列表
     * @return 实际生成的收藏记录数
     */
    private int generateFavorites(ImportSession session, List<Long> testUserIds, List<Long> testPostIds) {
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
            session.favoriteMapper.insert(favorite);
            session.track();
            count++;
            postFavoriteCount.merge(postId, 1, Integer::sum);
        }
        session.flush();

        for (Map.Entry<Long, Integer> entry : postFavoriteCount.entrySet()) {
            session.postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .eq(Post::getId, entry.getKey())
                    .set(Post::getFavoriteCount, entry.getValue()));
        }
        session.flush();
        return count;
    }

    /**
     * 生成测试关注关系
     *
     * @param session     导入会话
     * @param testUserIds 测试用户 ID 列表
     * @return 实际生成的关注关系数
     */
    private int generateFollows(ImportSession session, List<Long> testUserIds) {
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
            session.followMapper.insert(follow);
            session.track();
            count++;
        }
        session.flush();
        return count;
    }

    /**
     * 生成测试社团帖子关联
     *
     * @param session     导入会话
     * @param testClubIds 测试社团 ID 列表
     * @param testPostIds 测试帖子 ID 列表
     * @return 实际生成的社团帖子关联数
     */
    private int generateClubPosts(ImportSession session, List<Long> testClubIds, List<Long> testPostIds) {
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
            session.clubPostMapper.insert(clubPost);
            session.track();
            count++;
        }
        session.flush();
        return count;
    }

    /**
     * 生成测试私信会话与消息
     *
     * @param session     导入会话
     * @param testUserIds 测试用户 ID 列表
     * @return int数组，[0]=会话数，[1]=消息数
     */
    private int[] generateChats(ImportSession session, List<Long> testUserIds) {
        Set<String> sessionKeys = new HashSet<>();
        List<ChatSession> sessions = new ArrayList<>(TestDataConstants.CHAT_SESSION_COUNT);

        while (sessions.size() < TestDataConstants.CHAT_SESSION_COUNT) {
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

            ChatSession chatSession = new ChatSession();
            chatSession.setUser1Id(minId);
            chatSession.setUser2Id(maxId);
            session.chatSessionMapper.insert(chatSession);
            sessions.add(chatSession);
            session.track();
        }
        session.flush();

        int messageCount = 0;
        for (ChatSession chatSession : sessions) {
            int msgCount = 1 + random.nextInt(10);
            List<ChatMessage> messages = new ArrayList<>(msgCount);
            for (int j = 0; j < msgCount; j++) {
                ChatMessage msg = new ChatMessage();
                msg.setSessionId(chatSession.getId());
                if (random.nextBoolean()) {
                    msg.setSenderId(chatSession.getUser1Id());
                    msg.setReceiverId(chatSession.getUser2Id());
                } else {
                    msg.setSenderId(chatSession.getUser2Id());
                    msg.setReceiverId(chatSession.getUser1Id());
                }
                msg.setContent(CHAT_MESSAGE_POOL[random.nextInt(CHAT_MESSAGE_POOL.length)]);
                msg.setIsRead(random.nextInt(2));
                session.chatMessageMapper.insert(msg);
                messages.add(msg);
                session.track();
                messageCount++;
            }
            session.flush();
            ChatMessage last = messages.get(messages.size() - 1);
            session.chatSessionMapper.update(null, new LambdaUpdateWrapper<ChatSession>()
                    .eq(ChatSession::getId, chatSession.getId())
                    .set(ChatSession::getLastMessageId, last.getId())
                    .set(ChatSession::getLastMessageTime, new Date()));
        }
        session.flush();
        return new int[]{sessions.size(), messageCount};
    }

    /**
     * 生成测试通知
     *
     * @param session        导入会话
     * @param testUserIds    测试用户 ID 列表
     * @param testPostIds    测试帖子 ID 列表
     * @param testCommentIds 测试评论 ID 列表
     * @return 实际生成的通知数
     */
    private int generateNotifications(ImportSession session, List<Long> testUserIds, List<Long> testPostIds,
                                     List<Long> testCommentIds) {
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
                case 1:
                    notif.setContent("评论了你的帖子");
                    notif.setTargetId(testPostIds.get(random.nextInt(testPostIds.size())));
                    notif.setTargetType(1);
                    break;
                case 2:
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
                case 3:
                    notif.setContent("关注了你");
                    notif.setTargetId(fromUserId);
                    notif.setTargetType(3);
                    break;
                case 4:
                    notif.setContent("系统通知：欢迎使用校园论坛");
                    notif.setTargetId(null);
                    notif.setTargetType(null);
                    break;
                default:
                    break;
            }
            session.notificationMapper.insert(notif);
            session.track();
            count++;
        }
        session.flush();
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

    /**
     * 将唯一键冲突转为业务异常，其它运行时异常原样抛出
     *
     * @param e 导入过程中的运行时异常
     * @return 转换后的异常
     */
    private RuntimeException translateImportConflict(RuntimeException e) {
        if (containsDuplicate(e)) {
            return new BusinessException(ResultCode.BUSINESS_ERROR, "测试数据与已有记录冲突，请先移除后再导入");
        }
        return e;
    }

    /**
     * 判断异常链是否为唯一键冲突
     *
     * @param throwable 异常
     * @return 是否唯一键冲突
     */
    private boolean containsDuplicate(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataIntegrityViolationException
                    || current instanceof DuplicateKeyException
                    || current instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.contains("Duplicate entry")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * 社团生成结果
     *
     * @param clubIds     社团 ID 列表
     * @param memberCount 成员总数（含社长）
     */
    private record ClubGenResult(List<Long> clubIds, int memberCount) {
    }

    /**
     * 导入用 BATCH 会话：统一 flush，且不单独 commit
     */
    private static final class ImportSession {
        private final SqlSession sqlSession;
        private final TestDataMapper testDataMapper;
        private final RoleMapper roleMapper;
        private final UserMapper userMapper;
        private final UserRoleMapper userRoleMapper;
        private final UserSettingMapper userSettingMapper;
        private final ClubMapper clubMapper;
        private final ClubMemberMapper clubMemberMapper;
        private final ClubPostMapper clubPostMapper;
        private final PostMapper postMapper;
        private final CommentMapper commentMapper;
        private final LikeRecordMapper likeRecordMapper;
        private final FavoriteMapper favoriteMapper;
        private final FollowMapper followMapper;
        private final ChatSessionMapper chatSessionMapper;
        private final ChatMessageMapper chatMessageMapper;
        private final NotificationMapper notificationMapper;
        private int pending;

        private ImportSession(SqlSession sqlSession) {
            this.sqlSession = sqlSession;
            this.testDataMapper = sqlSession.getMapper(TestDataMapper.class);
            this.roleMapper = sqlSession.getMapper(RoleMapper.class);
            this.userMapper = sqlSession.getMapper(UserMapper.class);
            this.userRoleMapper = sqlSession.getMapper(UserRoleMapper.class);
            this.userSettingMapper = sqlSession.getMapper(UserSettingMapper.class);
            this.clubMapper = sqlSession.getMapper(ClubMapper.class);
            this.clubMemberMapper = sqlSession.getMapper(ClubMemberMapper.class);
            this.clubPostMapper = sqlSession.getMapper(ClubPostMapper.class);
            this.postMapper = sqlSession.getMapper(PostMapper.class);
            this.commentMapper = sqlSession.getMapper(CommentMapper.class);
            this.likeRecordMapper = sqlSession.getMapper(LikeRecordMapper.class);
            this.favoriteMapper = sqlSession.getMapper(FavoriteMapper.class);
            this.followMapper = sqlSession.getMapper(FollowMapper.class);
            this.chatSessionMapper = sqlSession.getMapper(ChatSessionMapper.class);
            this.chatMessageMapper = sqlSession.getMapper(ChatMessageMapper.class);
            this.notificationMapper = sqlSession.getMapper(NotificationMapper.class);
        }

        private void track() {
            pending++;
            if (pending >= TestDataConstants.BATCH_FLUSH_SIZE) {
                flush();
            }
        }

        private void flush() {
            sqlSession.flushStatements();
            pending = 0;
        }
    }

}
