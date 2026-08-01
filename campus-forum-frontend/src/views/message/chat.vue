<template>
  <div class="chat-page">
    <div class="chat-layout fade-in-up">
      <!-- 左侧会话列表 -->
      <div class="session-panel">
        <div class="session-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>私信会话</span>
        </div>
        <div class="session-list">
          <div
            v-for="(s, index) in sessions"
            :key="s.id"
            class="session-item fade-in-up"
            :class="{ active: currentSession && currentSession.id === s.id }"
            :style="{ animationDelay: `${index * 0.03}s` }"
            @click="selectSession(s)"
          >
            <!-- 头像 + 未读徽章 -->
            <el-badge :value="s.unreadCount" :hidden="!s.unreadCount" class="session-badge">
              <div class="session-avatar-wrap">
                <el-avatar :size="44" :src="s.otherAvatar" class="session-avatar">
                  {{ initialOf(s.otherUsername) }}
                </el-avatar>
                <span class="online-dot"></span>
              </div>
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
          <!-- 空状态 -->
          <div v-if="!sessionsLoading && sessions.length === 0" class="session-empty">
            <svg width="100" height="100" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="50" cy="50" r="45" fill="var(--color-primary-bg)" />
              <path d="M30 38 C30 34 33 32 36 32 L64 32 C67 32 70 34 70 38 L70 54 C70 58 67 60 64 60 L44 60 L36 67 L36 60 C33 60 30 58 30 54 Z" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="2.5" />
              <circle cx="42" cy="46" r="2" fill="var(--color-primary)" opacity="0.5" />
              <circle cx="50" cy="46" r="2" fill="var(--color-primary)" opacity="0.5" />
              <circle cx="58" cy="46" r="2" fill="var(--color-primary)" opacity="0.5" />
            </svg>
            <p class="session-empty-text">暂无会话</p>
          </div>
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-panel">
        <template v-if="currentSession || newChatMode">
          <!-- 顶部对方用户名 -->
          <div class="chat-header">
            <div class="chat-header-info">
              <el-avatar :size="32" :src="currentSession ? currentSession.otherAvatar : ''" class="header-avatar">
                {{ initialOf(currentSession ? currentSession.otherUsername : '') }}
              </el-avatar>
              <span class="chat-header-name">{{ chatTitle }}</span>
              <span class="chat-header-status">
                <span class="status-dot"></span>
                在线
              </span>
            </div>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list" v-loading="messagesLoading">
            <div
              v-for="(m, index) in messages"
              :key="m.id"
              class="message-item fade-in-up"
              :class="{ mine: isMine(m) }"
              :style="{ animationDelay: `${Math.min(index * 0.02, 0.2)}s` }"
            >
              <el-avatar :size="36" :src="isMine(m) ? myAvatar : m.senderAvatar" class="msg-avatar">
                {{ initialOf(isMine(m) ? myName : m.senderUsername) }}
              </el-avatar>
              <div class="message-body">
                <div class="message-time">{{ formatTime(m.createTime) }}</div>
                <div class="message-bubble">{{ m.content }}</div>
              </div>
            </div>
            <!-- 空状态 -->
            <div v-if="!messagesLoading && !newChatMode && messages.length === 0" class="msg-empty">
              <svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="60" cy="60" r="54" fill="var(--color-primary-bg)" />
                <path d="M36 46 C36 42 39 40 42 40 L78 40 C81 40 84 42 84 46 L84 64 C84 68 81 70 78 70 L54 70 L44 78 L44 70 C39 70 36 68 36 64 Z" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="2.5" />
                <circle cx="50" cy="55" r="2.5" fill="var(--color-primary)" opacity="0.4" />
                <circle cx="60" cy="55" r="2.5" fill="var(--color-primary)" opacity="0.4" />
                <circle cx="70" cy="55" r="2.5" fill="var(--color-primary)" opacity="0.4" />
              </svg>
              <p class="msg-empty-text">暂无消息，开始聊天吧</p>
            </div>
            <div v-if="newChatMode" class="msg-empty">
              <svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="60" cy="60" r="54" fill="var(--color-primary-bg)" />
                <path d="M36 46 C36 42 39 40 42 40 L78 40 C81 40 84 42 84 46 L84 64 C84 68 81 70 78 70 L54 70 L44 78 L44 70 C39 70 36 68 36 64 Z" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="2.5" />
                <path d="M52 55 L68 55" stroke="var(--color-primary)" stroke-width="2.5" stroke-linecap="round" opacity="0.5" />
                <path d="M52 62 L62 62" stroke="var(--color-primary)" stroke-width="2.5" stroke-linecap="round" opacity="0.3" />
              </svg>
              <p class="msg-empty-text">发送第一条消息后将创建会话</p>
            </div>
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
            <el-button type="primary" :loading="sendLoading" @click="handleSend" class="send-btn">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </template>
        <!-- 未选择会话时的空状态 -->
        <div v-else class="chat-empty">
          <svg width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="80" cy="80" r="72" fill="var(--color-primary-bg)" />
            <path d="M48 62 C48 56 52 53 57 53 L103 53 C108 53 112 56 112 62 L112 84 C112 90 108 93 103 93 L73 93 L60 103 L60 93 C52 93 48 90 48 84 Z" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="3" />
            <circle cx="66" cy="73" r="3" fill="var(--color-primary)" opacity="0.4" />
            <circle cx="80" cy="73" r="3" fill="var(--color-primary)" opacity="0.4" />
            <circle cx="94" cy="73" r="3" fill="var(--color-primary)" opacity="0.4" />
            <circle cx="40" cy="120" r="5" fill="var(--color-primary)" opacity="0.15" />
            <circle cx="120" cy="110" r="4" fill="var(--color-primary)" opacity="0.1" />
          </svg>
          <p class="chat-empty-text">请选择一个会话开始聊天</p>
        </div>
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
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-2);
}

