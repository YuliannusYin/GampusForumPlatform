# 开发者文档

本文档面向项目开发者，介绍技术栈、目录结构、本地开发环境搭建与架构设计。

> 如需了解 Docker 部署、运维与故障排查，请参阅 [README.md](../README.md)。

## 技术栈

### 后端

- **核心框架**：SpringBoot 3.2.5 + JDK 21
- **ORM 框架**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0
- **缓存**：Redis 7
- **安全认证**：Spring Security + JWT (jjwt 0.12.5)
- **实时通信**：WebSocket (STOMP)
- **接口文档**：Knife4j (OpenAPI3 Jakarta) 4.5.0
- **工具库**：Lombok

### 前端

- **核心框架**：Vue 3.4
- **构建工具**：Vite 5.2
- **UI 组件库**：Element Plus 2.7 + @element-plus/icons-vue
- **状态管理**：Pinia 2.1
- **路由管理**：Vue Router 4.3
- **HTTP 请求**：Axios 1.6
- **Markdown 编辑器**：md-editor-v3 4.0
- **图表库**：ECharts 5.5
- **样式预处理**：Sass 1.77

### 部署

- **容器化**：Docker + Docker Compose
- **反向代理**：Nginx

## 目录结构

```
CampusForumPlatform/
├── README.md                          # 部署运维指南
├── docs/
│   └── DEVELOPER_GUIDE.md             # 本文档
├── .env.docker                        # 环境变量模板
├── .gitignore                         # Git 忽略配置
├── docker-compose.yml                 # Docker Compose 编排
├── campus-forum-backend/              # 后端工程
│   ├── pom.xml                        # Maven 依赖与构建配置
│   ├── Dockerfile                     # 后端多阶段构建
│   └── src/
│       ├── main/
│       │   ├── java/com/campus/forum/
│       │   │   ├── CampusForumApplication.java   # 主启动类
│       │   │   ├── controller/       # 控制器层（REST 接口）
│       │   │   ├── service/          # 业务逻辑层
│       │   │   │   └── impl/         # 业务实现
│       │   │   ├── mapper/           # 数据访问层（MyBatis-Plus）
│       │   │   ├── entity/           # 实体类（对应数据库表）
│       │   │   ├── dto/              # 数据传输对象
│       │   │   │   ├── req/          # 请求 DTO
│       │   │   │   └── resp/         # 响应 DTO
│       │   │   ├── config/           # 配置类（Security/Redis/MyBatis/CORS/WebSocket/Knife4j）
│       │   │   ├── common/           # 公共模块
│       │   │   │   ├── result/       # 统一返回结果 Result<T>、PageResult、ResultCode
│       │   │   │   ├── exception/    # 全局异常处理 GlobalExceptionHandler
│       │   │   │   └── enums/        # 枚举类（积分类型等）
│       │   │   ├── security/         # 安全认证模块（JWT 过滤器、UserDetails、WebSocket 鉴权）
│       │   │   └── utils/            # 工具类（JwtUtils）
│       │   └── resources/
│       │       ├── application.yml          # 主配置文件
│       │       ├── application-dev.yml      # 开发环境配置
│       │       ├── application-prod.yml     # 生产环境配置
│       │       └── mapper/           # MyBatis XML 映射文件
│       └── test/                     # 测试代码
└── campus-forum-frontend/            # 前端工程
    ├── package.json                  # 依赖与脚本配置
    ├── vite.config.js                # Vite 构建配置
    ├── Dockerfile                    # 前端多阶段构建
    ├── index.html                    # HTML 入口
    └── src/
        ├── main.js                   # 应用入口
        ├── App.vue                   # 根组件
        ├── router/                   # 路由配置（含全局前置守卫）
        ├── store/                    # Pinia 状态管理（user store）
        ├── api/                      # 接口请求封装（auth/post/comment 等）
        ├── views/                    # 页面视图
        │   ├── home/                 # 首页
        │   ├── login/                # 登录页
        │   ├── register/             # 注册页
        │   ├── post/                 # 帖子详情与编辑
        │   ├── section/              # 板块页
        │   ├── search/               # 搜索页
        │   ├── user/                 # 个人中心
        │   ├── message/              # 消息中心（通知 + 私信）
        │   ├── admin/                # 管理后台
        │   └── error/                # 错误页
        ├── components/               # 公共组件（PostCard、PostList、CommentList）
        ├── layout/                   # 布局组件（导航、用户菜单）
        ├── utils/                    # 工具函数（request、format、websocket）
        └── assets/
            └── styles/               # 全局样式
```

## 本地开发环境搭建

### 前置环境要求

- JDK 21
- Maven 3.9+
- MySQL 8.0
- Redis 7
- Node.js 18+
- npm 或 pnpm

### 数据库准备

1. 创建数据库 `campus_forum`（字符集 utf8mb4）。
2. 执行 `docker/init/init.sql` 脚本初始化表结构与初始数据（管理员账号、默认板块、初始标签）。
3. 修改 `campus-forum-backend/src/main/resources/application-dev.yml` 中的数据库连接信息（默认指向 localhost:3306，用户名 root，密码 root）。

