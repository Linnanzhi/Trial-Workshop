<template>
  <div class="exam-room" :class="{ 'fullscreen-mode': isFullscreen }">
    <!-- 考试头部 -->
    <div class="room-header">
      <div class="room-title">
        <span class="room-icon">🛡️</span>
        <span>{{ examData?.exam?.title || '模拟考场' }}</span>
      </div>
      <div class="room-timer" :class="{ 'timer-warning': remainingTime < 300 }">
        ⏱️ {{ formatTime(remainingTime) }}
      </div>
      <div class="room-actions">
        <a-button type="text" @click="toggleFullscreen">
          {{ isFullscreen ? '退出全屏' : '全屏模式' }}
        </a-button>
        <a-popconfirm content="确定要提前交卷吗？" @ok="handleSubmit">
          <a-button type="primary" status="warning">交卷</a-button>
        </a-popconfirm>
      </div>
    </div>

    <div class="room-body">
      <!-- 题目区域 -->
      <div class="question-area">
        <div v-if="currentQuestion" class="question-panel trial-card">
          <div class="q-number">
            第 {{ currentIndex + 1 }} / {{ questions.length }} 题
            <span class="q-score">({{ currentQuestion.score }} 分)</span>
          </div>

          <div class="q-stem-text">{{ currentQuestion.stem }}</div>

          <!-- 选择题 -->
          <div v-if="[1, 2].includes(currentQuestion.type)" class="options-list">
            <div v-for="(opt, idx) in currentQuestion.options" :key="idx"
                 class="option-item"
                 :class="{
                   'option-selected': isOptionSelected(idx),
                 }"
                 @click="selectOption(idx)">
              <span class="option-letter">{{ String.fromCharCode(65 + idx) }}</span>
              <span class="option-text">{{ opt }}</span>
            </div>
          </div>

          <!-- 判断题 -->
          <div v-if="currentQuestion.type === 3" class="options-list">
            <div class="option-item" :class="{ 'option-selected': userAnswers[currentQuestion.id] === '对' }"
                 @click="userAnswers[currentQuestion.id] = '对'">
              <span class="option-letter">✓</span><span class="option-text">对</span>
            </div>
            <div class="option-item" :class="{ 'option-selected': userAnswers[currentQuestion.id] === '错' }"
                 @click="userAnswers[currentQuestion.id] = '错'">
              <span class="option-letter">✗</span><span class="option-text">错</span>
            </div>
          </div>

          <!-- 填空/简答 -->
          <div v-if="[4, 5].includes(currentQuestion.type)">
            <a-textarea v-model="userAnswers[currentQuestion.id]"
                        placeholder="请输入你的答案..." :auto-size="{ minRows: 4 }" />
          </div>

          <!-- 题目导航按钮 -->
          <div class="q-nav-buttons">
            <a-button :disabled="currentIndex === 0" @click="currentIndex--">上一题</a-button>
            <a-button v-if="currentIndex < questions.length - 1" type="primary"
                      @click="currentIndex++"
                      style="background: var(--primary-gradient); border: none;">
              下一题
            </a-button>
            <a-button v-else type="primary" status="success" @click="handleSubmit">
              交卷
            </a-button>
          </div>
        </div>
      </div>

      <!-- 答题卡 -->
      <div class="answer-card trial-card">
        <div class="answer-card-header">
          <h4 class="answer-card-title">📋 答题卡</h4>
          <span class="answer-card-count">{{ questions.length }} 题</span>
        </div>
        <div class="card-grid">
          <div v-for="(q, idx) in questions" :key="q.id"
               class="card-item"
               :class="{
                 'card-answered': userAnswers[q.id],
                 'card-current': idx === currentIndex,
               }"
               @click="currentIndex = idx">
            {{ idx + 1 }}
          </div>
        </div>
        <div class="card-legend">
          <span><i class="dot dot-current"></i> 当前</span>
          <span><i class="dot dot-answered"></i> 已答</span>
          <span><i class="dot dot-unanswered"></i> 未答</span>
        </div>
      </div>
    </div>

    <!-- 成绩弹窗 -->
    <a-modal v-model:visible="showResult" title="🎉 考试结果" :closable="false" :mask-closable="false"
             width="500px" ok-text="返回试卷列表" @ok="$router.push('/exams')">
      <div class="result-content" v-if="examResult">
        <div class="result-score">
          <div class="score-value" :class="examResult.passed ? 'score-pass' : 'score-fail'">
            {{ examResult.score }}
          </div>
          <div class="score-total">/ {{ examResult.totalScore }}</div>
        </div>
        <a-tag :color="examResult.passed ? 'green' : 'red'" size="large">
          {{ examResult.passed ? '🎊 恭喜通过！' : '😅 未通过' }}
        </a-tag>
        <div class="result-stats">
          <div>✅ 正确：{{ examResult.correctCount }} 题</div>
          <div>❌ 错误：{{ examResult.wrongCount }} 题</div>
          <div>⏱️ 用时：{{ Math.floor((examResult.duration || 0) / 60) }} 分 {{ (examResult.duration || 0) % 60 }} 秒</div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { examApi } from '../api/request'

