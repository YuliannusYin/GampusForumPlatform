<template>
  <div class="chat-page">
    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="session-panel">
        <div class="session-header">
          <span>私信会话</span>
        </div>
        <div class="session-list">
          <div
            v-for="s in sessions"
            :key="s.id"
            class="session-item"
            :class="{ active: currentSession && currentSession.id === s.id }"
            @click="selectSession(s)"
          >
            <!-- 头像 + 未读徽章 -->
            <el-badge :value="s.unreadCount" :hidden="!s.unreadCount" class="session-badge">
              <el-avatar :size="40" :src="s.otherAvatar">
                {{ initialOf(s.otherUsername) }}
              </el-avatar>
            </el-badge>
            <!-- 会话主体 -->
            <div class="session-body">
              <div class="session-top">
                <span class="session-name">{{ s.otherUsername }}</span>
                <span class="session-time">{{ formatTime(s.lastMessageTime) }}</span>
              </div>
              <div class="session-last">{{ s.lastMessageContent }}</div>
            </div>
          </div>
          <el-empty v-if="!sessionsLoading && sessions.length === 0" description="暂无会话" :image-size="60" />
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-panel">
        <template v-if="currentSession || newChatMode">
          <!-- 顶部对方用户名 -->
          <div class="chat-header">
            <span>{{ chatTitle }}</span>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list" v-loading="messagesLoading">
            <div
              v-for="m in messages"
              :key="m.id"
              class="message-item"
              :class="{ mine: isMine(m) }"
            >
              <el-avatar :size="32" :src="isMine(m) ? myAvatar : m.senderAvatar">
                {{ initialOf(isMine(m) ? myName : m.senderUsername) }}
              </el-avatar>
              <div class="message-body">
                <div class="message-time">{{ formatTime(m.createTime) }}</div>
                <div class="message-bubble">{{ m.content }}</div>
              </div>
            </div>
            <el-empty v-if="!messagesLoading && !newChatMode && messages.length === 0" description="暂无消息，开始聊天吧" :image-size="80" />
            <el-empty v-if="newChatMode" description="发送第一条消息后将创建会话" :image-size="80" />
          </div>

          <!-- 输入区 -->
          <div class="input-area">
            <el-input
              v-model="inputContent"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="输入消息，Enter 发送，Shift+Enter 换行"
              @keydown.enter.exact.prevent="handleSend"
            />
            <el-button type="primary" :loading="sendLoading" @click="handleSend">发送</el-button>
          </div>
        </template>
        <el-empty v-else description="请选择一个会话开始聊天" class="chat-empty" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formatTime } from '@/utils/format'
import { useUserStore } from '@/store/user'
import {
  getChatSessions,
  getChatMessages,
  markChatRead,
  sendMessage
} from '@/api/chat'
import { connect as wsConnect, disconnect as wsDisconnect } from '@/utils/websocket'

const route = useRoute()
const userStore = useUserStore()

// 当前登录用户信息（用于判断消息方向与显示头像）
const myUserId = computed(() => userStore.userInfo?.id)
const myAvatar = computed(() => userStore.userInfo?.avatar || '')
const myName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '')

// ===== 会话列表 =====
const sessionsLoading = ref(false)
const sessions = ref([])
// 当前选中会话
const currentSession = ref(null)
// 新建对话模式（从 query.userId 进入且无对应会话）
const newChatMode = ref(false)
const newChatUserId = ref(null)

// ===== 消息列表 =====
const messagesLoading = ref(false)
const messages = ref([])
const messageListRef = ref(null)

// ===== 输入与发送 =====
const inputContent = ref('')
const sendLoading = ref(false)

// 聊天窗口顶部标题
const chatTitle = computed(() => {
  if (newChatMode.value) {
    return `新对话 - 用户 ${newChatUserId.value}`
  }
  return currentSession.value?.otherUsername || '私信'
})

/**
 * 加载会话列表，并根据 query.userId 自动选中或进入新建对话模式
 */
const loadSessions = async () => {
  sessionsLoading.value = true
  try {
    const data = await getChatSessions()
    sessions.value = Array.isArray(data) ? data : []
    // 处理路由 query.userId：自动选中或进入新建对话
    const userId = route.query.userId
    if (userId) {
      const target = sessions.value.find((s) => s.otherUserId === Number(userId))
      if (target) {
        selectSession(target)
      } else {
        // 无对应会话：进入新建对话模式
        newChatMode.value = true
        newChatUserId.value = Number(userId)
        currentSession.value = null
        messages.value = []
      }
    }
  } catch (e) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    sessionsLoading.value = false
  }
}

