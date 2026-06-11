-- ============================================
-- 试炼坊 (Trial Workshop) - 完整数据库初始化脚本
-- ============================================
-- 版本: v1.0.0
-- 日期: 2026-02-25
-- 说明: 包含所有表结构和初始数据
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `trial_workshop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `trial_workshop`;

-- ============================================
-- 1. 用户管理
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 题库管理
-- ============================================

-- 题目分类表
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID, 0为顶级',
    `sort` INT DEFAULT 0 COMMENT '排序序号',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目分类表';

-- 标签表
DROP TABLE IF EXISTS `t_tag`;
CREATE TABLE `t_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#1890ff' COMMENT '标签颜色',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 题目表
DROP TABLE IF EXISTS `t_question`;
CREATE TABLE `t_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `type` TINYINT NOT NULL COMMENT '题型: 1-单选 2-多选 3-判断 4-填空 5-简答',
    `stem` TEXT NOT NULL COMMENT '题干',
    `options` JSON COMMENT '选项(JSON数组)',
    `answer` TEXT NOT NULL COMMENT '正确答案',
    `analysis` TEXT COMMENT '答案解析',
    `difficulty` TINYINT DEFAULT 3 COMMENT '难度: 1-5星',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `source` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '来源: MANUAL/AI_IMPORT/FILE_IMPORT',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_category` (`user_id`, `category_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 题目-标签关联表
DROP TABLE IF EXISTS `t_question_tag`;
CREATE TABLE `t_question_tag` (
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`question_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关联表';

-- ============================================
-- 3. 考试管理
-- ============================================

-- 试卷表
DROP TABLE IF EXISTS `t_exam`;
CREATE TABLE `t_exam` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
    `title` VARCHAR(200) NOT NULL COMMENT '试卷标题',
    `description` VARCHAR(500) DEFAULT '' COMMENT '试卷描述',
    `total_score` INT NOT NULL DEFAULT 100 COMMENT '总分',
    `pass_score` INT NOT NULL DEFAULT 60 COMMENT '及格分',
    `duration` INT NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
    `shuffle_question` TINYINT DEFAULT 0 COMMENT '是否打乱题序: 0-否 1-是',
    `shuffle_option` TINYINT DEFAULT 0 COMMENT '是否打乱选项: 0-否 1-是',
    `auto_collect_error` TINYINT DEFAULT 1 COMMENT '错题自动收录: 0-否 1-是',
    `question_count` INT DEFAULT 0 COMMENT '题目数量',
    `user_id` BIGINT NOT NULL COMMENT '创建者ID',
    `share_code` VARCHAR(32) DEFAULT NULL COMMENT '试卷分享码',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_share_code` (`share_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

-- 试卷-题目关联表
DROP TABLE IF EXISTS `t_exam_question`;
CREATE TABLE `t_exam_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `exam_id` BIGINT NOT NULL COMMENT '试卷ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `score` INT NOT NULL DEFAULT 5 COMMENT '本题分值',
    `order_seq` INT DEFAULT 0 COMMENT '排列顺序',
    PRIMARY KEY (`id`),
    KEY `idx_exam` (`exam_id`),
    KEY `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷题目关联表';

-- 考试记录表
DROP TABLE IF EXISTS `t_exam_record`;
CREATE TABLE `t_exam_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `exam_id` BIGINT NOT NULL COMMENT '试卷ID',
    `score` INT DEFAULT NULL COMMENT '得分',
    `total_score` INT DEFAULT 0 COMMENT '试卷总分',
    `correct_count` INT DEFAULT 0 COMMENT '正确题数',
    `wrong_count` INT DEFAULT 0 COMMENT '错误题数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-考试中 1-已交卷 2-已批改',
    `answers` JSON COMMENT '答题详情(JSON)',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration` INT DEFAULT 0 COMMENT '实际用时(秒)',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_exam` (`user_id`, `exam_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

-- ============================================
-- 4. 错题本
-- ============================================

-- 错题本表
DROP TABLE IF EXISTS `t_error_book`;
CREATE TABLE `t_error_book` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `error_count` INT DEFAULT 1 COMMENT '错误次数',
    `last_error_time` DATETIME COMMENT '最近错误时间',
    `next_review_time` DATETIME COMMENT '下次复习时间(艾宾浩斯)',
    `review_stage` INT DEFAULT 0 COMMENT '复习阶段: 0,1,2,3...',
    `mastered` TINYINT DEFAULT 0 COMMENT '是否已掌握: 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_next_review` (`user_id`, `next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本';

-- ============================================
-- 5. AI 智能解析
-- ============================================

-- AI 配置表
DROP TABLE IF EXISTS `t_ai_config`;
CREATE TABLE `t_ai_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `provider` VARCHAR(50) NOT NULL COMMENT 'AI提供商: openai, qianwen, deepseek',
    `api_key` VARCHAR(500) NOT NULL COMMENT 'API密钥(AES-128加密存储)',
    `model` VARCHAR(100) COMMENT '模型名称',
    `base_url` VARCHAR(255) COMMENT '自定义API地址',
    `max_tokens` INT DEFAULT 4000 COMMENT '最大token数',
    `temperature` DECIMAL(3,2) DEFAULT 0.3 COMMENT '温度参数(0.0-1.0)',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_provider` (`user_id`, `provider`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- AI 解析日志表
DROP TABLE IF EXISTS `t_ai_parse_log`;
CREATE TABLE `t_ai_parse_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `provider` VARCHAR(50) NOT NULL COMMENT 'AI提供商',
    `model` VARCHAR(100) COMMENT '使用的模型',
    `input_text` TEXT COMMENT '输入文本(截取前1000字符)',
    `questions_count` INT COMMENT '解析出的题目数量',
    `tokens_used` INT COMMENT '消耗的token数',
    `cost` DECIMAL(10,6) COMMENT '成本(美元)',
    `parse_time` INT COMMENT '解析耗时(毫秒)',
    `success` TINYINT COMMENT '是否成功: 0-失败 1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `create_time`),
    KEY `idx_provider` (`provider`),
    KEY `idx_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI解析日志表';

-- ============================================
-- 6. 初始数据
-- ============================================

-- 初始管理员账户
-- 用户名: admin
-- 密码: admin123 (BCrypt加密)
-- 注意: 首次启动后建议修改密码或创建新账号
INSERT INTO `t_user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36PQm4wQ0U1TrlCcnNI1v2e', '管理员', 'ADMIN', 1);

-- ============================================
-- 7. 验证安装
-- ============================================

-- 查看所有表
SHOW TABLES;

-- 验证用户表
SELECT COUNT(*) AS user_count FROM t_user;

-- ============================================
-- 完成提示
-- ============================================
SELECT '✅ 数据库初始化完成！' AS status;
SELECT '数据库名称: trial_workshop' AS info;
SELECT '字符集: utf8mb4' AS info;
SELECT '排序规则: utf8mb4_unicode_ci' AS info;
SELECT '初始管理员: admin / admin123' AS info;
SELECT '请访问 http://localhost:3000 开始使用' AS info;
