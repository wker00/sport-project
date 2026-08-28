import axios from 'axios'
import { toast } from '@/utils/toast'
import { useCounterStore } from '@/stores/counter'
import router from '@/router'

let hasShown401Toast = false
let hasShown500Toast = false

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

instance.interceptors.request.use(
  (config) => {
    const counterStore = useCounterStore()
    const token = counterStore.token
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

instance.interceptors.response.use(
  (res) => {
    const { code, message } = res.data ?? {}
    if (code !== undefined && code !== 200) {
      toast(message || '操作失败', 'error')
      const err = new Error(message || '操作失败')
      err.isBusinessError = true
      return Promise.reject(err)
    }
    return res
  },
  (error) => {
    if (error.isBusinessError) return Promise.reject(error)

    const counterStore = useCounterStore()
    if (error.response?.status === 401) {
      if (!hasShown401Toast) {
        hasShown401Toast = true
        const msg = error.response?.data?.message || '登录过期，请重新登录'
        toast(msg, 'error')
        counterStore.clearToken()
        router.push({ name: 'login' })
      }
      setTimeout(() => { hasShown401Toast = false }, 2000)
    } else {
      if (!hasShown500Toast) {
        hasShown500Toast = true
        let message = error.response?.data?.message || '服务器异常，请稍后再试'
        if (error.code === 'ECONNABORTED') {
          message = '请求超时，请稍后重试'
        } else if (error.code === 'ERR_NETWORK') {
          message = '网络连接失败，请检查网络'
        }
        toast(message, 'error')
      }
      setTimeout(() => { hasShown500Toast = false }, 2000)
    }
    return Promise.reject(error)
  }
)

export default instance
