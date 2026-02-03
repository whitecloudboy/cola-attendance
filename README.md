# 考勤与排班（qianyi）

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

## 第一步交付物（chonggou1）

- [x] 后端工程可启动
- [x] Swagger 文档
- [x] 登录接口返回 JWT
- [x] 部门、用户、角色、菜单 CRUD 与部门树、菜单树

第二步见 `docs/chonggou2.md`（排班、考勤、规则引擎等业务逻辑）。
