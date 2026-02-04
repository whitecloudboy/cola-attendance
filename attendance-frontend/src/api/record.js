import request from './request'

export function getRecordPage(params) {
  return request.get('/attendance/record/page', { params })
}

/** 下载打卡记录导入模板，返回 Blob */
export function downloadRecordTemplate() {
  return request.get('/attendance/record/template', { responseType: 'blob' })
}

export function importRecordExcel(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/attendance/record/import', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
