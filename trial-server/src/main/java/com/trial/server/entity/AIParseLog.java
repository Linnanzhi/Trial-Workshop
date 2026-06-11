package com.trial.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI解析日志实体类
 */
@Data
@TableName("t_ai_parse_log")
public class AIParseLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * AI提供商
     */
    private String provider;
    
    /**
     * 使用的模型
     */
    private String model;
    
    /**
     * 输入文本(截取前1000字符)
     */
    private String inputText;
    
    /**
     * 解析出的题目数量
     */
    private Integer questionsCount;
    
    /**
     * 消耗的token数
     */
    private Integer tokensUsed;
    
    /**
     * 成本(美元)
     */
    private BigDecimal cost;
    
    /**
     * 解析耗时(毫秒)
     */
    private Integer parseTime;
    
    /**
     * 是否成功
     */
    private Integer success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
