package com.trial.server.service.parser;

import com.trial.server.dto.QuestionDTO;
import com.trial.server.entity.AIConfig;
import com.trial.server.mapper.AIConfigMapper;
import com.trial.server.service.ai.AIService;
import com.trial.server.service.ai.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI题目解析器
 */
@Slf4j
@Component
public class AIQuestionParser implements QuestionParser {
    
    private final OpenAIService openAIService;
    private final AIConfigMapper aiConfigMapper;
    
    public AIQuestionParser(OpenAIService openAIService, AIConfigMapper aiConfigMapper) {
        this.openAIService = openAIService;
        this.aiConfigMapper = aiConfigMapper;
    }
    
    @Override
    public ParseResult parse(String text, ParseConfig config) {
        ParseResult result = new ParseResult();
        
        try {
            // 获取AI配置
            AIService.AIConfig aiConfig = getAIConfig(config);
            
            // 调用AI服务
            AIService.AIParseResult aiResult = openAIService.parse(text, aiConfig);
            
            // 转换结果
            result.setQuestions(aiResult.getQuestions());
            result.setUsedMode(ParseMode.AI);
            result.setTokensUsed(aiResult.getTokensUsed());
            result.setCost(aiResult.getCost());
            result.setParseTime(aiResult.getParseTime());
            result.setSuccess(aiResult.isSuccess());
            result.setErrorMessage(aiResult.getErrorMessage());
            
            // 应用配置的题型、分类、难度
            if (result.isSuccess() && result.getQuestions() != null) {
                for (QuestionDTO question : result.getQuestions()) {
                    if (config.getType() != null) {
                        question.setType(config.getType());
                    }
                    if (config.getCategoryId() != null) {
                        question.setCategoryId(config.getCategoryId());
                    }
                    if (config.getDifficulty() != null) {
                        question.setDifficulty(config.getDifficulty());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("AI解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean supports(ParseMode mode) {
        return mode == ParseMode.AI;
    }
    
    /**
     * 获取AI配置
     */
    private AIService.AIConfig getAIConfig(ParseConfig config) {
        // 从数据库获取用户的AI配置
        // 这里简化处理，实际应该根据userId查询
        // TODO: 需要从SecurityContext获取当前用户ID
        
        String provider = config.getAiProvider() != null ? config.getAiProvider() : "openai";
        String model = config.getAiModel() != null ? config.getAiModel() : "gpt-4o-mini";
        
        // 暂时使用默认配置，实际应该从数据库读取
        return new AIService.AIConfig(
            null, // apiKey需要从数据库读取
            model,
            null, // baseUrl
            4000, // maxTokens
            0.3   // temperature
        );
    }
}
