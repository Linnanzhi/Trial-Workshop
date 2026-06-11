package com.trial.server.dto;

import lombok.Data;

/**
 * AI解析请求DTO
 */
@Data
public class AIParseRequest {
    
    /**
     * 题目文本内容
     */
    private String text;
    
    /**
     * AI提供商: openai, qianwen, deepseek
     */
    private String aiProvider;
    
    /**
     * 模型名称(可选，默认使用配置的模型)
     */
    private String model;
    
    /**
     * 题型(可选)
     */
    private Integer type;
    
    /**
     * 分类ID(可选)
     */
    private Long categoryId;
    
    /**
     * 难度(可选)
     */
    private Integer difficulty;
}
