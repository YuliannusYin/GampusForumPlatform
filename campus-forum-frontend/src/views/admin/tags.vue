<template>
  <div class="tags-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">标签管理</h1>
        <p class="page-subtitle">管理帖子标签与分类</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增标签</el-button>
    </div>

    <!-- 标签卡片网格 -->
    <div class="tag-grid" v-loading="loading">
      <div
        v-for="(tag, index) in list"
        :key="tag.id"
        class="tag-card"
      >
        <div class="tag-card-top">
          <div class="tag-icon-wrap" :class="'grad-' + (index % 5)">
            <span class="tag-hash">#</span>
          </div>
          <div class="tag-actions">
            <el-button size="small" link @click="openEditDialog(tag)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(tag)">删除</el-button>
          </div>
        </div>
        <div class="tag-name">{{ tag.name }}</div>
        <div class="tag-count">
          <span class="count-num">{{ tag.postCount || 0 }}</span>
          <span class="count-label">篇帖子</span>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="暂无标签" class="tag-empty" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑标签' : '新增标签'"
      width="400px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTags } from '@/api/post'
import { createTag, updateTag, deleteTag } from '@/api/admin'

// 列表
const list = ref([])
const loading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({
  name: ''
})

const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

// 加载标签列表
const loadList = async () => {
  loading.value = true
  try {
    const data = await getTags()
    list.value = data || []
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    list.value = []
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const openCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  form.name = ''
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name || ''
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  formRef.value && formRef.value.clearValidate()
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const payload = { name: form.name }
    if (isEdit.value) {
      await updateTag(editId.value, payload)
      ElMessage.success('编辑成功')
    } else {
      await createTag(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    submitting.value = false
  }
}

// 删除标签
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除标签「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTag(row.id)
        ElMessage.success('删除成功')
        loadList()
      } catch (err) {
        // 错误已由 request.js 拦截器统一提示
      }
    })
    .catch(() => {})
}

// 首次加载
onMounted(() => {
  loadList()
})
</script>

<style scoped>
.tags-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ===== 页面标题 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0;
  line-height: var(--line-height-tight);
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: var(--space-1) 0 0;
}

/* ===== 标签卡片网格 ===== */
.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-4);
  min-height: 200px;
}

.tag-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-1);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.tag-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-3);
}

.tag-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.tag-hash {
  font-size: 20px;
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  line-height: 1;
}

/* Gradient backgrounds cycling through 5 gradients */
.grad-0 { background: var(--gradient-primary); }
.grad-1 { background: var(--gradient-mint); }
.grad-2 { background: var(--gradient-gold); }
.grad-3 { background: var(--gradient-sunset); }
.grad-4 { background: var(--gradient-purple); }

.tag-actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.tag-name {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-count {
  display: flex;
  align-items: baseline;
  gap: var(--space-1);
}

.count-num {
  font-family: var(--font-display);
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-extrabold);
  color: var(--color-text-2);
}

.count-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

/* Empty state */
.tag-empty {
  grid-column: 1 / -1;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .tag-grid {
    grid-template-columns: 1fr;
  }
}
</style>
