# Ubuntu 服务器部署指南

本文档说明如何在 Ubuntu 服务器上部署 CampusForumPlatform 校园论坛平台，并完成部署后验证（对应 SubTask 24.2）。

## 一、服务器环境要求

| 项目 | 最低版本 | 说明 |
| --- | --- | --- |
| Ubuntu | 20.04 LTS / 22.04 LTS / 24.04 LTS | 推荐 22.04 LTS |
| Docker Engine | 24.0+ | 通过官方仓库安装 |
| Docker Compose | v2.20+（plugin 形式） | `docker compose` 子命令 |
| 内存 | 2 GB | 推荐 4 GB（构建镜像阶段较吃内存） |
| 磁盘 | 10 GB 可用 | 镜像 + 数据卷 + 上传文件 |
| 端口 | 80 / 8080 / 3306 / 6379 | 需对外开放或内网可达 |

> 构建后端镜像时 Maven 会下载依赖，首次构建建议预留 4 GB 以上内存与 5 分钟时间。

## 二、安装 Docker 与 Docker Compose

依次执行以下命令（官方一键脚本，适用于 Ubuntu 20.04+）：

```bash
# 1. 更新 apt 索引
sudo apt-get update

# 2. 安装依赖
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# 3. 添加 Docker 官方 GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# 4. 添加 Docker apt 仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. 安装 Docker Engine + Compose plugin
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 6. 将当前用户加入 docker 组（免 sudo 调用 docker）
sudo usermod -aG docker $USER
newgrp docker

# 7. 验证
docker --version
docker compose version
```

> 国内服务器可改用阿里云/DaoCloud 镜像源加速镜像拉取，参考各厂商文档配置 `/etc/docker/daemon.json` 的 `registry-mirrors` 字段。

## 三、获取项目代码

```bash
# 方式一：从 GitHub 克隆
git clone https://github.com/<your-account>/GampusForumPlatform.git
cd GampusForumPlatform

# 方式二：上传本地代码到服务器
# 在本地打包（排除 node_modules / target / .git）后通过 scp/rsync 上传
```

## 四、配置环境变量

```bash
cp .env.docker .env
```

编辑 `.env`，**务必修改默认密码与 JWT 密钥**：

```bash
vi .env
```

建议修改为强随机值，例如：

```dotenv
MYSQL_ROOT_PASSWORD=<强随机密码>
MYSQL_DATABASE=campus_forum
MYSQL_USER=campus
MYSQL_PASSWORD=<强随机密码>
JWT_SECRET=<至少 32 字符的随机字符串>
JWT_ACCESS_TOKEN_EXPIRE=7200
JWT_REFRESH_TOKEN_EXPIRE=604800
```

> `.env` 已在 `.gitignore` 中忽略，不会进入版本库。

## 五、防火墙配置

```bash
# 放行 Web 与 API 端口（对外只需 80）
sudo ufw allow 80/tcp
sudo ufw allow 22/tcp
sudo ufw enable

# 如需外部直连数据库/缓存（不推荐生产环境开放），按需放行：
# sudo ufw allow 3306/tcp
# sudo ufw allow 6379/tcp
# sudo ufw allow 8080/tcp
```

## 六、构建与启动

```bash
docker compose up -d --build
```

首次执行会：
1. 拉取 `mysql:8.0`、`redis:7-alpine`、`maven:3.9-eclipse-temurin-21`、`node:20-alpine`、`nginx:stable-alpine`、`eclipse-temurin:21-jre` 基础镜像
2. 多阶段构建后端 jar 与前端静态资源
3. 启动四个容器并按依赖顺序等待健康检查通过

启动完成后查看状态：

```bash
docker compose ps
```

预期输出（四个服务均为 `Up`，mysql/redis 为 `healthy`）：

```
NAME                   STATUS                   PORTS
campus-forum-frontend  Up X minutes             0.0.0.0:80->80/tcp
campus-forum-backend   Up X minutes             0.0.0.0:8080->8080/tcp
campus-forum-redis     Up X minutes (healthy)   0.0.0.0:6379->6379/tcp
campus-forum-mysql     Up X minutes (healthy)   0.0.0.0:3306->3306/tcp
```

## 七、部署后验证（SubTask 24.2 验收项）

### 7.1 init.sql 自动执行验证

```bash
docker exec campus-forum-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "
USE campus_forum;
SHOW TABLES;
SELECT COUNT(*) AS section_count FROM section;
SELECT COUNT(*) AS tag_count FROM tag;
SELECT COUNT(*) AS role_count FROM role;
SELECT id, username, nickname FROM user;
"
```

