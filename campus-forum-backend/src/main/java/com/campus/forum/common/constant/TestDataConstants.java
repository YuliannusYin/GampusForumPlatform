package com.campus.forum.common.constant;

/**
 * 测试数据管理常量类
 * 定义测试数据生成/移除过程中使用的前缀、默认值与数据规模
 *
 * @author campus
 */
public final class TestDataConstants {

    private TestDataConstants() {
    }

    /** 测试用户名前缀 */
    public static final String USERNAME_PREFIX = "test_";

    /** 测试社团名称前缀 */
    public static final String CLUB_NAME_PREFIX = "【测试】";

    /** 测试帖子标题前缀 */
    public static final String POST_TITLE_PREFIX = "【测试】";

    /** 测试用户默认密码（明文，入库时 BCrypt 加密） */
    public static final String DEFAULT_PASSWORD = "test123456";

    /** 测试用户邮箱域名 */
    public static final String EMAIL_DOMAIN = "test.campus.edu";

    // ==================== 数据规模常量 ====================

    /** 测试用户数量 */
    public static final int USER_COUNT = 100;

    /** 测试帖子数量 */
    public static final int POST_COUNT = 500;

    /** 测试评论数量 */
    public static final int COMMENT_COUNT = 1000;

    /** 测试社团数量 */
    public static final int CLUB_COUNT = 10;

    /** 测试点赞记录数量 */
    public static final int LIKE_COUNT = 800;

    /** 测试收藏记录数量 */
    public static final int FAVORITE_COUNT = 500;

    /** 测试关注关系数量 */
    public static final int FOLLOW_COUNT = 300;

    /** 测试私信会话数量 */
    public static final int CHAT_SESSION_COUNT = 50;

    /** 测试私信消息数量（目标值，实际按会话随机生成） */
    public static final int CHAT_MESSAGE_COUNT = 200;

    /** 测试通知数量 */
    public static final int NOTIFICATION_COUNT = 150;

    /** 测试社团帖子关联数量 */
    public static final int CLUB_POST_COUNT = 100;

    /** 批量插入每批 flush 条数 */
    public static final int BATCH_FLUSH_SIZE = 200;

}
