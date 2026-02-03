import request from './request'

export function getMenuTree() {
  return request.get('/system/menu/tree')
}

export function getMenuPage(params) {
  return request.get('/system/menu/page', { params })
}

export function getMenu(id) {
  return request.get(`/system/menu/${id}`)
}

export function saveMenu(data) {
  return request.post('/system/menu', data)
}

export function updateMenu(data) {
  return request.put('/system/menu', data)
}

export function deleteMenu(id) {
  return request.delete(`/system/menu/${id}`)
}