### 后端启动

```bash
cd campus-forum-backend
mvn spring-boot:run
```

后端服务默认启动在 `http://localhost:8080`，所有接口以 `/api` 为前缀。

接口文档访问地址：`http://localhost:8080/api/doc.html`（Knife4j UI）。

### 前端启动

```bash
cd campus-forum-frontend
npm install
npm run dev
```

前端开发服务默认启动在 `http://localhost:5173`，已通过 Vite 代理将 `/api` 请求转发到后端 8080 端口。

### 默认账号

| 用户名 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| admin | admin123 | ROLE_ADMIN | 系统管理员 |
| superadmin | super123 | ROLE_SUPER_ADMIN | 超级管理员 |

> **安全建议**：部署后请立即修改默认密码。

## 后端开发说明

### 配置文件

| 文件 | 用途 | 激活方式 |
| --- | --- | --- |
| `application.yml` | 主配置，含公共设置与默认值 | 默认 |
| `application-dev.yml` | 开发环境，指向本地 MySQL/Redis | `spring.profiles.active=dev` |
| `application-prod.yml` | 生产环境，通过环境变量注入连接信息 | `spring.profiles.active=prod` |

关键配置项：

- **数据源**：`spring.datasource.url`，JDBC URL 包含 `characterEncoding=UTF-8&allowPublicKeyRetrieval=true`
- **Redis**：`spring.data.redis.host/port/password`
- **JWT**：`jwt.secret`（开发环境默认值，生产环境通过环境变量注入）
- **文件上传**：`campus.upload-path`，默认 `./uploads/`
- **MyBatis-Plus**：分页插件、自动填充（create_time/update_time）、逻辑删除（deleted 字段）

### 统一响应结构

所有接口返回 `Result<T>`：

```java
{
  "code": 200,        // 业务码，200 成功
  "message": "操作成功",
  "data": { ... }     // 泛型数据
}
```

分页接口返回 `PageResult<T>`：

```java
{
  "records": [...],
  "total": 100,
  "page": 1,
  "size": 10
}
```

业务码枚举见 `ResultCode`（成功/参数错误/未授权/禁止访问/资源不存在/系统异常等）。

### 认证机制

1. **登录**：`POST /api/auth/login`，校验成功后签发 Access Token（2h）与 Refresh Token（7天，存 Redis）
2. **请求鉴权**：前端在 HTTP Header 携带 `Authorization: Bearer <accessToken>`，`JwtAuthenticationFilter` 解析并校验
3. **Token 刷新**：`POST /api/auth/refresh`，携带 Refresh Token 签发新 Access Token
4. **登出**：`POST /api/auth/logout`，清除 Redis 中的 Refresh Token
5. **权限控制**：系统包含三种角色：
   - `ROLE_USER` - 普通用户
   - `ROLE_ADMIN` - 管理员（管理论坛内容和普通用户）
   - `ROLE_SUPER_ADMIN` - 超级管理员（管理网站和普通管理员）

   通过 `@PreAuthorize` 注解控制接口访问，如 `@PreAuthorize("hasRole('ADMIN')")` 控制管理员接口，`@PreAuthorize("hasRole('SUPER_ADMIN')")` 控制超级管理员接口，权限不足返回 403。

### 接口模块

| 模块 | Controller | 路径前缀 | 说明 |
| --- | --- | --- | --- |
| 认证 | AuthController | /api/auth | 注册、登录、登出、Token 刷新 |
| 用户 | UserController | /api/user | 个人资料、修改密码、我的发帖/收藏/点赞 |
| 板块 | SectionController | /api/sections | 板块 CRUD（管理员写，用户读） |
| 帖子 | PostController | /api/posts | 发帖、编辑、删除、列表、详情、搜索 |
| 评论 | CommentController | /api/comments | 发表评论、回复、删除、列表 |
| 互动 | InteractionController | /api/interactions | 点赞、收藏及状态查询 |
| 标签 | TagController | /api/tags | 标签列表、按标签检索 |
| 文件 | FileController | /api/files | 图片上传 |
| 通知 | NotificationController | /api/notifications | 通知列表、未读数、标记已读 |
| 私信 | ChatController | /api/chat | 发送消息、会话列表、历史消息 |
| 签到 | SignInController | /api/signin | 每日签到、签到状态 |
| 积分 | PointsController | /api/points | 积分明细 |
| 管理-用户 | AdminUserController | /api/admin/users | 用户管理（管理员） |
| 管理-帖子 | AdminPostController | /api/admin/posts | 帖子管理（管理员） |
| 管理-标签 | AdminTagController | /api/admin/tags | 标签管理（管理员） |
| 统计 | StatsController | /api/stats | 数据看板（管理员） |
| 社团 | ClubController | /api/clubs | 社团列表/详情/创建/加入/退出/发帖/成员管理 |
| 关注 | FollowController | /api/users/{userId}/follow | 关注/取关/关注列表/粉丝列表 |
| 用户主页 | UserProfileController | /api/users/{userId} | 公开信息/发帖/评论/社团 |
| 用户设置 | UserSettingController | /api/user/settings | 通知偏好读写 |
| 管理-管理员 | AdminAdminController | /api/admin/admins | 超级管理员管理普通管理员（仅 ROLE_SUPER_ADMIN） |
| 管理-社团审核 | AdminClubController | /api/admin/clubs | 管理员审核社团申请 |

