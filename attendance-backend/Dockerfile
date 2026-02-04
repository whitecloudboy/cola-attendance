# 考勤后端镜像：多阶段构建，JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
RUN apk add --no-cache maven && mvn -B dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache dumb-init

COPY --from=builder /app/target/attendance-backend-*.jar app.jar

# 生产务必通过环境变量传入 JWT_SECRET、数据库连接等
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["dumb-init", "sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
