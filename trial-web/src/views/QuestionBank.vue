<template>
  <div class="question-page">
    <div class="page-header">
      <h2 class="page-title">📚 智能题库</h2>
      <div class="header-actions">
        <a-space v-if="selectedIds.length > 0">
          <a-button type="outline" @click="showBatchCategoryModal = true">
            📁 批量分类 ({{ selectedIds.length }})
          </a-button>
          <a-button type="outline" @click="showBatchTagModal = true">
            🏷️ 批量标签 ({{ selectedIds.length }})
          </a-button>
          <a-button type="outline" status="danger" @click="handleBatchDelete">
            🗑️ 批量删除 ({{ selectedIds.length }})
          </a-button>
        </a-space>
        <a-button type="primary" @click="showAddModal = true"
                  style="background: var(--primary-gradient); border: none;">
          + 新增题目
        </a-button>
        <a-button @click="openImport" status="success">📥 批量导入</a-button>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="trial-card filter-bar">
      <a-checkbox v-model="selectAll" @change="handleSelectAll" v-if="questions.length > 0">
        全选
      </a-checkbox>
      <a-input v-model="query.keyword" placeholder="搜索题目关键词..." allow-clear
               style="width: 280px;" @change="loadQuestions">
        <template #prefix><icon-search /></template>
      </a-input>
      <a-select v-model="query.type" placeholder="题型" allow-clear style="width: 130px;" @change="loadQuestions">
        <a-option :value="1">单选题</a-option>
        <a-option :value="2">多选题</a-option>
        <a-option :value="3">判断题</a-option>
        <a-option :value="4">填空题</a-option>
        <a-option :value="5">简答题</a-option>
      </a-select>
      <a-select v-model="query.difficulty" placeholder="难度" allow-clear style="width: 120px;" @change="loadQuestions">
        <a-option :value="1">⭐</a-option>
        <a-option :value="2">⭐⭐</a-option>
        <a-option :value="3">⭐⭐⭐</a-option>
        <a-option :value="4">⭐⭐⭐⭐</a-option>
        <a-option :value="5">⭐⭐⭐⭐⭐</a-option>
      </a-select>
      <a-select v-model="query.categoryId" placeholder="分类" allow-clear style="width: 160px;" @change="loadQuestions">
        <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
      </a-select>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <div v-for="item in questions" :key="item.id" class="trial-card question-item"
           :class="{ 'selected': selectedIds.includes(item.id) }"
           @click="toggleSelect(item.id)">
        <div class="q-checkbox">
          <a-checkbox :model-value="selectedIds.includes(item.id)" @click.stop="toggleSelect(item.id)" />
        </div>
        <div class="q-content">
          <div class="q-header">
            <a-tag :color="typeColors[item.type]" size="small">{{ typeNames[item.type] }}</a-tag>
            <span class="difficulty-tag" :class="'difficulty-' + item.difficulty">
              {{ '⭐'.repeat(item.difficulty) }}
            </span>
            <span v-if="item.categoryName" class="q-category">{{ item.categoryName }}</span>
          </div>
          <div class="q-stem">{{ item.stem }}</div>
          <div class="q-tags" v-if="item.tags && item.tags.length > 0">
            <a-tag v-for="tag in item.tags" :key="tag.id" :color="tag.color" size="small">
              {{ tag.name }}
            </a-tag>
          </div>
          <div class="q-actions" @click.stop>
            <a-button type="text" size="small" @click="editQuestion(item)">编辑</a-button>
            <a-popconfirm content="确定删除该题目吗？" @ok="deleteQuestion(item.id)">
              <a-button type="text" size="small" status="danger">删除</a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>

      <a-empty v-if="questions.length === 0" description="暂无题目，快去添加吧！" />
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <a-pagination :total="total" :current="query.pageNum" :page-size="query.pageSize"
                    @change="(p: number) => { query.pageNum = p; loadQuestions() }" show-total />
    </div>

    <!-- 新增/编辑题目弹窗 -->
    <a-modal v-model:visible="showAddModal" :title="editingId ? '编辑题目' : '新增题目'"
             width="680px" @ok="handleSaveQuestion" ok-text="保存">
      <a-form :model="form" layout="vertical">
        <a-form-item label="题型" required>
          <a-select v-model="form.type">
            <a-option :value="1">单选题</a-option>
            <a-option :value="2">多选题</a-option>
            <a-option :value="3">判断题</a-option>
            <a-option :value="4">填空题</a-option>
            <a-option :value="5">简答题</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="题干" required>
          <a-textarea v-model="form.stem" :max-length="2000" show-word-limit :auto-size="{ minRows: 3 }" />
        </a-form-item>
        <a-form-item label="选项" v-if="[1,2].includes(form.type)">
          <div style="display: flex; flex-direction: column; width: 100%;">
            <div v-for="(opt, idx) in form.options" :key="idx" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: flex-start;">
              <a-tag style="margin-top: 4px;">{{ String.fromCharCode(65 + idx) }}</a-tag>
              <a-textarea v-model="form.options[idx]" :placeholder="'选项' + String.fromCharCode(65 + idx)" :auto-size="{ minRows: 1, maxRows: 6 }" style="flex: 1;" />
              <a-button v-if="form.options.length > 2" type="text" status="danger"
                        @click="form.options.splice(idx, 1)" style="margin-top: 1px;">✕</a-button>
            </div>
            <a-button type="dashed" long @click="form.options.push('')">+ 添加选项</a-button>
          </div>
        </a-form-item>
        <a-form-item label="正确答案" required>
          <a-input v-model="form.answer" placeholder="单选填 A/B/C/D, 多选填 AB/AC 等" />
        </a-form-item>
        <a-form-item label="解析">
          <a-textarea v-model="form.analysis" :auto-size="{ minRows: 2 }" />
        </a-form-item>
        <div style="display: flex; gap: 16px;">
          <a-form-item label="难度" style="flex: 1;">
            <a-rate v-model="form.difficulty" />
          </a-form-item>
          <a-form-item label="分类" style="flex: 1;">
            <a-select v-model="form.categoryId" allow-clear placeholder="选择分类">
              <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
            </a-select>
          </a-form-item>
        </div>
        <a-form-item label="标签">
          <a-select v-model="form.tagIds" multiple allow-clear placeholder="选择标签">
            <a-option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- ========== 批量导入弹窗 ========== -->
    <a-modal v-model:visible="showImportModal" title="📥 批量导入题目" width="1100px" :footer="false" top="20px">
      <div class="import-layout" :class="{ 'has-preview': parsedQuestions.length > 0 }">
        <div class="import-left">
          <a-tabs v-model:active-key="importTab">
        <!-- Tab 1: 文件上传 -->
        <a-tab-pane key="file" title="📄 文件上传">
          <div class="upload-area" v-if="importStep === 'upload'">
            <a-upload draggable
                      :auto-upload="false"
                      accept=".docx,.pdf,.txt"
                      :limit="1"
                      @change="handleFileChange"
                      :file-list="fileList">
              <template #upload-button>
                <div class="upload-trigger">
                  <div class="upload-icon">📄</div>
                  <div class="upload-title">点击或拖拽文件到此区域</div>
                  <div class="upload-hint">支持 .docx / .pdf / .txt 格式，最大 10MB</div>
                </div>
              </template>
            </a-upload>
            <div style="margin-top: 16px; text-align: right;">
              <a-button type="primary" :loading="importLoading" :disabled="fileList.length === 0"
                        @click="handleFileUpload"
                        style="background: var(--primary-gradient); border: none;">
                🔍 解析文件
              </a-button>
            </div>
          </div>
        </a-tab-pane>

        <!-- Tab 2: 文本粘贴 -->
        <a-tab-pane key="text" title="📝 文本粘贴">
          <!-- 导入配置 -->
          <div class="import-config">
            <a-form :model="importConfig" layout="inline">
              <a-form-item label="解析模式">
                <a-select v-model="importConfig.parseMode" style="width: 160px;">
                  <a-option value="regex">
                    <div class="mode-option">
                      <span>⚡ 快速解析</span>
                      <span class="mode-hint">免费，标准格式</span>
                    </div>
                  </a-option>
                  <a-option value="ai" :disabled="!aiConfigured">
                    <div class="mode-option">
                      <span>🤖 AI 智能解析</span>
                      <span class="mode-hint">消耗 token</span>
                    </div>
                  </a-option>
                  <a-option value="hybrid" :disabled="!aiConfigured">
                    <div class="mode-option">
                      <span>🔄 混合模式</span>
                      <span class="mode-hint">智能切换</span>
                    </div>
                  </a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="AI 提供商" v-if="importConfig.parseMode !== 'regex'">
                <a-select v-model="importConfig.aiProvider" style="width: 180px;" @change="handleImportProviderChange">
                  <a-option v-for="p in providerList" :key="p.name" :value="p.name">
                    <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                      <span>{{ p.displayName }}</span>
                      <a-tag v-if="!p.configured" color="red" size="small" style="margin-left: 8px;">未配置</a-tag>
                      <a-tag v-else color="green" size="small" style="margin-left: 8px;">{{ p.configuredModel || '已配置' }}</a-tag>
                    </div>
                  </a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="题型">
                <a-select v-model="importConfig.type" placeholder="自动识别" allow-clear style="width: 140px;">
                  <a-option :value="1">单选题</a-option>
                  <a-option :value="2">多选题</a-option>
                  <a-option :value="3">判断题</a-option>
                  <a-option :value="4">填空题</a-option>
                  <a-option :value="5">简答题</a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="分类">
                <a-select v-model="importConfig.categoryId" placeholder="选择分类" allow-clear style="width: 160px;">
                  <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
                </a-select>
              </a-form-item>
              <a-form-item label="难度">
                <a-rate v-model="importConfig.difficulty" />
              </a-form-item>
            </a-form>
          </div>
          
          <!-- AI 成本预估 -->
          <a-alert type="info" v-if="importConfig.parseMode !== 'regex' && importText.trim()" style="margin-bottom: 12px;">
            <template #icon>💰</template>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>预估消耗：约 {{ estimatedTokens }} tokens（约 ${{ estimatedCost.toFixed(4) }}）</span>
              <span>今日剩余：{{ remainingQuota }} tokens</span>
            </div>
          </a-alert>

          <a-alert type="info" style="margin-bottom: 12px;">
            <template #icon>💡</template>
            <div style="line-height: 1.6;">
              <b>支持多种题型格式：</b><br/>
              <b>单选/多选：</b>答案可写底部“答案：A”，也可写题干括号内如：1.测试题（A）<br/>
              <b>判断题：</b>答案为 对/错、√/×、true/false<br/>
              <b>填空题：</b>答案用 _____ 保留在底部，或直接以【答案】形式写在题干中<br/>
              <b>简答题：</b>无选项的开放性题目
            </div>
          </a-alert>
          <a-textarea v-model="importText" :placeholder="textPlaceholder" :auto-size="{ minRows: 10 }" />
          <div style="margin-top: 16px; text-align: right;">
            <a-button type="primary" :loading="importLoading" :disabled="!importText.trim()"
                      @click="handleTextParse"
                      style="background: var(--primary-gradient); border: none;">
              🔍 解析文本
            </a-button>
          </div>
        </a-tab-pane>
      </a-tabs>
        </div>

      <!-- 解析结果预览 -->
      <div v-if="parsedQuestions.length > 0 || unparsedBlocks.length > 0" class="import-right preview-section">
        <div class="preview-header">
          <span class="preview-title">
            ✅ 已识别 {{ parsedQuestions.length }} 道题目
            <span v-if="unparsedBlocks.length > 0" style="color: #f53f3f; margin-left: 8px;">
              ⚠️ {{ unparsedBlocks.length }} 个文本块未能解析
            </span>
          </span>
          <div style="display: flex; gap: 8px;">
            <a-button @click="parsedQuestions = []; unparsedBlocks = []">清空</a-button>
            <a-button v-if="parsedQuestions.length > 0" type="primary" :loading="importLoading" @click="handleConfirmImport"
                      style="background: var(--primary-gradient); border: none;">
              📥 确认导入全部
            </a-button>
          </div>
        </div>
        <div class="preview-list">
          <!-- 已解析的题目 -->
          <div v-for="(q, idx) in parsedQuestions" :key="'q-' + idx" class="preview-item trial-card">
            <div class="preview-item-header">
              <a-tag :color="typeColors[q.type] || 'arcoblue'" size="small">{{ typeNames[q.type] || '未知' }}</a-tag>
              <a-tag v-if="q.categoryName" color="gray" size="small">{{ q.categoryName }}</a-tag>
              <span class="preview-idx">#{{ idx + 1 }}</span>
              <a-button type="text" size="mini" status="danger" @click="parsedQuestions.splice(idx, 1)">移除</a-button>
            </div>
            <div class="preview-stem">{{ q.stem }}</div>
            <div v-if="q.options && q.options.length" class="preview-options">
              <span v-for="(opt, oi) in q.options" :key="oi" class="preview-opt">
                {{ String.fromCharCode(65 + oi) }}. {{ opt }}
              </span>
            </div>
            <div class="preview-answer" v-if="q.answer">答案：<b>{{ q.answer }}</b></div>
            <div class="preview-analysis" v-if="q.analysis">解析：{{ q.analysis }}</div>
          </div>
          
          <!-- 未解析的文本块（标红显示） -->
          <div v-for="(block, idx) in unparsedBlocks" :key="'u-' + idx" class="preview-item unparsed-block">
            <div class="unparsed-header">
              <a-tag color="red" size="small">❌ 未能解析</a-tag>
              <span class="preview-idx">块 #{{ idx + 1 }}</span>
            </div>
            <div class="unparsed-text">{{ block.text }}</div>
            <div class="unparsed-hint">
              💡 提示：请检查格式是否正确，或使用 AI 解析模式
            </div>
          </div>
        </div>
      </div>
      </div>
    </a-modal>

    <!-- 批量分类弹窗 -->
    <a-modal v-model:visible="showBatchCategoryModal" title="📁 批量设置分类" width="400px" @ok="handleBatchCategory">
      <a-form :model="{ categoryId: batchCategoryId }" layout="vertical">
        <a-form-item label="选择分类">
          <a-select v-model="batchCategoryId" placeholder="请选择分类" allow-clear>
            <a-option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-alert type="info">
          将为选中的 {{ selectedIds.length }} 道题目设置分类
        </a-alert>
      </a-form>
    </a-modal>

    <!-- 批量标签弹窗 -->
    <a-modal v-model:visible="showBatchTagModal" title="🏷️ 批量设置标签" width="400px" @ok="handleBatchTag">
      <a-form :model="{ tagIds: batchTagIds }" layout="vertical">
        <a-form-item label="选择标签">
          <a-select v-model="batchTagIds" placeholder="请选择标签（可多选）" multiple allow-clear>
            <a-option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</a-option>
          </a-select>
        </a-form-item>
        <a-alert type="info">
          将为选中的 {{ selectedIds.length }} 道题目设置标签
        </a-alert>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { questionApi, categoryTagApi } from '../api/request'
