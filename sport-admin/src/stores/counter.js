import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  const token = ref('')
  const adminInfo = ref({})

  function setToken(newToken) {
    token.value = newToken
  }

  function setAdminInfo(newAdminInfo) {
    adminInfo.value = newAdminInfo
  }

  function clearToken() {
    token.value = ''
    adminInfo.value = {}
  }

  return { token, adminInfo, setToken, setAdminInfo, clearToken }
}, {
  persist: {
    pick: ['token', 'adminInfo'],
  },
})
