import request from './request'

// AI 配置相关接口
export const aiConfigApi = {
    // 获取 AI 配置
    getConfig: () => request.get('/ai-config'),
    
    // 保存 AI 配置
    saveConfig: (data: {
        provider: string
        apiKey: string
        model?: string
        baseUrl?: string
        maxTokens?: number
        temperature?: number
    }) => request.post('/ai-config', data),
    
    // 测试 AI 连接
    testConnection: (provider: string, config?: any) => 
        request.post(`/ai-config/test?provider=${provider}`, config || null),
    
    // 删除 AI 配置
    deleteConfig: (provider: string) => 
        request.delete(`/ai-config/${provider}`),
    
    // 获取使用统计
    getUsageStats: (params?: { startDate?: string; endDate?: string }) => 
        request.get('/ai-config/usage-stats', { params }),
    
    // 获取今日剩余额度
    getRemainingQuota: () => request.get('/ai-config/remaining-quota'),
}

// AI 解析相关接口
export const aiParseApi = {
    // AI 解析文本题目
    parseText: (data: {
        text: string
        aiProvider: string
        model?: string
        type?: number
        categoryId?: number
        difficulty?: number
    }) => request.post('/questions/parse-text-ai', data, { timeout: 60000 }),
    
    // 预估 token 消耗
    estimateTokens: (text: string) => 
        request.post('/ai-parse/estimate', { text }),
    
    // 获取使用统计
    getUsageStats: (params?: { startDate?: string; endDate?: string }) => 
        request.get('/ai-parse/usage-stats', { params }),
    
    // 获取今日剩余额度
    getRemainingQuota: () => request.get('/ai-parse/remaining-quota'),
    
    // 获取解析历史
    getParseHistory: (params: { page: number; size: number }) => 
        request.get('/ai-parse/history', { params }),
}