import { aiConfigApi, aiParseApi } from '../api/ai'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import { useRouter } from 'vue-router'

const router = useRouter()

const typeNames: Record<number, string> = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答' }
const typeColors: Record<number, string> = { 1: 'arcoblue', 2: 'purple', 3: 'green', 4: 'orangered', 5: 'gold' }

const questions = ref<any[]>([])
const categories = ref<any[]>([])
const tags = ref<any[]>([])
const total = ref(0)
const showAddModal = ref(false)
const showImportModal = ref(false)
const showBatchCategoryModal = ref(false)
const showBatchTagModal = ref(false)
const batchCategoryId = ref<number | null>(null)
const batchTagIds = ref<number[]>([])
const importTab = ref('file')
const importText = ref('')
const importLoading = ref(false)
const editingId = ref<number | null>(null)
const importStep = ref<'upload' | 'preview'>('upload')
const fileList = ref<any[]>([])
const parsedQuestions = ref<any[]>([])
const unparsedBlocks = ref<any[]>([]) // 未解析的文本块
const importConfig = reactive({
  parseMode: 'regex' as 'regex' | 'ai' | 'hybrid',
  aiProvider: 'openai',
  type: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  difficulty: 3,
})
const selectedIds = ref<number[]>([])
const selectAll = ref(false)
const aiConfigured = ref(false)
const providerList = ref<any[]>([])
const estimatedTokens = ref(0)
const estimatedCost = ref(0)
const remainingQuota = ref(10000)

