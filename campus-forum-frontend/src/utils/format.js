// 时间/日期格式化工具

/**
 * 将时间格式化为 YYYY-MM-DD HH:mm
 * @param {string|number|Date} time 时间值
 * @returns {string} 格式化后的字符串，无效时返回空串
 */
export function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  if (isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

/**
 * 将时间格式化为 YYYY-MM-DD
 * @param {string|number|Date} time 时间值
 * @returns {string} 格式化后的字符串，无效时返回空串
 */
export function formatDate(time) {
  if (!time) return ''
  const date = new Date(time)
  if (isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}
