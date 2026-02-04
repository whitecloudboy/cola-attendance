# cola-attendance

[中文](README.md) | [English](README.en.md)

![License](https://img.shields.io/badge/License-MIT-green.svg)
![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F.svg)
![Vue](https://img.shields.io/badge/Vue-3-42b883.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)

An open-source attendance and duty scheduling system for real-world shift scenarios, including:
- Organization and permission management (department, user, role, menu)
- Scheduling (shift definitions and duty schedules)
- Attendance (devices, punch records, attendance results)
- Rule engine (DSL/SpEL)
- Scheduled jobs and E2E automation scripts

## 1. Tech Stack

- Backend: `Spring Boot 3`, `JDK 21`, `MyBatis-Plus`, `Spring Security`, `JWT`
- Frontend: `Vue 3`, `Vite`, `Element Plus`
- Database: `MySQL 8+`

## 2. Repository Structure

```text
cola-attendance/
├─ attendance-backend/      # Java backend
├─ attendance-frontend/     # Vue frontend
├─ docs/                    # DB scripts, design docs, E2E docs
├─ scripts/                 # E2E automation (Python + PowerShell)
├─ docker-compose.yml       # Local/demo orchestration
└─ TASKS.md                 # Project task list and progress
```

## 3. Core Features

- Auth and access control: JWT-based login, request header uses `token`
- System module: CRUD for department/user/role/menu, with tree structure support
- Scheduling module: shift management, schedule maintenance, batch scheduling/cancel
- Attendance module: device management, punch records (including Excel import and device callback), attendance results
- Rule engine: configurable attendance decision rules through DSL (SpEL)
- Scheduled jobs:
  - Generate empty attendance records daily at `01:00`
  - Run end-of-day supplement daily at `23:55`

## 4. Quick Start (Local Development)

### 4.1 Prerequisites

- JDK 21
- Maven 3.8+
- MySQL 8+
- Node.js 18+ (frontend only)

### 4.2 Initialize Database

1. Run schema scripts:
   - `docs/attendance-db-schema.sql`
   - `docs/attendance-db-schema-alter.sql`
2. Initialize admin user:
   - `attendance-backend/src/main/resources/sql/init-admin.sql`

Default admin account:
- Username: `admin`
- Password: `123456`

### 4.3 Start Backend

```bash
cd attendance-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Available after startup:
- Swagger: `http://localhost:8080/swagger-ui.html`

### 4.4 Start Frontend (Optional)

```bash
cd attendance-frontend
npm install
npm run dev
```

Default URL: `http://localhost:5173`  
Dev proxy: `/api -> http://localhost:8080` (see `attendance-frontend/vite.config.js`)

## 5. Docker (Backend + MySQL)

```bash
docker compose up -d
```

Notes:
- Starts `mysql` and `backend`
- Backend URL: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Current Compose setup does not auto-run SQL initialization scripts, so you still need to import:
  - `docs/attendance-db-schema.sql`
  - `docs/attendance-db-schema-alter.sql`
  - `attendance-backend/src/main/resources/sql/init-admin.sql`

## 6. E2E Full-Flow Test

Install dependencies:

```bash
pip install -r scripts/requirements.txt
```

Run one full cycle:

```powershell
.\scripts\full_link_cycle.ps1 -Cycles 1 -Date 2025-02-05 -SampleCount 5 -ReportDir logs
```

Flow includes:
- Cleanup test data
- Auto scheduling
- Punch simulation and rule triggering
- End-of-day supplement
- Correctness verification with report output

Details: `docs/e2e-attendance-test.md`

## 7. Key Environment Variables

Use environment variables for sensitive values:

| Variable | Description | Example |
|---|---|---|
| `JWT_SECRET` | JWT signing secret (required in production, recommended >= 32 chars) | `replace-with-random-secret` |
| `JWT_EXPIRATION_MS` | Token expiration in milliseconds | `86400000` |
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:mysql://mysql:3306/attendance?...` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `strong-password` |

Script-related:
- `COLA_ATTENDANCE_BASE_URL`: backend base URL for E2E scripts (default `http://localhost:8080`)

## 8. API Usage Convention

- Login endpoint: `POST /system/auth/login`
- Put login token into request header: `token: <jwt>`

## 9. Project Status

Completed:
- Main capabilities in system, schedule, attendance, and rule-engine modules
- E2E automation scripts and docs

Enhancement backlog: `TASKS.md`

## 10. Contributing and License

- Contributing guide: `CONTRIBUTING.md`
- License: `LICENSE`
