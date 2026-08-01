import request from '@/utils/request'

// 关注用户
export function followUser(userId) {
  return request.post(`/users/${userId}/follow`)
}

// 取消关注
export function unfollowUser(userId) {
  return request.delete(`/users/${userId}/follow`)
}

// 查询是否已关注
export function isFollowing(userId) {
  return request.get(`/users/${userId}/is-following`)
}

// 获取关注列表
export function getFollowings(userId, params) {
  return request.get(`/users/${userId}/followings`, { params })
}

// 获取粉丝列表
export function getFollowers(userId, params) {
  return request.get(`/users/${userId}/followers`, { params })
}

// 获取用户公开主页信息
export function getUserProfile(userId) {
  return request.get(`/users/${userId}`)
}

// 获取用户公开帖子
export function getUserPosts(userId, params) {
  return request.get(`/users/${userId}/posts`, { params })
}

// 获取用户评论历史
export function getUserComments(userId, params) {
  return request.get(`/users/${userId}/comments`, { params })
}

// 获取用户加入的社团
export function getUserClubs(userId) {
  return request.get(`/users/${userId}/clubs`)
}
