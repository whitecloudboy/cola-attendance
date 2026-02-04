-- =============================================================================
-- 小型医院场景测试数据脚本（admin 用户保留）
-- =============================================================================
-- 执行前：请确认已执行 attendance-db-schema.sql 及 attendance-db-schema-alter.sql
-- 执行后：部门 10 条（3 级）、班次 7 条、用户 13 条（admin 保留并更新 dept_id）
-- 密码：新增用户统一 123456
-- =============================================================================

USE `attendance`;
SET NAMES utf8mb4;

-- ---------- 1. 清理（保留 admin 用户） ----------
DELETE FROM `attendance_record`;
DELETE FROM `attendance_result`;
DELETE FROM `duty_schedule`;
DELETE FROM `sys_user_post`;
DELETE FROM `sys_user_role` WHERE `user_id` IN (SELECT `id` FROM (SELECT `id` FROM `sys_user` WHERE `username` != 'admin') AS _t);
DELETE FROM `sys_user` WHERE `username` != 'admin';
DELETE FROM `duty_shift`;
DELETE FROM `sys_dept`;

-- ---------- 2. 部门（3 级，10 条，指定 id 1～10） ----------
INSERT INTO `sys_dept` (`id`, `parent_id`, `name`, `sort`, `status`, `deleted`) VALUES
(1, NULL, '某某医院', 1, 1, 0),
(2, 1, '行政部', 1, 1, 0),
(3, 2, '院办', 1, 1, 0),
(4, 2, '人事科', 2, 1, 0),
(5, 1, '医务部', 2, 1, 0),
(6, 5, '内科', 1, 1, 0),
(7, 5, '外科', 2, 1, 0),
(8, 1, '护理部', 3, 1, 0),
(9, 8, '门诊护理组', 1, 1, 0),
(10, 8, '病区护理组', 2, 1, 0);

-- ---------- 3. 将 admin 归属到根部门 ----------
UPDATE `sys_user` SET `dept_id` = 1 WHERE `username` = 'admin';

-- ---------- 4. 班次（7 条：行政 3 + 值班 3 + 领导带班 1） ----------
-- 需已执行 alter 增加 dept_id, sort, group_no, color；若未执行请注释本段或去掉这四列
INSERT INTO `duty_shift` (`name`, `code`, `start_time`, `end_time`, `is_cross_day`, `shift_type`, `description`, `dept_id`, `sort`, `group_no`, `color`, `deleted`) VALUES
('行政上午', 'XZ-AM', '08:00:00', '12:00:00', 0, '行政', '上午班', NULL, 1, NULL, '#52c41a', 0),
('行政下午', 'XZ-PM', '14:00:00', '17:30:00', 0, '行政', '下午班', NULL, 2, NULL, '#52c41a', 0),
('行政全天', 'XZ-FULL', '08:30:00', '17:30:00', 0, '行政', '全天行政班', NULL, 3, NULL, '#52c41a', 0),
('白班', 'ZB-DAY', '08:00:00', '16:00:00', 0, '值班', '交接班-白班', 8, 4, '001', '#1890ff', 0),
('中班', 'ZB-MID', '16:00:00', '00:00:00', 1, '值班', '交接班-中班', 8, 5, '001', '#1890ff', 0),
('夜班', 'ZB-NIGHT', '00:00:00', '08:00:00', 1, '值班', '交接班-夜班', 8, 6, '001', '#1890ff', 0),
('领导带班', 'LEADER', '08:00:00', '08:00:00', 1, '领导带班', '24h 带班', NULL, 7, NULL, '#fa8c16', 0);

-- ---------- 5. 测试用户（13 人，密码 123456，is_duty_person=1） ----------
-- bcrypt 哈希与 init-admin 一致
INSERT INTO `sys_user` (`username`, `display_name`, `password_hash`, `dept_id`, `status`, `is_duty_person`, `deleted`) VALUES
('leader1', '张院长', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 3, 1, 1, 0),
('office1', '李主任', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 3, 1, 1, 0),
('office2', '王秘书', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 3, 1, 1, 0),
('hr1', '赵人事', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 4, 1, 1, 0),
('hr2', '钱人事', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 4, 1, 1, 0),
('doc_in1', '孙医生', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 6, 1, 1, 0),
('doc_in2', '周医生', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 6, 1, 1, 0),
('doc_out1', '吴医生', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 7, 1, 1, 0),
('doc_out2', '郑医生', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 7, 1, 1, 0),
('nurse1', '冯护士', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 9, 1, 1, 0),
('nurse2', '陈护士', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 9, 1, 1, 0),
('nurse3', '褚护士', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 10, 1, 1, 0),
('nurse4', '卫护士', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 10, 1, 1, 0);

-- ---------- 结束 ----------
-- 执行后可用 admin/123456 或 上述任意 username/123456 登录。
