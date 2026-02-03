import request from './request'

export function getUserPage(params) {
  return request.get('/system/user/page', { params })
}

export function getUser(id) {
  return request.get(`/system/user/${id}`)
}

export function saveUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(data) {
  return request.put('/system/user', data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}
