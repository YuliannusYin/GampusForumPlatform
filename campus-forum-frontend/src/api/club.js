import request from '@/utils/request'

// 社团分页列表
export function getClubs(params) {
  return request.get('/clubs', { params })
}

// 社团详情
export function getClubDetail(clubId) {
  return request.get(`/clubs/${clubId}`)
}

// 申请创建社团
export function createClub(data) {
  return request.post('/clubs', data)
}

// 编辑社团信息
export function updateClub(clubId, data) {
  return request.put(`/clubs/${clubId}`, data)
}

// 加入社团
export function joinClub(clubId) {
  return request.post(`/clubs/${clubId}/join`)
}

// 退出社团
export function leaveClub(clubId) {
  return request.delete(`/clubs/${clubId}/join`)
}

// 社团帖子列表
export function getClubPosts(clubId, params) {
  return request.get(`/clubs/${clubId}/posts`, { params })
}

// 在社团发帖
export function createClubPost(clubId, data) {
  return request.post(`/clubs/${clubId}/posts`, data)
}

// 删除社团帖子
export function removeClubPost(clubId, postId) {
  return request.delete(`/clubs/${clubId}/posts/${postId}`)
}

// 社团成员列表
export function getClubMembers(clubId) {
  return request.get(`/clubs/${clubId}/members`)
}

// 移除社团成员
export function removeClubMember(clubId, memberId) {
  return request.delete(`/clubs/${clubId}/members/${memberId}`)
}
