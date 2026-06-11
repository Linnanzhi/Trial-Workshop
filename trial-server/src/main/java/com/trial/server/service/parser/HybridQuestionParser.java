package com.trial.server.service.parser;

import com.trial.server.dto.QuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 混合题目解析器
 * 先尝试正则解析，失败时自动降级到AI解析
 */
@Slf4j
@Component
public class HybridQuestionParser implements QuestionParser {
    
    private final RegexQuestionParser regexParser;
    private final AIQuestionParser aiParser;
    
    // 标准格式检测模式
    private static final Pattern QUESTION_NUM_PATTERN = Pattern.compile("^\\s*\\d+\\s*[.、．)）]");
    private static final Pattern OPTION_PATTERN = Pattern.compile("^\\s*[（(]?\\s*[A-Za-z]\\s*[)）]?\\s*[.、．:：]");
    
    public HybridQuestionParser(RegexQuestionParser regexParser, AIQuestionParser aiParser) {
        this.regexParser = regexParser;
        this.aiParser = aiParser;
    }
    
    @Override
    public ParseResult parse(String text, ParseConfig config) {
        log.info("混合解析模式：开始解析");
        
        // 1. 先尝试正则解析
        ParseResult regexResult = regexParser.parse(text, config);
        
        // 2. 判断正则解析是否成功
        if (shouldUseAI(regexResult, text)) {
            log.info("正则解析失败或置信度低，切换到AI解析");
            
            // 3. 使用AI解析
            ParseResult aiResult = aiParser.parse(text, config);
            
            if (aiResult.isSuccess()) {
                aiResult.setUsedMode(ParseMode.HYBRID);
                log.info("AI解析成功，使用AI结果");
                return aiResult;
            } else {
                log.warn("AI解析也失败，返回正则解析结果");
                regexResult.setUsedMode(ParseMode.HYBRID);
                return regexResult;
            }
        }
        
        // 4. 正则解析成功，直接返回
        log.info("正则解析成功，使用正则结果");
        regexResult.setUsedMode(ParseMode.HYBRID);
        return regexResult;
    }
    
    @Override
    public boolean supports(ParseMode mode) {
        return mode == ParseMode.HYBRID || mode == ParseMode.AUTO;
    }
    
    /**
     * 判断是否应该使用AI解析
     */
    private boolean shouldUseAI(ParseResult regexResult, String text) {
        // 1. 正则解析失败
        if (!regexResult.isSuccess()) {
            return true;
        }
        
        // 2. 没有解析出任何题目
        if (regexResult.getQuestions() == null || regexResult.getQuestions().isEmpty()) {
            return true;
        }
        
        // 3. 检测文本格式是否标准
        if (!isStandardFormat(text)) {
            log.info("检测到非标准格式，建议使用AI解析");
            // 注意：这里不强制使用AI，因为正则已经成功了
            // 可以根据业务需求调整策略
            return false;
        }
        
        // 4. 检查解析结果的质量
        if (hasLowConfidence(regexResult.getQuestions())) {
            log.info("解析结果置信度低，建议使用AI解析");
            return false;
        }
        
        return false;
    }
    
    /**
     * 检测文本是否为标准格式
     */
    private boolean isStandardFormat(String text) {
        String[] lines = text.split("\\r?\\n");
        int questionCount = 0;
        int optionCount = 0;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (QUESTION_NUM_PATTERN.matcher(line).find()) {
                questionCount++;
            }
            if (OPTION_PATTERN.matcher(line).find()) {
                optionCount++;
            }
        }
        
        // 至少有1道题目，且有选项
        return questionCount > 0 && optionCount > 0;
    }
    
    /**
     * 检查解析结果是否置信度低
     */
    private boolean hasLowConfidence(List<QuestionDTO> questions) {
        for (QuestionDTO question : questions) {
            // 题干为空或过短
            if (question.getStem() == null || question.getStem().trim().length() < 5) {
                return true;
            }
            
            // 选择题没有选项
            if (question.getType() != null && 
                (question.getType() == 1 || question.getType() == 2) &&
                (question.getOptions() == null || question.getOptions().isEmpty())) {
                return true;
            }
            
            // 没有答案
            if (question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
                return true;
            }
        }
        
        return false;
    }
}
