import request from '@/utils/request'

// 获取签到状态
export function getSignInStatus() {
  return request.get('/signin/status')
}

// 签到
export function signIn() {
  return request.post('/signin')
}

// 获取签到记录
export function getSignInRecords(params) {
  return request.get('/signin/records', { params })
}
