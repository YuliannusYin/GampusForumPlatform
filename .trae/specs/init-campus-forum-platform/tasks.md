# Tasks

## 阶段一：项目初始化与基础设施
- [ ] Task 1: 初始化项目工程结构
  - [ ] SubTask 1.1: 创建根目录 README.md（项目说明、技术栈、启动方式）与 .gitignore
  - [ ] SubTask 1.2: 初始化 git 仓库并配置远程仓库 GampusForumPlatform
  - [ ] SubTask 1.3: 搭建后端 SpringBoot 3.2 + JDK21 工程骨架（campus-forum-backend，pom.xml，主启动类，application.yml）
  - [ ] SubTask 1.4: 搭建前端 Vue3 + Vite5 + ElementPlus 工程骨架（campus-forum-frontend，package.json，vite.config.js，main.js）
- [ ] Task 2: 搭建 Docker Compose 部署环境
  - [ ] SubTask 2.1: 编写后端 Dockerfile（多阶段构建：Maven 打包 + JDK21 运行）
  - [ ] SubTask 2.2: 编写前端 Dockerfile（npm 构建 + Nginx 托管静态资源）
  - [ ] SubTask 2.3: 编写 docker-compose.yml（mysql、redis、backend、frontend/nginx 四服务，volume 持久化）
  - [ ] SubTask 2.4: 编写 Nginx 配置（/api 代理后端、静态资源、uploads 路径映射）
  - [ ] SubTask 2.5: 编写 .env.docker 环境变量文件模板
- [ ] Task 3: 数据库设计与初始化脚本
  - [ ] SubTask 3.1: 设计全部表结构（user、role、user_role、section、post、tag、post_tag、comment、like_record、favorite、notification、chat_session、chat_message、sign_in_record、points_record、file_record）
  - [ ] SubTask 3.2: 编写 docker/init.sql 建表脚本（InnoDB + utf8mb4，BIGINT 自增主键）
  - [ ] SubTask 3.3: 编写初始数据脚本（管理员账号 BCrypt 密码、默认板块、初始标签）
  - [ ] SubTask 3.4: 配置 MySQL 容器启动自动执行 init.sql

## 阶段二：后端通用基础设施
- [ ] Task 4: 后端通用基础设施
  - [ ] SubTask 4.1: 统一响应结构 Result<T> 与业务码枚举
  - [ ] SubTask 4.2: 全局异常处理器（业务异常、参数校验异常、授权异常）
  - [ ] SubTask 4.3: MyBatis-Plus 配置（分页插件、自动填充、逻辑删除）
  - [ ] SubTask 4.4: Redis 配置与 RedisTemplate 序列化
  - [ ] SubTask 4.5: 跨域配置与 Knife4j(Swagger) 文档集成
  - [ ] SubTask 4.6: JWT 工具类（生成、解析、刷新，jjwt 库）
  - [ ] SubTask 4.7: Spring Security 配置 + JWT 认证过滤器 + 权限注解

## 阶段三：后端核心业务模块
- [ ] Task 5: 用户与认证模块
  - [ ] SubTask 5.1: User/Role/UserRole 实体、Mapper、Service
  - [ ] SubTask 5.2: 注册接口（用户名/邮箱唯一校验、BCrypt 加密、分配普通用户角色）
  - [ ] SubTask 5.3: 登录接口（校验、签发双 Token、Refresh Token 存 Redis）
  - [ ] SubTask 5.4: Token 刷新接口与登出接口（清除 Redis Token）
  - [ ] SubTask 5.5: 个人资料查询与修改接口、修改密码接口
  - [ ] SubTask 5.6: 我的发帖/收藏/点赞记录查询接口
