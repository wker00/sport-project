// 在 Vue 初始化前从 localStorage 恢复主题，避免闪烁
try {
  const saved = JSON.parse(localStorage.getItem('counter') || '{}')
  document.documentElement.setAttribute('data-theme', saved.theme || 'light')
} catch {}

import '@phosphor-icons/web/regular'
import '@phosphor-icons/web/fill'
import '@fontsource/plus-jakarta-sans/400.css'
import '@fontsource/plus-jakarta-sans/500.css'
import '@fontsource/plus-jakarta-sans/600.css'
import '@fontsource/plus-jakarta-sans/700.css'
import '@fontsource/plus-jakarta-sans/800.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/_themes.scss'
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
