# CampusForumPlatform 校园论坛与社区交流平台 Spec

## Why
作为软件工程专业的毕业设计，需要开发一个面向高校师生的校园论坛与社区交流 Web 应用，提供发帖、评论、私信、签到积分等社区互动能力。项目需同时支持 Windows11 本地开发与 Ubuntu 服务器 Docker 容器化部署，代码托管于 GitHub（仓库名 `GampusForumPlatform`，本地目录为 `CampusForumPlatform`）。

## What Changes
- 初始化前后端工程脚手架（SpringBoot 3.2 + Vue3 + Vite）
- 搭建 Docker Compose 部署环境（Nginx + 后端 + MySQL + Redis）
- 设计并实现用户体系、JWT 认证与权限控制
- 实现论坛核心功能：板块、发帖、评论、点赞、收藏、标签、搜索
- 实现社区扩展功能：消息通知、WebSocket 私信、文件上传、签到积分
- 实现后台管理功能：用户/帖子管理、数据统计看板
- 实现前端全部页面与交互，并完成前后端联调
- 完成本地与服务器 Docker 部署验证

## Impact
- Affected specs: 全新项目，无既有 spec 受影响
- Affected code: 
  - 后端：`campus-forum-backend/`（SpringBoot 工程）
  - 前端：`campus-forum-frontend/`（Vue3 工程）
  - 部署：`docker/`（Dockerfile、docker-compose.yml、nginx 配置、SQL 初始化脚本）
  - 文档：`README.md`、`.gitignore`

## 技术栈约定
- **后端**：SpringBoot 3.2.x + JDK21 + MyBatis-Plus 3.5.x + MySQL 8.0 + Redis 7 + Spring Security + JWT(jjwt) + WebSocket(STOMP) + Knife4j(Swagger)
- **前端**：Vue3 + Vite5 + ElementPlus + Pinia + Vue Router4 + Axios + md-editor-v3 + ECharts
- **部署**：Docker + Docker Compose + Nginx
- **版本管理**：Git + GitHub（仓库名 `GampusForumPlatform`）

## ADDED Requirements

### Requirement: 项目工程结构
系统 SHALL 按前后端分离方式组织工程，后端为独立 Maven 工程，前端为独立 Vite 工程，根目录包含 Docker 部署文件与 README。

#### Scenario: 工程初始化
- **WHEN** 开发者克隆仓库并查看根目录
- **THEN** 应看到 `campus-forum-backend/`、`campus-forum-frontend/`、`docker/`、`README.md`、`.gitignore` 等条目
- **AND** 后端工程使用 Maven 多模块或单模块结构，包含 `pom.xml` 与标准 `src/main/java`、`src/main/resources` 目录
- **AND** 前端工程包含 `package.json`、`vite.config.js`、`src/` 目录

### Requirement: Docker 容器化部署
系统 SHALL 提供基于 Docker Compose 的一键部署能力，包含 Nginx、后端、MySQL、Redis 四个服务，并兼容 Windows11 与 Ubuntu 环境。

#### Scenario: 本地开发部署
- **WHEN** 开发者在 Windows11 执行 `docker compose up -d`
- **THEN** MySQL、Redis、后端、Nginx 容器应依次启动
- **AND** Nginx 应将 `/api/**` 请求代理到后端容器，其余请求返回前端静态资源
- **AND** 前端可通过 `http://localhost` 访问，后端 API 可通过 `http://localhost/api` 访问

#### Scenario: 服务器部署
- **WHEN** 在 Ubuntu 服务器执行 `docker compose up -d`
- **THEN** 所有服务正常运行，数据库自动执行初始化 SQL 脚本
- **AND** 数据与上传文件通过 volume 持久化，容器重建不丢失数据

### Requirement: 用户注册与认证
系统 SHALL 提供基于 JWT 的用户注册、登录、登出与 Token 刷新机制，注册采用直接注册（用户名/邮箱 + 密码），无需邮箱验证码。

#### Scenario: 用户注册
- **WHEN** 用户提交合法的用户名、邮箱、密码
- **THEN** 系统校验用户名与邮箱唯一性后创建用户（密码 BCrypt 加密）
- **AND** 返回注册成功提示，默认分配普通用户角色

