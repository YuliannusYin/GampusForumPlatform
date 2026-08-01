# 测试数据管理 Spec

## Why
系统目前缺少可演示的测试数据，毕业设计验收时首页、帖子列表、社团、私信、统计图表等场景呈现空白或数据稀疏，难以体现真实使用效果。需要一个可在后台一键导入/移除的测试数据集，便于演示和压测，同时保证真实用户数据不会被误删。

## What Changes
- 后端新增 `AdminTestDataController`，提供三个接口：导入测试数据、移除测试数据、查询测试数据状态
- 后端新增 `TestDataService`，封装测试数据生成与清理逻辑
- 后端新增测试数据常量类 `TestDataConstants`，统一管理用户名前缀 `test_`、社团名前缀 `【测试】`、默认密码 `test123456` 等标识
- 前端 `layout.vue` 新增"测试数据"菜单项，仅对超级管理员（`ROLE_SUPER_ADMIN`）可见
- 前端新增页面 `views/admin/test-data.vue`，展示当前数据状态、提供一键导入/移除按钮、显示操作结果统计
- 前端 `api/admin.js` 新增测试数据相关 API 封装
- 前端 `router/index.js` 新增 `/admin/test-data` 路由，标记 `requiresSuperAdmin: true`
- 前端路由守卫增加超级管理员权限校验逻辑
- **BREAKING**：无（纯新增功能，不影响现有接口和页面）

## Impact
- Affected specs: 后台管理、超级管理员权限、数据看板
- Affected code:
  - 后端：新增 `controller/AdminTestDataController.java`、`service/TestDataService.java`、`service/impl/TestDataServiceImpl.java`、`common/constant/TestDataConstants.java`
  - 后端：复用现有 `UserMapper`、`PostMapper`、`CommentMapper`、`ClubMapper` 等所有 Mapper
  - 前端：修改 `views/admin/layout.vue`、`router/index.js`、`api/admin.js`、`store/user.js`（如需补充 `isSuperAdmin` getter）
  - 数据库：无表结构变更

## ADDED Requirements

### Requirement: 测试数据标识
系统 SHALL 使用统一的命名前缀标识测试数据，确保移除操作不会误删真实数据。
- 测试用户名统一以 `test_` 前缀开头（如 `test_alice_001`）
- 测试社团名统一以 `【测试】` 前缀开头
- 测试帖子标题统一以 `【测试】` 前缀开头
- 测试用户密码统一为 `test123456`（BCrypt 加密存储）
- 测试用户邮箱统一使用 `test+xxx@test.campus.edu` 格式（避免与真实邮箱冲突）

#### Scenario: 标识唯一性
- **WHEN** 系统生成测试用户
- **THEN** 用户名必须以 `test_` 开头，且与现有用户名不重复

#### Scenario: 邮箱冲突避免
- **WHEN** 系统生成测试用户邮箱
- **THEN** 邮箱必须使用 `test+xxx@test.campus.edu` 格式，且与现有邮箱不重复

---

### Requirement: 测试数据范围与规模
系统 SHALL 一次性生成覆盖核心内容、社交互动、社团三大类的测试数据，规模为大规模压测级别。

数据规模明细：
- 用户：100 个（`test_` 前缀，随机昵称、性别、头像、个人简介）
- 板块：复用现有 5 个板块，不新增
- 标签：复用现有 5 个标签，不新增
- 帖子：500 条（标题以 `【测试】` 开头，Markdown 正文，随机分布到 5 个板块，作者从 100 个测试用户中随机选取）
- 评论：1000 条（随机分布到 500 条帖子下，包含约 30% 二级回复）
- 点赞记录：约 800 条（随机给帖子和评论点赞）
- 收藏记录：约 500 条（随机收藏帖子）
- 关注关系：约 300 条（测试用户之间互相关注）
- 社团：10 个（名称以 `【测试】` 开头，每个社团 1 个社长 + 5~15 个成员，状态为已审核通过 `status=1`）
- 社团帖子关联：约 100 条（部分帖子同时关联到社团）
- 聊天会话：约 50 个（测试用户之间的私信会话）
- 聊天消息：约 200 条（分布在会话中）
- 通知：约 150 条（点赞、评论、关注触发的通知）

