# 考勤后端镜像：多阶段构建，JDK 21（使用 mcr 镜像，规避 Docker Hub 拉取不稳定）
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS builder
WORKDIR /app

COPY pom.xml .
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY src ./src
RUN mvn -B package -DskipTests -Dmaven.test.skip=true -Dmaven.wagon.http.retryHandler.count=5

FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends dumb-init && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/attendance-backend-*.jar app.jar

# 生产务必通过环境变量传入 JWT_SECRET、数据库连接等
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["dumb-init", "sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
