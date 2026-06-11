package com.trial.server.controller;

import com.trial.server.common.Result;
import com.trial.server.config.SecurityUtil;
import com.trial.server.dto.AIConfigDTO;
import com.trial.server.dto.AIUsageStatsDTO;
import com.trial.server.entity.AIConfig;
import com.trial.server.service.AIConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 配置控制器
 */
@Slf4j
@Api(tags = "AI 智能解析配置")
@RestController
@RequestMapping("/ai-config")
@RequiredArgsConstructor
public class AIConfigController {

    private final AIConfigService aiConfigService;

    @ApiOperation("获取 AI 配置")
    @GetMapping
    public Result<Map<String, Object>> getConfig() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            
            // 获取用户的所有配置
            List<AIConfig> configs = aiConfigService.getUserConfigs(userId);
            
            // 构建响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("enabled", true);
            
            // 构建提供商列表
            List<Map<String, Object>> providers = new ArrayList<>();
            providers.add(buildProviderInfo("openai", "OpenAI", 
                    Arrays.asList("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo"), 
                    "gpt-4o-mini", configs));
            providers.add(buildProviderInfo("qianwen", "通义千问", 
                    Arrays.asList("qwen-turbo", "qwen-plus", "qwen-max"), 
                    "qwen-turbo", configs));
            providers.add(buildProviderInfo("deepseek", "DeepSeek", 
                    Arrays.asList("deepseek-chat"), 
                    "deepseek-chat", configs));
            
            response.put("providers", providers);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取AI配置失败", e);
            return Result.error(500, "获取配置失败: " + e.getMessage());
        }
    }

    @ApiOperation("保存 AI 配置")
    @PostMapping
    public Result<?> saveConfig(@RequestBody AIConfigDTO dto) {
        try {
            log.info("收到保存AI配置请求: provider={}, model={}", dto.getProvider(), dto.getModel());
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("当前用户ID: {}", userId);
            
            aiConfigService.saveConfig(userId, dto);
            log.info("AI配置保存成功");
            return Result.success("配置保存成功");
        } catch (Exception e) {
            log.error("保存AI配置失败", e);
            return Result.error(500, "保存配置失败: " + e.getMessage());
        }
    }

    @ApiOperation("测试 AI 连接")
    @PostMapping("/test")
    public Result<?> testConnection(@RequestParam String provider, @RequestBody(required = false) AIConfigDTO dto) {
        try {
            log.info("收到测试连接请求: provider={}", provider);
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("当前用户ID: {}", userId);
            
            boolean success;
            
            // 如果提供了配置信息，使用提供的配置测试
            if (dto != null && dto.getApiKey() != null) {
                log.info("使用提供的配置进行测试");
                success = aiConfigService.testConnection(dto);
            } else {
                // 否则使用数据库中保存的配置测试
                log.info("使用数据库中的配置进行测试");
                success = aiConfigService.testConnection(userId, provider);
            }
            
            if (success) {
                log.info("连接测试成功");
                return Result.success("连接测试成功");
            } else {
                log.warn("连接测试失败");
                return Result.error(500, "连接测试失败，请检查配置");
            }
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return Result.error(500, "测试连接失败: " + e.getMessage());
        }
    }

    @ApiOperation("删除 AI 配置")
    @DeleteMapping("/{provider}")
    public Result<?> deleteConfig(@PathVariable String provider) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            aiConfigService.deleteConfig(userId, provider);
            return Result.success("配置已删除");
        } catch (Exception e) {
            log.error("删除配置失败", e);
            return Result.error(500, "删除配置失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("测试数据库连接")
    @GetMapping("/test-db")
    public Result<?> testDatabase() {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            log.info("测试数据库连接，用户ID: {}", userId);
            
            // 尝试查询配置
            List<AIConfig> configs = aiConfigService.getUserConfigs(userId);
            log.info("查询到 {} 条配置", configs.size());
            
            return Result.success("数据库连接正常，查询到 " + configs.size() + " 条配置");
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return Result.error(500, "数据库连接失败: " + e.getMessage());
        }
    }

    @ApiOperation("获取使用统计")
    @GetMapping("/usage-stats")
    public Result<AIUsageStatsDTO> getUsageStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        AIUsageStatsDTO stats = aiConfigService.getUsageStats(userId, startDate, endDate);
        return Result.success(stats);
    }

    @ApiOperation("获取今日剩余额度")
    @GetMapping("/remaining-quota")
    public Result<Integer> getRemainingQuota() {
        Long userId = SecurityUtil.getCurrentUserId();
        int remaining = aiConfigService.getRemainingQuota(userId);
        return Result.success(remaining);
    }

    /**
     * 构建提供商信息
     */
    private Map<String, Object> buildProviderInfo(String name, String displayName, 
                                                   List<String> models, String defaultModel,
                                                   List<AIConfig> configs) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", name);
        info.put("displayName", displayName);
        info.put("models", models);
        info.put("defaultModel", defaultModel);
        
        // 检查是否已配置，并返回已保存的模型信息
        boolean configured = false;
        for (AIConfig c : configs) {
            if (c.getProvider().equals(name) && c.getEnabled() != null && c.getEnabled() == 1) {
                configured = true;
                info.put("configuredModel", c.getModel());
                info.put("configuredBaseUrl", c.getBaseUrl());
                break;
            }
        }
        info.put("configured", configured);
        
        return info;
    }
}
