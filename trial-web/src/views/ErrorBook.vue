<template>
  <div class="error-book-page">
    <div class="page-header">
      <h2 class="page-title">📖 错题本</h2>
      <a-radio-group v-model="filter" type="button" @change="loadErrorBook">
        <a-radio :value="undefined">全部</a-radio>
        <a-radio :value="0">未掌握</a-radio>
        <a-radio :value="1">已掌握</a-radio>
      </a-radio-group>
    </div>

    <div class="error-list">
      <div v-for="item in errors" :key="item.id" class="trial-card error-item">
        <div class="error-header">
          <a-tag :color="typeColors[item.type]" size="small">{{ typeNames[item.type] }}</a-tag>
          <span class="error-count">错误 {{ item.errorCount }} 次</span>
          <a-tag v-if="item.mastered" color="green" size="small">已掌握 ✓</a-tag>
          <a-tag v-else color="orange" size="small">待复习</a-tag>
        </div>

        <div class="error-stem">{{ item.stem }}</div>

        <a-collapse :bordered="false" style="background: transparent;">
          <a-collapse-item header="查看答案与解析">
            <div class="error-answer">
              <div><strong>正确答案：</strong>{{ item.answer }}</div>
              <div v-if="item.analysis"><strong>解析：</strong>{{ item.analysis }}</div>
            </div>
          </a-collapse-item>
        </a-collapse>

        <div class="error-footer">
          <span class="error-time">
            下次复习：{{ item.nextReviewTime || '无' }}
          </span>
          <div class="error-actions" v-if="!item.mastered">
            <a-button type="outline" size="small" status="success"
                      @click="markMastered(item.id, true)">标记已掌握</a-button>
          </div>
        </div>
      </div>

      <a-empty v-if="errors.length === 0" description="暂无错题记录，继续加油！" />
    </div>

    <div class="pagination-wrap">
      <a-pagination :total="total" :current="pageNum" :page-size="pageSize"
                    @change="(p: number) => { pageNum = p; loadErrorBook() }" show-total />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { analysisApi } from '../api/request'

const typeNames: Record<number, string> = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答' }
const typeColors: Record<number, string> = { 1: 'arcoblue', 2: 'purple', 3: 'green', 4: 'orangered', 5: 'gold' }

const errors = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filter = ref<number | undefined>(undefined)

onMounted(() => loadErrorBook())

async function loadErrorBook() {
  const res: any = await analysisApi.errorBook({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    mastered: filter.value,
  })
  errors.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function markMastered(id: number, mastered: boolean) {
  await analysisApi.reviewFeedback(id, mastered)
  Message.success('标记成功')
  loadErrorBook()
}
</script>

<style scoped>
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.page-title { font-size: 24px; font-weight: 700; }

.error-list { display: flex; flex-direction: column; gap: 12px; }

.error-item { padding: 16px 20px; }
.error-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
}
.error-count { font-size: 12px; color: var(--accent-red); }
.error-stem { font-size: 15px; line-height: 1.6; margin-bottom: 8px; }
.error-answer {
  font-size: 14px; line-height: 1.8;
  color: var(--text-secondary);
}
.error-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 8px; padding-top: 8px; border-top: 1px solid var(--border-color);
}
.error-time { font-size: 12px; color: var(--text-secondary); }

.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }
</style>
