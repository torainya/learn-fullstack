# 后端镜像：多阶段构建
# 阶段 1：用 Maven + JDK 17 编译打包
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# 先单独拷贝 pom 下载依赖，利用 Docker 层缓存（依赖不变时不会重复下载）
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# 阶段 2：只保留 JRE 和 jar，镜像更小更安全
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
