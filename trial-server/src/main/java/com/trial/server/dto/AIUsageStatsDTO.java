package com.trial.server.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI使用统计DTO
 */
@Data
public class AIUsageStatsDTO {
    
    /**
     * 今日已用token数
     */
    private Integer todayTokensUsed;
    
    /**
     * 今日剩余额度
     */
    private Integer todayRemainingQuota;
    
    /**
     * 今日成本(美元)
     */
    private BigDecimal todayCost;
    
    /**
     * 总调用次数
     */
    private Integer totalCalls;
    
    /**
     * 成功次数
     */
    private Integer successCalls;
    
    /**
     * 失败次数
     */
    private Integer failedCalls;
}
