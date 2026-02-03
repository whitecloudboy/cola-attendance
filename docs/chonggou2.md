# 第二步：实现迁移的业务逻辑

> 在第一步工程框架与 system 模块就绪的前提下，本步骤实现排班、考勤记录、考勤结果与规则引擎等业务逻辑。

---

## 1. 核心领域模型与表对照

**数据库表结构**以 [attendance-db-schema.sql](attendance-db-schema.sql) 为准（库名 `attendance`），下表仅做领域概念与表名对照。

| 领域概念       | 表名                 | 说明 |
|----------------|----------------------|------|
| 考勤人员       | sys_user             | 系统用户即考勤人员，第一步已建 |
| 排班班次       | duty_shift           | 班次定义（时间、跨天、班次类型） |
| 排班结果       | duty_schedule        | 某人某天上什么班 |
| 考勤设备       | attendance_device    | 门禁/刷脸设备 |
| 原始打卡记录   | attendance_record    | 打卡事件流水 |
| 考勤结果       | attendance_result    | 按人+日期+班次聚合后的上下班状态 |
| 规则参数       | attendance_rule_param| 迟到缓冲等；DSL 先放配置文件 |

---

## 2. 业务模块与接口范围

### 2.1 排班（duty）

- **班次定义**：`duty_shift` 的 CRUD、列表。
- **排班结果**：`duty_schedule` 的维护（按人、按日期、按班次），支持批量生成与取消。

### 2.2 考勤（attendance）

- **设备**：`attendance_device` 的 CRUD，与打卡记录关联。
- **打卡记录**：`attendance_record` 的写入与查询；支持 CSV/Excel 导入（无设备时）。
- **考勤结果**：`attendance_result` 的生成与查询；定时任务生成空记录；打卡事件触发规则引擎更新状态；日终补录。

### 2.3 规则引擎（简化版）

- SpEL 或轻量规则；DSL 先放配置文件，`attendance_rule_param` 存基础参数。

---

## 3. 业务流程（实现顺序建议）

1. 排班：维护班次 → 生成排班。
2. 考勤事件采集：设备回调或 CSV/Excel 导入 → 写 `attendance_record`。
3. 考勤计算：定时生成空 `attendance_result` → 打卡事件走规则引擎更新 → 日终补录。

---

## 4. 与现有项目的“去耦合”原则

- 不直接复用 `io.renren.*`；不复制现有前端主题；代码重新组织；接口路径可按需兼容。

---

## 5. 第二步交付物（自检）

- [ ] 班次与排班 CRUD、列表/日历。
- [ ] 考勤设备、打卡记录 CRUD 与导入；考勤结果生成与查询。
- [ ] 定时任务、规则引擎、日终补录。
- [ ] 规则参数与 DSL 配置生效。

---

## 6. 后续演进方向

- 多租户；规则引擎版本化与灰度；Docker 一键部署（MySQL + attendance-backend + 可选 attendance-frontend）。
