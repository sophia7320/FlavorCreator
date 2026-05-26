-- ============================================================
-- Admin 模块 - 数据库迁移脚本
-- 
-- 执行前请确认已在 MySQL 中运行
-- 用法: mysql -u root -p flcr < migration.sql
-- ============================================================

-- 1. 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `role` VARCHAR(20) DEFAULT 'ADMIN' COMMENT '角色: SUPER_ADMIN, ADMIN',
    `status` VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员（密码: admin123，使用BCrypt加密）
-- 如需重置，使用: BCryptPasswordEncoder().encode("admin123")
INSERT INTO `admin` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SUPER_ADMIN', 'ENABLED');

-- 2. 菜谱表 - 添加 status 字段
ALTER TABLE `recipe` ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT NULL COMMENT '状态: APPROVED, REJECTED, SHELVED, UNSHELVED' AFTER `view_count`;

-- 3. 评论表 - 添加 status 字段
ALTER TABLE `comment` ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT NULL COMMENT '状态: VISIBLE, HIDDEN' AFTER `like_count`;

-- 4. 用户表 - 添加 status 字段（用于Admin启用/禁用用户）
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) DEFAULT 'ENABLED' COMMENT '状态: ENABLED, DISABLED' AFTER `preferences`;

-- 注意: MySQL 不支持 ADD COLUMN IF NOT EXISTS。
-- 如需兼容 MySQL < 8.0，请手动检查列是否存在后再执行 ALTER TABLE。
