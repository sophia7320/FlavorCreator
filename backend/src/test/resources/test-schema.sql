CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `openid` VARCHAR(64) NOT NULL,
    `unionid` VARCHAR(64) DEFAULT NULL,
    `nickname` VARCHAR(64) DEFAULT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `background` VARCHAR(255) DEFAULT NULL,
    `signature` VARCHAR(128) DEFAULT NULL,
    `gender` TINYINT DEFAULT 0,
    `address` VARCHAR(100) DEFAULT NULL,
    `age` INT DEFAULT NULL,
    `preferences` TEXT DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_openid` ON `user` (`openid`);

CREATE TABLE IF NOT EXISTS `ingredient` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `quantity` DECIMAL(10,2) NOT NULL DEFAULT 0,
    `unit` VARCHAR(16) NOT NULL,
    `category` VARCHAR(32) NOT NULL,
    `storage_condition` VARCHAR(32),
    `expire_date` DATE DEFAULT NULL,
    `readed` TINYINT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS `idx_ingredient_user_id` ON `ingredient` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_ingredient_category` ON `ingredient` (`category`);
CREATE INDEX IF NOT EXISTS `idx_ingredient_expire_date` ON `ingredient` (`expire_date`);

CREATE TABLE IF NOT EXISTS `common_ingredient` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `category` VARCHAR(32) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    `default_unit` VARCHAR(16) NOT NULL
);
CREATE INDEX IF NOT EXISTS `idx_common_category` ON `common_ingredient` (`category`);

CREATE TABLE IF NOT EXISTS `recipe` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(128) NOT NULL,
    `cover` VARCHAR(255),
    `images` TEXT,
    `author_id` BIGINT NOT NULL,
    `ingredients` TEXT,
    `steps` TEXT,
    `tips` TEXT,
    `desc` VARCHAR(255) DEFAULT NULL,
    `cook_time` VARCHAR(16),
    `difficulty` TINYINT,
    `calories` INT,
    `tags` TEXT,
    `category` VARCHAR(32),
    `source` TINYINT DEFAULT 2,
    `like_count` INT DEFAULT 0,
    `collection_count` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `view_count` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS `idx_author_id` ON `recipe` (`author_id`);
CREATE INDEX IF NOT EXISTS `idx_recipe_category` ON `recipe` (`category`);
CREATE INDEX IF NOT EXISTS `idx_recipe_created_at` ON `recipe` (`created_at`);

CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `recipe_id` BIGINT NOT NULL,
    `parent_id` BIGINT,
    `content` TEXT NOT NULL,
    `like_count` INT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS `idx_comment_recipe_id` ON `comment` (`recipe_id`);
CREATE INDEX IF NOT EXISTS `idx_comment_user_id` ON `comment` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_comment_parent_id` ON `comment` (`parent_id`);

CREATE TABLE IF NOT EXISTS `like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `target_type` TINYINT NOT NULL,
    `target_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_user_target` ON `like` (`user_id`, `target_type`, `target_id`);
CREATE INDEX IF NOT EXISTS `idx_like_target` ON `like` (`target_type`, `target_id`);

CREATE TABLE IF NOT EXISTS `collection` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `recipe_id` BIGINT NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_user_recipe` ON `collection` (`user_id`, `recipe_id`);
CREATE INDEX IF NOT EXISTS `idx_collection_user_id` ON `collection` (`user_id`);
CREATE INDEX IF NOT EXISTS `idx_collection_recipe_id` ON `collection` (`recipe_id`);

CREATE TABLE IF NOT EXISTS `ingredient_subscription` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `subscribed` TINYINT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_sub_user_id` ON `ingredient_subscription` (`user_id`);
