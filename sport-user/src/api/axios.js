import axios from 'axios'
import { toast } from '@/utils/toast'
import { useCounterStore } from '@/stores/counter'
import router from '@/router'

let last401Time = 0
let last502Time = false

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
  (res) => res,
  (error) => {
    const counterStore = useCounterStore()
    if (error.response?.status === 401) {
      if (Date.now() - last401Time > 2000) {
        last401Time = Date.now()
        const msg = error.response?.data?.message || '登录过期，请重新登录'
        toast(msg, 'error')
        counterStore.clearToken()
        router.push({ name: 'index' })
      }
    } else {
      let message = error.response?.data?.message
      if (!message) {
        if (error.code === 'ECONNABORTED') {
          message = '请求超时，请稍后重试'
        } else if (error.code === 'ERR_NETWORK') {
          message = '网络连接失败，请检查网络'
        } else {
          message = `服务器异常，请稍后再试`
        }
      }
      if (!last502Time) {
        last502Time = true
        console.log(`服务器异常 (${error.response?.status || 'NET'})`);
        toast(message, 'error')
      }
      setTimeout(() => { last502Time = false }, 2000)
    }
    return Promise.reject(error)
  }
)

export default instance