package com.trial.server.service.parser;

/**
 * 解析配置类
 */
public class ParseConfig {
    
    /**
     * 解析模式
     */
    private ParseMode mode;
    
    /**
     * AI提供商（当mode为AI或HYBRID时需要）
     */
    private String aiProvider;
    
    /**
     * AI模型
     */
    private String aiModel;
    
    /**
     * 题型（可选）
     */
    private Integer type;
    
    /**
     * 分类ID（可选）
     */
    private Long categoryId;
    
    /**
     * 难度（可选）
     */
    private Integer difficulty;
    
    public ParseConfig() {}
    
    public ParseConfig(ParseMode mode) {
        this.mode = mode;
    }
    
    public ParseMode getMode() { return mode; }
    public void setMode(ParseMode mode) { this.mode = mode; }
    
    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }
    
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
}