- [ ] Task 6: 板块与帖子模块
  - [ ] SubTask 6.1: Section 实体与 CRUD（管理员增删改、用户查询）
  - [ ] SubTask 6.2: Post 实体与 Mapper（含浏览量、点赞数、评论数冗余字段）
  - [ ] SubTask 6.3: 发帖接口（关联板块、标签，Markdown 正文）
  - [ ] SubTask 6.4: 编辑/删除帖子接口（作者或管理员权限校验）
  - [ ] SubTask 6.5: 帖子列表分页接口（按板块、按时间/热度排序，置顶优先）
  - [ ] SubTask 6.6: 帖子详情接口（浏览量 +1，返回作者、板块、标签、统计数）
- [ ] Task 7: 评论与回复模块
  - [ ] SubTask 7.1: Comment 实体（支持 parent_id 多级回复）
  - [ ] SubTask 7.2: 发表评论/回复接口（触发通知帖子作者）
  - [ ] SubTask 7.3: 评论列表接口（树形/分页加载子回复）
  - [ ] SubTask 7.4: 删除评论接口（作者或管理员）
- [ ] Task 8: 点赞与收藏模块
  - [ ] SubTask 8.1: like_record、favorite 实体与唯一索引（user_id+target_id+type）
  - [ ] SubTask 8.2: 点赞/取消点赞接口（帖子、评论，幂等，更新冗余计数）
  - [ ] SubTask 8.3: 收藏/取消收藏接口（幂等）
  - [ ] SubTask 8.4: 点赞/收藏状态查询接口
- [ ] Task 9: 标签模块
  - [ ] SubTask 9.1: Tag、PostTag 实体与 Mapper
  - [ ] SubTask 9.2: 发帖时新建/关联标签逻辑
  - [ ] SubTask 9.3: 标签列表与按标签检索帖子接口
- [ ] Task 10: 文件上传模块
  - [ ] SubTask 10.1: 文件上传接口（校验类型与大小，存本地 uploads/ 目录）
  - [ ] SubTask 10.2: 文件访问 URL 生成（与 Nginx 静态映射对齐）
  - [ ] SubTask 10.3: file_record 记录上传元数据
- [ ] Task 11: 搜索模块
  - [ ] SubTask 11.1: 帖子关键词搜索接口（标题/正文/作者 LIKE 查询）
  - [ ] SubTask 11.2: 支持按板块、时间范围筛选与排序

## 阶段四：后端扩展功能模块
- [ ] Task 12: 消息通知模块
  - [ ] SubTask 12.1: Notification 实体与 Mapper
  - [ ] SubTask 12.2: 通知生成服务（评论、点赞、私信触发）
  - [ ] SubTask 12.3: 通知列表、未读数量、标记已读接口
- [ ] Task 13: WebSocket 实时私信模块
  - [ ] SubTask 13.1: WebSocket(STOMP) 配置与握手拦截器（JWT 鉴权）
  - [ ] SubTask 13.2: chat_session、chat_message 实体与 Mapper
  - [ ] SubTask 13.3: 发送私信接口（在线推送 + 离线持久化）
  - [ ] SubTask 13.4: 会话列表与历史消息分页查询接口
  - [ ] SubTask 13.5: 用户在线状态维护（Redis）
- [ ] Task 14: 签到与积分模块
  - [ ] SubTask 14.1: sign_in_record、points_record 实体与 Mapper
  - [ ] SubTask 14.2: 每日签到接口（防重复、连续签到奖励规则）
  - [ ] SubTask 14.3: 积分变更服务与用户等级计算
  - [ ] SubTask 14.4: 签到状态、积分明细查询接口
- [ ] Task 15: 后台管理与统计模块
  - [ ] SubTask 15.1: 管理员用户管理接口（分页、封禁/解禁、角色修改）
  - [ ] SubTask 15.2: 管理员帖子管理接口（删除、置顶、加精）
  - [ ] SubTask 15.3: 板块/标签管理接口
  - [ ] SubTask 15.4: 数据统计接口（总量卡片、近 N 天发帖趋势、板块分布）

