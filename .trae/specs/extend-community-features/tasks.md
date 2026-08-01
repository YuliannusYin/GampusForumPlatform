# Tasks

## 阶段一：数据库与基础设施
- [x] Task 1: 数据库变更（新增表、角色与 superadmin 账号）
  - [x] SubTask 1.1: 在 init.sql 新增 `follow`/`club`/`club_member`/`club_post`/`user_setting` 表（均含逻辑删除字段与唯一索引）
  - [x] SubTask 1.2: 在 init.sql 新增 `ROLE_SUPER_ADMIN` 角色记录，并新增独立 `superadmin` 账号（BCrypt 密码），关联 ROLE_SUPER_ADMIN
  - [x] SubTask 1.3: 新建 `user_setting` 表存储通知偏好（避免侵入 user 表）；同步创建 5 个实体类与 Mapper（Follow/Club/ClubMember/ClubPost/UserSetting）
- [x] Task 2: 超级管理员角色分层（后端）
  - [x] SubTask 2.1: 调整所有内容管理类 `@PreAuthorize` 从 `hasRole('ADMIN')` 改为 `hasAnyRole('ADMIN','SUPER_ADMIN')`（AdminPostController/AdminTagController/AdminUserController/SectionController/StatsController）
  - [x] SubTask 2.2: 新增 `AdminAdminController`（`/api/admin/admins`），仅 `hasRole('SUPER_ADMIN')`，提供普通管理员账号的创建/列表/禁用/删除/重置密码
  - [x] SubTask 2.3: 新增 `AdminClubController`（`/api/admin/clubs`）审核社团申请（hasAnyRole('ADMIN','SUPER_ADMIN')）
  - [x] SubTask 2.4: JwtAuthenticationFilter/LoginUserDetails 确认能正确加载 ROLE_SUPER_ADMIN（已有从数据库加载角色逻辑，验证即可）

## 阶段二：游客浏览开放
- [x] Task 3: 游客浏览最大化开放
  - [x] SubTask 3.1: SecurityConfig 白名单新增 GET 放行：`/api/comments/**`、`/api/users/**`、`/api/clubs`、`/api/clubs/**`
  - [x] SubTask 3.2: 前端路由守卫调整：游客可访问帖子详情页、用户主页、社团页；写操作按钮在未登录时点击跳转登录页

## 阶段三：关注/粉丝
- [x] Task 4: 关注/粉丝关系
  - [x] SubTask 4.1: 新增 `Follow` 实体/Mapper/Service，关注/取关接口（`POST /api/users/{userId}/follow`、`DELETE /api/users/{userId}/follow`），幂等处理（重复关注不报错）
  - [x] SubTask 4.2: 新增查询接口：`GET /api/users/{userId}/followings`、`GET /api/users/{userId}/followers`（分页）、`GET /api/users/{userId}/is-following`（当前登录用户是否已关注）
  - [x] SubTask 4.3: 前端用户主页添加关注/取关按钮 + 关注数/粉丝数展示

## 阶段四：用户主页与设置
- [x] Task 5: 用户公开主页
  - [x] SubTask 5.1: 新增 `UserProfileController`（`/api/users/{userId}`），返回用户公开信息（昵称/头像/简介/积分/等级/注册时间/关注数/粉丝数/发帖数）
  - [x] SubTask 5.2: 新增 `GET /api/users/{userId}/posts`（该用户发帖分页）、`GET /api/users/{userId}/comments`（该用户评论历史分页）、`GET /api/users/{userId}/clubs`（加入的社团列表）
  - [x] SubTask 5.3: 前端新增用户主页页（`views/user/public-profile.vue`），含 Tab 切换：发帖/评论/社团
- [x] Task 6: 用户设置菜单
  - [x] SubTask 6.1: 后端 `user_setting` 表 CRUD（或复用 user 表字段），`GET/PUT /api/user/settings` 读写通知偏好
  - [x] SubTask 6.2: 通知生成处（评论/点赞/私信 Service）根据接收方 notify_* 偏好决定是否写入 notification
  - [x] SubTask 6.3: 前端新增设置页（`views/user/settings.vue`）：基本资料、头像上传、密码修改、通知偏好开关；布局菜单新增"设置"入口

## 阶段五：社团功能
- [x] Task 7: 社团功能-后端
  - [x] SubTask 7.1: 新增 `Club`/`ClubMember`/`ClubPost` 实体与 Mapper
  - [x] SubTask 7.2: 新增 `ClubController`（`/api/clubs`）：社团列表（分页/搜索）、社团详情、申请创建社团（登录用户）、加入/退出社团
  - [x] SubTask 7.3: `ClubController` 内集成社团帖子接口（`/api/clubs/{clubId}/posts`）：社团帖子列表、在社团发帖（需为社团成员）；帖子创建复用 PostService 并写入 club_post 关联
  - [x] SubTask 7.4: 社长管理接口集成在 `ClubController`：编辑社团信息、移除成员、删除社团内帖子，仅社长可操作
- [x] Task 8: 社团功能-前端
  - [x] SubTask 8.1: 新增社团列表页（`views/club/index.vue`）：搜索、卡片列表、创建社团入口
  - [x] SubTask 8.2: 新增社团详情页（`views/club/detail.vue`）：社团信息、帖子列表、加入/退出按钮、社长管理面板（成员列表/移除/编辑）
  - [x] SubTask 8.3: 布局导航新增"社团"菜单项；发帖页支持社团发帖（通过 clubId 查询参数调用社团发帖接口）

## 阶段六：集成与验证
- [ ] Task 9: 集成验证与文档更新
  - [ ] SubTask 9.1: 重新构建后端镜像 + 前端构建，docker compose up 验证四服务正常
  - [ ] SubTask 9.2: 冒烟测试：游客浏览全流程、superadmin 登录与管理员管理、社团创建审核加入发帖、关注/用户主页、设置页
  - [x] SubTask 9.3: 更新 docs/DEVELOPER_GUIDE.md（接口模块表、数据库表）与 README.md（默认账号表新增 superadmin）

# Task Dependencies
- Task 2 依赖 Task 1（需要 ROLE_SUPER_ADMIN 角色与表结构）
- Task 3 依赖 Task 1 完成（白名单引用新路径），但可与 Task 2 部分并行
- Task 4 与 Task 2/3 无强依赖，可并行
- Task 5 依赖 Task 4（用户主页展示关注数/粉丝数需 follow 表与接口）
- Task 6 与 Task 4/5 无强依赖，可并行
- Task 7 依赖 Task 1（club 表结构）
- Task 8 依赖 Task 7（前端调用后端接口）
- Task 9 依赖所有前置任务完成
