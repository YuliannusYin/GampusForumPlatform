# ========== 阶段1：构建阶段 ==========
# 使用 Node 20 alpine 镜像进行前端构建
FROM node:20-alpine AS build

# 设置工作目录
WORKDIR /app

# 先复制 package.json 和 package-lock.json，利用 Docker 缓存层加速依赖安装
COPY package*.json ./

# 使用淘宝镜像源安装依赖
RUN npm install --registry=https://registry.npmmirror.com

# 复制项目所有源代码
COPY . .

# 执行生产构建
RUN npm run build


# ========== 阶段2：运行阶段 ==========
# 使用 nginx alpine 镜像托管静态资源
FROM nginx:stable-alpine

# 从构建阶段复制产物到 nginx 静态资源目录
COPY --from=build /app/dist /usr/share/nginx/html

# 暴露 80 端口
EXPOSE 80

# 使用 nginx 默认启动命令
CMD ["nginx", "-g", "daemon off;"]
