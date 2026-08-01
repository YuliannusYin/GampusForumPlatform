import request from '@/utils/request'

// ===== 用户管理 =====
// 分页获取用户列表
export function getAdminUsers(params) {
  return request.get('/admin/users', { params })
}

// 修改用户状态（0正常 1封禁）
export function updateUserStatus(id, data) {
  return request.put(`/admin/users/${id}/status`, data)
}

// 修改用户角色
export function updateUserRole(id, data) {
  return request.put(`/admin/users/${id}/role`, data)
}

// ===== 帖子管理 =====
// 分页获取帖子列表
export function getAdminPosts(params) {
  return request.get('/admin/posts', { params })
}

// 删除帖子
export function deleteAdminPost(id) {
  return request.delete(`/admin/posts/${id}`)
}

// 置顶/取消置顶
export function updatePostTop(id, data) {
  return request.put(`/admin/posts/${id}/top`, data)
}

// 加精/取消加精
export function updatePostEssence(id, data) {
  return request.put(`/admin/posts/${id}/essence`, data)
}

// ===== 标签管理 =====
// 新建标签
export function createTag(data) {
  return request.post('/admin/tags', data)
}

// 编辑标签
export function updateTag(id, data) {
  return request.put(`/admin/tags/${id}`, data)
}

// 删除标签
export function deleteTag(id) {
  return request.delete(`/admin/tags/${id}`)
}

// ===== 板块管理（CRUD 复用 post.js 的 getSections，新增/编辑/删除在此）=====
// 新建板块
export function createSection(data) {
  return request.post('/sections', data)
}

// 编辑板块
export function updateSection(id, data) {
  return request.put(`/sections/${id}`, data)
}

// 删除板块
export function deleteSection(id) {
  return request.delete(`/sections/${id}`)
}

// ===== 数据统计 =====
// 总览统计
export function getOverview() {
  return request.get('/admin/stats/overview')
}

// 发帖趋势
export function getPostTrend(params) {
  return request.get('/admin/stats/post/trend', { params })
}

// 板块帖子分布
export function getSectionDistribution() {
  return request.get('/admin/stats/section/distribution')
}

// ===== 社团审核 =====
// 待审核社团列表
export function getPendingClubs(params) {
  return request.get('/admin/clubs', { params })
}

// 审核通过
export function approveClub(clubId) {
  return request.put(`/admin/clubs/${clubId}/approve`)
}

// 审核拒绝
export function rejectClub(clubId) {
  return request.put(`/admin/clubs/${clubId}/reject`)
}

// ===== 测试数据管理（仅超级管理员）=====
// 一键导入测试数据
export function importTestData() {
  return request.post('/admin/test-data/import')
}

// 一键移除测试数据
export function removeTestData() {
  return request.delete('/admin/test-data')
}

// 查询测试数据状态
export function getTestDataStatus() {
  return request.get('/admin/test-data/status')
}