#### Scenario: 用户登录
- **WHEN** 用户提交正确的账号（用户名或邮箱）与密码
- **THEN** 系统签发 Access Token（短期）与 Refresh Token（长期）
- **AND** Access Token 有效期 2 小时，Refresh Token 有效期 7 天，存于 Redis

#### Scenario: Token 刷新
- **WHEN** Access Token 过期且 Refresh Token 有效
- **THEN** 系统签发新的 Access Token 并返回

#### Scenario: 未授权访问拦截
- **WHEN** 未登录用户访问需认证接口
- **THEN** 返回 401 状态码与未授权提示

### Requirement: 用户个人中心
系统 SHALL 提供用户个人中心，支持查看与修改个人资料、头像上传、查看我的发帖/收藏/点赞记录。

#### Scenario: 修改个人资料
- **WHEN** 用户修改昵称、个人简介、头像
- **THEN** 系统更新用户信息并返回最新资料
- **AND** 头像通过文件上传接口存储到服务器本地磁盘

### Requirement: 板块与分类
系统 SHALL 提供论坛板块（Section）分类，管理员可创建/编辑/删除板块，普通用户可按板块浏览帖子。

#### Scenario: 浏览板块
- **WHEN** 用户访问首页或板块页
- **THEN** 显示板块列表，每个板块展示名称、简介、帖子数量

### Requirement: 帖子发布与管理
系统 SHALL 支持用户发帖（Markdown 富文本）、编辑、删除，帖子可关联板块与标签，支持浏览量统计。

#### Scenario: 发布帖子
- **WHEN** 登录用户提交标题、Markdown 正文、所属板块、标签
- **THEN** 系统创建帖子并跳转至帖子详情页
- **AND** 帖子状态为已发布，发布时间记录为当前时间

#### Scenario: 编辑/删除帖子
- **WHEN** 帖子作者请求编辑或删除自己的帖子
- **THEN** 系统允许操作并更新/移除帖子
- **AND** 非作者非管理员的请求应被拒绝（403）

#### Scenario: 帖子列表分页
- **WHEN** 用户访问板块或首页
- **THEN** 帖子按发布时间倒序分页展示，每页默认 10 条
- **AND** 列表项显示标题、作者、板块、标签、浏览量、点赞数、评论数

### Requirement: 评论与回复
系统 SHALL 支持帖子的评论与多级回复，评论支持 Markdown，记录点赞数。

#### Scenario: 发表评论
- **WHEN** 登录用户在帖子下提交评论内容
- **THEN** 评论创建成功并展示在评论列表
- **AND** 帖子评论数 +1，通知帖子作者

#### Scenario: 回复评论
- **WHEN** 用户回复某条评论
- **THEN** 回复作为子评论关联到父评论，支持多级展示

### Requirement: 点赞与收藏
系统 SHALL 支持对帖子、评论的点赞与取消点赞，支持帖子收藏与取消收藏，限制每用户对同一对象仅能点赞/收藏一次。

#### Scenario: 点赞帖子
- **WHEN** 登录用户首次点赞某帖子
- **THEN** 帖子点赞数 +1，记录用户点赞行为，通知帖子作者
- **AND** 再次点赞则取消，点赞数 -1

### Requirement: 标签系统
系统 SHALL 支持标签管理，发帖时可选择或新建标签，支持按标签检索帖子。

#### Scenario: 按标签浏览
- **WHEN** 用户点击某标签
- **THEN** 展示所有关联该标签的帖子列表

### Requirement: 搜索功能
系统 SHALL 提供关键词搜索，支持按标题、正文、作者检索帖子。

#### Scenario: 关键词搜索
- **WHEN** 用户输入关键词并搜索
- **THEN** 返回匹配的帖子列表，按相关度/时间排序
- **AND** 支持按板块、时间范围筛选

### Requirement: 文件上传
系统 SHALL 提供图片/文件上传接口，存储到服务器本地磁盘，返回访问 URL，供帖子图片、头像、私信附件使用。

#### Scenario: 上传图片
- **WHEN** 登录用户上传图片（限制类型 jpg/png/gif，大小 ≤ 5MB）
- **THEN** 系统保存文件到本地 `uploads/` 目录，返回可访问 URL
- **AND** Nginx 配置静态资源映射对外提供访问

