# E2E 考勤全链路测试说明

本文档描述E2E 考勤全链路测试：测试方法、测试范围、测试逻辑及使用说明。

---

## 1. 概述

E2E 测试覆盖考勤业务全流程：**排班 → 生成空考勤 → 打卡记录写入 → 规则引擎计算 → 日终补录 → 正确性校验**。脚本位于项目根目录 `scripts/` 下，通过 HTTP 调用 attendance-backend 接口完成自动化验证。

### 1.1 设计目标

1. **流程验证**：串起清理、排班、打卡、日终补录等步骤，验证端到端链路可正常运行。
2. **正确性校验**：根据打卡记录与班次时间窗推算期望的 `checkInTime`/`checkOutTime` 与状态，与 `attendance_result` 实际结果比对，验证规则引擎计算正确性。

---

## 2. 测试范围

| 环节 | 涉及数据 | 验证点 |
|------|----------|--------|
| 清理 | attendance_record、attendance_result、duty_schedule | 按指定日期删除测试数据，保证环境干净 |
| 排班 | duty_schedule、sys_user、duty_shift | 为用户生成指定日期的排班，每人至少一个班次 |
| 生成空考勤 | attendance_result | 按 duty_schedule 生成当日空记录 |
| 模拟打卡 | attendance_record | 按班次时间窗生成上下班打卡，通过 Excel 导入 |
| 日终补录 | attendance_result | 对当日考勤执行 end-check 规则，补录未打卡、异常等 |
| 正确性校验 | attendance_record、attendance_result | 预期 vs 实际比对，抽样输出结果 |

### 2.1 覆盖的业务能力

- 班次维护与排班
- 打卡记录导入（Excel）
- 打卡事件触发规则引擎更新考勤结果
- 定时生成空考勤、日终补录任务（含手动触发）
- 考勤结果查询与分页

---

## 3. 测试方法

### 3.1 环境要求

- **Python**：3.8+
- **curl**：系统已安装
- **依赖**：`pip install -r scripts/requirements.txt`（含 openpyxl）
- **后端**：attendance-backend 已启动，默认 `http://localhost:8080`
- **数据**：init-admin 已执行，存在 `admin/123456` 账号；需先维护班次（duty_shift）和用户（sys_user）,或使用模拟数据test-data-hospital.sql导入。

### 3.2 一键运行全链路

```powershell
cd D:\work\code\cola-attendance
.\scripts\full_link_cycle.ps1 -Cycles 1 -Date 2025-02-05 -SampleCount 5 -ReportDir logs
```

| 参数 | 说明 | 默认 |
|------|------|------|
| `-Cycles` | 执行轮数 | 1 |
| `-Date` | 测试日期 | 当天 |
| `-SampleCount` | 正确性校验抽样数量 | 5 |
| `-ReportDir` | 校验报告输出目录 | logs |

### 3.3 分步执行

```powershell
# 1. 清理指定日期测试数据
python scripts/cleanup_attendance.py --date 2025-02-05

# 2. 自动排班（为用户随机分配班次）
python scripts/auto_schedule.py --date 2025-02-05

# 3. 生成空考勤 + 模拟打卡 + 日终补录
python scripts/simulate_punch.py --date 2025-02-05 --trigger-generate --trigger-end

# 4. 正确性校验（抽样并输出 JSON 报告）
python scripts/verify_result.py --date 2025-02-05 --samples 5 --output logs/verify.json
```

---

## 4. 测试逻辑

### 4.1 清理（cleanup_attendance.py）

**顺序**：attendance_record → attendance_result → duty_schedule

- 分页查询指定日期的 record/result，逐条调用 `DELETE /attendance/record/{id}`、`DELETE /attendance/result/{id}`
- 查询当日 duty_schedule 列表，逐条调用 `DELETE /schedule/schedule/{id}`

### 4.2 自动排班（auto_schedule.py）

- 调用 `GET /system/user/page` 获取用户（优先 `isDutyPerson=1`）
- 调用 `GET /schedule/shift/list` 获取班次
- 为每个用户随机分配 1 个班次，调用 `POST /schedule/schedule/set` 设置排班

### 4.3 模拟打卡（simulate_punch.py）

1. **触发生成空考勤**：`POST /attendance/result/generate-empty?date=xxx`
2. **查询排班与班次**：获取当日 duty_schedule 及班次 startTime/endTime
3. **生成打卡时间**：按班次时间窗，为每人生成上班、下班两次打卡（可配置 `--start-offset`、`--end-offset` 分钟偏移）
4. **构建 Excel**：按「姓名或工号、打卡时间、设备编码(可选)」格式生成
5. **导入**：`POST /attendance/record/import` 上传 Excel
6. **触发日终补录**：`POST /attendance/result/trigger-end-task?date=xxx`

### 4.4 正确性校验（verify_result.py）

**核心逻辑**：基于打卡记录与班次，推算期望的 checkIn/checkOut，与 attendance_result 比对。

1. **取期望上班时间**：从当日打卡记录中，取最靠近计划上班时间的一次打卡
2. **取期望下班时间**：从当日打卡记录中，取计划上班与下班时间的中点之后、最靠近计划下班时间的一次打卡（支持跨天班次）
3. **比对**：期望的 start/end 与 `attendance_result` 的 `checkInTime`、`checkOutTime` 比较
4. **无打卡场景**：若无打卡记录，期望为“未打卡”，与实际 startStatus/endStatus 判断

**抽样**：`--samples N` 控制抽样数量，0 表示全部校验。输出 JSON 报告，含 summary 与每条约 detail。

---

## 5. 配置

| 配置项 | 说明 | 默认 |
|--------|------|------|
| `COLA_ATTENDANCE_BASE_URL` | 后端服务地址 | `http://localhost:8080` |
| `scripts/config.py` | 默认登录账号 | admin / 123456 |

---

## 6. 脚本清单

| 脚本 | 说明 |
|------|------|
| `http_client.py` | 通用 HTTP 客户端（curl 封装） |
| `config.py` | 配置（BASE_URL、登录账号） |
| `login.py` | 登录获取 JWT token |
| `cleanup_attendance.py` | 清理测试数据 |
| `auto_schedule.py` | 自动排班 |
| `simulate_punch.py` | 模拟打卡 + 触发生成空考勤与日终补录 |
| `verify_result.py` | 正确性校验 |
| `full_link_cycle.ps1` | 全链路编排 |
| `requirements.txt` | Python 依赖 |

---

## 7. 相关文档

- [attendance-db-schema.sql](attendance-db-schema.sql)：数据库表结构
- 项目任务与阶段见仓库根目录 `TASKS.md`
- attendance-db-schema-alter.sql：数据库表结构变更记录
