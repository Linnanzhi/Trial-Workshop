<template>
  <div class="ai-stats-container">
    <div class="page-header">
      <h2 class="page-title">🤖 AI 智能解析统计</h2>
      <a-button type="primary" @click="$router.push({ name: 'AIConfig' })">
        <template #icon><icon-settings /></template>
        返回配置页面
      </a-button>
    </div>

    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 24px">
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="今日消耗" 
            :value="stats.todayUsed" 
            suffix="tokens"
            :value-style="{ color: '#165dff' }"
          >
            <template #prefix>
              <span style="font-size: 24px; margin-right: 8px;">📊</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="今日剩余额度" 
            :value="stats.todayRemaining" 
            suffix="tokens"
            :value-style="{ color: stats.todayRemaining > 2000 ? '#0fbf60' : '#f53f3f' }"
          >
            <template #prefix>
              <span style="font-size: 24px; margin-right: 8px;">⚡</span>
            </template>
          </a-statistic>
          <a-progress 
            :percent="Math.max(0, Math.min(1, stats.todayUsed / 10000))" 
            :show-text="false"
            :color="stats.todayRemaining > 2000 ? '#0fbf60' : '#f53f3f'"
            style="margin-top: 12px"
            size="small"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="累计调用" 
            :value="stats.totalUsed" 
            suffix="次"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <span style="font-size: 24px; margin-right: 8px;">📈</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false" class="stat-card">
          <a-statistic 
            title="预估总成本" 
            :value="stats.totalCost" 
            :precision="4"
            prefix="$"
            :value-style="{ color: '#ff7d00' }"
          >
            <template #prefix>
              <span style="font-size: 24px; margin-right: 8px;">💰</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 使用历史 -->
    <a-card :bordered="false" class="history-card">
      <template #title>
        <span style="font-size: 16px; font-weight: 600;">📜 解析历史记录</span>
      </template>
      <template #extra>
        <a-range-picker 
          v-model="dateRange" 
          @change="loadHistory"
          style="width: 280px"
        />
      </template>

      <a-table 
        :data="historyList" 
        :pagination="pagination"
        :loading="loading"
        @page-change="handlePageChange"
        hoverable
        stripe
      >
        <template #columns>
          <a-table-column title="解析时间" data-index="createTime" :width="180">
            <template #cell="{ record }">
              <span style="color: var(--color-text-2);">{{ formatTime(record.createTime) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="提供商" data-index="provider" :width="120">
            <template #cell="{ record }">
              <a-tag :color="getProviderColor(record.provider)">
                {{ getProviderName(record.provider) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="使用模型" data-index="model" :width="160" />
          <a-table-column title="识别题目数" data-index="questionsCount" :width="120" align="center">
            <template #cell="{ record }">
              <a-badge :count="record.questionsCount" :dotStyle="{ backgroundColor: 'var(--color-primary-light-1)' }" />
              <a-tag color="arcoblue" v-if="record.questionsCount > 0">{{ record.questionsCount }}题</a-tag>
              <span v-else>-</span>
            </template>
          </a-table-column>
          <a-table-column title="消耗Tokens" data-index="tokensUsed" :width="120" align="right">
            <template #cell="{ record }">
              <span style="font-family: monospace;">{{ record.tokensUsed.toLocaleString() }}</span>
            </template>
          </a-table-column>
          <a-table-column title="费用" data-index="cost" :width="100" align="right">
            <template #cell="{ record }">
              <span style="font-family: monospace; color: #ff7d00;">${{ record.cost.toFixed(4) }}</span>
            </template>
          </a-table-column>
          <a-table-column title="耗时" data-index="parseTime" :width="100" align="right">
            <template #cell="{ record }">
              {{ (record.parseTime / 1000).toFixed(1) }}s
            </template>
          </a-table-column>
          <a-table-column title="状态" data-index="success" :width="100" align="center">
            <template #cell="{ record }">
              <a-tag :color="record.success ? 'green' : 'red'">
                {{ record.success ? '成功' : '失败' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="输入内容摘要" data-index="inputText" ellipsis>
            <template #cell="{ record }">
              <a-tooltip :content="record.inputText || '-'">
                <span class="text-truncate">{{ record.inputText || '-' }}</span>
              </a-tooltip>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconSettings } from '@arco-design/web-vue/es/icon'
import { aiParseApi } from '../api/ai'
import dayjs from 'dayjs'

const stats = reactive({
  todayUsed: 0,
  todayRemaining: 10000,
  totalUsed: 0,
  totalCost: 0,
})

const historyList = ref<any[]>([])
const loading = ref(false)
const dateRange = ref<[string, string]>()

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
})

const providerNames: Record<string, string> = {
  openai: 'OpenAI',
  qianwen: '通义千问',
  deepseek: 'DeepSeek',
}

const providerColors: Record<string, string> = {
  openai: 'arcoblue',
  qianwen: 'purple',
  deepseek: 'green',
}

const getProviderName = (provider: string) => {
  return providerNames[provider] || provider
}

const getProviderColor = (provider: string) => {
  return providerColors[provider] || 'gray'
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const loadStats = async () => {
  try {
    const res = await aiParseApi.getUsageStats()
    if (res.data) {
      stats.todayUsed = res.data.todayTokensUsed || 0
      stats.todayRemaining = res.data.todayRemainingQuota || 10000
      stats.totalUsed = res.data.totalCalls || 0
      stats.totalCost = res.data.totalCost || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadHistory = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.current,
      size: pagination.pageSize,
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await aiParseApi.getParseHistory(params)
    if (res.data) {
      historyList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error: any) {
    Message.error(error.message || '加载历史记录失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadHistory()
}

onMounted(() => {
  loadStats()
  loadHistory()
})
</script>

<style scoped>
.ai-stats-container {
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

.history-card {
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
}

.text-truncate {
  display: block;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text-2);
}
</style>
