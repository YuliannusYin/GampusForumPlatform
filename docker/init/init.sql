-- ======================================================================
-- 校园论坛平台数据库初始化脚本
-- 数据库：campus_forum
-- MySQL 8.0 / utf8mb4 / InnoDB
-- 说明：本脚本由 docker-compose 挂载到 MySQL 的
--       docker-entrypoint-initdb.d 目录，首次启动自动执行。
-- ======================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_forum
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_forum;

-- 客户端字符集设定，保证中文正常写入
SET NAMES utf8mb4;
-- 初始化脚本不依赖外键约束，关闭检查以保证清理顺序安全
SET FOREIGN_KEY_CHECKS = 0;

-- ======================================================================
-- 1. 用户表
-- ======================================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
    `email`       VARCHAR(100) NOT NULL COMMENT '邮箱',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender`      TINYINT      NOT NULL DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    `bio`          VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    `points`      INT          NOT NULL DEFAULT 0  COMMENT '积分',
    `level`        INT          NOT NULL DEFAULT 1  COMMENT '等级',
    `status`       TINYINT      NOT NULL DEFAULT 0  COMMENT '状态 0正常 1封禁',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ======================================================================
-- 2. 角色表
-- ======================================================================
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `code`        VARCHAR(50)  NOT NULL COMMENT '角色编码（如 ROLE_USER / ROLE_ADMIN）',
    `name`        VARCHAR(50)  NOT NULL COMMENT '角色名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ======================================================================
-- 3. 用户角色关联表
-- ======================================================================
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ======================================================================
-- 4. 板块表
-- ======================================================================
DROP TABLE IF EXISTS `section`;
CREATE TABLE `section` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '板块ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '板块名',
    `code`        VARCHAR(50)  DEFAULT NULL COMMENT '板块编码 confession=表白墙',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `icon`        VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序（升序）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';

-- ======================================================================
-- 5. 帖子表
-- ======================================================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
    `title`          VARCHAR(200) NOT NULL COMMENT '标题',
    `content`        LONGTEXT     DEFAULT NULL COMMENT 'Markdown正文',
    `summary`        VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `user_id`        BIGINT       NOT NULL COMMENT '作者ID',
    `section_id`     BIGINT       NOT NULL COMMENT '板块ID',
    `view_count`     INT          NOT NULL DEFAULT 0 COMMENT '浏览数',
    `like_count`     INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count`  INT          NOT NULL DEFAULT 0 COMMENT '评论数',
    `favorite_count` INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
    `is_top`         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是',
    `is_essence`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否精华 0否 1是',
    `is_anonymous`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否匿名 0否 1是',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0已发布 1草稿 2已删除',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_section_id` (`section_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- ======================================================================
-- 6. 标签表
-- ======================================================================
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '标签名',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ======================================================================
-- 7. 帖子标签关联表
-- ======================================================================
DROP TABLE IF EXISTS `post_tag`;
CREATE TABLE `post_tag` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
    `tag_id`      BIGINT   NOT NULL COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- ======================================================================
-- 8. 评论表
-- ======================================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
    `user_id`     BIGINT   NOT NULL COMMENT '评论者ID',
    `parent_id`   BIGINT   NOT NULL DEFAULT 0 COMMENT '父评论ID，0表示顶级评论',
    `content`     LONGTEXT DEFAULT NULL COMMENT 'Markdown内容',
    `like_count`  INT      NOT NULL DEFAULT 0 COMMENT '点赞数',
    `status`      TINYINT  NOT NULL DEFAULT 0 COMMENT '状态 0正常 1已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ======================================================================
-- 9. 点赞记录表
-- ======================================================================
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL COMMENT '点赞用户ID',
    `target_id`   BIGINT   NOT NULL COMMENT '目标对象ID',
    `target_type` TINYINT  NOT NULL COMMENT '目标类型 1帖子 2评论',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- ======================================================================
