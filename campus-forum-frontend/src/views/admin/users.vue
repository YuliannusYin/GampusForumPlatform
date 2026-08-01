<template>
  <div class="users-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">管理平台用户、角色与状态</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="用户名 / 昵称 / 邮箱"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常" :value="0" />
            <el-option label="封禁" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" class="admin-table">
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar">{{ initialOf(row) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="points" label="积分" width="90" align="center" />
        <el-table-column prop="level" label="等级" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="success" class="status-chip">正常</el-tag>
            <el-tag v-else type="danger" class="status-chip">封禁</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="160">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles || []"
              :key="role.id"
              :type="role.code === 'ROLE_ADMIN' ? 'warning' : 'info'"
              size="small"
              class="role-chip"
            >
              {{ role.name || role.code }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.status === 0 ? 'danger' : 'success'"
              size="small"
              link
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 0 ? '封禁' : '解禁' }}
            </el-button>
            <el-button type="primary" size="small" link @click="openRoleDialog(row)">
              修改角色
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadList"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 修改角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="修改角色" width="420px">
      <el-checkbox-group v-model="roleForm.roleIds">
        <div v-for="opt in roleOptions" :key="opt.id" class="role-item">
          <el-checkbox :value="opt.id">{{ opt.name }}（{{ opt.code }}）</el-checkbox>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSubmitting" @click="submitRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'
import { getAdminUsers, updateUserStatus, updateUserRole } from '@/api/admin'

// 查询条件
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined
})
// 列表数据
const list = ref([])
const total = ref(0)
const loading = ref(false)

// 角色弹窗
const roleDialogVisible = ref(false)
const roleSubmitting = ref(false)
const roleOptions = ref([
  { id: 1, code: 'ROLE_USER', name: '普通用户' },
  { id: 2, code: 'ROLE_ADMIN', name: '管理员' }
])
const roleForm = reactive({
  userId: null,
  roleIds: []
})

// 头像首字母占位
const initialOf = (row) => {
  const name = row.nickname || row.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
}

// 加载用户列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      status: query.status === '' ? undefined : query.status
    }
    const res = await getAdminUsers(params)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  query.page = 1
  loadList()
}

// 重置
const handleReset = () => {
  query.keyword = ''
  query.status = undefined
  query.page = 1
  loadList()
}

// 每页条数变化
const handleSizeChange = () => {
  query.page = 1
  loadList()
}

// 封禁/解禁
const handleToggleStatus = async (row) => {
  const target = row.status === 0 ? 1 : 0
  const action = target === 1 ? '封禁' : '解禁'
  try {
    await ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  try {
    await updateUserStatus(row.id, { status: target })
    ElMessage.success(`${action}成功`)
    loadList()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 打开修改角色弹窗
const openRoleDialog = (row) => {
  roleForm.userId = row.id
  // 根据已有角色提取 roleIds
  const roles = row.roles || []
  roleForm.roleIds = roles.map((r) => r.id)
  roleDialogVisible.value = true
}

// 提交角色修改
const submitRole = async () => {
  roleSubmitting.value = true
  try {
    await updateUserRole(roleForm.userId, { roleIds: roleForm.roleIds })
    ElMessage.success('角色修改成功')
    roleDialogVisible.value = false
    loadList()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    roleSubmitting.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.users-page {
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

/* ===== 筛选卡片 ===== */
.filter-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  box-shadow: var(--shadow-1);
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: var(--space-4);
}

.filter-card :deep(.el-form-item__label) {
  color: var(--color-text-2);
  font-weight: var(--font-weight-medium);
}

/* ===== 表格卡片 ===== */
.table-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.admin-table {
  border-radius: var(--radius-xl);
}

/* Status chip tags */
.status-chip {
  border-radius: var(--radius-full);
}

.role-chip {
  margin-right: var(--space-1);
  border-radius: var(--radius-full);
}

/* ===== 分页 ===== */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--color-border-light);
}

/* ===== 角色弹窗 ===== */
.role-item {
  padding: var(--space-2) 0;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .filter-card :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: var(--space-2);
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
