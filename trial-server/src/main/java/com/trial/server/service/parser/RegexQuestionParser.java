package com.trial.server.service.parser;

import com.trial.server.dto.QuestionDTO;
import com.trial.server.service.FileImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 正则表达式题目解析器
 * 重用现有的FileImportService逻辑
 */
@Slf4j
@Component
public class RegexQuestionParser implements QuestionParser {
    
    private final FileImportService fileImportService;
    
    public RegexQuestionParser(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }
    
    @Override
    public ParseResult parse(String text, ParseConfig config) {
        long startTime = System.currentTimeMillis();
        ParseResult result = new ParseResult();
        
        try {
            // 使用FileImportService的解析逻辑
            List<QuestionDTO> questions = fileImportService.parseTextToQuestions(
                text,
                config.getType(),
                config.getCategoryId(),
                config.getDifficulty()
            );
            
            result.setQuestions(questions);
            result.setUsedMode(ParseMode.REGEX);
            result.setTokensUsed(0); // 正则解析不消耗token
            result.setCost(0.0);
            result.setParseTime((int) (System.currentTimeMillis() - startTime));
            result.setSuccess(true);
            
            log.info("正则解析成功: 题目数={}, 耗时={}ms", questions.size(), result.getParseTime());
            
        } catch (Exception e) {
            log.error("正则解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setParseTime((int) (System.currentTimeMillis() - startTime));
        }
        
        return result;
    }
    
    @Override
    public boolean supports(ParseMode mode) {
        return mode == ParseMode.REGEX;
    }
}
