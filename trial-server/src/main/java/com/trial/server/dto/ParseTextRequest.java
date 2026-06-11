package com.trial.server.dto;

import lombok.Data;

/**
 * 文本解析请求
 */
@Data
public class ParseTextRequest {
    
    /** 要解析的文本内容 */
    private String text;
    
    /** 指定题型（可选）：1-单选 2-多选 3-判断 4-填空 5-简答 */
    private Integer type;
    
    /** 指定分类ID（可选） */
    private Long categoryId;
    
    /** 指定难度（可选）：1-5 */
    private Integer difficulty;
    
    /** AI 提供商（可选）：openai, qianwen, deepseek */
    private String aiProvider;
    
    /** AI 模型（可选） */
    private String model;
    
    /** 解析模式（可选）：regex, ai, hybrid */
    private String parseMode;
}
