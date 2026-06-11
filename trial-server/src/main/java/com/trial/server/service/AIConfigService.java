package com.trial.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.APIKeyEncryptor;
import com.trial.server.common.BusinessException;
import com.trial.server.config.SecurityUtil;
import com.trial.server.dto.AIConfigDTO;
import com.trial.server.entity.AIConfig;
import com.trial.server.entity.AIParseLog;
import com.trial.server.mapper.AIConfigMapper;
import com.trial.server.mapper.AIParseLogMapper;
import com.trial.server.service.ai.AIService;
import com.trial.server.service.ai.OpenAIService;
import com.trial.server.service.ai.QianwenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI配置服务
 */
@Slf4j
@Service
public class AIConfigService {
    
    private final AIConfigMapper aiConfigMapper;
    private final AIParseLogMapper aiParseLogMapper;
    private final OpenAIService openAIService;
    private final QianwenService qianwenService;
    
    public AIConfigService(AIConfigMapper aiConfigMapper, AIParseLogMapper aiParseLogMapper,
                          OpenAIService openAIService, QianwenService qianwenService) {
        this.aiConfigMapper = aiConfigMapper;
        this.aiParseLogMapper = aiParseLogMapper;
        this.openAIService = openAIService;
        this.qianwenService = qianwenService;
    }
    
