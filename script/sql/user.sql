-- 用户表
-- 用于存储微信小程序用户信息
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信 openid',
    `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像 URL',
    `background` VARCHAR(255) DEFAULT NULL COMMENT '背景图 URL',
    `signature` VARCHAR(128) DEFAULT NULL COMMENT '个性签名',
    `gender` TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    `address` VARCHAR(100) DEFAULT NULL COMMENT '地区',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `preferences` JSON DEFAULT NULL COMMENT '用户偏好设置',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';