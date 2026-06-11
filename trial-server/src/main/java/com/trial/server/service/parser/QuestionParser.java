package com.trial.server.service.parser;

import com.trial.server.dto.QuestionDTO;

import java.util.List;

/**
 * 题目解析器接口
 */
public interface QuestionParser {
    
    /**
     * 解析题目文本
     * 
     * @param text 题目文本
     * @param config 解析配置
     * @return 解析出的题目列表
     */
    ParseResult parse(String text, ParseConfig config);
    
    /**
     * 是否支持指定的解析模式
     */
    boolean supports(ParseMode mode);
    
    /**
     * 解析结果类
     */
    class ParseResult {
        private List<QuestionDTO> questions;
        private ParseMode usedMode;
        private Integer tokensUsed;
        private Double cost;
        private Integer parseTime;
        private boolean success;
        private String errorMessage;
        
        public ParseResult() {}
        
        public List<QuestionDTO> getQuestions() { return questions; }
        public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }
        
        public ParseMode getUsedMode() { return usedMode; }
        public void setUsedMode(ParseMode usedMode) { this.usedMode = usedMode; }
        
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