const route = useRoute()
const router = useRouter()
const examId = Number(route.params.examId)

const examData = ref<any>(null)
const questions = ref<any[]>([])
const currentIndex = ref(0)
const userAnswers = reactive<Record<number, string>>({})
const remainingTime = ref(0)
const recordId = ref<number | null>(null)
const showResult = ref(false)
const examResult = ref<any>(null)
const isFullscreen = ref(false)
let timer: any = null
let saveTimer: any = null

const currentQuestion = computed(() => questions.value[currentIndex.value])

onMounted(async () => {
  try {
    // 加载试卷
    const detailRes: any = await examApi.detail(examId)
    examData.value = detailRes.data
    questions.value = detailRes.data?.questions || []
    remainingTime.value = (detailRes.data?.exam?.duration || 60) * 60

    // 开始考试
    const startRes: any = await examApi.start(examId)
    recordId.value = startRes.data

    // 启动倒计时
    timer = setInterval(() => {
      remainingTime.value--
      if (remainingTime.value <= 0) {
        clearInterval(timer)
        handleSubmit()
      }
    }, 1000)

    // 每30秒自动保存
    saveTimer = setInterval(() => autoSave(), 30000)
  } catch (e) {
    Message.error('加载试卷失败')
  }
})

onUnmounted(() => {
  clearInterval(timer)
  clearInterval(saveTimer)
})

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function isOptionSelected(idx: number): boolean {
  const answer = userAnswers[currentQuestion.value?.id]
  if (!answer) return false
  return answer.includes(String.fromCharCode(65 + idx))
}

function selectOption(idx: number) {
  const q = currentQuestion.value
  if (!q) return
  const letter = String.fromCharCode(65 + idx)

  if (q.type === 1) {
    // 单选
    userAnswers[q.id] = letter
  } else if (q.type === 2) {
    // 多选
    const current = userAnswers[q.id] || ''
    if (current.includes(letter)) {
      userAnswers[q.id] = current.replace(letter, '')
    } else {
      userAnswers[q.id] = (current + letter).split('').sort().join('')
    }
  }
}

async function autoSave() {
  if (!recordId.value) return
  const answers = Object.entries(userAnswers).map(([qId, ans]) => ({
    questionId: Number(qId), userAnswer: ans
  }))
  try {
    await examApi.saveProgress(recordId.value, answers)
  } catch (e) { /* silent */ }
}

async function handleSubmit() {
  clearInterval(timer)
  clearInterval(saveTimer)

  const answers = questions.value.map(q => ({
    questionId: q.id,
    userAnswer: userAnswers[q.id] || '',
  }))

  try {
    const res: any = await examApi.submit({
      recordId: recordId.value,
      answers,
    })
    examResult.value = res.data
    showResult.value = true
  } catch (e) {
    Message.error('交卷失败')
  }
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    document.documentElement.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}
</script>