#### Scenario: 数据规模达标
- **WHEN** 超级管理员点击"导入测试数据"按钮
- **THEN** 系统生成上述规模的测试数据，并返回各类数据的实际生成数量

---

### Requirement: 测试数据导入接口
系统 SHALL 提供一个接口供超级管理员一键导入全部测试数据。
- 接口路径：`POST /api/admin/test-data/import`
- 权限：仅 `ROLE_SUPER_ADMIN` 可访问
- 请求体：无
- 响应体：返回各类数据生成数量统计

#### Scenario: 导入成功
- **WHEN** 超级管理员调用导入接口
- **THEN** 系统按顺序生成用户→社团→帖子→评论→点赞→收藏→关注→私信→通知，返回每种数据的生成数量

#### Scenario: 重复导入检测
- **WHEN** 数据库中已存在测试数据（用户名以 `test_` 开头的用户数 > 0）时调用导入接口
- **THEN** 返回业务错误，提示"已存在测试数据，请先移除后再导入"

#### Scenario: 导入失败回滚
- **WHEN** 导入过程中发生异常
- **THEN** 当前事务回滚，已生成的数据被撤销，返回错误信息

---

### Requirement: 测试数据移除接口
系统 SHALL 提供一个接口供超级管理员一键移除全部测试数据。
- 接口路径：`DELETE /api/admin/test-data`
- 权限：仅 `ROLE_SUPER_ADMIN` 可访问
- 请求体：无
- 响应体：返回各类数据移除数量统计

移除策略（基于 `test_` 用户名前缀和 `【测试】` 社团名前缀联动清理）：
1. 查询所有 `username LIKE 'test\_%'` 的用户ID集合（记为 `testUserIds`）
2. 查询所有 `name LIKE '【测试】%'` 的社团ID集合（记为 `testClubIds`）
3. 删除 `club_post` 中 `club_id IN testClubIds` 的关联
4. 删除 `club_member` 中 `club_id IN testClubIds` 或 `user_id IN testUserIds` 的记录
5. 删除 `club` 中 `id IN testClubIds` 的社团
6. 删除 `post_tag` 中 `post_id` 属于测试帖子的关联（测试帖子 = 作者在 `testUserIds` 中且标题以 `【测试】` 开头）
7. 删除 `comment` 中 `user_id IN testUserIds` 的评论
8. 删除 `like_record` 中 `user_id IN testUserIds` 的点赞
9. 删除 `favorite` 中 `user_id IN testUserIds` 的收藏
10. 删除 `follow` 中 `follower_id IN testUserIds` 或 `following_id IN testUserIds` 的关注
11. 删除 `chat_message` 中 `sender_id IN testUserIds` 或 `receiver_id IN testUserIds` 的消息
12. 删除 `chat_session` 中 `user1_id IN testUserIds` 或 `user2_id IN testUserIds` 的会话
13. 删除 `notification` 中 `user_id IN testUserIds` 或 `from_user_id IN testUserIds` 的通知
14. 删除 `post` 中 `user_id IN testUserIds` 的帖子
15. 删除 `user_role` 中 `user_id IN testUserIds` 的角色关联
16. 删除 `user_setting` 中 `user_id IN testUserIds` 的设置
17. 删除 `user` 中 `id IN testUserIds` 的用户

#### Scenario: 移除成功
- **WHEN** 超级管理员调用移除接口
- **THEN** 系统按上述顺序删除所有测试数据，返回每种数据的移除数量

#### Scenario: 无测试数据
- **WHEN** 数据库中不存在测试数据时调用移除接口
- **THEN** 返回各类数据移除数量均为 0，不抛出错误

#### Scenario: 移除不影响真实数据
- **WHEN** 调用移除接口
- **THEN** 真实用户、真实帖子、真实社团等数据保持不变

---

### Requirement: 测试数据状态查询接口
系统 SHALL 提供一个接口供超级管理员查询当前测试数据状态。
- 接口路径：`GET /api/admin/test-data/status`
- 权限：仅 `ROLE_SUPER_ADMIN` 可访问
- 响应体：返回各类测试数据的当前数量

#### Scenario: 查询状态
- **WHEN** 超级管理员访问测试数据管理页面
- **THEN** 系统返回各类测试数据的当前数量，前端展示在数据卡片中

---

