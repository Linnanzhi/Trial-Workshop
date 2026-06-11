package com.trial.server.service;

import com.trial.server.common.BusinessException;
import com.trial.server.common.JudgmentAnswerUtil;
import com.trial.server.dto.ParseResult;
import com.trial.server.dto.QuestionDTO;
import com.trial.server.entity.AIParseLog;
import com.trial.server.mapper.AIParseLogMapper;
import com.trial.server.service.ai.AIService;
import com.trial.server.service.ai.OpenAIService;
import com.trial.server.service.ai.QianwenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件导入服务 — 支持 Word(.docx) / PDF / 纯文本 三种格式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileImportService {

    private final AIConfigService aiConfigService;
    private final OpenAIService openAIService;
    private final QianwenService qianwenService;
    private final AIParseLogMapper aiParseLogMapper;

    /**
     * 解析上传的文件，返回识别到的题目列表（不入库）
     */
    public List<QuestionDTO> parseFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("文件名不能为空");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        String text;

        try (InputStream is = file.getInputStream()) {
            switch (ext) {
                case "docx":
                    text = extractTextFromWord(is);
                    break;
                case "pdf":
                    text = extractTextFromPdf(is);
                    break;
                case "txt":
                    text = new String(file.getBytes(), "UTF-8");
                    break;
                default:
                    throw new BusinessException("不支持的文件格式: " + ext + "，仅支持 .docx / .pdf / .txt");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件解析异常: {}", e.getMessage(), e);
            throw new BusinessException("文件解析失败: " + e.getMessage());
        }

        List<QuestionDTO> results = parseTextToQuestions(text);
        if (results.isEmpty()) {
            throw new BusinessException("未能从文件中识别到任何题目，请检查格式");
        }
        return results;
    }

    // ===== 文本提取 =====

    /**
     * 从 Word (.docx) 提取纯文本
     */
    private String extractTextFromWord(InputStream is) throws Exception {
        XWPFDocument doc = new XWPFDocument(is);
        StringBuilder sb = new StringBuilder();
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            String line = paragraph.getText().trim();
            if (!line.isEmpty()) {
                sb.append(line).append("\n");
            }
        }
        doc.close();
        return sb.toString();
    }

    /**
     * 从 PDF 提取纯文本
     */
    private String extractTextFromPdf(InputStream is) throws Exception {
        PDDocument doc = PDDocument.load(is);
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    // ===== 文本 → 结构化题目 =====

    /**
     * 解析文本为题目列表（简化版，不指定类型/分类/难度）
     */
    public List<QuestionDTO> parseTextToQuestions(String text) {
        return parseTextToQuestions(text, null, null, null);
    }

    /**
     * 解析文本为题目列表（完整版，返回解析结果包含未解析文本）
     */
    public ParseResult parseTextToQuestionsWithResult(String text, Integer specifiedType, 
                                                      Long categoryId, Integer difficulty) {
        List<QuestionDTO> questions = new ArrayList<>();
        List<ParseResult.UnparsedBlock> unparsedBlocks = new ArrayList<>();
        
        log.info("开始解析文本，文本长度: {}", text.length());
        
        // 尝试按题号分割（如：1. 或 1、 或 84、）
        String[] blocks = text.split("(?=\\d+[.、]\\s*)");
        
        log.info("按题号分割后得到 {} 个块", blocks.length);
        
        // 如果按题号分割失败（只有1个块），则按空行分割
        if (blocks.length <= 1) {
            blocks = text.split("\\n\\s*\\n");
            log.info("改用空行分割，得到 {} 个块", blocks.length);
        }
        
        int currentIndex = 0;
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i].trim();
            if (block.isEmpty()) {
                currentIndex += blocks[i].length();
                continue;
            }
            
            log.info("解析第 {} 个块，长度: {}, 内容前50字符: {}", 
                i + 1, block.length(), block.substring(0, Math.min(50, block.length())));
            
            QuestionDTO question = parseQuestionBlock(block, specifiedType, categoryId, difficulty);
            if (question != null) {
                questions.add(question);
                log.info("成功解析题目，题干: {}, 答案: {}", question.getStem(), question.getAnswer());
            } else {
                // 记录未解析的块
                int startIndex = currentIndex;
                int endIndex = currentIndex + block.length();
                unparsedBlocks.add(new ParseResult.UnparsedBlock(block, startIndex, endIndex));
                log.warn("解析失败，块内容: {}", block);
            }
            
            currentIndex += blocks[i].length();
        }
        
        log.info("解析完成，共解析出 {} 道题目，{} 个未解析块", questions.size(), unparsedBlocks.size());
        
        ParseResult result = new ParseResult();
        result.setQuestions(questions);
        result.setUnparsedBlocks(unparsedBlocks);
        return result;
    }

    /**
     * 解析文本为题目列表（完整版）
     */
    public List<QuestionDTO> parseTextToQuestions(String text, Integer specifiedType, 
                                                   Long categoryId, Integer difficulty) {
        List<QuestionDTO> questions = new ArrayList<>();
        
        log.info("开始解析文本，文本长度: {}", text.length());
        
        // 尝试按题号分割（如：1. 或 1、 或 84、）
        String[] blocks = text.split("(?=\\d+[.、]\\s*)");
        
        log.info("按题号分割后得到 {} 个块", blocks.length);
        
        // 如果按题号分割失败（只有1个块），则按空行分割
        if (blocks.length <= 1) {
            blocks = text.split("\\n\\s*\\n");
            log.info("改用空行分割，得到 {} 个块", blocks.length);
        }
        
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i].trim();
            if (block.isEmpty()) {
                continue;
            }
            
            log.info("解析第 {} 个块，长度: {}, 内容前50字符: {}", 
                i + 1, block.length(), block.substring(0, Math.min(50, block.length())));
            
            QuestionDTO question = parseQuestionBlock(block, specifiedType, categoryId, difficulty);
            if (question != null) {
                questions.add(question);
                log.info("成功解析题目，题干: {}, 答案: {}", question.getStem(), question.getAnswer());
            } else {
                log.warn("解析失败，块内容: {}", block);
            }
        }
        
        log.info("解析完成，共解析出 {} 道题目", questions.size());
        
        return questions;
    }

    /**
     * 解析单个题目块
     */
    private QuestionDTO parseQuestionBlock(String block, Integer specifiedType, 
                                           Long categoryId, Integer difficulty) {
        QuestionDTO question = new QuestionDTO();
        
        // 设置指定的属性
        if (categoryId != null) {
            question.setCategoryId(categoryId);
        }
        if (difficulty != null) {
            question.setDifficulty(difficulty);
        }
        
        // 尝试识别题型
        Integer type = specifiedType != null ? specifiedType : detectQuestionType(block);
        question.setType(type);
        
        // 根据题型解析
        switch (type) {
            case 1: // 单选题
            case 2: // 多选题
                return parseChoiceQuestion(block, type);
            case 3: // 判断题
                return parseJudgmentQuestion(block);
            case 4: // 填空题
                return parseBlankQuestion(block);
            case 5: // 简答题
                return parseEssayQuestion(block);
            default:
                return null;
        }
    }

    /**
     * 检测题型
     */
    private Integer detectQuestionType(String block) {
        // 判断题特征
        if (block.matches("(?s).*[（(]\\s*[对错√×✓✗TFtf]\\s*[)）].*")) {
            return 3;
        }
        
        // 选择题特征（有ABCD选项，支持多种分隔符）
        if (block.matches("(?s).*[A-D][.、．：:].*")) {
            // 检查是否有"多选"关键字
            if (block.contains("多选") || block.contains("多项")) {
                return 2;
            }
            return 1;
        }
        
        // 填空题特征
        if (block.contains("____") || block.contains("___")) {
            return 4;
        }
        
        // 默认简答题
        return 5;
    }

    /**
     * 解析选择题
     */
    private QuestionDTO parseChoiceQuestion(String block, Integer type) {
        QuestionDTO question = new QuestionDTO();
        question.setType(type);
        
        log.debug("开始解析选择题，原始块: {}", block);
        
        // 移除题号（如：84、 或 1. ）
        block = block.replaceFirst("^\\d+[.、]\\s*", "");
        log.debug("移除题号后: {}", block);
        
        // 提取题干（第一行或到第一个选项之前）
        Pattern stemPattern = Pattern.compile("^(.+?)(?=[A-D][.、．：:])", Pattern.DOTALL);
        Matcher stemMatcher = stemPattern.matcher(block);
        if (stemMatcher.find()) {
            question.setStem(stemMatcher.group(1).trim());
            log.debug("提取题干: {}", question.getStem());
        }
        
        // 提取选项
        List<String> options = new ArrayList<>();
        Pattern optionPattern = Pattern.compile("[A-D][.、．：:]\\s*(.+?)(?=[A-D][.、．：:]|答案|$)", Pattern.DOTALL);
        Matcher optionMatcher = optionPattern.matcher(block);
        while (optionMatcher.find()) {
            String option = optionMatcher.group(1).trim();
            options.add(option);
            log.debug("提取选项: {}", option);
        }
        question.setOptions(options);
        
        // 提取答案 - 优先从题干中的括号提取
        String answer = null;
        
        // 方式1: 从题干中的括号提取答案，如：题目（A）或 题目（  D  ）
        if (question.getStem() != null) {
            Pattern bracketAnswerPattern = Pattern.compile("[（(]\\s*([A-D]+)\\s*[)）]");
            Matcher bracketMatcher = bracketAnswerPattern.matcher(question.getStem());
            if (bracketMatcher.find()) {
                answer = bracketMatcher.group(1).trim();
                log.debug("从括号中提取答案: {}", answer);
                // 从题干中移除答案括号，并清理末尾的句号
                question.setStem(question.getStem()
                    .replaceAll("[（(]\\s*[A-D]+\\s*[)）]", "")
                    .replaceAll("[。.]+$", "")
                    .trim());
                log.debug("清理后的题干: {}", question.getStem());
            }
        }
        
        // 方式2: 从"答案："后面提取
        if (answer == null) {
            Pattern answerPattern = Pattern.compile("答案[：:】]?\\s*([A-D]+)");
            Matcher answerMatcher = answerPattern.matcher(block);
            if (answerMatcher.find()) {
                answer = answerMatcher.group(1).trim();
                log.debug("从答案行提取答案: {}", answer);
            }
        }
        
        question.setAnswer(answer);
        
        // 提取解析
        Pattern analysisPattern = Pattern.compile("解析[：:】]?\\s*(.+)$", Pattern.DOTALL);
        Matcher analysisMatcher = analysisPattern.matcher(block);
        if (analysisMatcher.find()) {
            question.setAnalysis(analysisMatcher.group(1).trim());
        }
        
        // 验证必填字段：题干和答案必须存在
        if (question.getStem() == null || question.getStem().trim().isEmpty() ||
            question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            log.warn("题目验证失败 - 题干: {}, 答案: {}", question.getStem(), question.getAnswer());
            return null;
        }
        
        log.debug("选择题解析成功");
        return question;
    }

    /**
     * 解析判断题
     */
    private QuestionDTO parseJudgmentQuestion(String block) {
        QuestionDTO question = new QuestionDTO();
        question.setType(3);
        
        // 提取题干
        Pattern stemPattern = Pattern.compile("^(.+?)(?=答案|$)", Pattern.DOTALL);
        Matcher stemMatcher = stemPattern.matcher(block);
        if (stemMatcher.find()) {
            question.setStem(stemMatcher.group(1).trim());
        }
        
        // 提取答案
        Pattern answerPattern = Pattern.compile("答案[：:】]?\\s*([对错√×✓✗TFtf])");
        Matcher answerMatcher = answerPattern.matcher(block);
        if (answerMatcher.find()) {
            String rawAnswer = answerMatcher.group(1);
            question.setAnswer(JudgmentAnswerUtil.normalizeJudgmentAnswer(rawAnswer));
        }
        
        // 提取解析
        Pattern analysisPattern = Pattern.compile("解析[：:】]?\\s*(.+)$", Pattern.DOTALL);
        Matcher analysisMatcher = analysisPattern.matcher(block);
        if (analysisMatcher.find()) {
            question.setAnalysis(analysisMatcher.group(1).trim());
        }
        
        // 验证必填字段：题干和答案必须存在
        if (question.getStem() == null || question.getStem().trim().isEmpty() ||
            question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            return null;
        }
        
        return question;
    }

    /**
     * 解析填空题
     */
    private QuestionDTO parseBlankQuestion(String block) {
        QuestionDTO question = new QuestionDTO();
        question.setType(4);
        
        // 提取题干
        Pattern stemPattern = Pattern.compile("^(.+?)(?=答案|$)", Pattern.DOTALL);
        Matcher stemMatcher = stemPattern.matcher(block);
        if (stemMatcher.find()) {
            question.setStem(stemMatcher.group(1).trim());
        }
        
        // 提取答案
        Pattern answerPattern = Pattern.compile("答案[：:】]?\\s*(.+?)(?=解析|$)", Pattern.DOTALL);
        Matcher answerMatcher = answerPattern.matcher(block);
        if (answerMatcher.find()) {
            question.setAnswer(answerMatcher.group(1).trim());
        }
        
        // 提取解析
        Pattern analysisPattern = Pattern.compile("解析[：:】]?\\s*(.+)$", Pattern.DOTALL);
        Matcher analysisMatcher = analysisPattern.matcher(block);
        if (analysisMatcher.find()) {
            question.setAnalysis(analysisMatcher.group(1).trim());
        }
        
        // 验证必填字段：题干和答案必须存在
        if (question.getStem() == null || question.getStem().trim().isEmpty() ||
            question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            return null;
        }
        
        return question;
    }

    /**
     * 解析简答题
     */
    private QuestionDTO parseEssayQuestion(String block) {
        QuestionDTO question = new QuestionDTO();
        question.setType(5);
        
        // 提取题干
        Pattern stemPattern = Pattern.compile("^(.+?)(?=答案|参考答案|$)", Pattern.DOTALL);
        Matcher stemMatcher = stemPattern.matcher(block);
        if (stemMatcher.find()) {
            question.setStem(stemMatcher.group(1).trim());
        }
        
        // 提取答案
        Pattern answerPattern = Pattern.compile("(?:答案|参考答案)[：:】]?\\s*(.+?)(?=解析|$)", Pattern.DOTALL);
        Matcher answerMatcher = answerPattern.matcher(block);
        if (answerMatcher.find()) {
            question.setAnswer(answerMatcher.group(1).trim());
        }
        
        // 提取解析
        Pattern analysisPattern = Pattern.compile("解析[：:】]?\\s*(.+)$", Pattern.DOTALL);
        Matcher analysisMatcher = analysisPattern.matcher(block);
        if (analysisMatcher.find()) {
            question.setAnalysis(analysisMatcher.group(1).trim());
        }
        
        // 验证必填字段：题干和答案必须存在
        if (question.getStem() == null || question.getStem().trim().isEmpty() ||
            question.getAnswer() == null || question.getAnswer().trim().isEmpty()) {
            return null;
        }
        
        return question;
    }

    /**
     * 使用 AI 解析文本题目
     * 自动选择已配置的 AI 服务
     */
    public List<QuestionDTO> parseTextWithAI(String text, String aiProvider, String model,
                                              Integer specifiedType, Long categoryId, Integer difficulty) {
        log.info("请求使用 AI 解析题目: provider={}, model={}, textLength={}", aiProvider, model, text.length());
        
        Long userId = com.trial.server.config.SecurityUtil.getCurrentUserId();
        AIParseLog parseLog = new AIParseLog();
        parseLog.setUserId(userId);
        parseLog.setInputText(text.length() > 1000 ? text.substring(0, 1000) : text);
        parseLog.setCreateTime(LocalDateTime.now());
        
        try {
            // 如果指定的提供商未配置，自动选择已配置的提供商
            String actualProvider = aiProvider;
            try {
                aiConfigService.getDecryptedConfig(aiProvider);
                log.info("使用指定的 AI 提供商: {}", aiProvider);
            } catch (BusinessException e) {
                log.warn("指定的提供商 {} 未配置，尝试自动选择已配置的提供商", aiProvider);
                actualProvider = findAvailableProvider();
                log.info("自动选择已配置的提供商: {}", actualProvider);
            }
            
            parseLog.setProvider(actualProvider);
            
            // 获取 AI 配置
            AIService.AIConfig config = aiConfigService.getDecryptedConfig(actualProvider);
            
            // 如果传入了 model，使用传入的 model，否则使用配置中的 model
            String actualModel = model;
            if (model != null && !model.trim().isEmpty()) {
                config = new AIService.AIConfig(
                    config.getApiKey(),
                    model,
                    config.getBaseUrl(),
                    config.getMaxTokens(),
                    config.getTemperature()
                );
            } else {
                actualModel = config.getModel();
            }
            
            parseLog.setModel(actualModel);
            
            // 根据提供商选择服务
            AIService aiService;
            switch (actualProvider.toLowerCase()) {
                case "openai":
                    aiService = openAIService;
                    break;
                case "qianwen":
                    aiService = qianwenService;
                    break;
                default:
                    throw new BusinessException("不支持的 AI 提供商: " + actualProvider);
            }
            
            // 调用 AI 解析
            log.info("开始调用 {} 进行解析", actualProvider);
            AIService.AIParseResult result = aiService.parse(text, config);
            
            if (!result.isSuccess()) {
                log.error("AI 解析失败: {}", result.getErrorMessage());
                
                // 保存失败日志
                parseLog.setSuccess(0);
                parseLog.setErrorMessage(result.getErrorMessage());
                parseLog.setParseTime(result.getParseTime());
                aiParseLogMapper.insert(parseLog);
                
                throw new BusinessException("AI 解析失败: " + result.getErrorMessage());
            }
            
            List<QuestionDTO> questions = result.getQuestions();
            log.info("AI 解析成功，解析出 {} 道题目，耗时 {}ms，消耗 {} tokens",
                    questions.size(), result.getParseTime(), result.getTokensUsed());
            
            // 保存成功日志
            parseLog.setSuccess(1);
            parseLog.setQuestionsCount(questions.size());
            parseLog.setTokensUsed(result.getTokensUsed());
            parseLog.setCost(BigDecimal.valueOf(result.getCost()));
            parseLog.setParseTime(result.getParseTime());
            aiParseLogMapper.insert(parseLog);
            
            // 设置指定的属性
            if (categoryId != null || difficulty != null || specifiedType != null) {
                for (QuestionDTO question : questions) {
                    if (categoryId != null) {
                        question.setCategoryId(categoryId);
                    }
                    if (difficulty != null) {
                        question.setDifficulty(difficulty);
                    }
                    if (specifiedType != null) {
                        question.setType(specifiedType);
                    }
                }
            }
            
            return questions;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI 解析异常", e);
            
            // 保存异常日志
            parseLog.setSuccess(0);
            parseLog.setErrorMessage(e.getMessage());
            try {
                aiParseLogMapper.insert(parseLog);
            } catch (Exception logException) {
                log.error("保存解析日志失败", logException);
            }
            
            throw new BusinessException("AI 解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 查找可用的 AI 提供商
     * 按优先级：qianwen > openai > deepseek
     */
    private String findAvailableProvider() {
        Long userId = com.trial.server.config.SecurityUtil.getCurrentUserId();
        
        // 按优先级尝试
        String[] providers = {"qianwen", "openai", "deepseek"};
        
        for (String provider : providers) {
            try {
                aiConfigService.getDecryptedConfig(provider);
                log.info("找到可用的提供商: {}", provider);
                return provider;
            } catch (BusinessException e) {
                log.debug("提供商 {} 未配置", provider);
            }
        }
        
        throw new BusinessException("未配置任何 AI 服务，请先在 AI 配置页面配置至少一个 AI 提供商");
    }
}
