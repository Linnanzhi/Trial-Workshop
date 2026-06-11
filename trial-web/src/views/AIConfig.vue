<template>
  <div class="ai-config-container">
    <div class="page-header">
      <h2 class="page-title">⚙️ AI 智能解析配置</h2>
      <a-button type="primary" @click="$router.push({ name: 'AIUsageStats' })">
        📊 查看使用统计
      </a-button>
    </div>

    <!-- 简易统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 24px">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="今日已用" 
            :value="usageStats.todayUsed" 
            suffix="tokens"
            :value-style="{ color: '#165dff' }"
          >
            <template #prefix>
              <span style="font-size: 20px">📊</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="今日剩余" 
            :value="usageStats.todayRemaining" 
            suffix="tokens"
            :value-style="{ color: usageStats.todayRemaining > 2000 ? '#0fbf60' : '#f53f3f' }"
          >
            <template #prefix>
              <span style="font-size: 20px">⚡</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="累计使用" 
            :value="usageStats.totalUsed" 
            suffix="次"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <span style="font-size: 20px">📈</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="今日成本" 
            :value="usageStats.totalCost" 
            :precision="4"
            prefix="$"
            :value-style="{ color: '#ff7d00' }"
          >
            <template #prefix>
              <span style="font-size: 20px">💰</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 配置表单卡片 -->
    <a-card :bordered="false" class="config-card">
      <template #title>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span style="font-size: 16px; font-weight: 600">模型设置</span>
          <a-space>
            <a-tag v-for="p in allProviders" :key="p.name" :color="p.configured ? 'green' : 'gray'" size="small">
              {{ p.displayName }}: {{ p.configured ? (p.configuredModel || '已配置') : '未配置' }}
            </a-tag>
          </a-space>
        </div>
      </template>

      <a-alert type="info" style="margin-bottom: 24px; border-radius: 8px;">
        配置 AI 服务后，可以使用智能解析功能，自动识别各种格式的题目。<br/>
        系统每日拥有免费 token 额度供调用。
      </a-alert>

      <a-form :model="form" layout="vertical" @submit="handleSave" style="max-width: 800px">
        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="AI 提供商" required field="provider">
              <a-select v-model="form.provider" placeholder="请选择 AI 提供商" @change="handleProviderChange" size="large">
                <a-option value="openai">
                  <div class="provider-option">
                    <span>OpenAI</span>
                    <span class="provider-desc">业界标杆，准确率高</span>
                  </div>
                </a-option>
                <a-option value="qianwen">
                  <div class="provider-option">
                    <span>通义千问</span>
                    <span class="provider-desc">中文理解优秀，性价比高</span>
                  </div>
                </a-option>
                <a-option value="deepseek">
                  <div class="provider-option">
                    <span>DeepSeek</span>
                    <span class="provider-desc">国产之光，性价比极高</span>
                  </div>
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :span="12">
            <a-form-item label="AI 模型" field="model">
              <a-select v-model="form.model" placeholder="请选择 AI 模型" size="large">
                <a-option 
                  v-for="model in availableModels" 
                  :key="model.value" 
                  :value="model.value"
                >
                  {{ model.label }}
                </a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="API Key" required field="apiKey">
          <a-input-password 
            v-model="form.apiKey" 
            placeholder="请输入 API Key（若未修改可留空）"
            allow-clear
            size="large"
          />
          <template #extra>
            <a :href="getApiKeyUrl" target="_blank" class="help-link">不知道如何获取 API Key？点击查阅官方教程</a>
          </template>
        </a-form-item>

        <a-form-item label="自定义 API 网关地址" field="baseUrl">
          <a-input 
            v-model="form.baseUrl" 
            placeholder="例如：https://api.openai.com/v1"
            allow-clear
            size="large"
          />
          <template #extra>
            如果使用本地代理或反向代理，可自定义网关。支持 OpenAI 兼容格式接口，默认可留空。
          </template>
        </a-form-item>

        <a-divider style="margin: 24px 0" />
        <h3 style="margin-bottom: 16px; font-weight: 500;">高级设置</h3>

        <a-row :gutter="24">
          <a-col :span="12">
            <a-form-item label="最大 Tokens" field="maxTokens">
              <a-input-number 
                v-model="form.maxTokens" 
                :min="1000" 
                :max="16000" 
                :step="1000"
                placeholder="限制AI每次生成的最大字数"
                size="large"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="温度参数 (Temperature)" field="temperature">
              <a-slider 
                v-model="form.temperature" 
                :min="0" 
                :max="1" 
                :step="0.1"
                :format-tooltip="(val) => `当前: ${val.toFixed(1)}`"
                show-input
              />
              <template #extra>
                温度越低回答越固定，建议设为 0.1 - 0.3。
              </template>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item style="margin-top: 20px;">
          <a-space size="medium">
            <a-button type="primary" html-type="submit" :loading="saving" size="large" class="action-btn">
              <template #icon><icon-save /></template>
              保存配置
            </a-button>
            <a-button @click="handleTest" :loading="testing" size="large" class="action-btn" type="outline">
              <template #icon><icon-thunderbolt /></template>
              测试连接
            </a-button>
            <a-popconfirm content="确定要删除当前配置吗？" type="warning" @ok="handleDelete">
              <a-button status="danger" v-if="hasConfigured" size="large" class="action-btn">
                <template #icon><icon-delete /></template>
                删除配置
              </a-button>
            </a-popconfirm>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconSave, IconThunderbolt, IconDelete } from '@arco-design/web-vue/es/icon'