### WebSocket 实时私信

- **协议**：STOMP over WebSocket
- **端点**：`/ws/**`（握手时通过 `JwtHandshakeInterceptor` 进行 JWT 鉴权）
- **消息代理**：`/user/queue/messages`（点对点推送）
- **在线状态**：通过 Redis 维护用户在线状态，`WebSocketEventListener` 监听连接/断开事件

## 前端开发说明

### 路径别名

`@` 指向 `src/` 目录，在 `vite.config.js` 中配置：

```js
resolve: {
  alias: { '@': path.resolve(__dirname, 'src') }
}
```

### API 请求封装

`src/utils/request.js` 封装 Axios：

- `baseURL`：开发环境通过 Vite 代理，生产环境为 `/api`
- 请求拦截器：自动注入 `Authorization: Bearer <token>`
- 响应拦截器：统一处理 `Result<T>` 结构，401 自动跳转登录页

接口请求按模块封装在 `src/api/` 下（`auth.js`、`post.js`、`comment.js` 等）。

### 状态管理

`src/store/user.js`（Pinia）管理用户状态：

- `token`：Access Token
- `userInfo`：用户信息（id、username、roles 等）
- 持久化：通过 `localStorage` 保存 token 与用户信息

### 路由与权限

`src/router/index.js` 定义路由表，全局前置守卫：

- 需登录的页面（`requiresAuth: true`）：未登录时跳转登录页，登录后跳回来源页
- 管理后台页面（`requiresAdmin: true`）：非管理员用户跳转 403

### Element Plus 按需引入

通过 `unplugin-auto-import` 与 `unplugin-vue-components` 自动导入 Element Plus 组件与 API，无需手动 import。

### 前端构建

```bash
cd campus-forum-frontend
npm run build
```

构建产物输出到 `dist/`，Docker 部署时由 Nginx 托管。

## 数据库设计概览

共 22 张表，均使用 InnoDB 引擎、utf8mb4 字符集、BIGINT 自增主键，支持逻辑删除（`deleted` 字段）。

| # | 表名 | 说明 |
| --- | --- | --- |
| 1 | user | 用户表 |
| 2 | role | 角色表 |
| 3 | user_role | 用户角色关联表 |
| 4 | section | 板块表 |
| 5 | post | 帖子表 |
| 6 | tag | 标签表 |
| 7 | post_tag | 帖子标签关联表 |
| 8 | comment | 评论表（parent_id 支持多级回复） |
| 9 | like_record | 点赞记录表 |
| 10 | favorite | 收藏表 |
| 11 | notification | 通知表 |
| 12 | chat_session | 私信会话表 |
| 13 | chat_message | 私信消息表 |
| 14 | sign_in_record | 签到记录表 |
| 15 | points_record | 积分记录表 |
| 16 | file_record | 文件上传记录表 |
| 17 | follow | 关注关系表 |
| 18 | club | 社团表 |
| 19 | club_member | 社团成员表 |
| 20 | club_post | 社团帖子关联表 |
| 21 | user_setting | 用户通知偏好 |
| 22 | report | 表白墙举报表 |

完整建表脚本见 `docker/init/init.sql`。

## 架构说明

### 整体架构

```
浏览器 ──HTTP──> Nginx (80) ──/api/──> SpringBoot (8080) ──> MySQL (3306)
                     │                     │                 Redis (6379)
                     │                     │
                  静态资源            WebSocket /ws/**
```

### Docker 编排

`docker-compose.yml` 定义四个服务：

- **mysql**：MySQL 8.0，首次启动自动执行 `docker/init/init.sql`，数据持久化到 `mysql_data` 卷
- **redis**：Redis 7，数据持久化到 `redis_data` 卷
- **backend**：SpringBoot 应用，多阶段构建（Maven 打包 → JRE 运行），依赖 mysql/redis 健康检查
- **frontend**：Nginx 托管前端静态资源，反向代理 `/api/` 到 backend，挂载 `uploads` 目录提供上传文件访问

### Nginx 代理规则

| 路径 | 转发目标 | 说明 |
| --- | --- | --- |
| `/api/` | `http://backend:8080/api/` | 后端 API（含 WebSocket 升级支持） |
| `/uploads/` | 本地静态文件 | 上传文件访问 |
| `/` | `index.html` | Vue SPA（history 模式回退） |

## 版本管理

项目使用 Git + GitHub 进行版本管理，仓库名为 `GampusForumPlatform`。

```bash
# 克隆仓库
git clone https://github.com/<your-account>/GampusForumPlatform.git

# 提交代码
git add .
git commit -m "feat: 描述本次变更"
git push origin main
```

分支策略（建议）：

- `main`：稳定分支，可部署
- `dev`：开发分支，日常集成
- `feature/*`：功能分支，开发完成后合并到 dev
