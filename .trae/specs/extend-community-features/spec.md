# 社区功能扩展 Spec

## Why

当前论坛已具备帖子、评论、点赞收藏、私信、签到等基础能力，但缺少微博/贴吧/Reddit 类产品的社区化特性：游客浏览受限（看帖看不到评论）、用户主页不可被他人查看、无用户设置入口、无"社团"形态的子社区组织、管理员角色扁平无法区分网站管理者与内容管理者。本扩展旨在补齐这些社区化能力，使平台更接近成熟社区产品形态。

## What Changes

- **游客浏览最大化开放**：评论列表、用户主页、社团列表与社团帖子对游客可见，仅写操作（发帖/评论/点赞/关注/加入社团等）需登录
- **新增超级管理员角色**：引入 `ROLE_SUPER_ADMIN`，新建独立 `superadmin` 账号；现有 `admin` 保持为普通管理员。超管可管理普通管理员账号与网站级设置，普通管理员管理论坛内容与普通用户
- **新增社团功能**：社团独立于现有板块体系（新表 `club` / `club_member` / `club_post` / `club_application`），每个社团由社团管理员（社长）管理；帖子可选择发到社团
- **优化用户主页**：新增公开用户主页 `/api/users/{userId}`，对外展示发帖、评论历史、加入的社团、关注/粉丝数
- **新增关注/粉丝关系**：新表 `follow`，用户可关注/取关他人，主页展示关注数与粉丝数
- **新增用户设置菜单**：前端独立设置页，含基本资料、头像、密码修改、通知偏好

## Impact

- Affected specs: init-campus-forum-platform（用户与认证、帖子、权限控制模块）
- Affected code:
  - 后端：`SecurityConfig`（白名单）、`UserController`/新增 `UserProfileController`、新增 `ClubController`/`ClubService`、新增 `FollowController`/`FollowService`、`AdminUserController`（超管管理管理员）、`init.sql`（新表+新角色+superadmin 账号）、各 `@PreAuthorize` 权限表达式
  - 前端：`router`（新增路由）、`views/user`（用户主页、设置页）、`views/club`（社团页）、`layout`（设置菜单入口）、`api/`（新接口封装）、`store/user`（角色判断）

## ADDED Requirements

### Requirement: 游客浏览最大化开放
系统 SHALL 允许未登录用户访问以下只读接口：评论列表、评论回复列表、用户公开主页、用户主页发帖/评论列表、社团列表、社团详情、社团帖子列表。写操作（发帖、评论、点赞、收藏、关注、加入社团、发私信、签到）SHALL 要求登录。

#### Scenario: 游客查看帖子评论
- **WHEN** 未登录用户打开帖子详情页
- **THEN** 帖子正文与评论列表均正常加载，不返回 401

#### Scenario: 游客查看用户主页
- **WHEN** 未登录用户访问 `/user/123`
- **THEN** 展示该用户的发帖、评论历史、社团、关注/粉丝数，不返回 401

#### Scenario: 游客尝试写操作
- **WHEN** 未登录用户点击关注/发帖/评论
- **THEN** 跳转登录页或返回 401

### Requirement: 超级管理员角色分层
系统 SHALL 引入 `ROLE_SUPER_ADMIN` 角色，与 `ROLE_ADMIN`（普通管理员）形成两级管理权限。超级管理员 SHALL 能管理普通管理员账号（创建、禁用、删除、重置密码）与网站级设置；普通管理员 SHALL 管理论坛内容（帖子/评论/板块/标签）与普通用户。

#### Scenario: 超管管理普通管理员
- **WHEN** 超级管理员访问管理员管理接口
- **THEN** 可创建/禁用/删除普通管理员账号

#### Scenario: 普通管理员不可访问超管接口
- **WHEN** 普通管理员访问管理员管理接口
- **THEN** 返回 403

