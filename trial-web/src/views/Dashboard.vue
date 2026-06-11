<template>
  <div class="dashboard-page">
    <h2 class="page-title">📊 学情仪表盘</h2>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="trial-card stat-card">
        <div class="stat-icon" style="background: rgba(47, 84, 235, 0.1);">📝</div>
        <div class="stat-info">
          <div class="stat-number">{{ dashboard.examCount || 0 }}</div>
          <div class="stat-label">考试总次数</div>
        </div>
      </div>
      <div class="trial-card stat-card">
        <div class="stat-icon" style="background: rgba(0, 185, 107, 0.1);">🎯</div>
        <div class="stat-info">
          <div class="stat-number">{{ dashboard.avgScore || 0 }}%</div>
          <div class="stat-label">平均得分率</div>
        </div>
      </div>
      <div class="trial-card stat-card">
        <div class="stat-icon" style="background: rgba(250, 140, 22, 0.1);">✅</div>
        <div class="stat-info">
          <div class="stat-number">{{ dashboard.correctRate || 0 }}%</div>
          <div class="stat-label">总正确率</div>
        </div>
      </div>
      <div class="trial-card stat-card">
        <div class="stat-icon" style="background: rgba(245, 63, 63, 0.1);">📖</div>
        <div class="stat-info">
          <div class="stat-number">{{ dashboard.reviewCount || 0 }}</div>
          <div class="stat-label">待复习错题</div>
        </div>
      </div>
      <div class="trial-card stat-card">
        <div class="stat-icon" style="background: rgba(114, 46, 209, 0.1);">⏱️</div>
        <div class="stat-info">
          <div class="stat-number">{{ dashboard.totalDuration || 0 }}</div>
          <div class="stat-label">练习时长(分)</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="trial-card chart-card">
        <h3>📈 成绩趋势</h3>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>
      <div class="trial-card chart-card">
        <h3>🎯 能力雷达</h3>
        <div ref="radarChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 今日待复习 -->
    <div class="trial-card review-section" v-if="todayReview.length > 0">
      <h3>🔔 今日待复习 (艾宾浩斯)</h3>
      <div class="review-list">
        <div v-for="item in todayReview" :key="item.errorBookId" class="review-item">
          <div class="review-stem">{{ item.stem }}</div>
          <div class="review-actions">
            <a-button type="outline" size="small" status="success"
                      @click="markReview(item.errorBookId, true)">已掌握 ✓</a-button>
            <a-button type="outline" size="small" status="warning"
                      @click="markReview(item.errorBookId, false)">还不会 ✗</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { analysisApi } from '../api/request'
import * as echarts from 'echarts'

const dashboard = ref<any>({})
const todayReview = ref<any[]>([])
const trendChartRef = ref<HTMLElement>()
const radarChartRef = ref<HTMLElement>()

onMounted(async () => {
  try {
    const [dashRes, reviewRes, radarRes]: any[] = await Promise.all([
      analysisApi.dashboard(),
      analysisApi.todayReview(),
      analysisApi.radar(),
    ])
    dashboard.value = dashRes.data || {}
    todayReview.value = reviewRes.data || []

    await nextTick()
    renderTrendChart(dashboard.value.trend || [])
    renderRadarChart(radarRes.data?.radar || [])
  } catch (e) {
    // fallback to empty
  }
})

function renderTrendChart(trend: any[]) {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    grid: { top: 30, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: trend.map((_, i) => `第${i + 1}次`),
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      axisLabel: { color: '#86909c' },
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLine: { show: false },
      axisLabel: { color: '#86909c' },
      splitLine: { lineStyle: { color: '#e5e6eb', type: 'dashed' } },
    },
    series: [{
      data: trend.map((t: any) => t.score),
      type: 'line',
      smooth: true,
      lineStyle: { color: '#2f54eb', width: 3 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(47,84,235,0.2)' },
          { offset: 1, color: 'rgba(47,84,235,0)' },
        ])
      },
      itemStyle: { color: '#2f54eb' },
    }],
    tooltip: { trigger: 'axis' },
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderRadarChart(radarData: any[]) {
  if (!radarChartRef.value || radarData.length === 0) return
  const chart = echarts.init(radarChartRef.value)
  chart.setOption({
    backgroundColor: 'transparent',
    radar: {
      indicator: radarData.map((d: any) => ({ name: d.name, max: 100 })),
      shape: 'polygon',
      axisName: { color: '#86909c' },
      axisLine: { lineStyle: { color: '#e5e6eb' } },
      splitLine: { lineStyle: { color: '#e5e6eb' } },
      splitArea: { areaStyle: { color: ['rgba(255,255,255,0.8)', 'rgba(242,245,248,0.5)'] } },
    },
    series: [{
      type: 'radar',
      data: [{
        value: radarData.map((d: any) => d.rate),
        name: '正确率',
        areaStyle: { color: 'rgba(47,84,235,0.15)' },
        lineStyle: { color: '#2f54eb', width: 2 },
        itemStyle: { color: '#2f54eb' },
      }],
    }],
    tooltip: {},
  })
  window.addEventListener('resize', () => chart.resize())
}

async function markReview(id: number, mastered: boolean) {
  await analysisApi.reviewFeedback(id, mastered)
  todayReview.value = todayReview.value.filter(r => r.errorBookId !== id)
}
</script>

<style scoped>
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  font-size: 28px;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card h3 {
  margin-bottom: 12px;
  font-size: 16px;
}

.chart-container {
  height: 300px;
}

.review-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.review-stem {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 16px;
}

.review-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 768px) {
  .charts-row { grid-template-columns: 1fr; }
}
</style>
