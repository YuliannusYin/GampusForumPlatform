<template>
  <div class="tags-page">
    <el-card shadow="never" class="table-card">
      <!-- 顶部操作栏 -->
      <div class="toolbar">
        <span class="toolbar-title">标签列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增标签</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="postCount" label="帖子数" width="140" align="center" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
  gap: 16px;
}

.table-card {
  border-radius: 6px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>
