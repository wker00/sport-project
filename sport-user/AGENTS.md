# SportZone 用户端 (vue-project-user)

## 命令

- `npm run dev` — Vite 开发服务器，端口 9090
- `npm run build` — 生产构建，输出到 `dist/`
- 无 lint / typecheck / test 脚本

## 架构

- 纯前端 SPA，后端 API 期望运行在 `localhost:8080`
- Vite 代理：`/api` → `http://localhost:8080`（配置在 `vite.config.js`）
- 入口：`src/main.js` → `src/App.vue` → `src/Home.vue`（布局壳，含 AppHeader + AppFooter）
- 路由：`src/router/index.js`（懒加载，history 模式）
- API 层：`src/api/axios.js`（实例 + 请求/响应拦截器）+ `src/api/manager.js`（所有接口定义）
- 状态管理：`src/stores/counter.js`（唯一 Pinia store，persistedstate 持久化 token/userInfo）
- 工具函数：`src/utils/toast.js`（ElMessage 封装）
- 样式变量：`src/assets/_variables.scss`（设计 token）

## 约定

- 组件统一使用 `<script setup>` + Composition API
- SCSS 中通过 `@use "@/assets/variables" as *` 引入设计 token
- Element Plus 全量引入，中文语言包 (`zhCn`)，图标通过 `app.component()` 全局注册
- 认证 token 存 Pinia（持久化 `persist.paths: ['token', 'userInfo']`），通过 `Authorization` header 发送
- 401 响应 → 清除 token + 路由守卫拦截 → 跳转首页（有 2s 防抖，避免重复弹窗）
- 路由 `meta.requiresAuth` 标记需登录的页面（购物车、用户中心等）
- `body.el-popup-parent--hidden` 覆盖为 `overflow: visible`（修复 Element Plus 弹窗锁定滚动条问题）

## 注意事项

- 无测试基础设施，无 CI 配置
- 修改 API 或 store 时注意 token 持久化路径 `persist.paths`
- 页面原型在 `页面原型/` 目录（独立 HTML，非 Vue 代码）
- Node 要求：`^20.19.0 || >=22.12.0`
