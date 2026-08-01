# Checklist

## 数据库与基础设施
- [x] init.sql 新增 follow 表，含 follower_id/following_id/created_time 与唯一索引
- [x] init.sql 新增 club 表，含 status(0待审核/1正常/2禁用)、member_count、post_count、逻辑删除字段
- [x] init.sql 新增 club_member 表，含 role(0普通成员/1社长)、joined_time，含 (club_id,user_id) 唯一索引
- [x] init.sql 新增 club_post 表（club_id, post_id）关联社团与帖子
- [x] init.sql 新增 club_application 表（社团加入申请，可选）或确认加入为直接加入无需审核
- [x] init.sql 新增 user_setting 表（或 user 表新增 notify_comment/notify_like/notify_message 字段）
- [x] init.sql 新增 ROLE_SUPER_ADMIN 角色记录
- [x] init.sql 新增独立 superadmin 账号并关联 ROLE_SUPER_ADMIN
- [x] superadmin 账号密码使用 BCrypt 加密，可用 superadmin/super123 登录

## 超级管理员角色分层
- [x] 所有内容管理类 Controller 的 @PreAuthorize 改为 hasAnyRole('ADMIN','SUPER_ADMIN')
- [x] 新增 AdminAdminController（/api/admin/admins），@PreAuthorize("hasRole('SUPER_ADMIN')")
- [x] AdminAdminController 提供普通管理员创建/列表/禁用/删除/重置密码接口
- [ ] 普通管理员访问 /api/admin/admins 返回 403 <!-- 需运行时验证 -->
- [x] 超级管理员可访问内容管理接口（帖子/用户/板块/标签/统计）
- [x] JwtAuthenticationFilter 能正确加载 ROLE_SUPER_ADMIN 角色

## 游客浏览开放
- [x] SecurityConfig 白名单 GET 放行 /api/comments/**
- [x] SecurityConfig 白名单 GET 放行 /api/users/**
- [x] SecurityConfig 白名单 GET 放行 /api/clubs 与 /api/clubs/**
- [ ] 游客访问帖子详情能正常加载评论列表（不返回 401） <!-- 需运行时验证 -->
- [ ] 游客访问用户主页不返回 401 <!-- 需运行时验证 -->
- [ ] 游客访问社团列表/详情不返回 401 <!-- 需运行时验证 -->
- [ ] 游客点击写操作（发帖/评论/关注/加入社团）被拦截或跳转登录 <!-- 需运行时验证 -->

## 关注/粉丝关系
- [x] POST /api/users/{userId}/follow 关注接口实现，幂等（重复关注不报错）
- [x] DELETE /api/users/{userId}/follow 取关接口实现
- [x] GET /api/users/{userId}/followings 返回关注列表分页
- [x] GET /api/users/{userId}/followers 返回粉丝列表分页
- [x] GET /api/users/{userId}/is-following 返回当前用户是否已关注
- [x] 关注后被关注者粉丝数、关注者关注数正确更新
- [x] 前端用户主页有关注/取关按钮与关注数/粉丝数展示

## 用户公开主页
- [x] GET /api/users/{userId} 返回用户公开信息（昵称/头像/简介/积分/等级/注册时间/关注数/粉丝数/发帖数）
- [x] GET /api/users/{userId}/posts 返回该用户发帖分页列表
- [x] GET /api/users/{userId}/comments 返回该用户评论历史分页
- [x] GET /api/users/{userId}/clubs 返回该用户加入的社团列表
- [x] 收藏与点赞列表不对外暴露（保持 /api/user/favorites、/api/user/likes 为登录本人才能访问）
- [x] 前端用户主页页（views/user/profile.vue）含发帖/评论/社团 Tab 切换
- [x] 前端用户主页路由 /user/:userId 对游客可访问

## 用户设置菜单
- [x] 后端 GET /api/user/settings 返回当前用户通知偏好
- [x] 后端 PUT /api/user/settings 可更新通知偏好
- [x] 通知生成处（评论/点赞/私信）根据接收方偏好决定是否写入 notification
- [x] 前端设置页 views/user/settings.vue 含基本资料、头像上传、密码修改、通知偏好开关
- [x] 布局导航菜单新增"设置"入口

## 社团功能-后端
- [x] Club/ClubMember/ClubPost/ClubApplication 实体与 Mapper 创建
- [x] GET /api/clubs 社团列表（分页/搜索），对游客可见
- [x] GET /api/clubs/{clubId} 社团详情，对游客可见
- [x] POST /api/clubs 申请创建社团（登录用户），状态为待审核
- [x] POST /api/clubs/{clubId}/join 加入社团（登录用户）
- [x] DELETE /api/clubs/{clubId}/join 退出社团
- [x] GET /api/clubs/{clubId}/posts 社团帖子列表，对游客可见
- [x] POST /api/clubs/{clubId}/posts 在社团发帖（需为社团成员）
- [x] 社长管理接口：编辑社团信息、移除成员、删除社团内帖子
- [x] 管理员审核社团接口（AdminClubController），审核通过后创建者成为社长
- [x] 帖子携带 clubId 时正确写入 club_post 关联表

## 社团功能-前端
- [x] 社团列表页 views/club/index.vue（搜索、卡片列表、创建入口）
- [x] 社团详情页 views/club/detail.vue（信息、帖子列表、加入/退出、社长管理面板）
- [x] 布局导航新增"社团"菜单项
- [x] 发帖页支持选择社团（仅展示已加入社团）

## 集成与验证
- [ ] 后端镜像重新构建成功，无编译错误 <!-- 需运行时验证 -->
- [x] 前端 npm run build 成功，无编译错误
- [ ] docker compose up 四服务正常（mysql/redis healthy，backend/frontend Up） <!-- 需运行时验证 -->
- [ ] 游客浏览全流程冒烟测试通过（板块→帖子→评论→用户主页→社团列表→社团帖子） <!-- 需运行时验证 -->
- [ ] superadmin 登录成功并能访问管理员管理接口 <!-- 需运行时验证 -->
- [ ] 普通管理员 admin 登录后访问管理员管理接口返回 403 <!-- 需运行时验证 -->
- [ ] 社团创建→审核→加入→发帖全流程通过 <!-- 需运行时验证 -->
- [ ] 关注→用户主页关注数/粉丝数更新正确 <!-- 需运行时验证 -->
- [ ] 设置页修改通知偏好后，关闭项不再产生通知 <!-- 需运行时验证 -->
- [x] docs/DEVELOPER_GUIDE.md 接口模块表与数据库表清单已更新
- [x] README.md 默认账号表已新增 superadmin