import { aiConfigApi, aiParseApi } from '../api/ai'
import { useRouter } from 'vue-router'

const router = useRouter()

const form = reactive({
  provider: 'openai',
  apiKey: '',
  model: '',
  baseUrl: '',
  maxTokens: 4000,
  temperature: 0.3,
})

const saving = ref(false)
const testing = ref(false)
const hasConfigured = ref(false)

const usageStats = reactive({
  todayUsed: 0,
  todayRemaining: 10000,
  totalUsed: 0,
  totalCost: 0,
})

const allProviders = ref<any[]>([])

const modelOptions = {
  openai: [
    { label: 'GPT-4o (推荐)', value: 'gpt-4o' },
    { label: 'GPT-4o Mini', value: 'gpt-4o-mini' },
    { label: 'GPT-3.5 Turbo', value: 'gpt-3.5-turbo' },
  ],
  qianwen: [
    { label: 'Qwen Max', value: 'qwen-max' },
    { label: 'Qwen Plus', value: 'qwen-plus' },
    { label: 'Qwen Turbo (推荐)', value: 'qwen-turbo' },
  ],
  deepseek: [
    { label: 'DeepSeek Chat', value: 'deepseek-chat' },
  ],
}

const availableModels = computed(() => {
  return modelOptions[form.provider as keyof typeof modelOptions] || []
})

const getApiKeyUrl = computed(() => {
  const urls = {
    openai: 'https://platform.openai.com/api-keys',
    qianwen: 'https://dashscope.console.aliyun.com/apiKey',
    deepseek: 'https://platform.deepseek.com/api_keys',
  }
  return urls[form.provider as keyof typeof urls] || '#'
})

const handleProviderChange = () => {
  // 切换提供商时，加载已保存的配置
  const provider = allProviders.value.find((p: any) => p.name === form.provider)
  if (provider && provider.configured) {
    form.model = provider.configuredModel || availableModels.value[0]?.value || ''
    form.baseUrl = provider.configuredBaseUrl || ''
    hasConfigured.value = true
  } else {
    form.model = availableModels.value[0]?.value || ''
    form.baseUrl = ''
    hasConfigured.value = false
  }
}

const handleSave = async () => {
  if (!form.apiKey && !hasConfigured.value) {
    Message.warning('请输入 API Key')
    return
  }

  saving.value = true
  try {
    await aiConfigApi.saveConfig(form)
    Message.success('配置保存成功')
    hasConfigured.value = true
    await loadConfig()
  } catch (error) {
    console.error('保存配置失败:', error)
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  if (!form.apiKey && !hasConfigured.value) {
    Message.warning('请先输入 API Key')
    return
  }

  testing.value = true
  try {
    // 发送当前表单配置进行测试
    await aiConfigApi.testConnection(form.provider, {
      provider: form.provider,
      apiKey: form.apiKey,
      model: form.model,
      baseUrl: form.baseUrl,
      maxTokens: form.maxTokens,
      temperature: form.temperature
    })
    Message.success('连接测试成功！')
  } catch (error: any) {
    Message.error(error.message || '连接测试失败')
  } finally {
    testing.value = false
  }
}

const handleDelete = async () => {
  try {
    await aiConfigApi.deleteConfig(form.provider)
    Message.success('配置已删除')
    hasConfigured.value = false
    form.apiKey = ''
  } catch (error) {
    console.error('删除配置失败:', error)
  }
}

const loadConfig = async () => {
  try {
    const res = await aiConfigApi.getConfig()
    if (res.data && res.data.providers) {
      allProviders.value = res.data.providers
      // 找到第一个已配置的提供商，加载其配置
      const configured = res.data.providers.find((p: any) => p.configured)
      if (configured) {
        hasConfigured.value = true
        form.provider = configured.name
        form.model = configured.configuredModel || configured.defaultModel
        form.baseUrl = configured.configuredBaseUrl || ''
      } else {
        hasConfigured.value = false
      }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}

const loadUsageStats = async () => {
  try {
    const res = await aiConfigApi.getUsageStats()
    if (res.data) {
      usageStats.todayUsed = res.data.todayTokensUsed || 0
      usageStats.todayRemaining = res.data.todayRemainingQuota || 10000
      usageStats.totalUsed = res.data.totalCalls || 0
      usageStats.totalCost = res.data.todayCost || 0
    }
  } catch (error) {
    console.error('加载使用统计失败:', error)
  }
}

onMounted(() => {
  loadConfig()
  loadUsageStats()
})
</script>

<style scoped>
.ai-config-container {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-1);
}

.config-card {
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
  background-color: var(--color-bg-2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-card :deep(.arco-statistic-title) {
  font-size: 14px;
  color: var(--color-text-3);
  margin-bottom: 8px;
}

.provider-option {
  display: flex;
  flex-direction: column;
}

.provider-desc {
  font-size: 12px;
  color: var(--color-text-3);
  margin-top: 2px;
}

.help-link {
  color: rgb(var(--primary-6));
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s;
}

.help-link:hover {
  color: rgb(var(--primary-5));
  text-decoration: underline;
}

.action-btn {
  padding: 0 24px;
  border-radius: 6px;
}
</style>