#### Scenario: 超管访问内容管理
- **WHEN** 超级管理员访问帖子/用户管理接口
- **THEN** 正常访问（超管包含普通管理员全部权限）

### Requirement: 社团功能
系统 SHALL 提供独立于板块的社团体系。每个社团对应一个大学社团，由社团管理员（社长）管理。登录用户可申请创建社团（需超管或管理员审核），创建者成为该社团社长。用户可加入/退出社团。帖子可选择发布到社团。

#### Scenario: 用户申请创建社团
- **WHEN** 登录用户提交社团创建申请
- **THEN** 创建状态为"待审核"的社团记录，等待管理员审核

#### Scenario: 管理员审核社团
- **WHEN** 管理员审核通过社团申请
- **THEN** 社团状态变为"正常"，申请人成为该社团社长（club_member 记录 role=社长）

#### Scenario: 用户加入社团
- **WHEN** 登录用户点击加入社团
- **THEN** 成为该社团普通成员，可在该社团下发帖

#### Scenario: 社长发帖到社团
- **WHEN** 社长或社团成员在社团页发帖
- **THEN** 帖子关联到该社团，出现在社团帖子列表

#### Scenario: 社长管理社团
- **WHEN** 社长访问社团管理接口
- **THEN** 可编辑社团信息、移除成员、删除社团内帖子

### Requirement: 用户公开主页
系统 SHALL 提供公开用户主页，展示该用户的发帖列表、评论历史、加入的社团列表、关注数与粉丝数。收藏与点赞列表不对外可见（保持隐私）。

#### Scenario: 查看他人主页
- **WHEN** 任意用户（含游客）访问 `/api/users/{userId}`
- **THEN** 返回该用户公开信息（昵称、头像、简介、积分、等级、注册时间、关注数、粉丝数、发帖数）

#### Scenario: 查看他人主页发帖
- **WHEN** 访问 `/api/users/{userId}/posts`
- **THEN** 返回该用户已发布帖子的分页列表

### Requirement: 关注/粉丝关系
系统 SHALL 提供用户间关注/取关能力，新表 `follow` 记录关注关系。用户主页展示关注数与粉丝数，登录用户可关注/取关他人。

#### Scenario: 关注用户
- **WHEN** 登录用户点击关注某用户
- **THEN** 写入 follow 记录，被关注者粉丝数 +1，关注者关注数 +1

#### Scenario: 重复关注幂等
- **WHEN** 用户对已关注的人再次点击关注
- **THEN** 不重复写入，返回成功

### Requirement: 用户设置菜单
系统 SHALL 在前端提供独立用户设置页，包含基本资料编辑、头像上传、密码修改、通知偏好设置（评论/点赞/私信通知开关）。

#### Scenario: 修改通知偏好
- **WHEN** 用户在设置页关闭"点赞通知"
- **THEN** 后续被点赞不再生成通知记录

## MODIFIED Requirements

### Requirement: 权限控制
原有 `ROLE_ADMIN` 同时承担网站管理与内容管理。现修改为两级：`ROLE_SUPER_ADMIN`（网站级，含超管专属接口 + 全部普通管理员权限）、`ROLE_ADMIN`（内容级，管理帖子/评论/板块/标签/普通用户）。`@PreAuthorize` 表达式中内容管理类接口保持 `hasRole('ADMIN')`（Spring Security `hasRole('ADMIN')` 仅匹配 ROLE_ADMIN，需调整为 `hasAnyRole('ADMIN','SUPER_ADMIN')` 以允许超管访问）；管理员管理类接口使用 `hasRole('SUPER_ADMIN')`。

### Requirement: 帖子发布
帖子创建接口 SHALL 支持可选的 `clubId` 参数，当携带 `clubId` 时将帖子关联到指定社团。社团帖子同时出现在社团帖子列表与全局帖子列表（全局列表可按筛选条件包含或排除社团帖）。

## REMOVED Requirements
无
