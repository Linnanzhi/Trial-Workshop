package com.trial.server.service.parser;

/**
 * 解析模式枚举
 */
public enum ParseMode {
    /**
     * 正则解析（快速、免费）
     */
    REGEX,
    
    /**
     * AI解析（智能、消耗token）
     */
    AI,
    
    /**
     * 混合模式（先正则，失败时使用AI）
     */
    HYBRID,
    
    /**
     * 自动选择（根据格式自动判断）
     */
    AUTO
}
