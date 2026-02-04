# 考勤与排班（cola-attendance）

开源版考勤与排班系统，第一步为工程框架与 system 模块（部门/用户/角色/菜单、登录 JWT）。

## 结构

- **attendance-backend**：Spring Boot 3 + JDK 21 + MyBatis-Plus + JWT，包名 `com.cola.attendance`
- **attendance-frontend**：（可选）Vue 3 + Vite + Element Plus
- **docs**：数据库脚本与设计文档

## 环境要求

- **JDK 21**（本工程单独用 21，与老工程 JDK 8 共存：见 [docs/jdk21-toolchains.md](docs/jdk21-toolchains.md) 配置 Maven 与 IDE 的 JDK 路径）
- MySQL 8+
- Maven 3.8+

## 快速开始

1. **数据库**：MySQL 8+，执行 `docs/attendance-db-schema.sql` 建库 `attendance` 及表。
2. **初始化管理员**（可选）：执行 `attendance-backend/src/main/resources/sql/init-admin.sql`，或自行插入 `sys_user`（密码需 BCrypt 加密，如 `123456` 对应 `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi`）。
3. **配置**：在 `attendance-backend/src/main/resources/application-dev.yml` 中填写数据库密码，启动时激活 `dev` profile。
4. **启动**：`cd attendance-backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev`
5. **接口文档**：http://localhost:8080/swagger-ui.html  
6. **登录**：POST `/system/auth/login`，Body `{"username":"admin","password":"123456"}`，返回的 `token` 放在请求头 `token` 中访问其他接口。

## 功能概览

- 后端工程可启动、Swagger 文档、登录 JWT、部门/用户/角色/菜单 CRUD 与部门树/菜单树
- 排班（班次、排班结果）、考勤（设备、打卡记录、考勤结果）、规则引擎（DSL 配置）、定时任务与 E2E 脚本见 `TASKS.md` 与 `docs/`

---

## 环境变量

生产或 Docker 部署时建议用环境变量覆盖敏感与可变配置，勿在仓库中提交真实密钥。

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `JWT_SECRET` | JWT 签名密钥（生产必设，建议 ≥32 字符） | 随机字符串 |
| `JWT_EXPIRATION_MS` | Token 有效期毫秒数 | `86400000`（24 小时） |
| `SPRING_DATASOURCE_URL` | 数据库 JDBC URL | `jdbc:mysql://mysql:3306/attendance?...` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | 强密码 |

本地开发可在 `application-dev.yml` 或 `application-local.yml`（已被 gitignore）中配置，或直接设置上述环境变量。

---

## 部署说明

### 后端

- **JDK 21**、Maven 打包：`cd attendance-backend && mvn -DskipTests package`
- 运行 jar：`java -Dspring.profiles.active=prod -jar target/attendance-backend-*.jar`
- 生产务必设置 `JWT_SECRET` 与数据库连接（环境变量或 `application-prod.yml`，且勿提交 prod 配置中的密码）。

### 前端

- 构建：`cd attendance-frontend && npm ci && npm run build`
- 将 `dist/` 用 Nginx 等静态托管，并配置反向代理 `/api` 到后端地址（见 `vite.config.js` 中 proxy 目标）。

### Docker 示例

见项目根目录 `docker-compose.yml` 与 `attendance-backend/Dockerfile`。一键启动后端 + MySQL：

```bash
docker compose up -d
```

后端接口：http://localhost:8080，Swagger：http://localhost:8080/swagger-ui.html。首次需在 MySQL 中执行 `docs/attendance-db-schema.sql` 与 `init-admin.sql`（或通过挂载 init 脚本）。
