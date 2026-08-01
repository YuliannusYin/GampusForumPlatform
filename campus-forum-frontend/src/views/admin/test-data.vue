<template>
  <div class="test-data-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">测试数据管理</h1>
        <p class="page-subtitle">管理系统测试数据的导入与移除</p>
      </div>
    </div>

    <!-- 顶部操作区 -->
    <div class="action-card">
      <div class="action-accent"></div>
      <div class="action-body">
        <div class="action-title-wrap">
          <div class="action-title">测试数据管理</div>
          <div class="action-subtitle">
            本功能仅超级管理员可用。测试数据使用 test_ 用户名前缀标识，可一键导入/移除，不影响真实数据。
          </div>
        </div>
        <div class="action-buttons">
          <el-button
            type="success"
            :icon="Download"
            :loading="importing"
            @click="handleImport"
          >
            一键导入测试数据
          </el-button>
          <el-button
            type="danger"
            :icon="Delete"
            :loading="removing"
            @click="handleRemove"
          >
            一键移除测试数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片区 -->
    <div class="stat-grid" v-loading="statusLoading">
      <div
        v-for="(card, index) in cards"
        :key="card.key"
        class="stat-card"
      >
        <div class="stat-card-body">
          <div class="stat-icon-wrap" :class="'grad-' + (index % 5)">
            <el-icon class="stat-icon"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-meta">
            <div class="stat-value" :class="{ 'stat-zero': !status[card.key] }">
              {{ status[card.key] || 0 }}
            </div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作结果区 -->
    <div v-if="lastResult" class="result-card">
      <div class="result-header">
        <span class="result-title">本次操作结果</span>
        <el-tag :type="lastResult.type === 'import' ? 'success' : 'danger'" size="small" class="status-chip">
          {{ lastResult.type === 'import' ? '导入' : '移除' }}
        </el-tag>
      </div>
      <el-table :data="resultRows" class="admin-table">
        <el-table-column label="数据类别" min-width="160">
          <template #default="{ row }">
            <el-icon class="result-row-icon"><component :is="row.icon" /></el-icon>
            {{ row.label }}
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="140" align="center">
          <template #default="{ row }">
            <span class="result-count">{{ row.count || 0 }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Delete } from '@element-plus/icons-vue'
import { importTestData, removeTestData, getTestDataStatus } from '@/api/admin'

// 当前状态
const status = reactive({
  users: 0,
  posts: 0,
  comments: 0,
  clubs: 0,
  likes: 0,
  favorites: 0,
  follows: 0,
  chatSessions: 0,
  chatMessages: 0,
  notifications: 0,
  clubMembers: 0,
  clubPosts: 0
})
const statusLoading = ref(false)

// 操作状态
const importing = ref(false)
const removing = ref(false)

// 操作结果
const lastResult = ref(null) // { type: 'import'|'remove', data: {...} }

// 卡片配置（渐变背景由 CSS 类 grad-<n> 控制）
const cards = [
  { key: 'users', label: '测试用户', icon: 'User' },
  { key: 'posts', label: '测试帖子', icon: 'Document' },
  { key: 'comments', label: '测试评论', icon: 'ChatDotRound' },
  { key: 'clubs', label: '测试社团', icon: 'UserFilled' },
  { key: 'likes', label: '点赞记录', icon: 'Pointer' },
  { key: 'favorites', label: '收藏记录', icon: 'Star' },
  { key: 'follows', label: '关注关系', icon: 'Connection' },
  { key: 'chatSessions', label: '私信会话', icon: 'ChatLineRound' },
  { key: 'chatMessages', label: '私信消息', icon: 'Message' },
  { key: 'notifications', label: '通知', icon: 'Bell' }
]

// 操作结果表格数据
const resultRows = computed(() => {
  if (!lastResult.value || !lastResult.value.data) return []
  const data = lastResult.value.data
  return cards
    .filter((card) => data[card.key] !== undefined && data[card.key] !== null)
    .map((card) => ({
      key: card.key,
      label: card.label,
      icon: card.icon,
      count: data[card.key]
    }))
})

// 加载状态
const loadStatus = async () => {
  statusLoading.value = true
  try {
    const data = await getTestDataStatus()
    Object.assign(status, data || {})
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    statusLoading.value = false
  }
}

// 导入
const handleImport = async () => {
  try {
    await ElMessageBox.confirm(
      '将向系统注入约 100 用户、500 帖子、1000 评论、10 社团、800 点赞、500 收藏、300 关注、50 私信会话等测试数据，是否继续？',
      '确认导入测试数据',
      { type: 'warning', confirmButtonText: '确认导入', cancelButtonText: '取消' }
    )
  } catch {
    return // 用户取消
  }

  importing.value = true
  lastResult.value = null
  try {
    const data = await importTestData()
    lastResult.value = { type: 'import', data }
    ElMessage.success('测试数据导入成功')
    await loadStatus()
  } catch (e) {
    // 错误已由拦截器处理（如重复导入提示）
  } finally {
    importing.value = false
  }
}

// 移除
const handleRemove = async () => {
  try {
    await ElMessageBox.confirm(
      '将移除系统中所有以 test_ 开头的用户及其关联数据（帖子、评论、点赞、收藏、私信、通知等），操作不可恢复，是否继续？',
      '确认移除测试数据',
      { type: 'error', confirmButtonText: '确认移除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  removing.value = true
  lastResult.value = null
  try {
    const data = await removeTestData()
    lastResult.value = { type: 'remove', data }
    ElMessage.success('测试数据移除成功')
    await loadStatus()
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    removing.value = false
  }
}

onMounted(() => {
  loadStatus()
})
</script>

<style scoped>
.test-data-page {
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

/* ===== 操作卡片 ===== */
.action-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.action-accent {
  height: 4px;
  background: var(--gradient-primary);
}

.action-body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-4);
  padding: var(--space-5);
}

.action-title-wrap {
  flex: 1;
  min-width: 280px;
}

.action-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin-bottom: var(--space-2);
}

.action-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  line-height: var(--line-height-relaxed);
}

.action-buttons {
  display: flex;
  gap: var(--space-3);
  flex-shrink: 0;
}

/* ===== 统计卡片 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-4);
}

.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
  box-shadow: var(--shadow-1);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-3);
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.stat-icon {
  font-size: 22px;
  color: var(--color-white);
}

/* Gradient backgrounds cycling through 5 gradients */
.grad-0 { background: var(--gradient-primary); }
.grad-1 { background: var(--gradient-mint); }
.grad-2 { background: var(--gradient-gold); }
.grad-3 { background: var(--gradient-sunset); }
.grad-4 { background: var(--gradient-purple); }

.stat-meta {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-family: var(--font-display);
  font-size: 22px;
  font-weight: var(--font-weight-extrabold);
  color: var(--color-text-1);
  line-height: 1.2;
}

.stat-zero {
  color: var(--color-text-4);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin-top: 2px;
}

/* ===== 操作结果区 ===== */
.result-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.result-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border-light);
}

.result-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.status-chip {
  border-radius: var(--radius-full);
}

.admin-table {
  border-radius: 0;
}

.result-row-icon {
  color: var(--color-primary);
  font-size: 16px;
  vertical-align: -2px;
  margin-right: var(--space-1);
}

.result-count {
  font-family: var(--font-display);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .action-body {
    flex-direction: column;
  }

  .action-buttons {
    width: 100%;
  }

  .action-buttons .el-button {
    flex: 1;
  }

  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