/**
 * 选中某个会话，加载聊天记录并标记已读
 */
const selectSession = async (session) => {
  currentSession.value = session
  newChatMode.value = false
  messages.value = []
  inputContent.value = ''
  messagesLoading.value = true
  try {
    const res = await getChatMessages(session.id, { page: 1, size: 20 })
    // 按时间正序排列
    const records = res.records || []
    messages.value = records.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
    // 标记该会话消息为已读
    try {
      await markChatRead(session.id)
      // 清零本地未读数
      session.unreadCount = 0
    } catch (e) {
      // 标记已读失败不影响展示
    }
    scrollToBottom()
  } catch (e) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    messagesLoading.value = false
  }
}

/**
 * 发送消息
 */
const handleSend = async () => {
  const content = inputContent.value.trim()
  if (!content) return

  // 确定接收者
  let receiverId
  if (newChatMode.value) {
    receiverId = newChatUserId.value
  } else if (currentSession.value) {
    receiverId = currentSession.value.otherUserId
  }
  if (!receiverId) {
    ElMessage.warning('请选择会话')
    return
  }

  sendLoading.value = true
  try {
    const msg = await sendMessage(receiverId, { content })
    if (currentSession.value) {
      // 已有会话：追加到消息列表并更新会话概要
      messages.value.push(msg)
      currentSession.value.lastMessageContent = msg.content
      currentSession.value.lastMessageTime = msg.createTime
      scrollToBottom()
    } else if (newChatMode.value) {
      // 新建对话：发送成功后会话已创建，刷新会话列表并选中
      newChatMode.value = false
      await loadSessions()
      const target = sessions.value.find((s) => s.otherUserId === receiverId)
      if (target) {
        await selectSession(target)
      }
    }
    inputContent.value = ''
  } catch (e) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    sendLoading.value = false
  }
}

/**
 * 判断消息是否为自己发送（决定气泡左右）
 */
const isMine = (m) => m.senderId === myUserId.value

/**
 * 滚动消息列表到底部
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

/**
 * WebSocket 收到新消息回调
 */
const onWsMessage = (message) => {
  // 若属于当前打开会话：追加到列表并标记已读
  if (currentSession.value && message.sessionId === currentSession.value.id) {
    messages.value.push(message)
    // 更新会话概要
    currentSession.value.lastMessageContent = message.content
    currentSession.value.lastMessageTime = message.createTime
    try {
      markChatRead(currentSession.value.id)
    } catch (e) {
      // 标记已读失败忽略
    }
    scrollToBottom()
  } else {
    // 其他会话：更新会话列表未读数与概要
    const session = sessions.value.find((s) => s.id === message.sessionId)
    if (session) {
      session.unreadCount = (session.unreadCount || 0) + 1
      session.lastMessageContent = message.content
      session.lastMessageTime = message.createTime
    } else {
      // 会话列表中没有该会话：重新加载会话列表
      loadSessions()
    }
    ElMessage.info(`收到来自 ${message.senderUsername || '新用户'} 的新消息`)
  }
}

onMounted(() => {
  loadSessions()
  // 建立 WebSocket 连接
  const token = userStore.token || localStorage.getItem('campus_token')
  if (token) {
    wsConnect(token, onWsMessage)
  }
})

onUnmounted(() => {
  // 断开 WebSocket 连接
  wsDisconnect()
})

/**
 * 取用户名首字母用于头像占位
 */
const initialOf = (name) => {
  if (!name) return ''
  return name.charAt(0).toUpperCase()
}
</script>

<style scoped>
.chat-page {
  max-width: 1100px;
  margin: 0 auto;
}

.chat-layout {
  display: flex;
  height: calc(100vh - 160px);
  min-height: 500px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
}

/* 左侧会话列表 */
.session-panel {
  width: 300px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.session-header {
  padding: 14px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid #f2f6fc;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
}

.session-badge {
  flex-shrink: 0;
}

.session-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.session-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.session-last {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧聊天窗口 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 14px 18px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  background: #fafafa;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

/* 单条消息 */
.message-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}

/* 自己的消息：靠右 */
.message-item.mine {
  flex-direction: row-reverse;
}

.message-body {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-item.mine .message-body {
  align-items: flex-end;
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

/* 气泡 */
.message-bubble {
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
}

/* 自己的气泡：绿色 */
.message-item.mine .message-bubble {
  background: #95ec69;
  color: #1a1a1a;
  border-color: #95ec69;
}

/* 输入区 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.input-area .el-input {
  flex: 1;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
