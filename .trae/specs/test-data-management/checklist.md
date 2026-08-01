# Checklist

## 后端实现验证
- [x] `TestDataConstants.java` 已创建，包含用户名前缀 `test_`、社团名前缀 `【测试】`、帖子标题前缀 `【测试】`、默认密码 `test123456`、邮箱域名 `test.campus.edu`、各数据规模常量
- [x] `TestDataService.java` 接口已创建，包含 `importTestData()`、`removeTestData()`、`status()` 三个方法
- [x] `TestDataImportResult.java`、`TestDataRemoveResult.java`、`TestDataStatus.java` 三个 VO 已创建，包含各类型数据数量字段
- [x] `TestDataServiceImpl.java` 已实现，注入所有需要的 Mapper 和 PasswordEncoder
- [x] `importTestData()` 实现重复导入检测：数据库中已存在 `test_` 前缀用户时抛出 `BusinessException`
- [x] `importTestData()` 使用 `@Transactional(rollbackFor = Exception.class)` 保证原子性
- [x] `importTestData()` 生成顺序为：用户→社团→帖子→评论→点赞→收藏→关注→私信→通知
- [x] 测试用户密码统一使用 BCrypt 加密的 `test123456`
- [x] 测试用户邮箱统一使用 `test+xxx@test.campus.edu` 格式，避免与真实邮箱冲突
- [x] 测试用户名、社团名、帖子标题均带规定前缀
- [x] 帖子的 view_count/like_count/comment_count/favorite_count 在生成点赞/评论/收藏后被正确回填
- [x] 评论的 like_count 在生成点赞后被正确回填
- [x] 社团的 member_count 在生成成员后被正确回填
- [x] chat_session 的 last_message_id 和 last_message_time 在生成消息后被正确回填
- [x] `removeTestData()` 按 spec 中描述的 17 步顺序删除，使用事务保证原子性
- [x] `removeTestData()` 仅删除测试数据，不删除真实数据（基于 `test_` 用户名前缀和 `【测试】` 社团名前缀联动）
- [x] `removeTestData()` 无测试数据时返回各类数量为 0，不抛出错误
- [x] `status()` 方法正确查询并返回各类测试数据的当前数量
- [x] `AdminTestDataController.java` 已创建，类级 `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- [x] Controller 路径为 `/api/admin/test-data`，提供 `POST /import`、`DELETE /`、`GET /status` 三个接口
- [x] 所有接口返回 `Result<T>` 统一响应结构

## 前端实现验证
- [x] `store/user.js` 已新增 `isSuperAdmin` getter，正确判断角色包含 `ROLE_SUPER_ADMIN`
- [x] `api/admin.js` 已新增 `importTestData()`、`removeTestData()`、`getTestDataStatus()` 三个方法
- [x] `views/admin/test-data.vue` 已创建，包含顶部操作区、统计卡片、操作结果明细三个区域
- [x] 页面 `onMounted` 时调用 `getTestDataStatus()` 加载当前状态
- [x] 导入按钮点击后弹出 `el-message-box.confirm` 二次确认，确认后调用 `importTestData()`
- [x] 移除按钮点击后弹出 `el-message-box.confirm` 二次确认（危险提示），确认后调用 `removeTestData()`
- [x] 操作进行中显示 `v-loading` 遮罩，按钮禁用防止重复点击
- [x] 操作完成后刷新统计卡片并展示结果明细
- [x] 业务错误（如重复导入）通过 `ElMessage.error` 提示，不崩溃页面
- [x] `views/admin/layout.vue` 已新增"测试数据"菜单项，使用 `v-if="userStore.isSuperAdmin"` 控制显示
- [x] 菜单项位于"社团审核"之后，图标使用 `MagicStick`
- [x] `router/index.js` 已新增 `/admin/test-data` 路由，meta 包含 `requiresAdmin: true` 和 `requiresSuperAdmin: true`
- [x] 路由守卫已新增 `requiresSuperAdmin` 校验分支，非超级管理员访问时提示"无权限访问"并跳回首页

## 权限与安全验证
- [x] 后端 `AdminTestDataController` 类级或方法级使用 `@PreAuthorize("hasRole('SUPER_ADMIN')")`
- [x] 普通管理员（`ROLE_ADMIN`）访问 `/api/admin/test-data/*` 返回 403（由 Spring Security `@PreAuthorize` 拦截）
- [x] 普通用户访问 `/api/admin/test-data/*` 返回 403（由 Spring Security `@PreAuthorize` 拦截）
- [x] 未登录访问 `/api/admin/test-data/*` 返回 401（由 JwtAuthenticationEntryPoint 处理）
- [x] 前端普通管理员看不到"测试数据"菜单项（`v-if="userStore.isSuperAdmin"` 控制渲染）
- [x] 前端普通管理员直接访问 `/admin/test-data` 被路由守卫拦截（`requiresSuperAdmin` 校验）

## 构建验证
- [x] 后端 `mvn compile` 编译通过，无错误（exit code 0）
- [x] 前端 `npm run build` 构建通过，无错误（exit code 0，产物含 test-data-CxpZ5F9O.js 4.79 kB）
- [x] 无未使用的 import、无 TypeScript/ESLint 警告（针对新增文件）
