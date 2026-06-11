<template>
  <div class="category-tag-page">
    <a-card class="page-card" :bordered="false">
      <a-tabs default-active-key="category" type="rounded">
        <!-- 分类管理 -->
        <a-tab-pane key="category" title="分类管理">
          <div class="tab-header">
            <a-button type="primary" @click="handleAddCategory">
              <template #icon><icon-plus /></template>
              新增分类
            </a-button>
          </div>

          <a-table :data="categories" :loading="categoryLoading" :pagination="false" class="data-table">
            <template #columns>
              <a-table-column title="分类名称" data-index="name" />
              <a-table-column title="排序" data-index="sort" :width="100" />
              <a-table-column title="创建时间" data-index="createTime" :width="180" />
              <a-table-column title="操作" :width="180" align="center">
                <template #cell="{ record }">
                  <a-space>
                    <a-button type="text" size="small" @click="handleEditCategory(record)">
                      编辑
                    </a-button>
                    <a-popconfirm content="确定删除该分类吗？" @ok="handleDeleteCategory(record.id)">
                      <a-button type="text" status="danger" size="small">
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-tab-pane>

        <!-- 标签管理 -->
        <a-tab-pane key="tag" title="标签管理">
          <div class="tab-header">
            <a-button type="primary" @click="handleAddTag">
              <template #icon><icon-plus /></template>
              新增标签
            </a-button>
          </div>

          <a-table :data="tags" :loading="tagLoading" :pagination="false" class="data-table">
            <template #columns>
              <a-table-column title="标签名称" data-index="name" />
              <a-table-column title="创建时间" data-index="createTime" :width="180" />
              <a-table-column title="操作" :width="180" align="center">
                <template #cell="{ record }">
                  <a-space>
                    <a-button type="text" size="small" @click="handleEditTag(record)">
                      编辑
                    </a-button>
                    <a-popconfirm content="确定删除该标签吗？" @ok="handleDeleteTag(record.id)">
                      <a-button type="text" status="danger" size="small">
                        删除
                      </a-button>
                    </a-popconfirm>
                  </a-space>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 分类编辑弹窗 -->
    <a-modal v-model:visible="categoryModalVisible" :title="categoryForm.id ? '编辑分类' : '新增分类'"
             @ok="handleCategorySubmit" @cancel="categoryModalVisible = false" width="500px">
      <a-form :model="categoryForm" layout="vertical">
        <a-form-item label="分类名称" required>
          <a-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model="categoryForm.sort" placeholder="数字越小越靠前" :min="0" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 标签编辑弹窗 -->
    <a-modal v-model:visible="tagModalVisible" :title="tagForm.id ? '编辑标签' : '新增标签'"
             @ok="handleTagSubmit" @cancel="tagModalVisible = false" width="500px">
      <a-form :model="tagForm" layout="vertical">
        <a-form-item label="标签名称" required>
          <a-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import { categoryTagApi } from '../api/request'

// 分类相关
const categories = ref<any[]>([])
const categoryLoading = ref(false)
const categoryModalVisible = ref(false)
const categoryForm = ref({
  id: null as number | null,
  name: '',
  sort: 0,
})

// 标签相关
const tags = ref<any[]>([])
const tagLoading = ref(false)
const tagModalVisible = ref(false)
const tagForm = ref({
  id: null as number | null,
  name: '',
})

onMounted(() => {
  loadCategories()
  loadTags()
})

// ===== 分类管理 =====
async function loadCategories() {
  categoryLoading.value = true
  try {
    const res = await categoryTagApi.listCategories()
    categories.value = res.data || []
  } catch (error) {
    Message.error('加载分类失败')
  } finally {
    categoryLoading.value = false
  }
}

function handleAddCategory() {
  categoryForm.value = { id: null, name: '', sort: 0 }
  categoryModalVisible.value = true
}

function handleEditCategory(record: any) {
  categoryForm.value = { ...record }
  categoryModalVisible.value = true
}

async function handleCategorySubmit() {
  if (!categoryForm.value.name) {
    Message.warning('请输入分类名称')
    return
  }

  try {
    if (categoryForm.value.id) {
      await categoryTagApi.updateCategory(categoryForm.value)
      Message.success('修改成功')
    } else {
      await categoryTagApi.addCategory(categoryForm.value)
      Message.success('添加成功')
    }
    categoryModalVisible.value = false
    loadCategories()
  } catch (error) {
    Message.error('操作失败')
  }
}

async function handleDeleteCategory(id: number) {
  try {
    await categoryTagApi.deleteCategory(id)
    Message.success('删除成功')
    loadCategories()
  } catch (error) {
    Message.error('删除失败')
  }
}

// ===== 标签管理 =====
async function loadTags() {
  tagLoading.value = true
  try {
    const res = await categoryTagApi.listTags()
    tags.value = res.data || []
  } catch (error) {
    Message.error('加载标签失败')
  } finally {
    tagLoading.value = false
  }
}

function handleAddTag() {
  tagForm.value = { id: null, name: '' }
  tagModalVisible.value = true
}

function handleEditTag(record: any) {
  tagForm.value = { ...record }
  tagModalVisible.value = true
}

async function handleTagSubmit() {
  if (!tagForm.value.name) {
    Message.warning('请输入标签名称')
    return
  }

  try {
    if (tagForm.value.id) {
      await categoryTagApi.updateTag(tagForm.value)
      Message.success('修改成功')
    } else {
      await categoryTagApi.addTag(tagForm.value)
      Message.success('添加成功')
    }
    tagModalVisible.value = false
    loadTags()
  } catch (error) {
    Message.error('操作失败')
  }
}

async function handleDeleteTag(id: number) {
  try {
    await categoryTagApi.deleteTag(id)
    Message.success('删除成功')
    loadTags()
  } catch (error) {
    Message.error('删除失败')
  }
}
</script>

<style scoped>
.category-tag-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-card {
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: var(--shadow-card);
}

.tab-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}

.data-table {
  margin-top: 16px;
}

:deep(.arco-table) {
  background: transparent;
}

:deep(.arco-table-th) {
  background: rgba(102, 126, 234, 0.05);
  font-weight: 600;
}

:deep(.arco-table-tr:hover) {
  background: rgba(102, 126, 234, 0.03);
}
</style>
