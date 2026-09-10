<template>
  <div class="reports-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">举报处理</h1>
        <p class="page-subtitle">处理表白墙帖子与评论举报，属实将删除对应内容</p>
      </div>
    </div>

    <div class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" :value="0" />
            <el-option label="属实已处理" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.targetType" placeholder="全部" clearable style="width: 140px">
            <el-option label="帖子" :value="1" />
            <el-option label="评论" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" class="admin-table">
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            {{ row.targetType === 1 ? '帖子' : '评论' }}
          </template>
        </el-table-column>
        <el-table-column label="内容摘要" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetTitle || '-' }}</template>
        </el-table-column>
        <el-table-column prop="targetAuthorUsername" label="真实作者" width="120" show-overflow-tooltip />
        <el-table-column prop="reporterUsername" label="举报人" width="120" show-overflow-tooltip />
        <el-table-column label="原因" width="110">
          <template #default="{ row }">{{ reasonText(row.reason) }}</template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="danger" size="small">属实</el-tag>
            <el-tag v-else type="info" size="small">驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="danger" size="small" link @click="openHandle(row, 1)">属实删除</el-button>
              <el-button type="primary" size="small" link @click="openHandle(row, 2)">驳回</el-button>
            </template>
            <span v-else class="text-muted">{{ row.handlerUsername || '已处理' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadList"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'
import { getAdminReports, handleAdminReport } from '@/api/report'

const reasonMap = {
  1: '垃圾广告',
  2: '辱骂骚扰',
  3: '色情低俗',
  4: '人身攻击',
  5: '其他'
}

const query = reactive({
  page: 1,
  size: 10,
  status: 0,
  targetType: undefined
})
const list = ref([])
const total = ref(0)
const loading = ref(false)

const reasonText = (reason) => reasonMap[reason] || '其他'

const loadList = async () => {
  loading.value = true
  try {
    const res = await getAdminReports({
      page: query.page,
      size: query.size,
      status: query.status === '' || query.status === null ? undefined : query.status,
      targetType: query.targetType || undefined
    })
    list.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  loadList()
}

const handleReset = () => {
  query.status = 0
  query.targetType = undefined
  query.page = 1
  loadList()
}

const handleSizeChange = () => {
  query.page = 1
  loadList()
}

const openHandle = (row, status) => {
  const action = status === 1 ? '确认违规并删除该内容' : '驳回该举报'
  ElMessageBox.prompt(`确定要${action}吗？可填写处理备注。`, '处理举报', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '处理备注（可选）',
    inputType: 'textarea'
  })
    .then(async ({ value }) => {
      await handleAdminReport(row.id, {
        status,
        handleRemark: value || undefined
      })
      ElMessage.success(status === 1 ? '已删除违规内容' : '已驳回')
      loadList()
    })
    .catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.reports-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.page-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: var(--space-1) 0 0;
}

.filter-card,
.table-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
}

.filter-card {
  padding: var(--space-4) var(--space-5);
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.table-card {
  overflow: hidden;
}

.text-muted {
  color: var(--color-text-4);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--color-border-light);
}
</style>
