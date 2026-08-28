import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/dialog.scss'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { initRsaKey } from './utils/rsa'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

pinia.use(piniaPluginPersistedstate)
app.use(pinia)

app.use(router)

app.use(ElementPlus, {
    locale: zhCn,
})

initRsaKey().then(() => {
  app.mount('#app')
})