    /**
     * 获取当前用户的AI配置
     */
    public AIConfig getConfig(String provider) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, provider)
               .eq(AIConfig::getEnabled, 1);
        
        AIConfig config = aiConfigMapper.selectOne(wrapper);
        
        // 如果查询到配置，解密API Key（但不返回给前端）
        if (config != null && config.getApiKey() != null) {
            // 注意：这里不解密，前端不需要看到API Key
            config.setApiKey("***"); // 隐藏API Key
        }
        
        return config;
    }
    
    /**
     * 获取所有提供商的配置状态
     */
    public Map<String, Object> getAllProviders() {
        Long userId = SecurityUtil.getCurrentUserId();
        
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", true);
        
        // 定义支持的提供商
        Map<String, Map<String, Object>> providers = new HashMap<>();
        
        // OpenAI
        Map<String, Object> openai = new HashMap<>();
        openai.put("name", "openai");
        openai.put("displayName", "OpenAI");
        openai.put("models", new String[]{"gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo"});
        openai.put("defaultModel", "gpt-4o-mini");
        openai.put("configured", isConfigured(userId, "openai"));
        providers.put("openai", openai);
        
        // 通义千问
        Map<String, Object> qianwen = new HashMap<>();
        qianwen.put("name", "qianwen");
        qianwen.put("displayName", "通义千问");
        qianwen.put("models", new String[]{"qwen-turbo", "qwen-plus", "qwen-max"});
        qianwen.put("defaultModel", "qwen-turbo");
        qianwen.put("configured", isConfigured(userId, "qianwen"));
        providers.put("qianwen", qianwen);
        
        // DeepSeek
        Map<String, Object> deepseek = new HashMap<>();
        deepseek.put("name", "deepseek");
        deepseek.put("displayName", "DeepSeek");
        deepseek.put("models", new String[]{"deepseek-chat"});
        deepseek.put("defaultModel", "deepseek-chat");
        deepseek.put("configured", isConfigured(userId, "deepseek"));
        providers.put("deepseek", deepseek);
        
        result.put("providers", providers);
        
        return result;
    }
    
    /**
     * 保存AI配置
     */
    @Transactional
    public void saveConfig(AIConfigDTO dto) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证必填字段
        if (dto.getProvider() == null || dto.getApiKey() == null) {
            throw new BusinessException("提供商和API Key不能为空");
        }
        
        // 查询是否已存在配置
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, dto.getProvider());
        
        AIConfig existingConfig = aiConfigMapper.selectOne(wrapper);
        
        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setApiKey(APIKeyEncryptor.encrypt(dto.getApiKey()));
            existingConfig.setModel(dto.getModel());
            existingConfig.setBaseUrl(dto.getBaseUrl());
            existingConfig.setMaxTokens(dto.getMaxTokens() != null ? dto.getMaxTokens() : 4000);
            existingConfig.setTemperature(dto.getTemperature() != null ? dto.getTemperature() : 0.3);
            existingConfig.setEnabled(1);
            existingConfig.setUpdateTime(LocalDateTime.now());
            
            aiConfigMapper.updateById(existingConfig);
            log.info("更新AI配置: userId={}, provider={}", userId, dto.getProvider());
        } else {
            // 创建新配置
            AIConfig newConfig = new AIConfig();
            newConfig.setUserId(userId);
            newConfig.setProvider(dto.getProvider());
            newConfig.setApiKey(APIKeyEncryptor.encrypt(dto.getApiKey()));
            newConfig.setModel(dto.getModel());
            newConfig.setBaseUrl(dto.getBaseUrl());
            newConfig.setMaxTokens(dto.getMaxTokens() != null ? dto.getMaxTokens() : 4000);
            newConfig.setTemperature(dto.getTemperature() != null ? dto.getTemperature() : 0.3);
            newConfig.setEnabled(1);
            newConfig.setCreateTime(LocalDateTime.now());
            newConfig.setUpdateTime(LocalDateTime.now());
            
            aiConfigMapper.insert(newConfig);
            log.info("创建AI配置: userId={}, provider={}", userId, dto.getProvider());
        }
    }
    
    /**
     * 测试连接
     */
    public boolean testConnection(AIConfigDTO dto) {
        try {
            log.info("测试AI连接: provider={}, model={}", dto.getProvider(), dto.getModel());
            
            // 验证必填字段
            if (dto.getApiKey() == null || dto.getApiKey().trim().isEmpty()) {
                log.warn("API Key 为空");
                return false;
            }
            
            AIService.AIConfig config = new AIService.AIConfig(
                dto.getApiKey(),
                dto.getModel() != null ? dto.getModel() : "gpt-4o-mini",
                dto.getBaseUrl(),
                dto.getMaxTokens() != null ? dto.getMaxTokens() : 4000,
                dto.getTemperature() != null ? dto.getTemperature() : 0.3
            );
            
            // 根据提供商测试连接
            switch (dto.getProvider()) {
                case "openai":
                    log.info("测试 OpenAI 连接");
                    return openAIService.testConnection(config);
                    
                case "qianwen":
                    log.info("测试通义千问连接");
                    return qianwenService.testConnection(config);
                    
                case "deepseek":
                    log.info("测试 DeepSeek 连接（暂未实现真实调用，返回成功）");
                    // TODO: 实现 DeepSeek 连接测试
                    // return deepseekService.testConnection(config);
                    return true;  // 暂时返回成功
                    
                default:
                    log.warn("不支持的提供商: {}", dto.getProvider());
                    return false;
            }
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return false;
        }
    }
    
    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(String provider) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, provider);
        
        aiConfigMapper.delete(wrapper);
        log.info("删除AI配置: userId={}, provider={}", userId, provider);
    }
    
    /**
     * 检查是否已配置
     */
    private boolean isConfigured(Long userId, String provider) {
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, provider)
               .eq(AIConfig::getEnabled, 1);
        
        return aiConfigMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 获取解密后的配置（内部使用）
     */
    public AIService.AIConfig getDecryptedConfig(String provider) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, provider)
               .eq(AIConfig::getEnabled, 1);
        
        AIConfig config = aiConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            throw new BusinessException("未配置" + provider + "服务");
        }
        
        // 解密API Key
        String decryptedKey = APIKeyEncryptor.decrypt(config.getApiKey());
        
        return new AIService.AIConfig(
            decryptedKey,
            config.getModel(),
            config.getBaseUrl(),
            config.getMaxTokens(),
            config.getTemperature()
        );
    }
    
    /**
     * 获取用户的所有配置
     */
    public java.util.List<AIConfig> getUserConfigs(Long userId) {
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getEnabled, 1);
        
        java.util.List<AIConfig> configs = aiConfigMapper.selectList(wrapper);
        
        // 隐藏API Key
        configs.forEach(config -> config.setApiKey("***"));
        
        return configs;
    }
    
    /**
     * 保存配置（带用户ID）
     */
    @Transactional
    public void saveConfig(Long userId, AIConfigDTO dto) {
        // 验证必填字段
        if (dto.getProvider() == null || dto.getApiKey() == null) {
            throw new BusinessException("提供商和API Key不能为空");
        }
        
        // 查询是否已存在配置
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, dto.getProvider());
        
        AIConfig existingConfig = aiConfigMapper.selectOne(wrapper);
        
        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setApiKey(APIKeyEncryptor.encrypt(dto.getApiKey()));
            existingConfig.setModel(dto.getModel());
            existingConfig.setBaseUrl(dto.getBaseUrl());
            existingConfig.setMaxTokens(dto.getMaxTokens() != null ? dto.getMaxTokens() : 4000);
            existingConfig.setTemperature(dto.getTemperature() != null ? dto.getTemperature() : 0.3);
            existingConfig.setEnabled(1);
            existingConfig.setUpdateTime(LocalDateTime.now());
            
            aiConfigMapper.updateById(existingConfig);
            log.info("更新AI配置: userId={}, provider={}", userId, dto.getProvider());
        } else {
            // 创建新配置
            AIConfig newConfig = new AIConfig();
            newConfig.setUserId(userId);
            newConfig.setProvider(dto.getProvider());
            newConfig.setApiKey(APIKeyEncryptor.encrypt(dto.getApiKey()));
            newConfig.setModel(dto.getModel());
            newConfig.setBaseUrl(dto.getBaseUrl());
            newConfig.setMaxTokens(dto.getMaxTokens() != null ? dto.getMaxTokens() : 4000);
            newConfig.setTemperature(dto.getTemperature() != null ? dto.getTemperature() : 0.3);
            newConfig.setEnabled(1);
            newConfig.setCreateTime(LocalDateTime.now());
            newConfig.setUpdateTime(LocalDateTime.now());
            
            aiConfigMapper.insert(newConfig);
            log.info("创建AI配置: userId={}, provider={}", userId, dto.getProvider());
        }
    }
    
    /**
     * 测试连接（带用户ID）
     */
    public boolean testConnection(Long userId, String provider) {
        try {
            log.info("测试已保存的配置: userId={}, provider={}", userId, provider);
            
            LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AIConfig::getUserId, userId)
                   .eq(AIConfig::getProvider, provider)
                   .eq(AIConfig::getEnabled, 1);
            
            AIConfig config = aiConfigMapper.selectOne(wrapper);
            
            if (config == null) {
                log.warn("未找到配置: userId={}, provider={}", userId, provider);
                return false;
            }
            
            // 解密API Key
            String decryptedKey = APIKeyEncryptor.decrypt(config.getApiKey());
            
            AIService.AIConfig aiConfig = new AIService.AIConfig(
                decryptedKey,
                config.getModel(),
                config.getBaseUrl(),
                config.getMaxTokens(),
                config.getTemperature()
            );
            
            // 根据提供商测试连接
            switch (provider) {
                case "openai":
                    log.info("测试 OpenAI 连接");
                    return openAIService.testConnection(aiConfig);
                    
                case "qianwen":
                    log.info("测试通义千问连接");
                    return qianwenService.testConnection(aiConfig);
                    
                case "deepseek":
                    log.info("测试 DeepSeek 连接（暂未实现真实调用，返回成功）");
                    // TODO: 实现 DeepSeek 连接测试
                    return true;
                    
                default:
                    log.warn("不支持的提供商: {}", provider);
                    return false;
            }
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return false;
        }
    }
    
    /**
     * 删除配置（带用户ID）
     */
    @Transactional
    public void deleteConfig(Long userId, String provider) {
        LambdaQueryWrapper<AIConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIConfig::getUserId, userId)
               .eq(AIConfig::getProvider, provider);
        
        aiConfigMapper.delete(wrapper);
        log.info("删除AI配置: userId={}, provider={}", userId, provider);
    }
    
    /**
     * 获取使用统计（真实数据库查询）
     */
    public com.trial.server.dto.AIUsageStatsDTO getUsageStats(Long userId, String startDate, String endDate) {
        com.trial.server.dto.AIUsageStatsDTO stats = new com.trial.server.dto.AIUsageStatsDTO();
        stats.setTodayTokensUsed(getTodayTokensUsed(userId));
        stats.setTodayRemainingQuota(getRemainingQuota(userId));
        stats.setTodayCost(BigDecimal.valueOf(getTodayCost(userId)));
        stats.setTotalCalls(getTotalCalls(userId));
        
        // 计算成功/失败次数
        LambdaQueryWrapper<AIParseLog> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(AIParseLog::getUserId, userId)
                      .eq(AIParseLog::getSuccess, 1);
        int successCalls = aiParseLogMapper.selectCount(successWrapper).intValue();
        stats.setSuccessCalls(successCalls);
        stats.setFailedCalls(getTotalCalls(userId) - successCalls);
        return stats;
    }
    
    /**
     * 获取今日剩余额度
     */
    public int getRemainingQuota(Long userId) {
        int dailyQuota = 10000; // 每天10000 tokens免费额度
        int todayUsed = getTodayTokensUsed(userId);
        return Math.max(0, dailyQuota - todayUsed);
    }
    
    /**
     * 获取今日已使用 tokens
     */
    public int getTodayTokensUsed(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        LambdaQueryWrapper<AIParseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIParseLog::getUserId, userId)
               .eq(AIParseLog::getSuccess, 1)
               .between(AIParseLog::getCreateTime, startOfDay, endOfDay);
        
        List<AIParseLog> logs = aiParseLogMapper.selectList(wrapper);
        return logs.stream()
                   .mapToInt(log -> log.getTokensUsed() != null ? log.getTokensUsed() : 0)
                   .sum();
    }
    
    /**
     * 获取总调用次数
     */
    public int getTotalCalls(Long userId) {
        LambdaQueryWrapper<AIParseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIParseLog::getUserId, userId);
        return aiParseLogMapper.selectCount(wrapper).intValue();
    }
    
    /**
     * 获取今日成本
     */
    public double getTodayCost(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        LambdaQueryWrapper<AIParseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIParseLog::getUserId, userId)
               .eq(AIParseLog::getSuccess, 1)
               .between(AIParseLog::getCreateTime, startOfDay, endOfDay);
        
        List<AIParseLog> logs = aiParseLogMapper.selectList(wrapper);
        return logs.stream()
                   .map(log -> log.getCost() != null ? log.getCost() : BigDecimal.ZERO)
                   .reduce(BigDecimal.ZERO, BigDecimal::add)
                   .doubleValue();
    }
    
    /**
     * 获取历史累计总成本
     */
    public double getTotalCost(Long userId) {
        LambdaQueryWrapper<AIParseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIParseLog::getUserId, userId)
               .eq(AIParseLog::getSuccess, 1);
        
        List<AIParseLog> logs = aiParseLogMapper.selectList(wrapper);
        return logs.stream()
                   .map(log -> log.getCost() != null ? log.getCost() : BigDecimal.ZERO)
                   .reduce(BigDecimal.ZERO, BigDecimal::add)
                   .doubleValue();
    }
    
    /**
     * 获取解析历史
     */
    public Page<AIParseLog> getParseHistory(Long userId, Integer page, Integer size) {
        Page<AIParseLog> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<AIParseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIParseLog::getUserId, userId)
               .orderByDesc(AIParseLog::getCreateTime);
        
        return aiParseLogMapper.selectPage(pageParam, wrapper);
    }
}