## 阶段五：前端基础设施与认证
- [ ] Task 16: 前端通用基础设施
  - [ ] SubTask 16.1: Axios 封装（baseURL、请求/响应拦截、Token 注入、401 跳登录）
  - [ ] SubTask 16.2: Pinia 用户 store（token、用户信息、登录态持久化）
  - [ ] SubTask 16.3: Vue Router 路由表与全局前置守卫（需登录页拦截）
  - [ ] SubTask 16.4: 全局 Layout 组件（顶部导航、侧边栏、未读消息徽标、用户菜单）
  - [ ] SubTask 16.5: ElementPlus 按需引入与主题配置、全局样式
- [ ] Task 17: 登录注册页面
  - [ ] SubTask 17.1: 登录页（账号密码、表单校验、登录提交、Token 存储）
  - [ ] SubTask 17.2: 注册页（用户名/邮箱/密码、校验、注册提交）

## 阶段六：前端业务页面
- [ ] Task 18: 首页与帖子列表页
  - [ ] SubTask 18.1: 首页布局（板块导航、热门帖子、签到入口）
  - [ ] SubTask 18.2: 帖子列表组件（分页、排序切换、板块/标签筛选）
  - [ ] SubTask 18.3: 帖子卡片组件（标题、作者、统计信息、标签）
- [ ] Task 19: 帖子详情与发帖编辑页
  - [ ] SubTask 19.1: 帖子详情页（Markdown 渲染、作者信息、统计、操作栏）
  - [ ] SubTask 19.2: 评论列表组件（树形回复、发表评论、点赞）
  - [ ] SubTask 19.3: 发帖/编辑页（md-editor-v3 编辑器、板块选择、标签选择、图片上传）
- [ ] Task 20: 个人中心页
  - [ ] SubTask 20.1: 个人资料页（头像上传、资料编辑、修改密码）
  - [ ] SubTask 20.2: 我的发帖/收藏/点赞 Tab 列表
  - [ ] SubTask 20.3: 我的积分与签到记录展示
- [ ] Task 21: 消息中心与私信页
  - [ ] SubTask 21.1: 通知列表页（分类 Tab、已读/未读、标记已读）
  - [ ] SubTask 21.2: 私信会话列表与聊天窗口（WebSocket 连接、实时收发）
- [ ] Task 22: 管理后台页面
  - [ ] SubTask 22.1: 后台 Layout 与侧边菜单
  - [ ] SubTask 22.2: 用户管理页（表格、封禁/解禁、角色修改）
  - [ ] SubTask 22.3: 帖子管理页（表格、删除、置顶、加精）
  - [ ] SubTask 22.4: 板块/标签管理页（增删改查）
  - [ ] SubTask 22.5: 数据看板页（统计卡片 + ECharts 折线图/饼图）

## 阶段七：联调与部署验证
- [ ] Task 23: 前后端联调
  - [ ] SubTask 23.1: 全功能走查（注册登录→发帖→评论→点赞→私信→签到→后台）
  - [ ] SubTask 23.2: 修复联调缺陷，统一接口字段命名与错误处理
- [ ] Task 24: Docker 部署验证
  - [ ] SubTask 24.1: Windows11 本地 docker compose up 验证四服务正常
  - [ ] SubTask 24.2: Ubuntu 服务器部署验证（含数据持久化、init.sql 自动执行）
  - [ ] SubTask 24.3: 部署后冒烟测试（核心流程通过）

# Task Dependencies
- Task 2 依赖 Task 1（需要工程骨架才能写 Dockerfile）
- Task 3 可与 Task 2 并行
- Task 4 依赖 Task 1（后端骨架）
- Task 5 依赖 Task 4（需要 JWT、Security 基础设施）
- Task 6/7/8/9/10/11 依赖 Task 4，可在 Task 5 后并行推进
- Task 12/13/14/15 依赖核心模块（Task 6/7/8）完成
- Task 16 依赖 Task 1（前端骨架），可与后端任务并行
- Task 17 依赖 Task 16
- Task 18-22 依赖 Task 16/17 与对应后端模块
- Task 23 依赖前后端各模块基本完成
- Task 24 依赖 Task 23
