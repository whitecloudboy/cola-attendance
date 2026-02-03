-- 开源版考勤与排班系统数据库结构
-- 适用：MySQL 8+，字符集 utf8mb4
--
-- 通用字段约定：
--   id         : 主键，bigint 自增
--   created_at : 创建时间
--   updated_at : 更新时间
--   created_by : 创建人ID（可选）
--   updated_by : 更新人ID（可选）
--   deleted    : 软删除 0=否 1=是；查询时默认带 deleted=0
--
-- 表命名：组织/权限 sys_ 前缀，排班 duty_，考勤 attendance_。

-- 数据库名：attendance
CREATE DATABASE IF NOT EXISTS `attendance` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `attendance`;

SET NAMES utf8mb4;

/* ============= 1. 组织与权限基础表 ============= */

-- 部门表
CREATE TABLE `sys_dept` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id`   BIGINT DEFAULT NULL COMMENT '父部门ID（顶级为NULL）',
  `name`        VARCHAR(64) NOT NULL COMMENT '部门名称',
  `sort`        INT DEFAULT 0 COMMENT '排序值（从小到大）',
  `status`      TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 1=正常 0=停用',

  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`  BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  KEY `idx_sys_dept_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';


-- 系统用户表（既用于登录，也可作为考勤人员）
CREATE TABLE `sys_user` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`      VARCHAR(50) NOT NULL COMMENT '登录账号',
  `display_name`  VARCHAR(50) NOT NULL COMMENT '显示名称/真实姓名',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希值',
  `dept_id`       BIGINT DEFAULT NULL COMMENT '所属部门ID',
  `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone`         VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status`        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
  `is_duty_person` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否值班/参与排班 1=是 0=否',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',

  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`    BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表（登录账号逻辑删除后不建议复用）';


-- 角色表
CREATE TABLE `sys_role` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code`        VARCHAR(50) NOT NULL COMMENT '角色编码（英文标识）',
  `name`        VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',

  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`  BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


-- 用户-角色关联表
CREATE TABLE `sys_user_role` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`   BIGINT NOT NULL COMMENT '用户ID',
  `role_id`   BIGINT NOT NULL COMMENT '角色ID',

  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
  KEY `idx_sys_user_role_user` (`user_id`),
  KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';


-- 菜单/权限点表
CREATE TABLE `sys_menu` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id`   BIGINT DEFAULT NULL COMMENT '父菜单ID（顶级为NULL）',
  `name`        VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `path`        VARCHAR(100) DEFAULT NULL COMMENT '路由路径',
  `component`   VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
  `type`        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '类型 1=菜单 2=按钮',
  `permission`  VARCHAR(100) DEFAULT NULL COMMENT '权限标识（如 attendance:record:list）',
  `icon`        VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `sort`        INT DEFAULT 0 COMMENT '排序值（从小到大）',
  `visible`     TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见 1=可见 0=隐藏',

  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`  BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  KEY `idx_sys_menu_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单与权限点表';


-- 角色-菜单关联表
CREATE TABLE `sys_role_menu` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id`   BIGINT NOT NULL COMMENT '角色ID',
  `menu_id`   BIGINT NOT NULL COMMENT '菜单ID',

  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`role_id`, `menu_id`),
  KEY `idx_sys_role_menu_role` (`role_id`),
  KEY `idx_sys_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';


/* ============= 2. 排班与考勤领域表 ============= */

-- 班次定义表
CREATE TABLE `duty_shift` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`          VARCHAR(50) NOT NULL COMMENT '班次名称',
  `code`          VARCHAR(50) DEFAULT NULL COMMENT '班次编码',
  `start_time`    TIME NOT NULL COMMENT '上班时间',
  `end_time`      TIME NOT NULL COMMENT '下班时间',
  `is_cross_day`  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否跨天 1=是 0=否',
  `shift_type`    VARCHAR(32) DEFAULT NULL COMMENT '班次类型（如：行政、值班、夜班等）',
  `description`   VARCHAR(255) DEFAULT NULL COMMENT '备注/说明',

  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`    BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_shift_code` (`code`),
  KEY `idx_duty_shift_type` (`shift_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次定义表（code 可选，有则全局唯一）';


-- 排班结果表：某人某天上什么班
CREATE TABLE `duty_schedule` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`       BIGINT NOT NULL COMMENT '用户ID（参与考勤的人员）',
  `dept_id`       BIGINT DEFAULT NULL COMMENT '部门ID',
  `work_date`     DATE NOT NULL COMMENT '工作日期',
  `shift_id`      BIGINT NOT NULL COMMENT '班次ID',
  `shift_type`    VARCHAR(32) DEFAULT NULL COMMENT '班次类型（冗余自班次表）',
  `status`        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 1=有效 0=已取消',

  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`    BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_duty_schedule_user_date_shift` (`user_id`, `work_date`, `shift_id`),
  KEY `idx_duty_schedule_dept` (`dept_id`),
  KEY `idx_duty_schedule_date` (`work_date`),
  KEY `idx_duty_schedule_user_date` (`user_id`, `work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班结果表（同一人同一天同一班次仅一条）';


