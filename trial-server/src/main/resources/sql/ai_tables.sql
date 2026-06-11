-- AI 配置表
CREATE TABLE IF NOT EXISTS t_ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider VARCHAR(50) NOT NULL COMMENT 'AI提供商: openai, qianwen, deepseek',
    api_key VARCHAR(500) NOT NULL COMMENT 'API密钥(加密存储)',
    model VARCHAR(100) COMMENT '模型名称',
    base_url VARCHAR(255) COMMENT '自定义API地址',
    max_tokens INT DEFAULT 4000 COMMENT '最大token数',
    temperature DECIMAL(3,2) DEFAULT 0.3 COMMENT '温度参数',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_provider (user_id, provider),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI配置表';

-- AI 解析日志表
CREATE TABLE IF NOT EXISTS t_ai_parse_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider VARCHAR(50) NOT NULL COMMENT 'AI提供商',
    model VARCHAR(100) COMMENT '使用的模型',
    input_text TEXT COMMENT '输入文本(截取前1000字符)',
    questions_count INT COMMENT '解析出的题目数量',
    tokens_used INT COMMENT '消耗的token数',
    cost DECIMAL(10,6) COMMENT '成本(美元)',
    parse_time INT COMMENT '解析耗时(毫秒)',
    success TINYINT COMMENT '是否成功: 0-失败 1-成功',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI解析日志表';
