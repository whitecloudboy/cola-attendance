# cola-attendance

[中文](README.zh-CN.md) | [English](README.md)

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)
![Vue](https://img.shields.io/badge/Vue-3-42b883.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)

一个面向值班/考勤场景的开源系统，覆盖：
- 组织与权限（部门、用户、角色、菜单）
- 排班（班次、排班结果）
- 考勤（设备、打卡记录、考勤结果）
- 规则引擎（DSL/SpEL）
- 定时任务与 E2E 全链路脚本

## 0. 第一性说明（先看这个）

### 这个仓库到底是什么

- 这是一个**可运行的前后端系统**（`attendance-backend` + `attendance-frontend`）
- 不是“纯设计文档仓库”：`docs/` 和 `scripts/` 是服务于系统落地与验证的配套
- 规则引擎不是概念演示，而是已接入考勤结果计算的核心模块

### 解决什么问题

- 面向中小单位，替代人工考勤统计与排班管理
- 把排班、打卡采集、考勤结果生成、规则判定串成一条完整链路
- 支持规则可配置，便于不同单位按制度调整

### 30 秒看懂

- 只启动后端 + 数据库，就能通过 API/Swagger 使用核心能力
- 启动前端后，就是完整可用的管理系统
- 运行 `scripts/` 能自动验证全链路考勤流程

### 最快跑起来

```bash
# 1）如果 3306/8080 被占用，先设置端口（可选）
# PowerShell:
# $env:MYSQL_PORT='3308'; $env:BACKEND_PORT='18082'

# 2）启动 backend + mysql
docker compose up -d

# 3）打开 Swagger
# 默认: http://localhost:8080/swagger-ui.html
# 自定义: http://localhost:<BACKEND_PORT>/swagger-ui.html
```

## 1. 技术栈

- 后端：`Spring Boot 3`、`JDK 21`、`MyBatis-Plus`、`Spring Security`、`JWT`
- 前端：`Vue 3`、`Vite`、`Element Plus`
- 数据库：`MySQL 8+`

## 2. 仓库结构

```text
cola-attendance/
|-- attendance-backend/      # Java 后端
|-- attendance-frontend/     # Vue 前端
|-- docs/                    # 数据库脚本、设计文档、E2E说明
|-- scripts/                 # E2E 自动化脚本（Python + PowerShell）
|-- docker-compose.yml       # 本地/Demo 运行编排
`-- TASKS.md                 # 项目任务清单与阶段状态
```

## 3. 核心功能

- 认证与权限：登录返回 JWT，请求头使用 `token`
- System 模块：部门/用户/角色/菜单 CRUD，支持树结构
- 排班模块：班次管理、排班维护、批量排班/取消
- 考勤模块：设备管理、打卡记录（含 Excel 导入、设备回调）、考勤结果
- 规则引擎：支持通过 DSL（SpEL）配置上下班判定规则
- 定时任务：
  - 每日 `01:00` 生成空考勤记录
  - 每日 `23:55` 执行日终补录

## 4. 本地开发快速开始

### 4.1 前置要求

- JDK 21
- Maven 3.8+
- MySQL 8+
- Node.js 18+（仅前端需要）

### 4.2 初始化数据库

1. 执行建表脚本：
   - `docs/attendance-db-schema.sql`
   - `docs/attendance-db-schema-alter.sql`
2. 初始化管理员：
   - `attendance-backend/src/main/resources/sql/init-admin.sql`

默认管理员账号：
- 用户名：`admin`
- 密码：`123456`

### 4.3 启动后端

```bash
cd attendance-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

启动后可访问：
- Swagger：`http://localhost:8080/swagger-ui.html`

### 4.4 启动前端（可选）

```bash
cd attendance-frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`  
开发代理：`/api -> http://localhost:8080`（见 `attendance-frontend/vite.config.js`）

## 5. Docker 运行（后端 + MySQL）

```bash
docker compose up -d
```

说明：
- Compose 会启动 `mysql` 和 `backend`
- MySQL 首次初始化会自动执行（`/docker-entrypoint-initdb.d`）：
  - `docs/attendance-db-schema.sql`
  - `docs/attendance-db-schema-alter.sql`
  - `attendance-backend/src/main/resources/sql/init-admin.sql`
- 默认主机端口：
  - MySQL：`3306`（来自 `${MYSQL_PORT:-3306}`）
  - 后端：`8080`（来自 `${BACKEND_PORT:-8080}`）
- 如果端口被占用，可指定自定义端口：

```powershell
$env:MYSQL_PORT='3308'
$env:BACKEND_PORT='18082'
docker compose up -d
```

- 然后访问：
  - 后端：`http://localhost:<BACKEND_PORT>`
  - Swagger：`http://localhost:<BACKEND_PORT>/swagger-ui.html`
- 如需重新执行数据库初始化脚本，删除数据卷后重建：

```bash
docker compose down -v
docker compose up -d
```

## 6. E2E 全链路测试

安装依赖：

```bash
pip install -r scripts/requirements.txt
```

一键跑全链路：

```powershell
.\scripts\full_link_cycle.ps1 -Cycles 1 -Date 2025-02-05 -SampleCount 5 -ReportDir logs
```

流程包含：
- 清理测试数据
- 自动排班
- 模拟打卡并触发规则
- 日终补录
- 正确性校验并输出报告

详细说明见：`docs/e2e-attendance-test.md`

## 7. 关键配置项

建议通过环境变量覆盖敏感配置：

| 变量名 | 说明 | 示例 |
|---|---|---|
| `JWT_SECRET` | JWT 签名密钥（生产必设，建议 >= 32 字符） | `replace-with-random-secret` |
| `JWT_EXPIRATION_MS` | Token 有效期（毫秒） | `86400000` |
| `SPRING_DATASOURCE_URL` | 数据库 JDBC URL | `jdbc:mysql://mysql:3306/attendance?...` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `strong-password` |

脚本相关：
- `COLA_ATTENDANCE_BASE_URL`：E2E 脚本请求的后端地址（默认 `http://localhost:8080`）

## 8. 接口访问约定

- 登录接口：`POST /system/auth/login`
- 登录成功后，把返回的 token 放到请求头：`token: <jwt>`

## 9. 项目状态

当前已完成：
- system、schedule、attendance、rule-engine 主功能
- E2E 自动化脚本与文档

待增强项见：`TASKS.md`

## 10. 贡献与许可

- 贡献指南：`CONTRIBUTING.md`
- 开源许可：`LICENSE`
