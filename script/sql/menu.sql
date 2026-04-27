-- [废弃] 旧版菜谱设计，表名和字段命名与当前 project 的 recipe 表不一致
-- 当前有效的建表脚本请参见 user.sql / community.sql / ingredient.sql
-- 此文件保留供参考

-- 1. 食谱主表
CREATE TABLE `flavor_recipe` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '菜谱标题',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '一句话简介',
  `cuisine_type` VARCHAR(50) DEFAULT NULL COMMENT '菜系 (如: 川菜, 粤菜)',
  `difficulty` VARCHAR(20) DEFAULT 'medium' COMMENT '难度 (easy, medium, hard)',
  `cooking_time` INT DEFAULT 0 COMMENT '烹饪耗时(分钟)',
  `calories` INT DEFAULT 0 COMMENT '总卡路里',
