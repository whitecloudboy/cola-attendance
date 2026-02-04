import axios from 'axios'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers['token'] = userStore.token
  }
  return config
})

request.interceptors.response.use(
  res => {
    if (res.config.responseType === 'blob') return res.data
    const { code, data, msg } = res.data
    if (code === 0) return data
    ElMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  err => {
    if (err.response?.status === 401 || err.response?.status === 403) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    } else {
      ElMessage.error(err.response?.data?.msg || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