<style scoped>
.exam-room {
  min-height: 100vh;
  background: var(--bg-color);
  padding: 0;
}

.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.room-title {
  display: flex; align-items: center; gap: 8px;
  font-size: 18px; font-weight: 600;
}
.room-icon { font-size: 24px; }

.room-timer {
  font-size: 28px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--accent-blue);
}
.timer-warning { color: var(--accent-red); animation: blink 1s infinite; }
@keyframes blink { 50% { opacity: 0.5; } }

.room-actions { display: flex; gap: 8px; }

.room-body {
  display: flex;
  gap: 20px;
  padding: 24px;
  max-width: 1280px;
  margin: 0 auto;
}

.question-area {
  flex: 1;
  min-width: 0;
}

.question-panel { padding: 28px; }

.q-number {
  font-size: 14px; color: var(--text-secondary); margin-bottom: 16px;
}
.q-score { color: var(--accent-orange); }

.q-stem-text {
  font-size: 17px; line-height: 1.8; margin-bottom: 24px;
}

.options-list { display: flex; flex-direction: column; gap: 10px; }

.option-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
}
.option-item:hover {
  border-color: var(--accent-blue);
  background: rgba(47, 84, 235, 0.05);
}
.option-selected {
  border-color: var(--accent-blue) !important;
  background: rgba(47, 84, 235, 0.1) !important;
}

.option-letter {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.04);
  font-weight: 600;
  font-size: 14px;
}
.option-selected .option-letter {
  background: var(--accent-blue);
  color: white;
}

.q-nav-buttons {
  display: flex; justify-content: space-between; margin-top: 32px;
}

/* 答题卡 */
.answer-card {
  width: 280px;
  min-width: 280px;
  padding: 22px;
  position: sticky;
  top: 96px;
  align-self: flex-start;
}

.answer-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.answer-card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.answer-card-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--menu-selected-bg);
  color: var(--accent-blue);
  font-size: 12px;
  font-weight: 600;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 14px;
}

.card-item {
  width: 100%;
  aspect-ratio: 1;
  min-height: 38px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-card);
}
.card-item:hover {
  border-color: var(--accent-blue);
  transform: translateY(-1px);
}

.card-current {
  border-color: var(--accent-blue);
  background: rgba(47, 84, 235, 0.15);
  color: var(--accent-blue);
  box-shadow: inset 0 0 0 1px rgba(47, 84, 235, 0.08);
}

.card-answered {
  background: rgba(0, 185, 107, 0.15);
  border-color: var(--accent-green);
  color: var(--accent-green);
}

.card-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.card-legend span {
  display: inline-flex;
  align-items: center;
}

.dot {
  display: inline-block; width: 10px; height: 10px;
  border-radius: 50%; margin-right: 4px; vertical-align: middle;
}
.dot-current { background: var(--accent-blue); }
.dot-answered { background: var(--accent-green); }
.dot-unanswered { background: rgba(0, 0, 0, 0.06); border: 1px solid var(--border-color); }

@media (max-width: 1024px) {
  .room-body {
    flex-direction: column;
  }

  .answer-card {
    width: 100%;
    min-width: 0;
    position: static;
  }

  .card-grid {
    grid-template-columns: repeat(auto-fit, minmax(40px, 1fr));
  }
}

/* 成绩弹窗 */
.result-content { text-align: center; padding: 20px 0; }
.result-score { display: flex; align-items: baseline; justify-content: center; gap: 4px; margin-bottom: 16px; }
.score-value { font-size: 64px; font-weight: 700; }
.score-pass { color: var(--accent-green); }
.score-fail { color: var(--accent-red); }
.score-total { font-size: 24px; color: var(--text-secondary); }
.result-stats { margin-top: 20px; display: flex; flex-direction: column; gap: 8px; font-size: 15px; }
</style>
