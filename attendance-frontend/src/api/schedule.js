import request from './request'

export function getScheduleList(params) {
  return request.get('/schedule/schedule/list', { params })
}

export function setSchedule(data) {
  return request.post('/schedule/schedule/set', data)
}

export function deleteSchedule(id) {
  return request.delete(`/schedule/schedule/${id}`)
}
