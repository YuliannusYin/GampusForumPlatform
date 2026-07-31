# CampusForumPlatform 校园论坛与社区交流平台

## 项目简介

CampusForumPlatform 是一个面向高校师生的校园论坛与社区交流平台毕业设计项目。平台提供帖子发布与浏览、评论互动、点赞收藏、话题分区、用户中心、消息通知等核心功能，旨在为校园群体提供一个友好、高效、安全的线上交流空间。

项目采用前后端分离架构，后端基于 SpringBoot 3.2 + JDK 21 构建 RESTful API，前端基于 Vue3 + Vite5 构建单页应用，通过 Docker Compose 实现一键部署。

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
├── README.md                          # 项目说明文档
├── .gitignore                         # Git 忽略配置
├── campus-forum-backend/              # 后端工程
│   ├── pom.xml                        # Maven 依赖与构建配置
│   └── src/
│       ├── main/
│       │   ├── java/com/campus/forum/
│       │   │   ├── CampusForumApplication.java   # 主启动类
│       │   │   ├── controller/       # 控制器层
│       │   │   ├── service/          # 业务逻辑层
│       │   │   │   └── impl/         # 业务实现
│       │   │   ├── mapper/           # 数据访问层
│       │   │   ├── entity/           # 实体类
│       │   │   ├── dto/              # 数据传输对象
│       │   │   │   ├── req/          # 请求 DTO
│       │   │   │   └── resp/         # 响应 DTO
│       │   │   ├── config/           # 配置类
│       │   │   ├── common/           # 公共模块
│       │   │   │   ├── result/       # 统一返回结果
│       │   │   │   ├── exception/    # 全局异常处理
│       │   │   │   └── enums/        # 枚举类
│       │   │   ├── security/         # 安全认证模块
│       │   │   └── utils/            # 工具类
│       │   └── resources/
│       │       ├── application.yml          # 主配置文件
│       │       ├── application-dev.yml      # 开发环境配置
│       │       ├── application-prod.yml     # 生产环境配置
│       │       └── mapper/           # MyBatis XML 映射文件
│       └── test/                     # 测试代码
└── campus-forum-frontend/            # 前端工程
    ├── package.json                  # 依赖与脚本配置
    ├── vite.config.js                # Vite 构建配置
    ├── index.html                    # HTML 入口
    └── src/
        ├── main.js                   # 应用入口
        ├── App.vue                   # 根组件
        ├── router/                   # 路由配置
        ├── store/                    # Pinia 状态管理
        ├── api/                      # 接口请求封装
        ├── views/                    # 页面视图
        ├── components/               # 公共组件
        ├── layout/                   # 布局组件
        ├── utils/                    # 工具函数
        └── assets/
            └── styles/               # 全局样式
```

## 本地开发启动方式

### 前置环境要求

- JDK 21
- Maven 3.9+
- MySQL 8.0
- Redis 7
- Node.js 18+
- npm 或 pnpm

### 数据库准备

1. 创建数据库 `campus_forum`（字符集 utf8mb4）。
2. 修改 `campus-forum-backend/src/main/resources/application-dev.yml` 中的数据库连接信息（默认指向 localhost:3306）。

### 后端启动

```bash
cd campus-forum-backend
mvn spring-boot:run
```

后端服务默认启动在 `http://localhost:8080/api`，接口文档访问地址为 `http://localhost:8080/api/doc.html`。

### 前端启动

```bash
cd campus-forum-frontend
npm install
npm run dev
```

前端开发服务默认启动在 `http://localhost:5173`，已通过 Vite 代理将 `/api` 请求转发到后端 8080 端口。

## Docker 部署方式

### 环境变量准备

部署前需先准备环境变量文件。项目根目录提供了 `.env.docker` 模板（可提交到 git，不含真实密码），请先复制为 `.env` 再使用：

```bash
# Windows (PowerShell)
Copy-Item .env.docker .env

# Linux / macOS
cp .env.docker .env
```

> 注意：`.env` 文件包含敏感信息，已被 `.gitignore` 忽略，请勿提交到仓库。请根据实际生产环境修改其中的 `MYSQL_*` 与 `JWT_SECRET` 等配置项。docker compose 会自动加载同目录下的 `.env` 文件，无需手动指定。

### 构建与启动

使用 Docker Compose 一键编排后端、前端、MySQL、Redis 服务：

```bash
docker-compose up -d --build
```

### 服务说明

- **nginx**：前端静态资源服务与反向代理，暴露 80 端口
- **backend**：后端 SpringBoot 应用，暴露 8080 端口
- **mysql**：MySQL 8.0 数据库，暴露 3306 端口
- **redis**：Redis 7 缓存服务，暴露 6379 端口

### 生产环境配置

生产环境通过环境变量注入连接信息，参考 `application-prod.yml`，包括 `MYSQL_HOST`、`MYSQL_USER`、`MYSQL_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 等。
