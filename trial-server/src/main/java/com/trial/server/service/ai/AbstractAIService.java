package com.trial.server.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trial.server.dto.QuestionDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 服务抽象基类
 * 提供通用的 JSON 解析和题目提取功能
 */
@Slf4j
public abstract class AbstractAIService implements AIService {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从 AI 响应中提取 JSON 内容
     * 支持纯 JSON 或包含 ```json 代码块的响应
     */
    protected String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return null;
        }

        response = response.trim();

        // 尝试提取 ```json 代码块
        Pattern codeBlockPattern = Pattern.compile("```json\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = codeBlockPattern.matcher(response);
        if (matcher.find()) {
            String json = matcher.group(1).trim();
            log.debug("从代码块中提取 JSON，长度: {}", json.length());
            return json;
        }

        // 尝试提取 { ... } 或 [ ... ] JSON 对象/数组
        int firstBrace = response.indexOf('{');
        int firstBracket = response.indexOf('[');
        
        int startIndex = -1;
        char endChar = 0;
        
        if (firstBrace >= 0 && (firstBracket < 0 || firstBrace < firstBracket)) {
            startIndex = firstBrace;
            endChar = '}';
        } else if (firstBracket >= 0) {
            startIndex = firstBracket;
            endChar = ']';
        }
        
        if (startIndex >= 0) {
            int endIndex = response.lastIndexOf(endChar);
            if (endIndex > startIndex) {
                String json = response.substring(startIndex, endIndex + 1);
                log.debug("从响应中提取 JSON，长度: {}", json.length());
                return json;
            }
        }

        // 如果响应本身就是 JSON
        if (response.startsWith("{") || response.startsWith("[")) {
            log.debug("响应本身是 JSON，长度: {}", response.length());
            return response;
        }

        log.warn("无法从响应中提取 JSON 内容");
        return null;
    }

    /**
     * 解析 AI 返回的 JSON 为题目列表
     */
    protected List<QuestionDTO> parseAIResponse(String jsonContent) {
        List<QuestionDTO> questions = new ArrayList<>();

        try {
            log.info("开始解析 JSON，长度: {}", jsonContent.length());
            JsonNode root = objectMapper.readTree(jsonContent);
            log.info("JSON 解析成功，根节点类型: {}", root.getNodeType());
            JsonNode questionsNode = null;

            // 尝试多种可能的 JSON 结构
            if (root.isArray()) {
                log.info("JSON 是数组类型");
                questionsNode = root;
            } else if (root.has("questions")) {
                log.info("JSON 包含 questions 字段");
                questionsNode = root.get("questions");
            } else if (root.has("data")) {
                log.info("JSON 包含 data 字段");
                JsonNode dataNode = root.get("data");
                if (dataNode.isArray()) {
                    questionsNode = dataNode;
                } else if (dataNode.has("questions")) {
                    questionsNode = dataNode.get("questions");
                }
            }

            if (questionsNode == null || !questionsNode.isArray()) {
                log.error("无法找到题目数组，JSON 结构: {}", root.toPrettyString());
                return questions;
            }

            log.info("找到题目数组，包含 {} 个元素", questionsNode.size());

            // 解析每道题目
            for (JsonNode questionNode : questionsNode) {
                try {
                    QuestionDTO question = parseQuestionNode(questionNode);
                    if (question != null) {
                        questions.add(question);
                        log.debug("成功解析题目: {}", question.getStem());
                    }
                } catch (Exception e) {
                    log.error("解析单个题目失败: {}", e.getMessage());
                }
            }

            log.info("解析完成，共解析出 {} 道题目", questions.size());

        } catch (Exception e) {
            log.error("解析 AI 响应 JSON 失败", e);
        }

        return questions;
    }

    /**
     * 解析单个题目节点
     */
    private QuestionDTO parseQuestionNode(JsonNode node) {
        QuestionDTO question = new QuestionDTO();

        // 题型
        if (node.has("type")) {
            question.setType(node.get("type").asInt());
        }

        // 题干
        if (node.has("stem")) {
            question.setStem(node.get("stem").asText());
        }

        // 选项（选择题）
        if (node.has("options")) {
            JsonNode optionsNode = node.get("options");
            List<String> options = new ArrayList<>();
            if (optionsNode.isArray()) {
                for (JsonNode optionNode : optionsNode) {
                    options.add(optionNode.asText());
                }
            }
            question.setOptions(options);
        }

        // 答案
        if (node.has("answer")) {
            question.setAnswer(node.get("answer").asText());
        }

        // 解析
        if (node.has("analysis")) {
            question.setAnalysis(node.get("analysis").asText());
        }

        // 难度
        if (node.has("difficulty")) {
            question.setDifficulty(node.get("difficulty").asInt());
        }

        return question;
    }

    /**
     * 构建系统提示词
     */
    protected String buildSystemPrompt() {
        return "你是一个专业的题目解析助手。请将用户提供的文本解析为结构化的题目数据。\n\n" +
                "要求：\n" +
                "1. 识别题型：1=单选题，2=多选题，3=判断题，4=填空题，5=简答题\n" +
                "2. 提取题干、选项（如有）、答案、解析（如有）\n" +
                "3. 返回 JSON 数组格式，每个题目包含：type, stem, options(可选), answer, analysis(可选)\n" +
                "4. 只返回 JSON，不要其他说明文字\n\n" +
                "示例格式：\n" +
                "[\n" +
                "  {\n" +
                "    \"type\": 1,\n" +
                "    \"stem\": \"题目内容\",\n" +
                "    \"options\": [\"选项A\", \"选项B\", \"选项C\", \"选项D\"],\n" +
                "    \"answer\": \"A\",\n" +
                "    \"analysis\": \"解析内容\"\n" +
                "  }\n" +
                "]";
    }

    /**
     * 构建用户提示词
     */
    protected String buildUserPrompt(String text) {
        return "请解析以下题目文本：\n\n" + text;
    }

    /**
     * 测试连接
     */
    @Override
    public boolean testConnection(AIConfig config) {
        try {
            AIParseResult result = parse("测试题目：1+1=?", config);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("连接测试失败", e);
            return false;
        }
    }
}
