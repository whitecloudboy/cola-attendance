import request from './request'

export function getDeptTree() {
  return request.get('/system/dept/tree')
}

export function getDeptPage(params) {
  return request.get('/system/dept/page', { params })
}

export function getDept(id) {
  return request.get(`/system/dept/${id}`)
}

export function saveDept(data) {
  return request.post('/system/dept', data)
}

export function updateDept(data) {
  return request.put('/system/dept', data)
}

export function deleteDept(id) {
  return request.delete(`/system/dept/${id}`)
}
