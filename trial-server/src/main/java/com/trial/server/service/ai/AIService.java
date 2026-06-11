package com.trial.server.service.ai;

import com.trial.server.dto.QuestionDTO;

import java.util.List;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 解析题目文本
     * 
     * @param text 题目文本
     * @param config AI配置
     * @return 解析出的题目列表
     */
    AIParseResult parse(String text, AIConfig config);
    
    /**
     * 测试连接
     * 
     * @param config AI配置
     * @return 是否连接成功
     */
    boolean testConnection(AIConfig config);
    
    /**
     * 获取提供商名称
     */
    String getProviderName();
    
    /**
     * AI配置类
     */
    class AIConfig {
        private String apiKey;
        private String model;
        private String baseUrl;
        private Integer maxTokens;
        private Double temperature;
        
        public AIConfig(String apiKey, String model, String baseUrl, Integer maxTokens, Double temperature) {
            this.apiKey = apiKey;
            this.model = model;
            this.baseUrl = baseUrl;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
        }
        
        public String getApiKey() { return apiKey; }
        public String getModel() { return model; }
        public String getBaseUrl() { return baseUrl; }
        public Integer getMaxTokens() { return maxTokens; }
        public Double getTemperature() { return temperature; }
    }
    
    /**
     * AI解析结果类
     */
    class AIParseResult {
        private List<QuestionDTO> questions;
        private Integer tokensUsed;
        private Double cost;
        private Integer parseTime;
        private boolean success;
        private String errorMessage;
        
        public AIParseResult() {}
        
        public List<QuestionDTO> getQuestions() { return questions; }
        public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
        
        public Integer getTokensUsed() { return tokensUsed; }
        public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
        
        public Double getCost() { return cost; }
        public void setCost(Double cost) { this.cost = cost; }
        
        public Integer getParseTime() { return parseTime; }
        public void setParseTime(Integer parseTime) { this.parseTime = parseTime; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
