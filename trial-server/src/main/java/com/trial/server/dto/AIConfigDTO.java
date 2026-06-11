package com.trial.server.dto;

import lombok.Data;

/**
 * AI配置请求DTO
 */
@Data
public class AIConfigDTO {
    
    /**
     * AI提供商: openai, qianwen, deepseek
     */
    private String provider;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 自定义API地址(可选)
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
}
