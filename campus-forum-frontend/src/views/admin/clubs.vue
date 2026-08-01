<template>
  <div class="clubs-page">
    <el-card shadow="never" class="table-card">
      <!-- 顶部操作栏 -->
      <div class="toolbar">
        <span class="toolbar-title">待审核社团列表</span>
        <el-button :icon="Refresh" @click="loadList">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="name" label="社团名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="简介" min-width="240" show-overflow-tooltip />
        <el-table-column prop="creatorName" label="创建者" width="140" align="center" show-overflow-tooltip />
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="success" size="small" link :loading="row._approving" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" size="small" link :loading="row._rejecting" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next, total"
          background
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getPendingClubs, approveClub, rejectClub } from '@/api/admin'
import { formatTime } from '@/utils/format'

// 列表数据
const list = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载待审核社团列表
const loadList = async () => {
  loading.value = true
  try {
    const res = await getPendingClubs({ page: currentPage.value, size: pageSize.value })
    list.value = (res.records || []).map((item) => ({
      ...item,
      _approving: false,
      _rejecting: false
    }))
    total.value = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 审核通过
const handleApprove = async (row) => {
  row._approving = true
  try {
    await approveClub(row.id)
    ElMessage.success('已通过审核')
    list.value = list.value.filter((c) => c.id !== row.id)
    total.value = Math.max(0, total.value - 1)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    row._approving = false
  }
}

// 审核拒绝
const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要拒绝社团「${row.name}」的申请吗？`, '提示', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  row._rejecting = true
  try {
    await rejectClub(row.id)
    ElMessage.success('已拒绝申请')
    list.value = list.value.filter((c) => c.id !== row.id)
    total.value = Math.max(0, total.value - 1)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    row._rejecting = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.clubs-page {
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

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
