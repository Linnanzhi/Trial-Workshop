package com.trial.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trial.server.common.Result;
import com.trial.server.config.SecurityUtil;
import com.trial.server.dto.AIParseRequest;
import com.trial.server.dto.QuestionDTO;
import com.trial.server.entity.AIParseLog;
import com.trial.server.service.AIConfigService;
import com.trial.server.service.FileImportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 解析控制器
 */
@Api(tags = "AI 智能解析")
@RestController
@RequestMapping("/ai-parse")
@RequiredArgsConstructor
public class AIParseController {

    private final FileImportService fileImportService;
    private final AIConfigService aiConfigService;

    @ApiOperation("AI 解析文本题目")
    @PostMapping("/parse-text")
    public Result<?> parseText(@RequestBody AIParseRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 验证文本
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            return Result.error(400, "文本内容不能为空");
        }
        
        // 检查额度
        int estimatedTokens = estimateTokens(request.getText());
        int remaining = aiConfigService.getRemainingQuota(userId);
        if (estimatedTokens > remaining) {
            return Result.error(403, "今日 AI 解析额度不足");
        }
        
        // 执行解析
        long startTime = System.currentTimeMillis();
        List<QuestionDTO> questions = fileImportService.parseTextWithAI(
                request.getText(),
                request.getAiProvider(),
                request.getModel(),
                request.getType(),
                request.getCategoryId(),
                request.getDifficulty()
        );
        long parseTime = System.currentTimeMillis() - startTime;
        
        // 设置元数据
        Map<String, Object> meta = new HashMap<>();
        meta.put("totalQuestions", questions.size());
        meta.put("parseTime", parseTime);
        meta.put("tokensUsed", estimatedTokens);
        meta.put("cost", estimatedTokens * 0.00000015);
        
        // 构建响应 - 直接返回 questions 作为 data，meta 作为额外字段
        Map<String, Object> response = new HashMap<>();
        response.put("data", questions);
        response.put("meta", meta);
        
        return Result.success(response);
    }

    @ApiOperation("预估 token 消耗")
    @PostMapping("/estimate")
    public Result<Map<String, Object>> estimateTokens(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        if (text == null || text.trim().isEmpty()) {
            return Result.error(400, "文本内容不能为空");
        }
        
        int tokens = estimateTokens(text);
        double cost = tokens * 0.00000015; // OpenAI gpt-4o-mini 价格
        
        Map<String, Object> result = new HashMap<>();
        result.put("tokens", tokens);
        result.put("cost", cost);
        
        return Result.success(result);
    }

    @ApiOperation("获取解析历史")
    @GetMapping("/history")
    public Result<Page<AIParseLog>> getParseHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<AIParseLog> history = aiConfigService.getParseHistory(userId, page, size);
        return Result.success(history);
    }

    @ApiOperation("获取使用统计")
    @GetMapping("/usage-stats")
    public Result<?> getUsageStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 获取统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayTokensUsed", aiConfigService.getTodayTokensUsed(userId));
        stats.put("todayRemainingQuota", aiConfigService.getRemainingQuota(userId));
        stats.put("totalCalls", aiConfigService.getTotalCalls(userId));
        stats.put("todayCost", aiConfigService.getTodayCost(userId));
        stats.put("totalCost", aiConfigService.getTotalCost(userId));
        
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
     * 简单估算 token 数量
     * 中文约 2 字符 = 1 token，英文约 4 字符 = 1 token
     */
    private int estimateTokens(String text) {
        int chineseChars = 0;
        for (char c : text.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FA5) {
                chineseChars++;
            }
        }
        int otherChars = text.length() - chineseChars;
        return (int) Math.ceil(chineseChars / 2.0 + otherChars / 4.0);
    }
}
