-- ============================================================
-- Admin 密码 BCrypt 加密迁移脚本
-- ============================================================
-- BCrypt hash 长度为 60 字符，标准格式：$2a$10$...
-- 确保 admin.password 字段长度至少为 60，推荐 VARCHAR(255)
-- ============================================================

-- Step 1: 检查并修改 password 字段长度（如果当前不够长）
ALTER TABLE `admin` MODIFY COLUMN `password` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '密码(BCrypt加密)';

-- Step 2: 将现有明文密码转为 BCrypt 加密
-- 注意：这里需要使用应用层的 PasswordUtil.encrypt() 进行转换
-- 因为 BCrypt 需要随机盐值，SQL 无法直接实现
-- 替代方案：管理员重新设置密码（推荐），或通过 Java 程序批量转换
-- 
-- 批量转换命令示例（在 Java 中执行）：
--   List<Admin> admins = adminMapper.selectList(null);
--   for (Admin admin : admins) {
--       if (!PasswordUtil.match(admin.getPassword(), admin.getPassword())) {
--           admin.setPassword(PasswordUtil.encrypt(admin.getPassword()));
--           adminMapper.updateById(admin);
--       }
--   }
--
-- 由于 PasswordUtil.match() 兼容明文密码，
-- 存量明文密码在迁移完成前仍可正常登录。
-- 迁移完成后建议删除此脚本，确保所有密码均为 BCrypt 格式。
