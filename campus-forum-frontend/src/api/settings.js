import request from '@/utils/request'

// 获取用户设置
export function getSettings() {
  return request.get('/user/settings')
}

// 更新用户设置
export function updateSettings(data) {
  return request.put('/user/settings', data)
}
