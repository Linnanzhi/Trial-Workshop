<template>
  <div class="exam-list-page">
    <div class="page-header">
      <h2 class="page-title">🛠️ 考试工坊</h2>
      <div style="display: flex; gap: 12px;">
        <a-button @click="showImportModal = true">📥 导入试卷</a-button>
        <a-button type="primary" @click="$router.push('/exams/create')"
                  style="background: var(--primary-gradient); border: none;">
          + 创建试卷
        </a-button>
      </div>
    </div>

    <div class="exam-grid">
      <div v-for="exam in exams" :key="exam.id" class="trial-card exam-card">
        <div class="exam-title">{{ exam.title }}</div>
        <div class="exam-desc">{{ exam.description || '暂无描述' }}</div>
        <div class="exam-meta">
          <span>📝 {{ exam.questionCount }} 题</span>
          <span>⏱ {{ exam.duration }} 分钟</span>
          <span>🎯 {{ exam.passScore }} 分及格</span>
        </div>
        <div class="exam-footer">
          <a-button type="primary" size="small" @click="startExam(exam.id)"
                    style="background: var(--primary-gradient); border: none;">
            开始考试
          </a-button>
          <div style="display: flex; gap: 8px;">
            <a-button type="text" size="small" @click="handleShare(exam.id)">分享</a-button>
            <a-popconfirm content="确定删除该试卷？" @ok="deleteExam(exam.id)">
              <a-button type="text" size="small" status="danger">删除</a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>

      <a-empty v-if="exams.length === 0" description="还没有试卷，去组卷中心创建一份吧！" />
    </div>

    <div class="pagination-wrap">
      <a-pagination :total="total" :current="pageNum" :page-size="pageSize"
                    @change="(p: number) => { pageNum = p; loadExams() }" show-total />
    </div>

    <!-- 试卷分享弹窗 -->
    <a-modal v-model:visible="showShareModal" title="试卷分享码" @ok="showShareModal = false" :footer="false">
      <div style="text-align: center; padding: 20px;">
        <div style="font-size: 16px; margin-bottom: 12px;">请复制以下分享码给其他用户导入：</div>
        <div style="font-size: 28px; font-weight: bold; color: var(--accent-blue); letter-spacing: 4px; border: 2px dashed rgba(102, 126, 234, 0.3); padding: 12px; border-radius: 8px; background: rgba(102, 126, 234, 0.05); cursor: pointer;" @click="copyShareCode(currentShareCode)">
          {{ currentShareCode }}
        </div>
      </div>
    </a-modal>

    <!-- 导入试卷弹窗 -->
    <a-modal v-model:visible="showImportModal" title="导入试卷" @ok="handleImport">
      <a-input v-model="importShareCode" placeholder="请输入6位试卷分享码" allow-clear size="large" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { examApi } from '../api/request'

const router = useRouter()
const exams = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)

const showShareModal = ref(false)
const currentShareCode = ref('')
const showImportModal = ref(false)
const importShareCode = ref('')

onMounted(() => loadExams())

async function loadExams() {
  const res: any = await examApi.list({ pageNum: pageNum.value, pageSize: pageSize.value })
  exams.value = res.data?.records || []
  total.value = res.data?.total || 0
}

async function startExam(examId: number) {
  router.push(`/exam-room/${examId}`)
}

async function deleteExam(id: number) {
  await examApi.remove(id)
  Message.success('删除成功')
  loadExams()
}

async function handleShare(id: number) {
  try {
    const res: any = await examApi.share(id)
    currentShareCode.value = res.data
    showShareModal.value = true
  } catch (e: any) {
    Message.error(e.message || '生成分享码失败')
  }
}

async function handleImport() {
  if (!importShareCode.value) {
    Message.warning('请输入分享码')
    return false
  }
  try {
    await examApi.importShared(importShareCode.value)
    Message.success('导入成功！新试卷和题库已拷贝至您的账户。')
    showImportModal.value = false
    importShareCode.value = ''
    loadExams()
    return true
  } catch (e: any) {
    Message.error(e.message || '导入失败')
    return false
  }
}

function copyShareCode(code: string) {
  navigator.clipboard.writeText(code).then(() => {
    Message.success('分享码已复制到剪贴板！')
  }).catch(() => {
    Message.error('复制失败，请手动复制')
  })
}
</script>

<style scoped>
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
}
.page-title { font-size: 24px; font-weight: 700; }

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.exam-card { display: flex; flex-direction: column; gap: 12px; }
.exam-title { font-size: 18px; font-weight: 600; }
.exam-desc { font-size: 13px; color: var(--text-secondary); }
.exam-meta { display: flex; gap: 16px; font-size: 13px; color: var(--text-secondary); }
.exam-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }

.pagination-wrap { margin-top: 24px; display: flex; justify-content: center; }
</style>