const query = reactive({
  pageNum: 1, pageSize: 10,
  keyword: '', type: undefined as number | undefined,
  difficulty: undefined as number | undefined,
  categoryId: undefined as number | undefined,
})

const form = reactive({
  type: 1, stem: '', options: ['', '', '', ''],
  answer: '', analysis: '', difficulty: 3,
  categoryId: undefined as number | undefined,
  tagIds: [] as number[],
})

onMounted(() => {
  loadQuestions()
  loadCategoriesAndTags()
  checkAIConfig()
  loadRemainingQuota()
})

async function loadQuestions() {
  const res: any = await questionApi.list(query)
  questions.value = res.data?.records || []
  total.value = res.data?.total || 0
  selectAll.value = false
  selectedIds.value = []
}

async function loadCategoriesAndTags() {
  const [catRes, tagRes]: any[] = await Promise.all([
    categoryTagApi.listCategories(),
    categoryTagApi.listTags(),
  ])
  categories.value = catRes.data || []
  tags.value = tagRes.data || []
}

function editQuestion(item: any) {
  editingId.value = item.id
  form.type = item.type
  form.stem = item.stem
  form.options = item.options || ['', '', '', '']
  form.answer = item.answer
  form.analysis = item.analysis || ''
  form.difficulty = item.difficulty
  form.categoryId = item.categoryId
  form.tagIds = item.tags?.map((t: any) => t.id) || []
  showAddModal.value = true
}

