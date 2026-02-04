import request from './request'

export function getShiftList(params) {
  return request.get('/schedule/shift/list', { params })
}

export function getShiftPage(params) {
  return request.get('/schedule/shift/page', { params })
}

export function getShift(id) {
  return request.get(`/schedule/shift/${id}`)
}

export function saveShift(data) {
  return request.post('/schedule/shift', data)
}

export function updateShift(data) {
  return request.put('/schedule/shift', data)
}

export function deleteShift(id) {
  return request.delete(`/schedule/shift/${id}`)
}
