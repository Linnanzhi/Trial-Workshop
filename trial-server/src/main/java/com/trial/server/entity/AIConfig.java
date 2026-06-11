package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI配置实体类
 */
@Data
@TableName("t_ai_config")
public class AIConfig {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * AI提供商: openai, qianwen, deepseek
     */
    private String provider;
    
    /**
     * API密钥(加密存储)
     */
    private String apiKey;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 自定义API地址
     */
    private String baseUrl;
    
    /**
     * 最大token数
     */
    private Integer maxTokens;
    
    /**
     * 温度参数
     */
    private Double temperature;
    
    /**
     * 是否启用
     */
    private Integer enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
