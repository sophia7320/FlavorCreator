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
  `source` TINYINT DEFAULT 1 COMMENT '来源 (1:系统库, 2:AI生成)',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_title` (`title`),
  INDEX `idx_cuisine` (`cuisine_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱主表';

-- 2. 食谱-食材关联表 (一对多)
-- 为什么不用JSON存食材？因为我们需要统计“哪些菜用了鸡蛋”，或者计算总营养值，结构化存储更方便分析
CREATE TABLE `flavor_recipe_ingredient` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `recipe_id` BIGINT NOT NULL COMMENT '关联食谱ID',
  `ingredient_name` VARCHAR(100) NOT NULL COMMENT '食材名称 (如: 牛腩)',
  `quantity` DECIMAL(10, 2) DEFAULT NULL COMMENT '数量 (如: 500)',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位 (如: g, ml, 个)',
  `detail` VARCHAR(100) DEFAULT NULL COMMENT '处理细节 (如: 切块, 去皮)',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  INDEX `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱食材详情表';

-- 3. 食谱步骤表 (一对多)
CREATE TABLE `flavor_recipe_step` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `recipe_id` BIGINT NOT NULL COMMENT '关联食谱ID',
  `step_number` INT NOT NULL COMMENT '步骤序号 (1, 2, 3...)',
  `description` TEXT NOT NULL COMMENT '步骤文字描述',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '步骤演示图',
  `duration` INT DEFAULT 0 COMMENT '该步骤建议耗时(秒)',
  INDEX `idx_recipe_id` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱步骤详情表';
