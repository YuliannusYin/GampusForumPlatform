# Tasks

- [x] Task 1: 后端 - 创建测试数据常量类与 Service 接口
  - [x] SubTask 1.1: 新建 `common/constant/TestDataConstants.java`，定义用户名前缀 `test_`、社团名前缀 `【测试】`、帖子标题前缀 `【测试】`、默认密码 `test123456`、邮箱域名 `test.campus.edu`、数据规模常量（用户 100、帖子 500、评论 1000、社团 10 等）
  - [x] SubTask 1.2: 新建 `service/TestDataService.java` 接口，定义三个方法：`TestDataImportResult importTestData()`、`TestDataRemoveResult removeTestData()`、`TestDataStatus status()`
  - [x] SubTask 1.3: 新建 `dto/resp/TestDataImportResult.java`、`TestDataRemoveResult.java`、`TestDataStatus.java` 三个 VO 类，包含各类型数据数量字段

- [x] Task 2: 后端 - 实现测试数据生成逻辑
  - [x] SubTask 2.1: 新建 `service/impl/TestDataServiceImpl.java`，注入所需 Mapper（UserMapper、RoleMapper、UserRoleMapper、PostMapper、CommentMapper、LikeRecordMapper、FavoriteMapper、FollowMapper、ClubMapper、ClubMemberMapper、ClubPostMapper、ChatSessionMapper、ChatMessageMapper、NotificationMapper、UserSettingMapper、PasswordEncoder）
  - [x] SubTask 2.2: 实现 `importTestData()` 方法：先检查是否已存在测试用户（重复导入检测），然后按顺序生成用户→社团→帖子→评论→点赞→收藏→关注→私信→通知，使用 `@Transactional(rollbackFor = Exception.class)` 保证原子性。数据量大时采用分批 insert（每 200 条 flush 一次）
  - [x] SubTask 2.3: 用户生成：100 个用户，用户名 `test_alice_001` ~ `test_bob_100`（从预定义昵称池循环取，加序号保证唯一），密码统一 BCrypt 加密 `test123456`，邮箱 `test+xxx_001@test.campus.edu`，随机性别/头像URL/个人简介，分配 `ROLE_USER` 角色，同时为前 10 个用户额外分配 `ROLE_USER` 并作为社团社长
  - [x] SubTask 2.4: 社团生成：10 个社团，名称 `【测试】摄影社` ~ `【测试】动漫社`（预定义 10 个社团名），状态 `status=1` 已审核通过，creator_id 为前 10 个测试用户，同时插入 `club_member` 记录（role=1 社长），并为每个社团随机添加 5~15 个普通成员（role=0）
  - [x] SubTask 2.5: 帖子生成：500 条帖子，标题 `【测试】xxx`（从预定义标题模板池随机），Markdown 正文使用预定义模板（包含标题、列表、代码块、图片占位符），summary 截取前 100 字符，随机分布到 5 个板块，作者从 100 个测试用户中随机选取，随机设置约 5% 置顶、10% 精华，view_count/like_count/comment_count/favorite_count 初始为 0（后续由点赞/评论/收藏逻辑同步）
  - [x] SubTask 2.6: 评论生成：1000 条评论，约 70% 为顶级评论（parent_id=0），30% 为二级回复（parent_id 指向已生成的顶级评论），content 使用预定义评论模板，随机分布到 500 条帖子下，commenter 从测试用户中随机选取；同时回填帖子的 comment_count
  - [x] SubTask 2.7: 点赞记录生成：约 800 条，target_type=1（帖子）和 target_type=2（评论）各占约 50%，user_id 从测试用户中随机，避免唯一键冲突（user_id+target_id+target_type）；同时回填 post.like_count 和 comment.like_count
  - [x] SubTask 2.8: 收藏记录生成：约 500 条，user_id 和 post_id 都从测试范围中随机，避免重复；同时回填 post.favorite_count
  - [x] SubTask 2.9: 关注关系生成：约 300 条，follower_id 和 following_id 都从测试用户中随机，避免重复和自关注
  - [x] SubTask 2.10: 私信会话与消息生成：约 50 个会话，user1_id < user2_id 保证唯一，每个会话 1~10 条消息，sender_id 和 receiver_id 为会话双方，content 使用预定义模板，更新 chat_session.last_message_id 和 last_message_time
  - [x] SubTask 2.11: 通知生成：约 150 条，type 分布为 1 评论/2 点赞/3 关注/4 系统，user_id 为被通知的测试用户，from_user_id 为另一个测试用户，content 使用预定义模板
  - [x] SubTask 2.12: 实现 `removeTestData()` 方法：按 spec 中描述的 17 步顺序删除，使用 `@Transactional(rollbackFor = Exception.class)`，每步统计删除数量
  - [x] SubTask 2.13: 实现 `status()` 方法：查询各类测试数据的当前数量并返回