### Requirement: 超级管理员后台入口
系统 SHALL 在后台管理侧边栏为超级管理员提供"测试数据"菜单入口，普通管理员（`ROLE_ADMIN`）不可见。
- 菜单项路径：`/admin/test-data`
- 菜单项图标：`MagicStick` 或 `DataLine`
- 仅当 `userStore.isSuperAdmin === true` 时渲染该菜单项

#### Scenario: 超级管理员可见
- **WHEN** 超级管理员登录后访问后台
- **THEN** 侧边栏显示"测试数据"菜单项，点击跳转到测试数据管理页面

#### Scenario: 普通管理员不可见
- **WHEN** 普通管理员登录后访问后台
- **THEN** 侧边栏不显示"测试数据"菜单项；直接访问 `/admin/test-data` 时被路由守卫拦截并提示"无权限访问"

---

### Requirement: 测试数据管理前端页面
系统 SHALL 提供一个测试数据管理页面，包含以下要素：
- 顶部操作区：两个按钮"一键导入测试数据"（主按钮，绿色）、"一键移除测试数据"（危险按钮，红色）
- 中部统计卡片：展示当前各类测试数据的数量（用户、帖子、评论、社团、点赞、收藏、关注、私信会话、私信消息、通知）
- 操作结果区：导入/移除操作完成后，展示本次操作的各类数据数量明细
- 二次确认：点击导入或移除按钮时弹出 `el-message-box` 确认框，提示操作影响
- 加载状态：操作进行中显示 `v-loading` 遮罩，禁止重复点击

#### Scenario: 导入流程
- **WHEN** 超级管理员点击"一键导入测试数据"按钮
- **THEN** 弹出确认框"将向系统注入约 100 用户、500 帖子、1000 评论、10 社团等测试数据，是否继续？"
- **WHEN** 用户点击确认
- **THEN** 按钮禁用并显示加载中，调用导入接口，完成后刷新统计卡片并展示结果明细

#### Scenario: 移除流程
- **WHEN** 超级管理员点击"一键移除测试数据"按钮
- **THEN** 弹出确认框"将移除系统中所有以 test_ 开头的用户及其关联数据，操作不可恢复，是否继续？"
- **WHEN** 用户点击确认
- **THEN** 按钮禁用并显示加载中，调用移除接口，完成后刷新统计卡片并展示结果明细

---

### Requirement: 超级管理员权限校验
系统 SHALL 在前端路由守卫和后端接口双重校验超级管理员权限。
- 前端：路由 meta 新增 `requiresSuperAdmin: true`，守卫中检查 `userStore.isSuperAdmin`
- 后端：`AdminTestDataController` 类级或方法级使用 `@PreAuthorize("hasRole('SUPER_ADMIN')")`

#### Scenario: 普通管理员访问被拒
- **WHEN** 普通管理员（`ROLE_ADMIN`）尝试访问 `/api/admin/test-data/*` 接口
- **THEN** 后端返回 403 Forbidden

#### Scenario: 用户 Store 补充 isSuperAdmin getter
- **WHEN** 用户登录后，store 解析角色列表
- **THEN** `isSuperAdmin` 返回 `true` 当且仅当角色包含 `ROLE_SUPER_ADMIN`

## MODIFIED Requirements

### Requirement: 后台管理侧边栏菜单
在 [layout.vue](file:///e:/B_ProjectLibrary/CampusForumPlatform/campus-forum-frontend/src/views/admin/layout.vue) 的 `<el-menu>` 中新增一个仅超级管理员可见的菜单项"测试数据"，位于"社团审核"之后。
```vue
<el-menu-item v-if="userStore.isSuperAdmin" index="/admin/test-data">
  <el-icon><MagicStick /></el-icon>
  <span>测试数据</span>
</el-menu-item>
```

### Requirement: 前端路由守卫
在 [router/index.js](file:///e:/B_ProjectLibrary/CampusForumPlatform/campus-forum-frontend/src/router/index.js) 的全局前置守卫中，新增超级管理员权限校验分支：
```javascript
// 需要超级管理员权限但非超级管理员：提示并回到首页
if (to.meta.requiresSuperAdmin && !userStore.isSuperAdmin) {
  ElMessage.error('无权限访问')
  next('/')
  return
}
```

## REMOVED Requirements
无
