package com.trial.server.dto;

import lombok.Data;

import java.util.List;

/**
 * AI解析响应DTO
 */
@Data
public class AIParseResponse {
    
    /**
     * 解析出的题目列表
     */
    private List<QuestionDTO> questions;
    
    /**
     * 元数据
     */
    private MetaData meta;
    
    @Data
    public static class MetaData {
        /**
         * 题目总数
         */
        private Integer totalQuestions;
        
        /**
         * 解析耗时(毫秒)
         */
        private Integer parseTime;
        
        /**
         * 消耗的token数
         */
        private Integer tokensUsed;
        
        /**
         * 预估成本(美元)
         */
        private Double cost;
    }
}
