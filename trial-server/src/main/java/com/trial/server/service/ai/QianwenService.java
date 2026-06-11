package com.trial.server.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trial.server.dto.QuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 通义千问服务实现类
 */
@Slf4j
@Service
public class QianwenService extends AbstractAIService {
    
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";
    private static final String CHAT_ENDPOINT = "/services/aigc/text-generation/generation";
    private static final int MAX_RETRIES = 2;
    private static final int RETRY_DELAY_MS = 1000;
    
    private final RestTemplate restTemplate;
    
    public QianwenService() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public AIParseResult parse(String text, AIConfig config) {
        long startTime = System.currentTimeMillis();
        AIParseResult result = new AIParseResult();
        
        try {
            String requestBody = buildRequestBody(text, config);
            String baseUrl = (config.getBaseUrl() != null && !config.getBaseUrl().trim().isEmpty()) 
                ? config.getBaseUrl().trim() 
                : DEFAULT_BASE_URL;
            String url = baseUrl + CHAT_ENDPOINT;
            
            String response = callAPIWithRetry(url, requestBody, config.getApiKey());
            JsonNode responseNode = objectMapper.readTree(response);
            
            String content = responseNode.path("output").path("text").asText();
            log.info("通义千问返回内容长度: {}", content.length());
            log.info("通义千问返回原始内容: {}", content);
            
            int tokensUsed = responseNode.path("usage").path("total_tokens").asInt(0);
            
            // 提取 JSON
            String jsonContent = extractJsonFromResponse(content);
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                log.error("无法从响应中提取 JSON 内容");
                result.setSuccess(false);
                result.setErrorMessage("无法从响应中提取 JSON 内容");
                result.setParseTime((int) (System.currentTimeMillis() - startTime));
                return result;
            }
            log.info("提取的 JSON 内容: {}", jsonContent);
            
            List<QuestionDTO> questions = parseAIResponse(jsonContent);
            double cost = calculateCost(tokensUsed, config.getModel());
            
            result.setQuestions(questions);
            result.setTokensUsed(tokensUsed);
            result.setCost(cost);
            result.setParseTime((int) (System.currentTimeMillis() - startTime));
            result.setSuccess(true);
            
            log.info("通义千问解析成功: 题目数={}, tokens={}, 耗时={}ms", questions.size(), tokensUsed, result.getParseTime());
            
        } catch (Exception e) {
            log.error("通义千问解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            result.setParseTime((int) (System.currentTimeMillis() - startTime));
        }
        
        return result;
    }
    
    @Override
    public boolean testConnection(AIConfig config) {
        try {
            log.info("开始测试通义千问连接");
            String requestBody = buildTestRequestBody(config);
            log.info("请求体构建成功");
            
            String baseUrl = (config.getBaseUrl() != null && !config.getBaseUrl().trim().isEmpty()) 
                ? config.getBaseUrl().trim() 
                : DEFAULT_BASE_URL;
            String url = baseUrl + CHAT_ENDPOINT;
            log.info("请求URL: {}", url);
            
            String response = callAPI(url, requestBody, config.getApiKey());
            log.info("API调用成功，响应: {}", response);
            log.info("通义千问连接测试成功");
            return true;
        } catch (Exception e) {
            log.error("通义千问连接测试失败，错误信息: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "qianwen";
    }
    
    private String buildRequestBody(String text, AIConfig config) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", config.getModel() != null ? config.getModel() : "qwen-turbo");
            
            ObjectNode input = root.putObject("input");
            ArrayNode messages = input.putArray("messages");
            
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", buildSystemPrompt());
            
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", buildUserPrompt(text));
            
            ObjectNode parameters = root.putObject("parameters");
            parameters.put("result_format", "text");
            
            if (config.getTemperature() != null) {
                parameters.put("temperature", config.getTemperature());
            }
            if (config.getMaxTokens() != null) {
                parameters.put("max_tokens", config.getMaxTokens());
            }
            
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败", e);
        }
    }
    
    private String buildTestRequestBody(AIConfig config) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", config.getModel() != null ? config.getModel() : "qwen-turbo");
            
            ObjectNode input = root.putObject("input");
            ArrayNode messages = input.putArray("messages");
            
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", "test");
            
            ObjectNode parameters = root.putObject("parameters");
            parameters.put("result_format", "text");
            parameters.put("max_tokens", 10);
            
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("构建测试请求体失败", e);
        }
    }
    
    private String callAPIWithRetry(String url, String requestBody, String apiKey) {
        Exception lastException = null;
        
        for (int i = 0; i <= MAX_RETRIES; i++) {
            try {
                return callAPI(url, requestBody, apiKey);
            } catch (Exception e) {
                lastException = e;
                if (i < MAX_RETRIES) {
                    log.warn("通义千问API调用失败，正在重试 ({}/{})", i + 1, MAX_RETRIES);
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new RuntimeException("通义千问API调用失败，已重试" + MAX_RETRIES + "次", lastException);
    }
    
    private String callAPI(String url, String requestBody, String apiKey) {
        try {
            log.info("准备调用通义千问API");
            log.debug("请求URL: {}", url);
            log.debug("请求体: {}", requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("X-DashScope-SSE", "disable");
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            log.info("API响应状态码: {}", response.getStatusCode());
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMsg = "API调用失败: " + response.getStatusCode() + ", 响应: " + response.getBody();
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            return response.getBody();
        } catch (Exception e) {
            log.error("调用通义千问API时发生异常: {}", e.getMessage(), e);
            throw new RuntimeException("调用通义千问API失败: " + e.getMessage(), e);
        }
    }
    
    protected double calculateCost(int tokensUsed, String model) {
        // 通义千问价格（每百万tokens，人民币）
        double pricePerMillion;
        if (model != null && model.contains("qwen-turbo")) {
            pricePerMillion = 0.3;
        } else if (model != null && model.contains("qwen-plus")) {
            pricePerMillion = 0.8;
        } else if (model != null && model.contains("qwen-max")) {
            pricePerMillion = 4.0;
        } else {
            pricePerMillion = 0.3;
        }
        
        return tokensUsed * pricePerMillion / 1_000_000;
    }
}
