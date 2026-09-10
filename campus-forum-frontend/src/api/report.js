import request from '@/utils/request'

/** 提交举报（表白墙帖/评） */
export function createReport(data) {
  return request.post('/reports', data)
}

/** 管理端举报列表 */
export function getAdminReports(params) {
  return request.get('/admin/reports', { params })
}

/** 处理举报：status 1属实 2驳回 */
export function handleAdminReport(id, data) {
  return request.put(`/admin/reports/${id}/handle`, data)
}
