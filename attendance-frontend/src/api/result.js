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