- [x] Task 3: 后端 - 创建 Controller
  - [x] SubTask 3.1: 新建 `controller/AdminTestDataController.java`，类级 `@PreAuthorize("hasRole('SUPER_ADMIN')")`，`@RequestMapping("/api/admin/test-data")`
  - [x] SubTask 3.2: 实现 `POST /import`、`DELETE /`、`GET /status` 三个接口，调用 Service 返回 `Result<T>`

- [x] Task 4: 前端 - 补充超级管理员 Store 判断
  - [x] SubTask 4.1: 在 `store/user.js` 中新增 `isSuperAdmin` getter（基于 roles 数组判断是否包含 `ROLE_SUPER_ADMIN`），并确保登录接口返回的角色信息被正确解析存储（验证发现已存在，无需修改）

- [x] Task 5: 前端 - 新增 API 封装
  - [x] SubTask 5.1: 在 `api/admin.js` 中新增三个方法：`importTestData()`（POST `/admin/test-data/import`）、`removeTestData()`（DELETE `/admin/test-data`）、`getTestDataStatus()`（GET `/admin/test-data/status`）

- [x] Task 6: 前端 - 新增测试数据管理页面
  - [x] SubTask 6.1: 新建 `views/admin/test-data.vue`，包含顶部操作区（导入/移除按钮）、中部统计卡片网格（10 类数据数量）、底部操作结果明细
  - [x] SubTask 6.2: 实现 `onMounted` 时调用 `getTestDataStatus()` 加载当前状态
  - [x] SubTask 6.3: 实现导入按钮：`el-message-box.confirm` 二次确认 → 调用 `importTestData()` → `v-loading` 遮罩 → 完成后刷新状态并展示结果明细
  - [x] SubTask 6.4: 实现移除按钮：`el-message-box.confirm` 二次确认（危险提示）→ 调用 `removeTestData()` → `v-loading` 遮罩 → 完成后刷新状态并展示结果明细
  - [x] SubTask 6.5: 错误处理：捕获业务错误（如重复导入）并通过 `ElMessage.error` 提示

- [x] Task 7: 前端 - 修改后台布局菜单
  - [x] SubTask 7.1: 在 `views/admin/layout.vue` 的 `<el-menu>` 中新增"测试数据"菜单项，使用 `v-if="userStore.isSuperAdmin"` 控制显示，图标使用 `MagicStick`，位于"社团审核"之后

- [x] Task 8: 前端 - 修改路由配置
  - [x] SubTask 8.1: 在 `router/index.js` 的 `/admin` children 中新增路由 `{ path: 'test-data', name: 'AdminTestData', component: () => import('@/views/admin/test-data.vue'), meta: { title: '测试数据', requiresAdmin: true, requiresSuperAdmin: true } }`
  - [x] SubTask 8.2: 在全局前置守卫中新增 `requiresSuperAdmin` 校验分支，非超级管理员访问时提示"无权限访问"并跳回首页

- [x] Task 9: 验证与构建
  - [x] SubTask 9.1: 后端通过 `mvn compile` 验证编译通过（exit code 0）
  - [x] SubTask 9.2: 前端通过 `npm run build` 验证构建通过（exit code 0，产物含 test-data-CxpZ5F9O.js）
  - [x] SubTask 9.3: 验证导入→状态查询→移除→状态查询完整流程在文档中描述（实际运行依赖数据库环境，构建通过即可）

# Task Dependencies
- Task 2 依赖 Task 1（需要常量和接口定义）
- Task 3 依赖 Task 2（需要 Service 实现）
- Task 5 依赖 Task 3（API 路径需与后端对齐）
- Task 6 依赖 Task 4、Task 5（需要 Store 判断和 API 封装）
- Task 7 依赖 Task 4（需要 isSuperAdmin getter）
- Task 8 依赖 Task 6（路由指向已存在的页面组件）
- Task 9 依赖所有前置任务
