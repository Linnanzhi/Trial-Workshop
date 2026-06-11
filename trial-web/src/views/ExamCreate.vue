<template>
  <div class="exam-create-page">
    <h2 class="page-title">🧪 组卷中心</h2>

    <div class="create-layout">
      <div class="trial-card create-form">
        <a-form :model="form" layout="vertical">
          <a-form-item label="试卷标题" required>
            <a-input v-model="form.title" placeholder="给试卷起个名字" />
          </a-form-item>
          <a-form-item label="试卷描述">
            <a-textarea v-model="form.description" :auto-size="{ minRows: 2 }" />
          </a-form-item>

          <div style="display: flex; gap: 16px;">
            <a-form-item label="考试时长(分钟)" style="flex: 1;">
              <a-input-number v-model="form.duration" :min="5" :max="300" />
            </a-form-item>
            <a-form-item label="及格分" style="flex: 1;">
              <a-input-number v-model="form.passScore" :min="0" />
            </a-form-item>
            <a-form-item label="每题分值" style="flex: 1;">
              <a-input-number v-model="form.scorePerQuestion" :min="1" />
            </a-form-item>
          </div>

          <div style="display: flex; gap: 16px;">
            <a-form-item label="打乱题序" style="flex: 1;">
              <a-switch v-model="shuffleQ" />
            </a-form-item>
            <a-form-item label="打乱选项" style="flex: 1;">
              <a-switch v-model="shuffleO" />
            </a-form-item>
            <a-form-item label="错题自动收录" style="flex: 1;">
              <a-switch v-model="autoError" checked-value="1" unchecked-value="0" />
            </a-form-item>
          </div>

          <a-divider>组卷方式</a-divider>

          <a-radio-group v-model="mode" type="button" size="large" style="margin-bottom: 16px;">
            <a-radio value="auto">🎲 自动抽题</a-radio>
            <a-radio value="manual">✋ 手动选题</a-radio>
          </a-radio-group>

          <!-- 自动抽题配置 -->
          <template v-if="mode === 'auto'">
            <div style="display: flex; gap: 16px;">
              <a-form-item label="从分类抽题" style="flex: 1;">
                <a-select v-model="form.categoryId" allow-clear placeholder="不限">
                  <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="抽取数量" style="flex: 1;">
                <a-input-number v-model="form.autoCount" :min="1" :max="200" />
              </a-form-item>
            </div>
            <div style="display: flex; gap: 16px;">
              <a-form-item label="最低难度" style="flex: 1;">
                <a-input-number v-model="form.minDifficulty" :min="1" :max="5" />
              </a-form-item>
              <a-form-item label="最高难度" style="flex: 1;">
                <a-input-number v-model="form.maxDifficulty" :min="1" :max="5" />
              </a-form-item>
            </div>
          </template>

          <!-- 手动选题 -->
          <template v-if="mode === 'manual'">
            <a-alert type="info" style="margin-bottom: 12px;">
              从题库中勾选题目加入本次试卷，已选 {{ selectedIds.length }} 题
            </a-alert>
            <a-checkbox-group v-model="selectedIds">
              <div v-for="q in allQuestions" :key="q.id" class="manual-q-item">
                <a-checkbox :value="q.id">
                  <span class="manual-q-stem">{{ q.stem }}</span>
                </a-checkbox>
              </div>
            </a-checkbox-group>
          </template>

          <a-form-item style="margin-top: 24px;">
            <a-button type="primary" long size="large" @click="handleCreate" :loading="creating"
                      style="background: var(--primary-gradient); border: none; height: 48px; font-size: 16px;">
              🚀 立即组卷
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { examApi, questionApi, categoryTagApi } from '../api/request'

const router = useRouter()
const mode = ref('auto')
const creating = ref(false)
const shuffleQ = ref(false)
const shuffleO = ref(false)
const autoError = ref(true)
const categories = ref<any[]>([])
const allQuestions = ref<any[]>([])
const selectedIds = ref<number[]>([])

const form = reactive({
  title: '', description: '',
  duration: 60, passScore: 60, scorePerQuestion: 5,
  categoryId: undefined as number | undefined,
  autoCount: 10, minDifficulty: 1, maxDifficulty: 5,
})

onMounted(async () => {
  const catRes: any = await categoryTagApi.listCategories()
  categories.value = catRes.data || []
})

watch(mode, async (val) => {
  if (val === 'manual' && allQuestions.value.length === 0) {
    const res: any = await questionApi.list({ pageNum: 1, pageSize: 200 })
    allQuestions.value = res.data?.records || []
  }
})

async function handleCreate() {
  if (!form.title) {
    Message.warning('请输入试卷标题')
    return
  }
  creating.value = true
  try {
    const payload: any = {
      ...form,
      shuffleQuestion: shuffleQ.value ? 1 : 0,
      shuffleOption: shuffleO.value ? 1 : 0,
      autoCollectError: autoError.value ? 1 : 0,
    }
    if (mode.value === 'manual') {
      payload.questionIds = selectedIds.value
    }
    await examApi.create(payload)
    Message.success('组卷成功！')
    router.push('/exams')
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 24px; }

.create-form { max-width: 720px; margin: 0 auto; }

.manual-q-item {
  padding: 8px 0;
  border-bottom: 1px solid var(--border-color);
}
.manual-q-stem {
  font-size: 14px;
}
</style>
