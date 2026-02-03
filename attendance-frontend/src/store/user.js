import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOKEN_KEY = 'attendance_token'
const USER_KEY = 'attendance_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  function setLogin(payload) {
    token.value = payload.token
    userInfo.value = {
      username: payload.username,
      displayName: payload.displayName,
      userId: payload.userId
    }
    localStorage.setItem(TOKEN_KEY, payload.token)
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, userInfo, setLogin, logout }
})
