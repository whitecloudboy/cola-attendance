import request from './request'

export function getPostList(params) {
  return request.get('/system/post/list', { params })
}

export function getPostPage(params) {
  return request.get('/system/post/page', { params })
}

export function getPost(id) {
  return request.get(`/system/post/${id}`)
}

export function savePost(data) {
  return request.post('/system/post', data)
}

export function updatePost(data) {
  return request.put('/system/post', data)
}

export function deletePost(id) {
  return request.delete(`/system/post/${id}`)
}
