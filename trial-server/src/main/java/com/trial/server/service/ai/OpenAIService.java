package com.trial.server.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trial.server.dto.QuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * OpenAI服务实现类
 */
@Slf4j
@Service
public class OpenAIService extends AbstractAIService {
    
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 1000;
    
    private final RestTemplate restTemplate;
    
    public OpenAIService() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public AIParseResult parse(String text, AIConfig config) {
        long startTime = System.currentTimeMillis();
        AIParseResult result = new AIParseResult();
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(text, config);
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
            String url = baseUrl + CHAT_COMPLETIONS_ENDPOINT;
            
            // 调用API（带重试）
            String response = callAPIWithRetry(url, requestBody, config.getApiKey());
            
            // 解析响应
            JsonNode responseNode = objectMapper.readTree(response);
            
            // 提取内容
            String content = responseNode
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
            
            // 提取token使用情况
            int tokensUsed = responseNode
                .path("usage")
                .path("total_tokens")
                .asInt(0);
            
            // 解析题目
            List<QuestionDTO> questions = parseAIResponse(content);
            
            // 计算成本
            double cost = calculateCost(tokensUsed, config.getModel());
            
            // 设置结果
            result.setQuestions(questions);
            result.setTokensUsed(tokensUsed);
            result.setCost(cost);
            result.setParseTime((int) (System.currentTimeMillis() - startTime));
            result.setSuccess(true);
            
            log.info("OpenAI解析成功: 题目数={}, tokens={}, 成本=${}, 耗时={}ms",
                questions.size(), tokensUsed, cost, result.getParseTime());
            
        } catch (Exception e) {
            log.error("OpenAI解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setParseTime((int) (System.currentTimeMillis() - startTime));
        }
        
        return result;
    }
    
    @Override
    public boolean testConnection(AIConfig config) {
        try {
            String requestBody = buildTestRequestBody(config);
            String baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
            String url = baseUrl + CHAT_COMPLETIONS_ENDPOINT;
            
            callAPI(url, requestBody, config.getApiKey());
            return true;
        } catch (Exception e) {
            log.error("OpenAI连接测试失败", e);
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "openai";
    }
    
    /**
     * 构建请求体
     */
    private String buildRequestBody(String text, AIConfig config) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", config.getModel() != null ? config.getModel() : "gpt-4o-mini");
            root.put("temperature", config.getTemperature() != null ? config.getTemperature() : 0.3);
            root.put("max_tokens", config.getMaxTokens() != null ? config.getMaxTokens() : 4000);
            
            ArrayNode messages = root.putArray("messages");
            
            // 系统消息
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", buildSystemPrompt());
            
            // 用户消息
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", buildUserPrompt(text));
            
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败", e);
        }
    }
    
    /**
     * 构建测试请求体
     */
    private String buildTestRequestBody(AIConfig config) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", config.getModel() != null ? config.getModel() : "gpt-4o-mini");
            root.put("max_tokens", 10);
            
            ArrayNode messages = root.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", "test");
            
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("构建测试请求体失败", e);
        }
    }
    
    /**
     * 调用API（带重试）
     */
    private String callAPIWithRetry(String url, String requestBody, String apiKey) {
        Exception lastException = null;
        
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                return callAPI(url, requestBody, apiKey);
            } catch (Exception e) {
                lastException = e;
                if (i < MAX_RETRIES) {
                    log.warn("OpenAI API调用失败，正在重试 ({}/{})", i + 1, MAX_RETRIES);
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new RuntimeException("OpenAI API调用失败，已重试" + MAX_RETRIES + "次", lastException);
    }
    
    /**
     * 调用API
     */
    private String callAPI(String url, String requestBody, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("API调用失败: " + response.getStatusCode());
        }
        
        return response.getBody();
    }
    
    protected double calculateCost(int tokensUsed, String model) {
        // OpenAI价格（每百万tokens）
        double pricePerMillion;
        if (model != null && model.contains("gpt-4o-mini")) {
            pricePerMillion = 0.15;
        } else if (model != null && model.contains("gpt-4o")) {
            pricePerMillion = 2.5;
        } else if (model != null && model.contains("gpt-3.5-turbo")) {
            pricePerMillion = 0.5;
        } else {
            pricePerMillion = 0.15; // 默认
        }
        
        return tokensUsed * pricePerMillion / 1_000_000;
    }
}
