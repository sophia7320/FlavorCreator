-- 创味机 Community 模块数据库表

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `recipe_id` BIGINT NOT NULL COMMENT '菜谱ID',
    `parent_id` BIGINT COMMENT '父评论ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_recipe_id` (`recipe_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '类型: 1-菜谱 2-评论',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `collection` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `recipe_id` BIGINT NOT NULL COMMENT '菜谱ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_recipe` (`user_id`, `recipe_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
