import request from './request'

export function getRolePage(params) {
  return request.get('/system/role/page', { params })
}

export function getRoleList() {
  return request.get('/system/role/list')
}

export function getRole(id) {
  return request.get(`/system/role/${id}`)
}

export function saveRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(data) {
  return request.put('/system/role', data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}