### Requirement: 消息通知
系统 SHALL 提供站内消息通知，当用户收到评论、点赞、私信时生成通知，支持已读/未读管理。

#### Scenario: 收到通知
- **WHEN** 用户的帖子被评论/点赞或收到私信
- **THEN** 系统生成站内通知，前端顶部显示未读数量徽标
- **AND** 用户可标记单条或全部通知为已读

### Requirement: WebSocket 实时私信
系统 SHALL 基于 WebSocket(STOMP) 实现用户间实时私信聊天，支持历史消息查询与在线状态展示。

#### Scenario: 发送私信
- **WHEN** 登录用户向另一用户发送私信
- **THEN** 若对方在线则实时推送消息，若离线则存为离线消息
- **AND** 消息持久化到数据库，会话列表更新最新消息

#### Scenario: 查询聊天记录
- **WHEN** 用户打开与某用户的聊天窗口
- **THEN** 分页加载历史消息，按时间正序展示

### Requirement: 签到与积分
系统 SHALL 提供每日签到功能，签到获得积分，连续签到有额外奖励，积分可用于用户等级展示。

#### Scenario: 每日签到
- **WHEN** 用户当日首次签到
- **THEN** 获得基础积分，连续签到天数 +1，触发连续签到奖励规则
- **AND** 当日重复签到被拒绝

#### Scenario: 积分等级
- **WHEN** 用户积分达到某等级阈值
- **THEN** 用户等级自动提升并在个人资料展示

### Requirement: 后台管理
系统 SHALL 提供管理员后台，支持用户管理（封禁/解禁/角色）、帖子管理（删除/置顶/加精）、板块管理、标签管理。

#### Scenario: 管理员封禁用户
- **WHEN** 管理员对某用户执行封禁操作
- **THEN** 该用户无法登录与发帖，状态置为封禁
- **AND** 普通用户访问管理后台应被拒绝（403）

#### Scenario: 帖子置顶/加精
- **WHEN** 管理员对帖子置顶或加精
- **THEN** 帖子在列表中优先展示或显示精华标记

### Requirement: 数据统计看板
系统 SHALL 提供管理员数据统计看板，展示用户数、帖子数、评论数、活跃趋势等核心指标。

#### Scenario: 查看看板
- **WHEN** 管理员访问数据看板
- **THEN** 展示总用户数、总帖子数、总评论数、今日新增等统计卡片
- **AND** 展示近 7/30 天发帖趋势折线图、板块帖子分布饼图（ECharts）

### Requirement: 前端页面与交互
系统 SHALL 提供完整的前端页面，包括登录注册、首页、板块、帖子详情、发帖编辑、个人中心、消息中心、管理后台等页面。

#### Scenario: 前端路由守卫
- **WHEN** 未登录用户访问需登录页面（如发帖、个人中心）
- **THEN** 自动跳转至登录页并记录来源地址
- **AND** 登录成功后跳回来源页

#### Scenario: 响应式布局
- **WHEN** 用户在不同屏幕尺寸访问
- **THEN** 页面在桌面端正常展示，移动端基本可用（毕设不做强制移动适配）

### Requirement: 后端通用基础设施
系统 SHALL 提供统一响应结构、全局异常处理、跨域配置、MyBatis-Plus 配置、Redis 配置、Swagger 文档、日志配置等基础设施。

#### Scenario: 统一响应
- **WHEN** 任意接口被调用
- **THEN** 返回统一 JSON 结构 `{code, message, data}`
- **AND** 业务异常被全局异常处理器捕获并返回对应错误码

### Requirement: 数据库设计
系统 SHALL 设计完整的数据库表结构，覆盖用户、角色、帖子、板块、标签、评论、点赞、收藏、通知、私信、签到、积分、文件等实体。

#### Scenario: 表结构初始化
- **WHEN** MySQL 容器首次启动
- **THEN** 自动执行 `init.sql` 创建所有表与初始数据（管理员账号、默认板块）
- **AND** 表使用 InnoDB 引擎、utf8mb4 字符集，主键为 BIGINT 自增
