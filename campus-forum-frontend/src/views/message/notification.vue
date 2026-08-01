<template>
  <div class="notification-page">
    <!-- 顶部标题与操作 -->
    <div class="page-header fade-in-up">
      <div class="header-left">
        <h2 class="page-title">
          <span class="title-icon">
            <el-icon><Bell /></el-icon>
          </span>
          消息通知
        </h2>
        <span v-if="list.filter(i => i.isRead === 0).length > 0" class="unread-badge">{{ list.filter(i => i.isRead === 0).length }} 条未读</span>
      </div>
      <el-button type="primary" plain :loading="markAllLoading" @click="handleMarkAllRead">
        <el-icon><Check /></el-icon>
        全部已读
      </el-button>
    </div>

    <!-- 分类标签 -->
    <div class="tab-bar fade-in-up delay-1">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="评论" name="1" />
        <el-tab-pane label="点赞" name="2" />
        <el-tab-pane label="私信" name="3" />
        <el-tab-pane label="系统" name="4" />
      </el-tabs>
    </div>

    <!-- 通知列表 -->
    <div v-loading="loading" class="list-wrap">
      <div
        v-for="(item, index) in list"
        :key="item.id"
        class="notification-item fade-in-up"
        :class="{ unread: item.isRead === 0 }"
        :style="{ animationDelay: `${index * 0.04}s` }"
        @click="handleClickItem(item)"
      >
        <!-- 头像 + 类型图标 -->
        <div class="avatar-wrap">
          <el-avatar :size="44" :src="item.fromUserAvatar" class="noti-avatar">
            {{ initialOf(item.fromUsername) }}
          </el-avatar>
          <span class="type-icon" :class="{ 'type-comment': item.type === 1, 'type-like': item.type === 2, 'type-message': item.type === 3, 'type-system': item.type === 4 }">
            <el-icon v-if="item.type === 1"><ChatDotRound /></el-icon>
            <el-icon v-else-if="item.type === 2"><Pointer /></el-icon>
            <el-icon v-else-if="item.type === 3"><ChatLineRound /></el-icon>
            <el-icon v-else><Bell /></el-icon>
          </span>
        </div>

        <!-- 主体内容 -->
        <div class="item-body">
          <div class="item-top">
            <span class="item-username">{{ item.fromUsername || '系统' }}</span>
            <el-tag size="small" effect="plain" :type="typeTagType(item.type)" round>
              {{ typeText(item.type) }}
            </el-tag>
          </div>
          <div class="item-content">{{ item.content }}</div>
          <div class="item-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(item.createTime) }}
          </div>
        </div>

        <!-- 未读小圆点 -->
        <span v-if="item.isRead === 0" class="unread-dot"></span>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && list.length === 0" class="empty-state fade-in-up">
        <svg class="empty-illustration" width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="80" cy="80" r="72" fill="var(--color-primary-bg)" />
          <circle cx="80" cy="62" r="24" fill="var(--color-primary)" opacity="0.15" />
          <path d="M56 100 C56 86 66 78 80 78 C94 78 104 86 104 100" stroke="var(--color-primary)" stroke-width="4" stroke-linecap="round" fill="none" opacity="0.3" />
          <rect x="48" y="44" width="64" height="48" rx="12" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="3" />
          <path d="M60 58 L100 58" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.5" />
          <path d="M60 70 L88 70" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.3" />
          <circle cx="110" cy="44" r="14" fill="var(--color-primary)" />
          <path d="M110 38 L110 50 M104 44 L116 44" stroke="var(--color-white)" stroke-width="2.5" stroke-linecap="round" />
          <circle cx="44" cy="110" r="6" fill="var(--color-primary)" opacity="0.2" />
          <circle cx="120" cy="100" r="4" fill="var(--color-primary)" opacity="0.15" />
        </svg>
        <p class="empty-text">暂无通知</p>
        <p class="empty-subtext">新的消息会在这里通知你</p>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="page.total > 0">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatTime } from '@/utils/format'
import {
  getNotifications,
  markAsRead,
  markAllAsRead
} from '@/api/notification'

const router = useRouter()

// 当前激活的分类标签（all / 1 / 2 / 3 / 4）
const activeTab = ref('all')

// 列表状态
const loading = ref(false)
const list = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

// 全部已读按钮加载状态
const markAllLoading = ref(false)

/**
 * 加载通知列表
 */
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: page.current,
      size: page.size
    }
    // "全部"不传 type
    if (activeTab.value !== 'all') {
      params.type = Number(activeTab.value)
    }
    const res = await getNotifications(params)
    list.value = res.records || []
    page.total = res.total || 0
  } catch (e) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    loading.value = false
  }
}

/**
 * 切换分类标签：重置分页并重新加载
 */
const handleTabChange = () => {
  page.current = 1
  loadList()
}