-- 10. 收藏表
-- ======================================================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ======================================================================
-- 11. 通知表
-- ======================================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`      BIGINT       NOT NULL COMMENT '接收者ID',
    `from_user_id` BIGINT       DEFAULT NULL COMMENT '发送者ID（系统通知为NULL）',
    `type`         TINYINT      NOT NULL COMMENT '类型 1评论 2点赞 3私信 4系统',
    `content`      VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    `target_id`    BIGINT       DEFAULT NULL COMMENT '目标对象ID',
    `target_type`  TINYINT      DEFAULT NULL COMMENT '目标类型',
    `is_read`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- ======================================================================
-- 12. 聊天会话表
-- 约定：user1_id < user2_id，保证两个用户间仅存在一个会话
-- ======================================================================
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user1_id`         BIGINT   NOT NULL COMMENT '用户1ID（较小者）',
    `user2_id`         BIGINT   NOT NULL COMMENT '用户2ID（较大者）',
    `last_message_id`  BIGINT   DEFAULT NULL COMMENT '最后消息ID',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_pair` (`user1_id`, `user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ======================================================================
-- 13. 聊天消息表
-- ======================================================================
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id`  BIGINT   NOT NULL COMMENT '会话ID',
    `sender_id`   BIGINT   NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT   NOT NULL COMMENT '接收者ID',
    `content`     LONGTEXT DEFAULT NULL COMMENT '消息内容',
    `is_read`     TINYINT  NOT NULL DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ======================================================================
-- 14. 签到记录表
-- ======================================================================
DROP TABLE IF EXISTS `sign_in_record`;
CREATE TABLE `sign_in_record` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT   NOT NULL COMMENT '用户ID',
    `sign_date`       DATE     NOT NULL COMMENT '签到日期',
    `continuous_days` INT      NOT NULL DEFAULT 1 COMMENT '连续签到天数',
    `points`          INT      NOT NULL DEFAULT 0 COMMENT '本次获得积分',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

-- ======================================================================
-- 15. 积分记录表
-- ======================================================================
DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `change_value` INT         NOT NULL COMMENT '积分变化值（可正可负）',
    `type`        TINYINT      NOT NULL COMMENT '类型 1签到 2发帖 3评论 4点赞被赞 5管理员调整',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- ======================================================================
-- 16. 文件记录表
-- ======================================================================
DROP TABLE IF EXISTS `file_record`;
CREATE TABLE `file_record` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        BIGINT       NOT NULL COMMENT '上传用户ID',
    `original_name`  VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name`    VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `path`           VARCHAR(500) NOT NULL COMMENT '存储路径',
    `url`            VARCHAR(500) DEFAULT NULL COMMENT '访问URL',
    `size`           BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `type`           VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- ======================================================================
-- 17. 关注关系表
-- ======================================================================
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `follower_id`  BIGINT   NOT NULL COMMENT '关注者用户ID',
    `following_id` BIGINT   NOT NULL COMMENT '被关注者用户ID',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- ======================================================================
-- 18. 社团表
-- ======================================================================
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '社团ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '社团名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '社团简介',
    `logo`        VARCHAR(255) DEFAULT NULL COMMENT '社团Logo URL',
    `creator_id`  BIGINT       NOT NULL COMMENT '创建者用户ID',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0待审核 1正常 2禁用',
    `member_count` INT         NOT NULL DEFAULT 0 COMMENT '成员数',
    `post_count`  INT          NOT NULL DEFAULT 0 COMMENT '帖子数',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团表';

-- ======================================================================
-- 19. 社团成员表
-- ======================================================================
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id`     BIGINT   NOT NULL COMMENT '社团ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `role`        TINYINT  NOT NULL DEFAULT 0 COMMENT '角色 0普通成员 1社长',
    `joined_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_user` (`club_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团成员表';

-- ======================================================================
-- 20. 社团帖子关联表
-- ======================================================================
DROP TABLE IF EXISTS `club_post`;
CREATE TABLE `club_post` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `club_id`     BIGINT   NOT NULL COMMENT '社团ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_post` (`club_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团帖子关联表';

