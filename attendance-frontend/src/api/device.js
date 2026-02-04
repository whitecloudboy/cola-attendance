import request from './request'

export function getDeviceList(params) {
  return request.get('/attendance/device/list', { params })
}

export function getDevicePage(params) {
  return request.get('/attendance/device/page', { params })
}

export function getDevice(id) {
  return request.get(`/attendance/device/${id}`)
}

export function saveDevice(data) {
  return request.post('/attendance/device', data)
}

export function updateDevice(data) {
  return request.put('/attendance/device', data)
}

export function deleteDevice(id) {
  return request.delete(`/attendance/device/${id}`)
}
