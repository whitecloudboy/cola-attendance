# 第一步：创建基本工程框架与包结构

> 本步骤只搭架子：工程结构、包分层、数据库初始化、通用配置与安全骨架，不实现具体考勤/排班业务。  
> 包结构与「开源最小化架构」对齐，便于第二步按规则引擎、排班、考勤、设备适配逐块落地。

---

## 1. 项目定位（共识）

- **目标**：从现有企业内部系统中抽离“考勤 + 排班”核心能力，重写为独立、可开源的小型项目。
- **使用场景**：中小组织/团队自部署考勤，支持基础排班、刷脸/门禁事件导入、自动计算考勤结果。
- **不包含**：企业内部专用门户、OA 流程；与具体厂商深度绑定的实现细节（如海康 SDK 封装）。

---

## 2. 技术栈

- **后端**：JDK 21、Spring Boot 3.5.4、Springdoc 2.8.4、MyBatis-Plus、MySQL（库名 `attendance`）、Spring Security + JWT
- **前端（可选）**：Vue 3 + Vite + Element Plus

---

## 3. 仓库与模块结构

- `attendance-backend/`：本仓库已按此实现
- `attendance-frontend/`：可选
- `docs/`：attendance-db-schema.sql、chonggou1.md、chonggou2.md

---

## 4. 后端包结构（Java）

见代码 `com.cola.attendance`：config、common、security、core、module/system（第一步）、module/schedule、module/attendance、rule、adapter（第二步占位）。

---

## 5. 数据库初始化

使用 [attendance-db-schema.sql](attendance-db-schema.sql) 建库 `attendance` 及所有表。

---

## 6. 第一步交付物（自检）

- [x] 后端工程可启动
- [x] Swagger 文档
- [x] 登录接口返回 JWT
- [x] 部门、用户、角色、菜单 CRUD 与部门树、菜单树

第一步完成后，再进入 [chonggou2.md](chonggou2.md) 实现迁移的业务逻辑。
