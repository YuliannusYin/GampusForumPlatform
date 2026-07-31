# ========== 阶段1：构建阶段 ==========
# 使用 Maven + JDK21 镜像进行编译打包
FROM maven:3.9-eclipse-temurin-21 AS build

# 设置工作目录
WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层提前下载依赖，加速后续构建
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再复制源代码
COPY src ./src

# 执行打包，跳过测试
RUN mvn clean package -DskipTests


# ========== 阶段2：运行阶段 ==========
# 使用精简的 JRE 运行镜像，减小最终镜像体积
FROM eclipse-temurin:21-jre

# 设置时区为 Asia/Shanghai
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的 jar 文件
COPY --from=build /app/target/*.jar app.jar

# 创建文件上传目录
RUN mkdir -p /app/uploads

# 暴露后端服务端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
