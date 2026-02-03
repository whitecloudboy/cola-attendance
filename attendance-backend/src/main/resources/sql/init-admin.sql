-- 首次初始化：插入管理员用户（密码 123456，需先执行 attendance-db-schema.sql 建表）
-- 执行后可用 admin / 123456 登录

INSERT INTO `sys_user` (`username`, `display_name`, `password_hash`, `status`, `is_duty_person`)
VALUES ('admin', '管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 1, 0)
ON DUPLICATE KEY UPDATE `id` = `id`;

-- 若需角色可先插入 sys_role，再插入 sys_user_role 关联
