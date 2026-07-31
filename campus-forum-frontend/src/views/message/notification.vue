<template>
  <div class="notification-page">
    <!-- 顶部标题与操作 -->
    <div class="page-header">
      <h2 class="page-title">消息通知</h2>
      <el-button type="primary" plain :loading="markAllLoading" @click="handleMarkAllRead">
        全部已读
      </el-button>
    </div>

    <!-- 分类标签 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="评论" name="1" />
      <el-tab-pane label="点赞" name="2" />
      <el-tab-pane label="私信" name="3" />
      <el-tab-pane label="系统" name="4" />
    </el-tabs>

    <!-- 通知列表 -->
    <div v-loading="loading" class="list-wrap">
      <div
        v-for="item in list"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleClickItem(item)"
      >
        <!-- 发送者头像 -->
        <el-avatar :size="40" :src="item.fromUserAvatar">
          {{ initialOf(item.fromUsername) }}
        </el-avatar>

        <!-- 主体内容 -->
        <div class="item-body">
          <div class="item-top">
            <span class="item-username">{{ item.fromUsername || '系统' }}</span>
            <el-tag size="small" effect="plain" :type="typeTagType(item.type)">
              {{ typeText(item.type) }}
            </el-tag>
          </div>
          <div class="item-content">{{ item.content }}</div>
          <div class="item-time">{{ formatTime(item.createTime) }}</div>
        </div>

        <!-- 未读小圆点 -->
        <span v-if="item.isRead === 0" class="unread-dot"></span>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />
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
  margin-bottom: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.list-wrap {
  min-height: 200px;
}

/* 单条通知 */
.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  position: relative;
}

.notification-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.notification-item.unread {
  background: #f0f9ff;
  border-color: #d9ecff;
}

/* 主体内容 */
.item-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-username {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.item-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  word-break: break-word;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

/* 未读小圆点 */
.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  position: absolute;
  top: 16px;
  right: 16px;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
