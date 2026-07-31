import request from '@/utils/request'

// 获取通知列表（支持分页与类型/已读过滤）
export function getNotifications(params) {
  return request.get('/notifications', { params })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request.get('/notifications/unread/count')
}

// 标记单条通知为已读
export function markAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

// 标记全部通知为已读
export function markAllAsRead() {
  return request.put('/notifications/read/all')
}

// 删除单条通知
export function deleteNotification(id) {
  return request.delete(`/notifications/${id}`)
}
