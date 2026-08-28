import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  const token = ref('')
  const userInfo = ref({})
  const cartItems = ref([])
  const cartCount = computed(() => cartItems.value.reduce((sum, i) => sum + i.quantity, 0))
  const theme = ref('light')

  function setTheme(newTheme) {
    theme.value = newTheme
  }

  function toggleTheme() {
    const next = theme.value === 'dark' ? 'light' : 'dark'
    setTheme(next)
  }

  function setToken(newToken) {
    token.value = newToken
  }

  function setUserInfo(newAdminInfo) {
    userInfo.value = newAdminInfo
  }

  function setCartItems(items) {
    cartItems.value = items
  }

  function getToken() {
    return token.value
  }

  function getUserInfo() {
    return userInfo.value
  }

  function clearToken() {
    token.value = ''
    userInfo.value = {}
    cartItems.value = []
  }

  async function fetchCartList() {
    if (!token.value) { cartItems.value = []; return }
    try {
      const { getCartList } = await import('@/api/manager')
      const res = await getCartList()
      if (res.data.code === 200) {
        cartItems.value = (res.data.data || []).map(i => ({ ...i, checked: i.checked ?? 1 }))
      }
    } catch { cartItems.value = [] }
  }

  return { token, userInfo, cartCount, cartItems, theme, setToken, setUserInfo, setCartItems, clearToken, getToken, getUserInfo, fetchCartList, setTheme, toggleTheme }
}, {
  persist: {
    paths: ['token', 'userInfo', 'theme', 'cartItems']
  }
})