async function handleSaveQuestion() {
  if (!form.stem || !form.answer) {
    Message.warning('请填写题干和答案')
    return
  }
  if (editingId.value) {
    await questionApi.update({ id: editingId.value, ...form })
    Message.success('修改成功')
  } else {
    await questionApi.add(form)
    Message.success('添加成功')
  }
  showAddModal.value = false
  editingId.value = null
  resetForm()
  loadQuestions()
}

async function deleteQuestion(id: number) {
  await questionApi.remove(id)
  Message.success('删除成功')
  selectedIds.value = selectedIds.value.filter(sid => sid !== id)
  loadQuestions()
}

function toggleSelect(id: number) {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
  selectAll.value = selectedIds.value.length === questions.value.length
}

function handleSelectAll(checked: boolean) {
  if (checked) {
    selectedIds.value = questions.value.map(q => q.id)
  } else {
    selectedIds.value = []
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    Message.warning('请先选择要删除的题目')
    return
  }
  
  const modal = Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedIds.value.length} 道题目吗？此操作不可恢复。`,
    okText: '确认删除',
    cancelText: '取消',
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        await questionApi.batchRemove(selectedIds.value)
        Message.success(`成功删除 ${selectedIds.value.length} 道题目`)
        selectedIds.value = []
        loadQuestions()
      } catch (error) {
        Message.error('批量删除失败')
      }
    }
  })
}

async function handleBatchCategory() {
  if (selectedIds.value.length === 0) {
    Message.warning('请先选择要操作的题目')
    return
  }
  
  try {
    await questionApi.batchUpdateCategory(selectedIds.value, batchCategoryId.value)
    Message.success(`成功为 ${selectedIds.value.length} 道题目设置分类`)
    showBatchCategoryModal.value = false
    batchCategoryId.value = null
    selectedIds.value = []
    selectAll.value = false
    loadQuestions()
  } catch (error: any) {
    Message.error(error?.message || '批量设置分类失败')
  }
}

async function handleBatchTag() {
  if (selectedIds.value.length === 0) {
    Message.warning('请先选择要操作的题目')
    return
  }
  
  try {
    await questionApi.batchUpdateTags(selectedIds.value, batchTagIds.value)
    Message.success(`成功为 ${selectedIds.value.length} 道题目设置标签`)
    showBatchTagModal.value = false
    batchTagIds.value = []
    selectedIds.value = []
    selectAll.value = false
    loadQuestions()
  } catch (error: any) {
    Message.error(error?.message || '批量设置标签失败')
  }
}

function resetForm() {
  form.type = 1; form.stem = ''; form.options = ['', '', '', '']
  form.answer = ''; form.analysis = ''; form.difficulty = 3
  form.categoryId = undefined; form.tagIds = []
}

const textPlaceholder = computed(() => {
  if (importConfig.type === 1) {
    return `请粘贴【单选题】文本，例如：

