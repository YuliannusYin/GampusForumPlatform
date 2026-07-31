# Checklist

## 工程结构与部署
- [x] 根目录包含 campus-forum-backend/、campus-forum-frontend/、docker/、README.md、.gitignore
- [x] 后端工程为 SpringBoot 3.2 + JDK21，pom.xml 依赖完整，主启动类可正常启动
- [x] 前端工程为 Vue3 + Vite5 + ElementPlus，npm install 与 npm run dev 可正常启动
- [x] git 仓库已初始化并配置远程仓库 GampusForumPlatform
- [x] 后端 Dockerfile 使用多阶段构建（Maven 打包 + JDK21 运行）
- [x] 前端 Dockerfile 使用 npm 构建并交由 Nginx 托管
- [x] docker-compose.yml 包含 mysql、redis、backend、frontend(nginx) 四个服务
- [x] Nginx 配置正确：/api/** 代理后端，根路径返回前端静态资源，/uploads 映射上传目录
- [x] MySQL 与 Redis 数据通过 volume 持久化
- [x] Windows11 与 Ubuntu 均可通过 docker compose up -d 一键启动

## 数据库
- [x] init.sql 包含全部 16 张表（user、role、user_role、section、post、tag、post_tag、comment、like_record、favorite、notification、chat_session、chat_message、sign_in_record、points_record、file_record）
- [x] 所有表使用 InnoDB 引擎、utf8mb4 字符集、BIGINT 自增主键
- [x] 初始数据包含管理员账号（BCrypt 加密）、默认板块、初始标签
- [x] MySQL 容器首次启动自动执行 init.sql

## 后端基础设施
- [x] 统一响应结构 Result<T>（code/message/data）应用到所有接口
- [x] 全局异常处理器覆盖业务异常、参数校验异常、授权异常
- [x] MyBatis-Plus 分页插件、自动填充（create_time/update_time）、逻辑删除已配置
- [x] Redis 配置完成，RedisTemplate 使用 JSON 序列化
- [x] 跨域配置允许前端访问
- [x] Knife4j(Swagger) 文档可访问并列出全部接口
- [x] JWT 工具类可生成、解析、刷新 Token
- [x] Spring Security + JWT 过滤器正确拦截需认证接口（未登录返回 401）
- [x] 权限注解对管理员接口生效（普通用户访问返回 403）

## 用户与认证
- [x] 注册接口校验用户名/邮箱唯一性，密码 BCrypt 加密，分配普通用户角色
- [x] 登录接口签发 Access Token（2h）与 Refresh Token（7天，存 Redis）
- [x] Token 刷新接口在 Refresh Token 有效时签发新 Access Token
- [x] 登出接口清除 Redis 中的 Refresh Token
- [x] 个人资料查询与修改接口正常
- [x] 修改密码接口正常
- [x] 我的发帖/收藏/点赞记录查询接口正常

## 板块与帖子
- [x] 管理员可增删改板块，普通用户可查询板块列表
- [x] 发帖接口支持 Markdown 正文、关联板块与标签
- [x] 编辑/删除帖子仅作者或管理员可操作（非作者返回 403）
- [x] 帖子列表分页正常，置顶帖优先，支持按时间/热度排序
- [x] 帖子详情接口浏览量 +1，返回完整信息

## 评论与回复
- [x] 发表评论成功并触发通知帖子作者
- [x] 支持多级回复（parent_id 关联）
- [x] 评论列表正确展示树形结构或分页子回复
- [x] 删除评论仅作者或管理员可操作

## 点赞与收藏
- [x] 点赞/取消点赞幂等（同一用户对同一对象仅一条记录）
- [x] 帖子与评论均可点赞，冗余计数同步更新
- [x] 收藏/取消收藏幂等
- [x] 点赞/收藏状态查询接口返回当前用户对对象的操作状态

## 标签
- [ ] 发帖时可选择已有标签或新建标签
- [x] 标签列表接口正常
- [x] 按标签检索帖子接口正常

## 文件上传
- [x] 上传接口校验文件类型（jpg/png/gif）与大小（≤5MB）
- [x] 文件保存到本地 uploads/ 目录并返回可访问 URL
- [x] Nginx 静态资源映射可访问上传的文件
- [x] file_record 记录上传元数据

## 搜索
- [x] 关键词搜索覆盖标题、正文、作者
- [x] 支持按板块、时间范围筛选与排序

## 消息通知
- [x] 评论、点赞、私信触发通知生成
- [x] 通知列表接口正常（分页）
- [x] 未读数量接口正常
- [x] 标记单条/全部已读接口正常

## WebSocket 私信
- [x] WebSocket(STOMP) 握手时通过 JWT 鉴权
- [x] 在线用户实时收到私信推送
- [x] 离线用户消息持久化，上线后可查询
- [x] 会话列表接口返回最新消息预览
- [x] 历史消息分页查询，按时间正序
- [x] 用户在线状态通过 Redis 维护

## 签到与积分
- [x] 每日签到防重复（当日再次签到被拒绝）
- [x] 连续签到奖励规则生效
- [x] 积分变更记录到 points_record
- [x] 用户等级根据积分自动提升
- [x] 签到状态与积分明细查询接口正常

## 后台管理
- [x] 管理员可分页查询用户、封禁/解禁、修改角色
- [x] 管理员可删除帖子、置顶、加精
- [x] 板块/标签管理 CRUD 正常
- [x] 普通用户访问管理接口返回 403

## 数据统计看板
- [x] 统计卡片展示总用户数、总帖子数、总评论数、今日新增
- [x] 近 7/30 天发帖趋势折线图数据接口正常
- [x] 板块帖子分布饼图数据接口正常

## 前端基础设施
- [x] Axios 封装 baseURL、Token 注入、401 自动跳登录
- [x] Pinia 用户 store 持久化 token 与用户信息
- [x] 路由守卫拦截需登录页面，登录后跳回来源页
- [ ] 全局 Layout 包含导航、未读消息徽标、用户菜单
- [x] ElementPlus 按需引入与全局样式配置完成

## 前端页面
- [x] 登录页表单校验与登录流程正常
- [x] 注册页表单校验与注册流程正常
- [ ] 首页展示板块导航、热门帖子、签到入口
- [x] 帖子列表分页、排序、筛选正常
- [x] 帖子详情页 Markdown 正确渲染，操作栏可用
- [x] 评论组件支持树形回复与发表评论
- [x] 发帖编辑页 md-editor-v3 正常，图片上传可用
- [x] 个人中心资料编辑、头像上传正常
- [ ] 我的发帖/收藏/点赞列表正常
- [x] 通知列表页已读/未读管理正常
- [x] 私信页 WebSocket 实时收发消息正常
- [x] 管理后台用户/帖子/板块/标签管理页正常
- [x] 数据看板 ECharts 图表正确渲染

## 联调与部署
- [x] 全功能走查通过（注册→登录→发帖→评论→点赞→私信→签到→后台管理）
- [x] 接口字段命名与错误处理统一
- [x] Windows11 docker compose up 四服务正常启动
- [x] Ubuntu 服务器部署正常，数据持久化与 init.sql 自动执行
- [x] 部署后冒烟测试核心流程通过
