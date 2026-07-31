import request from '@/utils/request'

// 获取私信会话列表
export function getChatSessions() {
  return request.get('/chat/sessions')
}

// 获取指定会话的消息记录（分页）
export function getChatMessages(sessionId, params) {
  return request.get(`/chat/messages/${sessionId}`, { params })
}

// 获取私信未读消息数量
export function getUnreadChatCount() {
  return request.get('/chat/messages/unread/count')
}

// 标记指定会话的消息为已读
export function markChatRead(sessionId) {
  return request.put(`/chat/messages/${sessionId}/read`)
}

// 发送私信给指定接收者
export function sendMessage(receiverId, data) {
  return request.post(`/chat/messages/${receiverId}`, data)
}
