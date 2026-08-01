<template>
  <div class="test-data-page">
    <!-- 顶部操作区 -->
    <el-card shadow="never" class="action-card">
      <div class="action-header">
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
    </el-card>

    <!-- 统计卡片区 -->
    <el-row :gutter="20" class="stat-row" v-loading="statusLoading">
      <el-col :xs="12" :sm="8" :md="6" :lg="4" :xl="3" v-for="card in cards" :key="card.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-body">
            <el-icon class="stat-icon" :style="{ color: card.color, backgroundColor: card.bg }">
              <component :is="card.icon" />
            </el-icon>
            <div class="stat-meta">
              <div class="stat-value" :class="{ 'stat-zero': !status[card.key] }">
                {{ status[card.key] || 0 }}
              </div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作结果区 -->
    <el-card v-if="lastResult" shadow="never" class="result-card">
      <template #header>
        <div class="result-header">
          <span class="result-title">本次操作结果</span>
          <el-tag :type="lastResult.type === 'import' ? 'success' : 'danger'" size="small">
            {{ lastResult.type === 'import' ? '导入' : '移除' }}
          </el-tag>
        </div>
      </template>
      <el-table :data="resultRows" border stripe>
        <el-table-column label="数据类别" min-width="160">
          <template #default="{ row }">
            <el-icon class="result-row-icon" :style="{ color: row.color }">
              <component :is="row.icon" />
            </el-icon>
            {{ row.label }}
          </template>
        </el-table-column>
        <el-table-column prop="count" label="数量" width="140" align="center">
          <template #default="{ row }">
            <span class="result-count">{{ row.count || 0 }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
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

// 卡片配置
const cards = [
  { key: 'users', label: '测试用户', icon: 'User', color: '#409eff', bg: '#ecf5ff' },
  { key: 'posts', label: '测试帖子', icon: 'Document', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'comments', label: '测试评论', icon: 'ChatDotRound', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'clubs', label: '测试社团', icon: 'UserFilled', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'likes', label: '点赞记录', icon: 'Pointer', color: '#909399', bg: '#f4f4f5' },
  { key: 'favorites', label: '收藏记录', icon: 'Star', color: '#ff9c44', bg: '#fdf6ec' },
  { key: 'follows', label: '关注关系', icon: 'Connection', color: '#409eff', bg: '#ecf5ff' },
  { key: 'chatSessions', label: '私信会话', icon: 'ChatLineRound', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'chatMessages', label: '私信消息', icon: 'Message', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'notifications', label: '通知', icon: 'Bell', color: '#f56c6c', bg: '#fef0f0' }
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
      color: card.color,
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
  gap: 20px;
}

/* 顶部操作区 */
.action-card {
  border-radius: 6px;
}

.action-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.action-title-wrap {
  flex: 1;
  min-width: 280px;
}

.action-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.action-subtitle {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 0;
}

.stat-card {
  margin-bottom: 20px;
  border-radius: 6px;
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  font-size: 28px;
  border-radius: 8px;
  flex-shrink: 0;
}

.stat-meta {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-zero {
  color: #c0c4cc;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 操作结果区 */
.result-card {
  border-radius: 6px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.result-row-icon {
  font-size: 16px;
  vertical-align: -2px;
  margin-right: 4px;
}

.result-count {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
