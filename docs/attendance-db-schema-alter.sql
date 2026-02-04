-- =============================================================================
-- 考勤与排班库 DDL 变更脚本（历史累计）
-- =============================================================================
-- 用途：在已执行 attendance-db-schema.sql 的 MySQL 8+ 库上，按时间顺序执行所有变更。
-- 使用：执行前请备份。新环境可全量执行；若某段已执行过会报错，注释该段后重跑。
-- 维护：每次新增变更请在本文件末尾追加，并增加时间注释与变更说明。
-- =============================================================================

-- 变更历史（仅作索引，实际 DDL 见下方按时间分节）：
--   [2025-02-03] 岗位表、用户-岗位关联表；班次表增加 dept_id/sort/group_no/color

-- =============================================================================

USE `attendance`;

SET NAMES utf8mb4;


-- ---------- [2025-02-03] 岗位与班次扩展 ----------
-- 内容：岗位多选、班次归属部门、部门/班次顺序、班次组号（交接班）、班次颜色

/* 1. 岗位与用户-岗位关联 */

-- 岗位表（人员可多选岗位）
CREATE TABLE IF NOT EXISTS `sys_post` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50) NOT NULL COMMENT '岗位名称',
  `code`        VARCHAR(50) DEFAULT NULL COMMENT '岗位编码',
  `dept_id`     BIGINT DEFAULT NULL COMMENT '归属部门ID（可选）',
  `sort`        INT DEFAULT 0 COMMENT '排序值（排班页展示顺序）',
  `status`      TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 1=正常 0=停用',

  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`  BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  KEY `idx_sys_post_dept` (`dept_id`),
  KEY `idx_sys_post_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- 用户-岗位关联表（多对多）
CREATE TABLE IF NOT EXISTS `sys_user_post` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`   BIGINT NOT NULL COMMENT '用户ID',
  `post_id`   BIGINT NOT NULL COMMENT '岗位ID',

  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_post` (`user_id`, `post_id`),
  KEY `idx_sys_user_post_user` (`user_id`),
  KEY `idx_sys_user_post_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户岗位关联表';

/* 2. 班次表 duty_shift 新增字段 */

ALTER TABLE `duty_shift`
  ADD COLUMN `dept_id` BIGINT DEFAULT NULL COMMENT '归属部门ID（可选）' AFTER `code`,
  ADD KEY `idx_duty_shift_dept` (`dept_id`);

ALTER TABLE `duty_shift`
  ADD COLUMN `sort` INT DEFAULT 0 COMMENT '排序值（从小到大，排班页显示顺序）' AFTER `dept_id`;

ALTER TABLE `duty_shift`
  ADD COLUMN `group_no` VARCHAR(32) DEFAULT NULL COMMENT '班次组号，用于交接班匹配下一班' AFTER `sort`;

ALTER TABLE `duty_shift`
  ADD COLUMN `color` VARCHAR(32) DEFAULT NULL COMMENT '颜色标记，如 #1890ff' AFTER `group_no`;

-- ---------- 说明（本段） ----------
-- sys_dept 已有 sort，无需改动。若本段部分 ALTER 已执行会报 Duplicate column，注释对应 ALTER 后重跑。
-- 回滚（仅无依赖数据时）：DROP TABLE sys_user_post, sys_post；对 duty_shift 逐列 DROP COLUMN。


-- ---------- 后续变更在此追加，格式示例 ----------
-- ---------- [YYYY-MM-DD] 变更简述 ----------
-- 具体 DDL...

--删除报错 清除多余的约束
ALTER TABLE attendance.duty_schedule DROP KEY uk_duty_schedule_user_date_shift;
