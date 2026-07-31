import request from '@/utils/request'

// 获取当前用户个人资料
export function getProfile() {
  return request.get('/user/profile')
}

// 更新个人资料（nickname, bio, avatar, gender）
export function updateProfile(data) {
  return request.put('/user/profile', data)
}

// 修改密码（oldPassword, newPassword）
export function changePassword(data) {
  return request.put('/user/password', data)
}

// 获取我的发帖列表（分页）
export function getMyPosts(params) {
  return request.get('/user/posts', { params })
}

// 获取我的收藏列表（分页）
export function getMyFavorites(params) {
  return request.get('/user/favorites', { params })
}

// 获取我的点赞列表（分页）
export function getMyLikes(params) {
  return request.get('/user/likes', { params })
}

// 上传文件（multipart，字段名 file）
export function uploadFile(formData) {
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取积分明细列表（分页）
export function getPointsRecords(params) {
  return request.get('/points/records', { params })
}

// 获取签到记录列表（分页）
export function getSigninRecords(params) {
  return request.get('/signin/records', { params })
}
