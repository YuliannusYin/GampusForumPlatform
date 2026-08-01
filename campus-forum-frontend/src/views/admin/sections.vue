<template>
  <div class="sections-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">板块管理</h1>
        <p class="page-subtitle">管理论坛板块与分类</p>
      </div>
    </div>

    <div class="table-card">
      <!-- 顶部操作栏 -->
      <div class="toolbar">
        <span class="toolbar-title">板块列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增板块</el-button>
      </div>

      <el-table :data="list" v-loading="loading" class="admin-table">
        <el-table-column label="名称" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="section-name-cell">
              <span class="section-dot" :class="'dot-color-' + (row.id % 8)"></span>
              <span class="section-name-text">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="100" align="center">
          <template #default="{ row }">
            <div v-if="row.icon" class="section-icon-box">{{ row.icon }}</div>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column label="帖子数" width="110" align="center">
          <template #default="{ row }">
            <span class="count-chip">{{ row.postCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑板块' : '新增板块'"
      width="480px"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入板块名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入板块描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="图标（可选）" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right" />
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
import { getSections } from '@/api/post'
import { createSection, updateSection, deleteSection } from '@/api/admin'

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
  name: '',
  description: '',
  icon: '',
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入板块名称', trigger: 'blur' }]
}

// 加载板块列表
const loadList = async () => {
  loading.value = true
  try {
    const data = await getSections()
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
  Object.assign(form, { name: '', description: '', icon: '', sort: 0 })
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    description: row.description || '',
    icon: row.icon || '',
    sort: row.sort ?? 0
  })
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
    const payload = {
      name: form.name,
      description: form.description,
      icon: form.icon,
      sort: form.sort
    }
    if (isEdit.value) {
      await updateSection(editId.value, payload)
      ElMessage.success('编辑成功')
    } else {
      await createSection(payload)
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

// 删除板块
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除板块「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteSection(row.id)
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
.sections-page {
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

/* ===== 表格卡片 ===== */
.table-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border-light);
}

.toolbar-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.admin-table {
  border-radius: 0;
}

/* Section name cell with colored dot */
.section-name-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.section-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.dot-color-0 { background: var(--color-primary); }
.dot-color-1 { background: var(--color-success); }
.dot-color-2 { background: var(--color-warning); }
.dot-color-3 { background: var(--color-danger); }
.dot-color-4 { background: var(--color-violet); }
.dot-color-5 { background: var(--color-teal); }
.dot-color-6 { background: var(--color-coral); }
.dot-color-7 { background: var(--color-primary-hover); }

.section-name-text {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-1);
}

/* Section icon box */
.section-icon-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--color-bg-page);
  font-size: 20px;
}

.text-muted {
  color: var(--color-text-4);
}

/* Post count chip */
.count-chip {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .toolbar {
    padding: var(--space-3) var(--space-4);
  }
}
</style>