/**
 * 点击单条通知：标记已读并按类型跳转
 */
const handleClickItem = async (item) => {
  // 未读则调用标记已读接口
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
    } catch (e) {
      // 标记失败不阻塞跳转
    }
  }
  // 按通知类型跳转
  switch (item.type) {
    case 1:
    case 2:
      // 评论 / 点赞：跳转对应帖子
      if (item.targetId) {
        router.push(`/post/${item.targetId}`)
      }
      break
    case 3:
      // 私信：跳转与发送者的会话
      if (item.fromUserId) {
        router.push({ path: '/message/chat', query: { userId: item.fromUserId } })
      }
      break
    case 4:
      // 系统通知：不跳转
      break
  }
}

/**
 * 全部标记已读
 */
const handleMarkAllRead = async () => {
  markAllLoading.value = true
  try {
    await markAllAsRead()
    ElMessage.success('已全部标记为已读')
    // 本地更新已读状态
    list.value.forEach((item) => {
      item.isRead = 1
    })
  } catch (e) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    markAllLoading.value = false
  }
}

/**
 * 通知类型文本
 */
const typeText = (type) => {
  switch (type) {
    case 1:
      return '评论'
    case 2:
      return '点赞'
    case 3:
      return '私信'
    case 4:
      return '系统'
    default:
      return '通知'
  }
}

/**
 * 通知类型标签颜色
 */
const typeTagType = (type) => {
  switch (type) {
    case 1:
      return 'primary'
    case 2:
      return 'danger'
    case 3:
      return 'success'
    case 4:
      return 'info'
    default:
      return 'info'
  }
}

/**
 * 取用户名首字母用于头像占位
 */
const initialOf = (name) => {
  if (!name) return ''
  return name.charAt(0).toUpperCase()
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.notification-page {
  max-width: 900px;
  margin: 0 auto;
}

/* 顶部标题区 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  margin: 0;
  color: var(--color-text-1);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-heading);
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  color: var(--color-white);
  font-size: 18px;
  box-shadow: var(--shadow-primary);
}

.unread-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-caption);
  font-weight: var(--font-weight-semibold);
  background: var(--color-danger-tag-bg);
  color: var(--color-danger);
}

/* Tab bar */
.tab-bar {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 0 var(--space-4);
  margin-bottom: var(--space-4);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.tab-bar :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.tab-bar :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tab-bar :deep(.el-tabs__item) {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-medium);
  padding: 0 var(--space-4);
  height: 48px;
  line-height: 48px;
}

.tab-bar :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: var(--radius-full);
  background: var(--gradient-primary);
}

.list-wrap {
  min-height: 200px;
}

/* 单条通知 */
.notification-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-4);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-3);
  cursor: pointer;
  transition: all var(--transition-base);
  position: relative;
  opacity: 0;
}

.notification-item:hover {
  box-shadow: var(--shadow-3);
  border-color: var(--color-primary-3);
  transform: translateY(-2px);
}

.notification-item.unread {
  background: var(--color-primary-bg);
  border-color: var(--color-primary-3);
}

.notification-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: var(--gradient-primary);
  border-radius: 0 var(--radius-full) var(--radius-full) 0;
}

/* 头像 + 类型图标 */
.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.noti-avatar {
  background: var(--gradient-primary);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
}

.type-icon {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 20px;
  height: 20px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: var(--color-white);
  border: 2px solid var(--color-bg-card);
}

.type-icon.type-comment {
  background: var(--color-primary);
}

.type-icon.type-like {
  background: var(--color-danger);
}

.type-icon.type-message {
  background: var(--color-success);
}

.type-icon.type-system {
  background: var(--color-warning);
}

/* 主体内容 */
.item-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.item-top {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.item-username {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.item-content {
  font-size: var(--font-size-body);
  color: var(--color-text-2);
  line-height: var(--line-height-normal);
  word-break: break-word;
}

.item-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 2px;
}

/* 未读小圆点 */
.unread-dot {
  width: 9px;
  height: 9px;
  border-radius: var(--radius-full);
  background: var(--color-danger);
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  box-shadow: 0 0 0 4px var(--color-danger-tag-bg);
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 4px var(--color-danger-tag-bg); }
  50% { box-shadow: 0 0 0 7px transparent; }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.empty-illustration {
  margin-bottom: var(--space-4);
}

.empty-text {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-2);
  margin: 0 0 var(--space-1) 0;
}

.empty-subtext {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: 0;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

/* 响应式 */
@media (max-width: 768px) {
  .notification-page {
    padding: 0 var(--space-2);
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .notification-item {
    padding: var(--space-3);
  }

  .tab-bar {
    padding: 0 var(--space-2);
  }

  .tab-bar :deep(.el-tabs__item) {
    padding: 0 var(--space-2);
    font-size: var(--font-size-sm);
  }
}
</style>