预期：
- 16 张表（user / role / user_role / section / post / tag / post_tag / comment / like_record / favorite / notification / chat_session / chat_message / sign_in_record / points_record / file_record）
- `section_count = 5`（校园生活、学习交流、二手交易、失物招领、表白墙）
- `tag_count = 5`（求助、分享、讨论、公告、经验）
- `role_count = 2`（ROLE_USER、ROLE_ADMIN）
- 存在 `admin` 用户（id=1）

### 7.2 数据持久化验证

```bash
# 1. 查看命名卷
docker volume ls | grep campusforumplatform

# 2. 重启 MySQL 容器
docker compose restart mysql

# 3. 等待健康检查通过后，再次查询数据
docker compose ps   # 等待 mysql 状态变为 (healthy)
docker exec campus-forum-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
  -e "USE campus_forum; SELECT COUNT(*) FROM section;"
```

预期：重启后 `section` 表数据仍在（= 5），证明 `mysql_data` 卷持久化生效。

### 7.3 冒烟测试（核心接口）

```bash
# 1. 前端首页可访问
curl -I http://localhost/
# 预期：HTTP/1.1 200 OK

# 2. 板块列表（公开接口）
curl -s http://localhost/api/sections
# 预期：返回 5 个板块的 JSON

# 3. 帖子列表（公开接口）
curl -s "http://localhost/api/posts?page=1&size=10"
# 预期：返回分页结构 records/total/page/size

# 4. 管理员登录
TOKEN=$(curl -s -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"admin123"}' \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo "AccessToken: $TOKEN"
# 预期：打印一串 JWT

# 5. 鉴权接口（带 Token）
curl -s -H "Authorization: Bearer $TOKEN" http://localhost/api/user/profile
# 预期：返回 admin 用户信息

# 6. 未登录访问受保护接口
curl -s -o /dev/null -w "%{http_code}\n" http://localhost/api/user/profile
# 预期：返回 401（body code=401）

# 7. 接口文档
curl -I http://localhost/api/doc.html
# 预期：HTTP/1.1 200 OK
```

> 安全建议：冒烟测试通过后，建议修改 admin 默认密码，或在生产环境通过 `UPDATE user SET password=...` 替换为强密码的 BCrypt 哈希。

## 八、常用运维操作

```bash
# 查看实时日志
docker compose logs -f backend
docker compose logs -f mysql
docker compose logs -f frontend

# 重启单个服务
docker compose restart backend

# 停止全部服务（保留数据卷）
docker compose down

# 停止并删除数据卷（⚠️ 会清空数据库与 Redis，仅在重置时使用）
docker compose down -v

# 更新代码后重新构建并启动
git pull
docker compose up -d --build

# 备份 MySQL 数据
docker exec campus-forum-mysql mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" \
  campus_forum > backup_$(date +%Y%m%d).sql

# 恢复 MySQL 数据
docker exec -i campus-forum-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" \
  campus_forum < backup_YYYYMMDD.sql
```

## 九、目录权限说明

`docker-compose.yml` 将宿主机 `./uploads` 目录挂载到 backend（读写）与 frontend/nginx（只读）容器。首次启动前创建该目录并保证 backend 容器可写：

```bash
mkdir -p uploads
# backend 容器以 root 运行，默认可写；如改为非 root 运行需：
# sudo chown -R 1000:1000 uploads
```

## 十、常见问题排查

### 1. 端口被占用

```bash
sudo lsof -i :80
sudo lsof -i :3306
# 杀掉占用进程或修改 docker-compose.yml 端口映射
```

### 2. 后端启动失败：连接数据库超时

```bash
# 检查 mysql 容器是否健康
docker compose ps
# 查看 mysql 日志
docker compose logs mysql
# 确认 backend 环境变量 MYSQL_HOST=mysql（容器名）已注入
docker compose exec backend env | grep MYSQL
```

### 3. 镜像拉取超时

配置国内镜像加速：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com"
  ]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 4. 中文乱码

JDBC URL 已包含 `characterEncoding=UTF-8`，MySQL 容器启动参数已设置 `--character-set-server=utf8mb4`。如仍乱码，检查客户端连接是否执行了 `SET NAMES utf8mb4`。

### 5. 前端访问 502 Bad Gateway

后端尚未就绪，nginx 代理失败。查看后端日志：

```bash
docker compose logs --tail=100 backend
```

等待 `Started CampusForumApplication` 日志出现后重试。