1. 软件中的可执行部分是（D）。
A. 程序、文档和数据
B. 程序和文档
C. 文档和数据
D. 程序和数据`;
  } else if (importConfig.type === 2) {
    return `请粘贴【多选题】文本，例如：

1. 面向对象的三大特征包括（ABC）：
A. 封装
B. 继承
C. 多态
D. 指针`;
  } else if (importConfig.type === 3) {
    return `请粘贴【判断题】文本，例如：

1. Java是一种编译型语言。
答案：错
解析：Java是编译+解释型语言`;
  } else if (importConfig.type === 4) {
    return `请粘贴【填空题】文本，例如：

1. 类的操作和属性的可见性通常分为public 、【private】和 【protected】3种。`;
  } else if (importConfig.type === 5) {
    return `请粘贴【简答题】文本，例如：

1. 简述TCP三次握手过程。
答案：第一次客户端发送SYN...`;
  } else {
    return `请粘贴题目文本（支持混合题型），例如：

【单选题示例】
1. 软件中的可执行部分是（D）。
(A).程序、文档和数据
(B).程序和文档
(C).文档和数据
(D).程序和数据

【判断题示例】
2. Java是一种编译型语言。
答案：错
解析：Java是编译+解释型语言

【填空题示例】
3. 类的操作和属性的可见性通常分为public 、【private】和 【protected】3种。