/* 左侧会话列表 */
.session-panel {
  width: 300px;
  flex-shrink: 0;
  border-right: 1px solid var(--color-border-lighter);
  display: flex;
  flex-direction: column;
  background: var(--color-bg-subtle);
}

.session-header {
  padding: var(--space-4) var(--space-4);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  border-bottom: 1px solid var(--color-border-lighter);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-heading);
}

.session-header .el-icon {
  color: var(--color-primary);
  font-size: 20px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2);
}

.session-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  cursor: pointer;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
  margin-bottom: var(--space-1);
  opacity: 0;
}

.session-item:hover {
  background: var(--color-bg-hover);
}

.session-item.active {
  background: var(--color-primary-bg);
  box-shadow: inset 3px 0 0 var(--color-primary);
}

.session-badge {
  flex-shrink: 0;
}

.session-avatar-wrap {
  position: relative;
}

.session-avatar {
  background: var(--gradient-primary);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: var(--radius-full);
  background: var(--color-success);
  border: 2px solid var(--color-bg-subtle);
}

.session-item.active .online-dot {
  border-color: var(--color-primary-bg);
}

.session-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.session-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.session-name {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
  flex-shrink: 0;
}

.session-last {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 会话列表空状态 */
.session-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.session-empty-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: var(--space-2) 0 0;
}

/* 右侧聊天窗口 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--color-bg-card);
}

.chat-header {
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid var(--color-border-lighter);
  background: var(--color-bg-card);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.header-avatar {
  background: var(--gradient-primary);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.chat-header-name {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.chat-header-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-caption);
  color: var(--color-success);
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: var(--radius-full);
  background: var(--color-success);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
  background: var(--color-bg-subtle);
}

/* 单条消息 */
.message-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  opacity: 0;
}

/* 自己的消息：靠右 */
.message-item.mine {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
  background: var(--gradient-primary);
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
}

.message-body {
  display: flex;
  flex-direction: column;
  max-width: 65%;
}

.message-item.mine .message-body {
  align-items: flex-end;
}

.message-time {
  font-size: var(--font-size-mini);
  color: var(--color-text-3);
  margin-bottom: var(--space-1);
  padding: 0 var(--space-1);
}

/* 气泡 */
.message-bubble {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-xl);
  font-size: var(--font-size-body);
  line-height: var(--line-height-normal);
  word-break: break-word;
  background: var(--color-bg-card);
  color: var(--color-text-1);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
  position: relative;
}

/* 对方气泡：左下角尖角 */
.message-bubble::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 12px;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 6px 8px 6px 0;
  border-color: transparent var(--color-bg-card) transparent transparent;
}

/* 自己的气泡：渐变蓝色 */
.message-item.mine .message-bubble {
  background: var(--gradient-primary);
  color: var(--color-white);
  border-color: transparent;
  box-shadow: var(--shadow-primary);
}

.message-item.mine .message-bubble::before {
  left: auto;
  right: -6px;
  border-width: 6px 0 6px 8px;
  border-color: transparent transparent transparent var(--color-primary);
}

/* 消息区空状态 */
.msg-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.msg-empty-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: var(--space-3) 0 0;
}

/* 输入区 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-top: 1px solid var(--color-border-lighter);
  background: var(--color-bg-card);
}

.input-area .el-input {
  flex: 1;
}

.send-btn {
  height: 56px;
  border-radius: var(--radius-xl);
  padding: 0 var(--space-5);
}

/* 未选择会话时的空状态 */
.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chat-empty-text {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-2);
  margin: var(--space-4) 0 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
    height: calc(100vh - 120px);
    border-radius: var(--radius-lg);
  }

  .session-panel {
    width: 100%;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid var(--color-border-lighter);
  }

  .session-list {
    padding: var(--space-1);
  }

  .session-item {
    padding: var(--space-2) var(--space-3);
  }

  .chat-header {
    padding: var(--space-2) var(--space-3);
  }

  .chat-header-status {
    display: none;
  }

  .message-body {
    max-width: 80%;
  }

  .input-area {
    padding: var(--space-2) var(--space-3);
  }

  .send-btn {
    height: 48px;
    padding: 0 var(--space-3);
  }
}
</style>
