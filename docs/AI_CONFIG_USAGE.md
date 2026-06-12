# 🤖 AI 配置使用指南

AI 智能解析功能的配置和使用说明

## 📋 目录

- [功能概述](#功能概述)
- [支持的 AI 服务](#支持的-ai-服务)
- [配置步骤](#配置步骤)
- [API 文档](#api-文档)
- [常见问题](#常见问题)

---

## 功能概述

### 核心功能

✅ **多 AI 服务支持** - OpenAI/通义千问/DeepSeek  
✅ **文本解析** - 智能识别题目和答案  
✅ **文件上传** - 支持 Word/PDF 文件导入  
✅ **题目创建** - 自动创建题目到题库  
✅ **使用统计** - 追踪 token 消耗和成本  
✅ **额度管理** - 按日限额控制  

### 支持的 AI 提供商

| 提供商 | 模型 | 价格 | 优缺点 |
|--------|------|------|--------|
| **OpenAI** | gpt-4o-mini | $0.00015/1K tokens | 准确度高，成本低 |
| **阿里云** | qwen-max | ¥0.02/1M tokens | 中文优化好，便宜 |
| **DeepSeek** | deepseek-chat | ¥0.14/1M tokens | 超便宜，推荐 |

---

## 支持的 AI 服务

### 1. OpenAI (ChatGPT)

#### 获取 API Key

1. 访问 https://platform.openai.com
2. 登录或注册账户
3. 点击 "API Keys"
4. 创建新的 API Key
5. 复制保存（只显示一次）

#### 价格

- **gpt-4o-mini** (推荐)
  - 输入：$0.00015 / 1K tokens
  - 输出：$0.0006 / 1K tokens
  - 平均：解析一份 10 页试卷 ≈ $0.02

#### 限制

- 速率限制：3,500 RPM (请求/分钟)
- Token 限制：200,000 TPM
- 国内访问需要代理或 VPN

### 2. 阿里云 (通义千问)

#### 获取 API Key

1. 访问 https://console.aliyun.com
2. 登录阿里云账户（需国内身份）
3. 进入"模型服务"
4. 选择"通义千问"
5. 生成 API Key

#### 价格

- **qwen-max** 
  - 输入：¥0.02 / 1M tokens
  - 输出：¥0.06 / 1M tokens
  - 平均：解析一份 10 页试卷 ≈ ¥0.01

#### 特点

- ✅ 中文支持最好
- ✅ 价格便宜
- ✅ 国内访问无限制
- ❌ 需要国内身份认证

### 3. DeepSeek

#### 获取 API Key

1. 访问 https://platform.deepseek.com
2. 注册账户
3. 进入"API"菜单
4. 生成新的 API Key
5. 复制保存

#### 价格

- **deepseek-chat**
  - 输入：¥0.14 / 1M tokens
  - 输出：¥0.28 / 1M tokens
  - 平均：解析一份 10 页试卷 ≈ ¥0.001

#### 特点

- ✅ 国内厂商，访问快
- ✅ 价格最便宜
- ✅ 支持长上下文
- ✓ 推荐首选

---

## 配置步骤

### 步骤 1：获取 API Key

根据上面选择的 AI 服务获取 API Key。

### 步骤 2：登录系统

1. 打开应用首页
2. 输入用户名、密码登录
3. 进入"设置" → "AI 配置"

### 步骤 3：添加 AI 服务

#### 操作流程

1. 点击"添加新服务"
2. 选择 AI 提供商（OpenAI/通义千问/DeepSeek）
3. 输入 API Key
4. 选择模型
5. 配置参数（可选）
6. 点击"测试连接"
7. 保存配置

#### 配置表单

```
AI 提供商：[OpenAI ▼]
API Key：[sk-proj-xxxxxxxxxx]
模型：[gpt-4o-mini ▼]
最大 Token 数：[4000]
温度参数：[0.3]
Base URL：[https://api.openai.com/v1] (可选)

[测试连接]  [保存]
```

### 步骤 4：测试连接

配置完成后，点击"测试连接"：

```
✅ 连接成功！
剩余额度：10000 tokens ($ 1.50)
```

### 步骤 5：设置每日限额

```
每日额度：[10000 tokens ▼]
（推荐：小于账户日均消费）
```

---

## 配置参数详解

### max_tokens（最大 Token 数）

**推荐值：4000**

```
- 最小：1000（可能无法完整解析）
- 推荐：4000（足以处理大多数试卷）
- 最大：128000（OpenAI gpt-4o-mini 的上限）
```

### temperature（温度参数）

**推荐值：0.3**

```
范围：0.0 - 2.0

0.0 - 0.3：
  - 确定性强，输出稳定
  - 适合题目解析

0.3 - 0.7：
  - 平衡确定性和创意性
  - 适合生成内容

0.7 - 2.0：
  - 随机性强，创意性高
  - 不适合题目解析
```

### base_url（自定义 API 地址）

**默认：** OpenAI 提供

**使用场景：**
- 使用代理服务
- 使用国内镜像
- 本地部署的模型

**示例：**
```
https://api.openai.com/v1              # 官方 OpenAI
https://api.openai-proxy.com/v1        # 第三方代理
http://localhost:8000/v1               # 本地部署
```

---

## API 文档

### 1. 添加 AI 配置

**请求：**
```http
POST /api/ai-config
Content-Type: application/json

{
  "provider": "openai",
  "apiKey": "sk-proj-xxxxx",
  "model": "gpt-4o-mini",
  "maxTokens": 4000,
  "temperature": 0.3,
  "enabled": true
}
```

**响应：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 123,
    "provider": "openai",
    "model": "gpt-4o-mini",
    "enabled": true,
    "createTime": "2026-02-25 10:30:00"
  }
}
```

### 2. 获取配置列表

**请求：**
```http
GET /api/ai-config
```

**响应：**
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "id": 123,
      "provider": "openai",
      "model": "gpt-4o-mini",
      "enabled": true,
      "createTime": "2026-02-25 10:30:00"
    }
  ]
}
```

### 3. 测试 AI 连接

**请求：**
```http
POST /api/ai-config/test
Content-Type: application/json

{
  "provider": "openai",
  "apiKey": "sk-proj-xxxxx",
  "model": "gpt-4o-mini"
}
```

**响应成功：**
```json
{
  "code": 0,
  "msg": "连接成功",
  "data": {
    "status": "success",
    "message": "API 连接正常",
    "tokensUsed": 50
  }
}
```

**响应失败：**
```json
{
  "code": 1,
  "msg": "error",
  "data": {
    "status": "failed",
    "message": "API Key 无效或已过期",
    "error": "Invalid API Key"
  }
}
```

### 4. 删除配置

**请求：**
```http
DELETE /api/ai-config/123
```

### 5. 获取使用统计

**请求：**
```http
GET /api/ai-parse/usage-stats
```

**响应：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "todayTokensUsed": 2500,
    "todayRemainingQuota": 7500,
    "totalCalls": 15,
    "todayCost": 0.38,
    "totalCost": 12.50
  }
}
```

---

## 成本计算

### Token 消耗估算

```
中文：约 2 字符 = 1 token
英文：约 4 字符 = 1 token

示例：
  输入："请分析这道题"（8 字符）≈ 4 tokens
  输出：平均 300-500 tokens

一份 10 页试卷：
  - 题目数：~50 题
  - 平均每题输入：200 tokens
  - 平均每题输出：100 tokens
  - 总计：50 × 300 = 15,000 tokens
```

### 成本对比

```
假设解析 100 份试卷（150 万 tokens）：

OpenAI (gpt-4o-mini)：
  - 输入：150万 × $0.00015 / 1000 = $225
  - 输出：150万 × $0.0006 / 1000 = $900
  - 总计：≈ $1125

DeepSeek (deepseek-chat)：
  - 输入：150万 × ¥0.14 / 1M = ¥210
  - 输出：150万 × ¥0.28 / 1M = ¥420
  - 总计：≈ ¥630（约 $87）

推荐：使用 DeepSeek，成本最低！
```

---

## 常见问题

### Q1: 哪个 AI 服务最好？

**A:** 根据场景选择：

- **准确度优先** → OpenAI (gpt-4o-mini)
- **成本优先** → DeepSeek
- **中文优化** → 阿里云 (通义千问)
- **国内访问** → 阿里云或 DeepSeek

**推荐：** DeepSeek（性价比最高）

### Q2: API Key 泄露了怎么办？

**A:** 立即删除：

1. 登录对应的 AI 服务平台
2. 进入 API 管理界面
3. 找到泄露的 Key
4. 点击"删除"或"重新生成"
5. 在应用中更新新的 Key

### Q3: 显示"额度不足"怎么办？

**A:** 有两种可能：

1. **账户额度已用完**
   - 登录 AI 服务平台充值
   - 或等待下一个计费周期

2. **每日限额已达到**
   - 在"AI 配置"中增加每日限额
   - 或等待明天重置

### Q4: 解析失败或结果不对？

**A:** 检查以下项：

- [ ] API Key 是否正确
- [ ] 模型是否支持
- [ ] 账户是否有余额
- [ ] 网络连接是否正常
- [ ] 输入文本格式是否正确

### Q5: 支持离线使用吗？

**A:** 不支持，需要网络连接。但可以配置本地模型：

```
使用 Ollama 在本地运行大模型：
1. 安装 Ollama：https://ollama.ai
2. 运行：ollama run llama2
3. 在 base_url 中填：http://localhost:11434/v1
```

---

## 最佳实践

### 1. 多服务配置

建议配置多个 AI 服务作为备份：

```
主服务：OpenAI (准确度最高)
备用服务：DeepSeek (最便宜)
```

### 2. 成本控制

```yaml
# application.yml
trial:
  ai:
    dailyQuota: 10000      # 每日限额 10000 tokens
    warningThreshold: 0.8  # 80% 时发出警告
    maxCostPerDay: 1.0     # 每日成本上限 $1
```

### 3. 错误处理

总是使用 try-catch 处理 AI 解析：

```java
try {
    List<QuestionDTO> questions = aiService.parseText(text);
} catch (AIServiceException e) {
    // 记录错误
    log.error("AI 解析失败", e);
    // 返回友好提示
    return Result.error(500, "智能解析暂时不可用，请稍后重试");
}
```

### 4. 监控和告警

定期检查 AI 使用情况：

```
每日检查：
  - [ ] 今日 Token 消耗
  - [ ] 今日成本
  - [ ] 剩余额度
  - [ ] API 错误率
```

---

**✅ AI 配置完成！** 

现在可以开始使用智能解析功能了。参考 [解析功能说明](UNPARSED_TEXT_FEATURE.md)
