<template>
  <div class="clubs-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">社团审核</h1>
        <p class="page-subtitle">审核待批准的社团申请</p>
      </div>
      <el-button :icon="Refresh" @click="loadList">刷新</el-button>
    </div>

    <!-- 社团卡片网格 -->
    <div class="club-grid" v-loading="loading">
      <div
        v-for="(club, index) in list"
        :key="club.id"
        class="club-card"
      >
        <div class="club-card-top">
          <div class="club-icon-wrap" :class="'grad-' + (index % 5)">
            <span class="club-initial">{{ (club.name || 'C').charAt(0) }}</span>
          </div>
          <span class="status-badge">待审核</span>
        </div>
        <div class="club-name">{{ club.name }}</div>
        <div class="club-desc">{{ club.description || '暂无简介' }}</div>
        <div class="club-meta">
          <span class="meta-item">
            <span class="meta-label">创建者</span>
            <span class="meta-value">{{ club.creatorName || '-' }}</span>
          </span>
          <span class="meta-item">
            <span class="meta-label">创建时间</span>
            <span class="meta-value">{{ formatTime(club.createTime) }}</span>
          </span>
        </div>
        <div class="club-actions">
          <el-button
            type="success"
            size="small"
            :loading="club._approving"
            @click="handleApprove(club)"
          >
            通过
          </el-button>
          <el-button
            type="danger"
            size="small"
            plain
            :loading="club._rejecting"
            @click="handleReject(club)"
          >
            拒绝
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="暂无待审核社团" class="club-empty" />
    </div>

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

/* ===== 社团卡片网格 ===== */
.club-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-4);
  min-height: 200px;
}

.club-card {
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

.club-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-3);
}

.club-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.club-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.club-initial {
  font-family: var(--font-display);
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

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-caption);
  font-weight: var(--font-weight-medium);
  background: var(--color-warning-bg);
  color: var(--color-warning);
}

.club-name {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  line-height: var(--line-height-normal);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 42px;
}

.club-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-3) 0;
  border-top: 1px solid var(--color-border-light);
  border-bottom: 1px solid var(--color-border-light);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
}

.meta-label {
  color: var(--color-text-3);
  flex-shrink: 0;
  min-width: 60px;
}

.meta-value {
  color: var(--color-text-2);
  font-weight: var(--font-weight-medium);
}

.club-actions {
  display: flex;
  gap: var(--space-2);
}

.club-actions .el-button {
  flex: 1;
}

/* Empty state */
.club-empty {
  grid-column: 1 / -1;
}

/* ===== 分页 ===== */
.pagination-wrap {
  display: flex;
  justify-content: center;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .club-grid {
    grid-template-columns: 1fr;
  }
}
</style>
