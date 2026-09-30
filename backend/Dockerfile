# ---------- 第一阶段：构建 ----------

# i.e. 我要在一个装好 Gradle+JDK21 的环境里开始干活，这个环境我给它取名叫 builder
FROM gradle:9.5.1-jdk21 AS builder

# “容器”内部新建的目录，与本机 backend/ 路径无关，之后的 COPY/RUN 都基于这里
WORKDIR /app

# 先只拷贝跟依赖相关的文件，利用 Docker 层缓存：
# 如果源码改了但依赖没变，下面这层可以复用缓存，不用重新下载依赖
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./

# 再拷贝真正的源码
COPY src ./src

# 打包成 jar，跳过测试（构建镜像阶段没有数据库可连）
RUN ./gradlew build -x test --no-daemon

# ---------- 第二阶段：运行 ----------

# 这里只需要JRE，21为了跟build.gradle.kts里的java.toolchain一致，alphine基于 Alpine Linux（一个极简的 Linux 发行版，体积比 Ubuntu/Debian 小很多，专门为了让最终镜像更小）
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 只从 builder 阶段拿打包好的 jar，不带 Gradle/JDK 等构建工具
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