-- 考勤设备表（可用于门禁/刷脸设备管理）
CREATE TABLE `attendance_device` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_code`   VARCHAR(64) NOT NULL COMMENT '设备编码（内部唯一标识）',
  `device_name`   VARCHAR(100) NOT NULL COMMENT '设备名称',
  `device_type`   VARCHAR(32) DEFAULT NULL COMMENT '设备类型（如：door, face, card）',
  `ip_address`    VARCHAR(64) DEFAULT NULL COMMENT '设备IP地址',
  `port`          INT DEFAULT NULL COMMENT '设备端口号',
  `location`      VARCHAR(200) DEFAULT NULL COMMENT '设备安装位置',
  `dept_id`       BIGINT DEFAULT NULL COMMENT '所属部门ID（可选）',
  `is_attendance` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否参与考勤 1=是 0=否',
  `status`        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态 1=启用 0=停用',
  `remark`        VARCHAR(255) DEFAULT NULL COMMENT '备注',

  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`    BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`       TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_device_code` (`device_code`),
  KEY `idx_attendance_device_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤设备表';


-- 原始打卡/开门记录表
CREATE TABLE `attendance_record` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`      BIGINT NOT NULL COMMENT '用户ID',
  `user_name`    VARCHAR(50) NOT NULL COMMENT '用户姓名（冗余）',
  `dept_id`      BIGINT DEFAULT NULL COMMENT '部门ID（冗余）',
  `event_time`   DATETIME NOT NULL COMMENT '事件时间/打卡时间',
  `device_id`    BIGINT DEFAULT NULL COMMENT '考勤设备ID（关联 attendance_device.id）',
  `event_type`   VARCHAR(20) DEFAULT NULL COMMENT '事件类型（如：open、in、out，可选）',
  `temperature`  VARCHAR(20) DEFAULT NULL COMMENT '体温（如有）',
  `mask_status`  TINYINT(1) DEFAULT NULL COMMENT '口罩状态 0=未知 1=不戴 2=戴口罩',
  `remark`       VARCHAR(255) DEFAULT NULL COMMENT '备注',

  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`   BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`   BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  KEY `idx_attendance_record_user_time` (`user_id`, `event_time`),
  KEY `idx_attendance_record_dept_time` (`dept_id`, `event_time`),
  KEY `idx_attendance_record_device_time` (`device_id`, `event_time`),
  KEY `idx_attendance_record_event_time` (`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原始打卡记录表';


-- 聚合后的考勤结果表（按人+日期+班次，一人一日一班次一条；无排班时 shift_id 可为空）
CREATE TABLE `attendance_result` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `attendance_date`  DATE NOT NULL COMMENT '考勤日期',
  `user_id`          BIGINT NOT NULL COMMENT '用户ID',
  `user_name`        VARCHAR(50) NOT NULL COMMENT '用户姓名（冗余）',
  `dept_id`          BIGINT DEFAULT NULL COMMENT '部门ID',
  `dept_name`        VARCHAR(50) DEFAULT NULL COMMENT '部门名称（冗余）',
  `schedule_id`      BIGINT DEFAULT NULL COMMENT '排班结果ID（可选）',
  `shift_id`         BIGINT DEFAULT NULL COMMENT '班次ID',
  `shift_type`       VARCHAR(32) DEFAULT NULL COMMENT '班次类型（如行政、值班等）',

  `planned_start_time` TIME DEFAULT NULL COMMENT '计划上班时间',
  `check_in_time`      TIME DEFAULT NULL COMMENT '实际上班打卡时间',
  `start_status`       TINYINT(1) DEFAULT NULL COMMENT '上班状态 1=正常 2=迟到 3=未打卡 4=异常',

  `planned_end_time`   TIME DEFAULT NULL COMMENT '计划下班时间',
  `check_out_time`     TIME DEFAULT NULL COMMENT '实际下班打卡时间',
  `end_status`         TINYINT(1) DEFAULT NULL COMMENT '下班状态 1=正常 2=早退 3=未打卡 4=异常',

  `absent_status`      TINYINT(1) DEFAULT 0 COMMENT '是否旷工 0=否 1=是',
  `remark`             VARCHAR(500) DEFAULT NULL COMMENT '备注',

  `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`         BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`         BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`            TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_result_user_date_shift` (`user_id`, `attendance_date`, `shift_id`),
  KEY `idx_attendance_result_user_date` (`user_id`, `attendance_date`),
  KEY `idx_attendance_result_dept_date` (`dept_id`, `attendance_date`),
  KEY `idx_attendance_result_date` (`attendance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤结果表';


/* ============= 3. 规则参数（DSL 之外的基础参数） ============= */

-- 考勤规则参数配置表（DSL 本身先放在配置文件中）
CREATE TABLE `attendance_rule_param` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code`        VARCHAR(50) NOT NULL COMMENT '参数编码（如：late_buffer_minutes）',
  `name`        VARCHAR(50) NOT NULL COMMENT '参数名称',
  `value`       VARCHAR(100) NOT NULL COMMENT '参数值（字符串形式，业务自行解析）',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '参数说明',

  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`  BIGINT DEFAULT NULL COMMENT '创建人ID',
  `updated_by`  BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0=否 1=是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_rule_param_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则参数表';

-- ---------- 设计说明 ----------
-- 1. 关联表（sys_user_role / sys_role_menu）保留 id 主键，便于 ORM 与扩展。
-- 2. 考勤结果唯一键 (user_id, attendance_date, shift_id)：shift_id 为 NULL 时 MySQL 允许多条，用于无排班考勤。
-- 3. 未显式建外键，便于迁移与分库；关联关系见字段注释，由应用层保证一致性。
-- 4. 冗余字段（user_name / dept_name / shift_type）用于列表与报表，减少联表。
