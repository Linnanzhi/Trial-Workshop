/**
 * AI 解析功能使用示例
 * 
 * 本文件展示如何在前端调用 AI 解析相关的 API
 */

import { aiConfigApi, aiParseApi } from '../api/ai'
import { Message } from '@arco-design/web-vue'

// ============================================
// 示例 1: 配置 AI 服务
// ============================================

export async function exampleConfigureAI() {
  try {
    // 保存 AI 配置
    await aiConfigApi.saveConfig({
      provider: 'openai',
      apiKey: 'sk-your-api-key-here',
      model: 'gpt-4o-mini',
      baseUrl: 'https://api.openai.com/v1', // 可选
      maxTokens: 4000,
      temperature: 0.3,
    })
    
    Message.success('AI 配置保存成功')
  } catch (error: any) {
    Message.error(error.message || '配置保存失败')
  }
}

// ============================================
// 示例 2: 测试 AI 连接
// ============================================

export async function exampleTestConnection() {
  try {
    await aiConfigApi.testConnection('openai')
    Message.success('连接测试成功！')
  } catch (error: any) {
    Message.error(error.message || '连接测试失败')
  }
}

// ============================================
// 示例 3: 获取 AI 配置
// ============================================

export async function exampleGetConfig() {
  try {
    const res = await aiConfigApi.getConfig()
    console.log('AI 配置:', res.data)
    
    // 检查是否已配置
    const hasConfigured = res.data.providers.some((p: any) => p.configured)
    console.log('是否已配置:', hasConfigured)
    
    return res.data
  } catch (error) {
    console.error('获取配置失败:', error)
  }
}

// ============================================
// 示例 4: 使用 AI 解析题目
// ============================================

export async function exampleParseWithAI() {
  const questionText = `
1. 什么是面向对象编程的三大特性？
A. 封装、继承、多态
B. 封装、继承、接口
C. 继承、多态、抽象
D. 封装、多态、抽象

2. Java 是一种编译型语言。
答案：错
解析：Java 是编译+解释型语言

3. 类的操作和属性的可见性通常分为 public、_____ 和 _____ 三种。
答案：private、protected
  `
  
  try {
    const res = await aiParseApi.parseText({
      text: questionText,
      aiProvider: 'openai',
      model: 'gpt-4o-mini', // 可选
      type: undefined, // 自动识别题型
      categoryId: 1, // 可选，指定分类
      difficulty: 3, // 可选，指定难度
    })
    
    console.log('解析结果:', res.data)
    console.log('元数据:', res.meta)
    
    // 显示解析信息
    if (res.meta) {
      Message.success(
        `解析成功！识别 ${res.data.length} 道题目，` +
        `消耗 ${res.meta.tokensUsed} tokens，` +
        `耗时 ${(res.meta.parseTime / 1000).toFixed(1)}s`
      )
    }
    
    return res.data
  } catch (error: any) {
    Message.error(error.message || 'AI 解析失败')
  }
}

// ============================================
// 示例 5: 预估 Token 消耗
// ============================================

export function estimateTokens(text: string): { tokens: number; cost: number } {
  // 简单估算：中文约 2 字符 = 1 token，英文约 4 字符 = 1 token
  const chineseChars = (text.match(/[\u4e00-\u9fa5]/g) || []).length
  const otherChars = text.length - chineseChars
  const tokens = Math.ceil(chineseChars / 2 + otherChars / 4)
  
  // 估算成本（以 OpenAI gpt-4o-mini 为例：$0.15/1M tokens）
  const costPerToken = 0.00000015
  const cost = tokens * costPerToken
  
  return { tokens, cost }
}

// ============================================
// 示例 6: 获取剩余额度
// ============================================

export async function exampleGetRemainingQuota() {
  try {
    const res = await aiParseApi.getRemainingQuota()
    console.log('今日剩余额度:', res.data, 'tokens')
    
    if (res.data < 1000) {
      Message.warning('今日 AI 解析额度不足 1000 tokens')
    }
    
    return res.data
  } catch (error) {
    console.error('获取剩余额度失败:', error)
  }
}

// ============================================
// 示例 7: 获取使用统计
// ============================================

export async function exampleGetUsageStats() {
  try {
    const res = await aiParseApi.getUsageStats({
      startDate: '2024-01-01',
      endDate: '2024-12-31',
    })
    
    console.log('使用统计:', res.data)
    console.log('今日已用:', res.data.todayUsed, 'tokens')
    console.log('今日剩余:', res.data.todayRemaining, 'tokens')
    console.log('累计使用:', res.data.totalUsed, 'tokens')
    console.log('累计成本:', res.data.totalCost, 'USD')
    
    return res.data
  } catch (error) {
    console.error('获取使用统计失败:', error)
  }
}

// ============================================
// 示例 8: 获取解析历史
// ============================================

export async function exampleGetParseHistory() {
  try {
    const res = await aiParseApi.getParseHistory({
      page: 1,
      size: 10,
    })
    
    console.log('解析历史:', res.data)
    
    // 遍历历史记录
    res.data.records.forEach((record: any) => {
      console.log(`
        时间: ${record.createTime}
        提供商: ${record.provider}
        模型: ${record.model}
        题目数: ${record.questionsCount}
        消耗: ${record.tokensUsed} tokens
        成本: $${record.cost.toFixed(4)}
        状态: ${record.success ? '成功' : '失败'}
      `)
    })
    
    return res.data
  } catch (error) {
    console.error('获取解析历史失败:', error)
  }
}

// ============================================
// 示例 9: 完整的解析流程
// ============================================

export async function exampleCompleteParseFlow(questionText: string) {
  try {
    // 1. 检查 AI 配置
    const config = await aiConfigApi.getConfig()
    const hasConfigured = config.data.providers.some((p: any) => p.configured)
    
    if (!hasConfigured) {
      Message.warning('请先配置 AI 服务')
      return
    }
    
    // 2. 预估 token 消耗
    const { tokens, cost } = estimateTokens(questionText)
    console.log(`预估消耗: ${tokens} tokens (约 $${cost.toFixed(4)})`)
    
    // 3. 检查剩余额度
    const remainingQuota = await aiParseApi.getRemainingQuota()
    if (tokens > remainingQuota.data) {
      Message.warning('今日 AI 解析额度不足')
      return
    }
    
    // 4. 执行 AI 解析
    const parseResult = await aiParseApi.parseText({
      text: questionText,
      aiProvider: 'openai',
      model: 'gpt-4o-mini',
    })
    
    // 5. 显示解析结果
    console.log('解析成功，识别题目:', parseResult.data)
    Message.success(`成功解析 ${parseResult.data.length} 道题目`)
    
    // 6. 刷新使用统计
    await exampleGetUsageStats()
    
    return parseResult.data
  } catch (error: any) {
    Message.error(error.message || '解析流程失败')
  }
}

// ============================================
// 示例 10: 删除 AI 配置
// ============================================

export async function exampleDeleteConfig() {
  try {
    await aiConfigApi.deleteConfig('openai')
    Message.success('AI 配置已删除')
  } catch (error: any) {
    Message.error(error.message || '删除配置失败')
  }
}

// ============================================
// 使用示例
// ============================================

/*
// 在 Vue 组件中使用：

import { 
  exampleConfigureAI, 
  exampleParseWithAI,
  exampleCompleteParseFlow 
} from '@/examples/ai-parse-example'

// 配置 AI
await exampleConfigureAI()

// 解析题目
const questions = await exampleParseWithAI()

// 完整流程
const result = await exampleCompleteParseFlow(questionText)
*/
