-- ======================================================================
-- 增量脚本：匿名表白墙 + 举报治理
-- 适用：已经用旧版 init.sql 初始化过的 MySQL 数据卷
--
-- 用法示例（Docker）：
--   docker exec -i campus-forum-mysql mysql -uroot -p<密码> campus_forum < docker/alter/alter_confession.sql
-- 本地开发（application-dev.yml 默认 root/root）：
--   mysql -uroot -proot campus_forum < docker/alter/alter_confession.sql
--
-- 若某列/表已存在，对应语句会报 Duplicate column/table，可忽略后继续。
-- ======================================================================

USE campus_forum;
SET NAMES utf8mb4;

ALTER TABLE `section`
    ADD COLUMN `code` VARCHAR(50) DEFAULT NULL COMMENT '板块编码 confession=表白墙' AFTER `name`;

ALTER TABLE `section`
    ADD UNIQUE KEY `uk_code` (`code`);

ALTER TABLE `post`
    ADD COLUMN `is_anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名 0否 1是' AFTER `is_essence`;

CREATE TABLE IF NOT EXISTS `report` (
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

UPDATE `section`
SET `code` = 'confession',
    `description` = '本墙发帖前台匿名、评论实名，违规可举报；管理员后台可追溯真实作者'
WHERE `name` = '表白墙'
  AND (`code` IS NULL OR `code` = '');

UPDATE `post` p
INNER JOIN `section` s ON p.section_id = s.id
SET p.is_anonymous = 1
WHERE s.code = 'confession';
