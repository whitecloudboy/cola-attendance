import request from './request'

export function getResultPage(params) {
  return request.get('/attendance/result/page', { params })
}

export function getResultList(params) {
  return request.get('/attendance/result/list', { params })
}

export function generateEmptyResult(date) {
  return request.post('/attendance/result/generate-empty', null, { params: { date } })
}

/** 考勤结算：触发指定日期的日终补录（未打卡等状态更新） */
export function triggerEndTask(date) {
  return request.post('/attendance/result/trigger-end-task', null, { params: { date } })
}