【简答题示例】
4. 简述面向对象的三大特性。
答案：封装、继承、多态`;
  }
});

// ===== 导入功能 =====

function openImport() {
  parsedQuestions.value = []
  unparsedBlocks.value = []
  importText.value = ''
  fileList.value = []
  importStep.value = 'upload'
  importConfig.type = undefined
  importConfig.categoryId = undefined
  importConfig.difficulty = 3
  showImportModal.value = true
}

function handleFileChange(fileItems: any[]) {
  fileList.value = fileItems
}

/** 上传文件并解析 */
async function handleFileUpload() {
  if (fileList.value.length === 0) return
  importLoading.value = true
  try {
    const file = fileList.value[0].file
    const res: any = await questionApi.uploadFile(file)
    const lists = res.data || []
    
    // 附加分类名称供预览显示
    lists.forEach((item: any) => {
      if (item.categoryId) {
        const cat = categories.value.find((c: any) => c.id === item.categoryId)
        if (cat) item.categoryName = cat.name
      }
    })
    
    parsedQuestions.value = lists
    Message.success(`解析成功，共识别 ${parsedQuestions.value.length} 道题目`)
  } catch (e: any) {
    Message.error(e?.message || '文件解析失败')
  } finally {
    importLoading.value = false
  }
}

/** 文本粘贴解析 */
async function handleTextParse() {
  if (!importText.value.trim()) return
  
  // 如果使用 AI 解析，检查当前选择的提供商是否已配置
  if (importConfig.parseMode !== 'regex') {
    const selectedProvider = providerList.value.find((p: any) => p.name === importConfig.aiProvider)
    if (!selectedProvider || !selectedProvider.configured) {
      Message.warning(`请先配置 ${selectedProvider?.displayName || 'AI'} 服务后再使用`)
      return
    }
    if (estimatedTokens.value > remainingQuota.value) {
      Message.warning('今日 AI 解析额度不足')
      return
    }
  }
  
  importLoading.value = true
  try {
    let res: any
    
    if (importConfig.parseMode === 'regex') {
      // 使用正则解析
      res = await questionApi.parseText(importText.value, importConfig)
      
      // 处理返回的数据结构 { questions: [...], unparsedBlocks: [...] }
      const questions = res.data?.questions || []
      const unparsed = res.data?.unparsedBlocks || []
      
      // 附加分类名称
      questions.forEach((item: any) => {
        if (item.categoryId) {
          const cat = categories.value.find((c: any) => c.id === item.categoryId)
          if (cat) item.categoryName = cat.name
        }
      })
      
      parsedQuestions.value = questions
      unparsedBlocks.value = unparsed
      
      if (questions.length === 0 && unparsed.length === 0) {
        Message.warning('未能解析出有效题目，请检查格式')
      } else {
        let msg = `解析完成，识别 ${questions.length} 道题目`
        if (unparsed.length > 0) {
          msg += `，${unparsed.length} 个文本块未能解析`
        }
        Message.success(msg)
      }
    } else {
      // 使用 AI 解析
      res = await aiParseApi.parseText({
        text: importText.value,
        aiProvider: importConfig.aiProvider,
        type: importConfig.type,
        categoryId: importConfig.categoryId,
        difficulty: importConfig.difficulty,
      })
      
      // 显示 AI 解析的元数据
      if (res.data && res.data.meta) {
        Message.success(
          `AI 解析完成！消耗 ${res.data.meta.tokensUsed} tokens，耗时 ${(res.data.meta.parseTime / 1000).toFixed(1)}s`
        )
        // 刷新剩余额度
        await loadRemainingQuota()
      }
      
      // AI 解析返回的是 { data: [...], meta: {...} }
      const lists = res.data?.data || []
      
      // 附加分类名称
      lists.forEach((item: any) => {
        if (item.categoryId) {
          const cat = categories.value.find((c: any) => c.id === item.categoryId)
          if (cat) item.categoryName = cat.name
        }
      })
      
      parsedQuestions.value = lists
      unparsedBlocks.value = [] // AI 解析不返回未解析块
      
      if (lists.length === 0) {
        Message.warning('未能解析出有效题目，请检查格式')
      } else {
        Message.success(`解析成功，共识别 ${lists.length} 道题目`)
      }
    }
  } catch (e: any) {
    Message.error(e?.message || '文本解析失败')
  } finally {
    importLoading.value = false
  }
}

/** 确认导入 */
async function handleConfirmImport() {
  if (parsedQuestions.value.length === 0) return
  importLoading.value = true
  try {
    await questionApi.batchImport(parsedQuestions.value)
    Message.success(`成功导入 ${parsedQuestions.value.length} 道题目`)
    parsedQuestions.value = []
    showImportModal.value = false
    loadQuestions()
  } catch (e: any) {
    Message.error(e?.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

// ===== AI 相关功能 =====

async function checkAIConfig() {
  try {
    const res = await aiConfigApi.getConfig()
    if (res.data && res.data.providers) {
      providerList.value = res.data.providers
      aiConfigured.value = res.data.providers.some((p: any) => p.configured)
      // 自动选择第一个已配置的提供商
      const firstConfigured = res.data.providers.find((p: any) => p.configured)
      if (firstConfigured) {
        importConfig.aiProvider = firstConfigured.name
      }
    }
  } catch (error) {
    console.error('检查 AI 配置失败:', error)
  }
}

function handleImportProviderChange(provider: string) {
  const p = providerList.value.find((item: any) => item.name === provider)
  if (p && !p.configured) {
    Modal.confirm({
      title: '该 AI 提供商尚未配置',
      content: `${p.displayName} 尚未配置 API Key，是否前往配置页面？`,
      okText: '去配置',
      cancelText: '取消',
      onOk: () => {
        router.push({ name: 'AIConfig' })
      },
    })
  }
}

async function loadRemainingQuota() {
  try {
    const res = await aiParseApi.getRemainingQuota()
    if (res.data !== undefined) {
      remainingQuota.value = res.data
    }
  } catch (error) {
    console.error('加载剩余额度失败:', error)
  }
}

// 监听文本变化，预估 token 消耗
watch(() => importText.value, (newText) => {
  if (importConfig.parseMode !== 'regex' && newText.trim()) {
    // 简单估算：中文约 2 字符 = 1 token，英文约 4 字符 = 1 token
    const chineseChars = (newText.match(/[\u4e00-\u9fa5]/g) || []).length
    const otherChars = newText.length - chineseChars
    estimatedTokens.value = Math.ceil(chineseChars / 2 + otherChars / 4)
    
    // 估算成本（以 OpenAI gpt-4o-mini 为例：$0.15/1M tokens）
    const costPerToken = 0.00000015
    estimatedCost.value = estimatedTokens.value * costPerToken
  } else {
    estimatedTokens.value = 0
    estimatedCost.value = 0
  }
})

</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 24px; font-weight: 700; }
.header-actions { display: flex; gap: 8px; }

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding: 16px 20px;
}

.question-list { display: flex; flex-direction: column; gap: 12px; }

.question-item { 
  padding: 16px 20px;
  display: flex;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
}
.question-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.question-item.selected {
  border: 2px solid var(--accent-blue);
  background: rgba(47, 84, 235, 0.03);
}
.q-checkbox {
  display: flex;
  align-items: flex-start;
  padding-top: 2px;
}
.q-content {
  flex: 1;
}
.q-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.q-category { font-size: 12px; color: var(--text-secondary); }
.q-stem { font-size: 15px; line-height: 1.6; margin-bottom: 8px; }
.q-tags { display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 8px; }
.q-actions { display: flex; gap: 4px; justify-content: flex-end; }

.pagination-wrap { margin-top: 20px; display: flex; justify-content: center; }

/* 上传区域样式 */
.import-config {
  background: rgba(102, 126, 234, 0.05);
  padding: 16px;
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}
.import-config :deep(.arco-form-item) {
  margin-bottom: 0;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  background: var(--bg-card-hover);
  cursor: pointer;
  transition: all 0.3s;
}
.upload-trigger:hover {
  border-color: var(--accent-blue);
  background: rgba(47, 84, 235, 0.04);
}
.upload-icon { font-size: 48px; margin-bottom: 12px; }
.upload-title { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.upload-hint { font-size: 13px; color: var(--text-tertiary); }

/* 导入弹窗左右布局 */
.import-layout {
  display: flex;
  gap: 24px;
  align-items: stretch;
}
.import-left {
  flex: 1;
  min-width: 0;
  transition: all 0.3s;
}
.import-layout.has-preview .import-left {
  flex: 0 0 40%;
}
.import-right {
  flex: 0 0 calc(60% - 24px);
  border-left: 1px solid var(--border-color);
  padding-left: 24px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 预览区域 */
.preview-section {
  margin-top: 0;
  border-top: none;
  padding-top: 0;
  height: 100%;
}
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--accent-green);

  margin-bottom: 8px;
}
.preview-idx {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-tertiary);
  flex: 1;
}
.preview-stem {
  font-size: 15px;
  font-weight: 500;
  line-height: 1.6;
  margin-bottom: 10px;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
}
.preview-options {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
  padding-left: 8px;
}
.preview-opt {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-wrap: break-word;
}
.preview-answer {
  font-size: 14px;
  color: var(--accent-blue);
  margin-bottom: 4px;
  padding: 8px 12px;
  background: rgba(47, 84, 235, 0.05);
  border-radius: 4px;
}
.preview-analysis {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 6px;
  line-height: 1.5;
  padding: 8px 12px;
  background: var(--bg-card-hover);
  border-radius: 4px;
}

/* AI 解析模式选项样式 */
.mode-option {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.mode-hint {
  font-size: 11px;
  color: #86909c;
}

/* 未解析文本块样式 */
.unparsed-block {
  background: #fff1f0 !important;
  border: 2px solid #f53f3f !important;
  box-shadow: 0 2px 8px rgba(245, 63, 63, 0.15) !important;
}
.unparsed-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.unparsed-text {
  font-size: 14px;
  line-height: 1.8;
  color: #1d2129;
  padding: 12px;
  background: white;
  border-radius: 4px;
  border-left: 3px solid #f53f3f;
  margin-bottom: 10px;
  white-space: pre-wrap;
  word-break: break-word;
}
.unparsed-hint {
  font-size: 12px;
  color: #f77234;
  padding: 8px 12px;
  background: rgba(255, 125, 0, 0.08);
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