-- ======================================================================
-- 21. 用户设置表
-- ======================================================================
DROP TABLE IF EXISTS `user_setting`;
CREATE TABLE `user_setting` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT   NOT NULL COMMENT '用户ID',
    `notify_comment` TINYINT NOT NULL DEFAULT 1 COMMENT '评论通知开关 1开启 0关闭',
    `notify_like`   TINYINT  NOT NULL DEFAULT 1 COMMENT '点赞通知开关 1开启 0关闭',
    `notify_message` TINYINT NOT NULL DEFAULT 1 COMMENT '私信通知开关 1开启 0关闭',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- ======================================================================
-- 22. 举报表
-- ======================================================================
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reporter_id`    BIGINT       NOT NULL COMMENT '举报人ID',
    `target_type`    TINYINT      NOT NULL COMMENT '目标类型 1帖子 2评论',
    `target_id`      BIGINT       NOT NULL COMMENT '目标ID',
    `reason`         TINYINT      NOT NULL COMMENT '原因 1垃圾广告 2辱骂骚扰 3色情低俗 4人身攻击 5其他',
    `description`    VARCHAR(500) DEFAULT NULL COMMENT '补充说明',
    `status`         TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0待处理 1属实已处理 2驳回',
    `handler_id`     BIGINT       DEFAULT NULL COMMENT '处理人ID',
    `handle_remark`  VARCHAR(255) DEFAULT NULL COMMENT '处理备注',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_reporter_id` (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================================
-- 初始数据
-- ======================================================================

-- ---------- 角色 ----------
INSERT INTO `role` (`id`, `code`, `name`, `description`) VALUES
    (1, 'ROLE_USER',  '普通用户', '普通注册用户，拥有基础操作权限'),
    (2, 'ROLE_ADMIN', '管理员',   '系统管理员，拥有后台管理权限'),
    (3, 'ROLE_SUPER_ADMIN', '超级管理员', '网站超级管理员，管理管理员账号与网站级设置');

-- ---------- 管理员账号 ----------
-- 默认密码：admin123（BCrypt 加密）
INSERT INTO `user` (`id`, `username`, `password`, `email`, `nickname`, `gender`, `points`, `level`, `status`) VALUES
    (1, 'admin', '$2a$10$E5oYA2DyrWNWT14lAiembuBOAxhdLEj5AIOPGVQZvsh4/YABrNDa2', 'admin@campus.edu', '管理员', 0, 0, 99, 0);

-- ---------- 超级管理员账号 ----------
-- 默认密码：super123（BCrypt 加密）
INSERT INTO `user` (`id`, `username`, `password`, `email`, `nickname`, `gender`, `points`, `level`, `status`) VALUES
    (2, 'superadmin', '$2a$10$r5PYbKmxz/UyBx46HAgHt.xIiYA3k7j1Uj7.0AKMuWAfiaEJbL7HS', 'superadmin@campus.edu', '超级管理员', 0, 0, 99, 0);

-- ---------- 给 admin 分配 ROLE_ADMIN 角色 ----------
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
    (1, 2);

-- ---------- 给 superadmin 分配 ROLE_SUPER_ADMIN 角色 ----------
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
    (2, 3);

-- ---------- 默认板块 ----------
INSERT INTO `section` (`id`, `name`, `code`, `description`, `icon`, `sort`) VALUES
    (1, '校园生活', NULL,         '分享校园日常、活动资讯、生活经验', NULL, 1),
    (2, '学习交流', NULL,         '课程讨论、学习资料、考试经验分享', NULL, 2),
    (3, '二手交易', NULL,         '书籍、数码、生活用品等闲置物品交易', NULL, 3),
    (4, '失物招领', NULL,         '丢失物品寻回、捡到物品归还信息发布', NULL, 4),
    (5, '表白墙',   'confession', '本墙发帖前台匿名、评论实名，违规可举报；管理员后台可追溯真实作者', NULL, 5);

-- ---------- 初始标签 ----------
INSERT INTO `tag` (`id`, `name`) VALUES
    (1, '求助'),
    (2, '分享'),
    (3, '讨论'),
    (4, '公告'),
    (5, '经验');

-- ======================================================================
-- 初始化完成
-- ======================================================================
