# vue-project-admin — 管理后台

Vue 3 + Vite 8 + Element Plus 后台管理系统，端口 **9091**。后端 API 代理至 `http://localhost:8080`。

## 命令

```bash
npm run dev     # Vite dev server → localhost:9091
npm run build   # 生产构建
npm run preview # 预览生产构建
```

无 lint / test / typecheck 脚本。Node 要求 `^20.19.0 || >=22.12.0`（`package.json` engines）。

## 关键文件

| 文件 | 职责 |
|---|---|
| `src/api/axios.js` | axios 实例，`baseURL: '/api'`，请求拦截器附加 token，响应拦截器处理 401/5xx |
| `src/api/manager.js` | 所有 API 函数，按模块分组导出，**不要重复创建 axios 实例** |
| `src/stores/counter.js` | Pinia store（名称 `counter`），管理 `token` + `adminInfo`，persist 用 `pick` |
| `src/utils/toast.js` | `toast(message, type)` — ElMessage 封装，**不要直接用 ElMessage** |
| `src/router/index.js` | 路由定义 + `beforeEach` 守卫 |
| `src/Home.vue` | 侧边栏布局壳，AppSidebar + AppHeader + `<router-view>` |

项目根目录 `管理端接口文档.md` 包含完整后端 API 定义，涉及新接口开发时优先查阅。

## 组件结构

公共组件仅 3 个：`AppSidebar.vue`（菜单）、`AppHeader.vue`（顶栏）、`ProductFormDialog.vue`（商品表单弹窗）。

## Element Plus 配置

`main.js` 全局注册了 Element Plus（中文语言包 `zh-cn`）和所有 `@element-plus/icons-vue` 图标，组件中可直接使用 `<User />` 等图标，无需单独 import。

## 文件上传约定

商品、分类、积分商品的创建/更新接口均为 `multipart/form-data`。`manager.js` 中通过 `{ headers: { 'Content-Type': undefined } }` 让 axios 自动设置 boundary：

```js
// 正确做法 — axios 自动处理 boundary
axios.post('/admin/category', data, { headers: { 'Content-Type': undefined } })

// 错误做法 — 手动设置会导致 boundary 丢失
axios.post('/admin/category', data, { headers: { 'Content-Type': 'multipart/form-data' } })
```

## 登录流程

1. `adminLogin({ username, password })` → 拿到 token
2. `counterStore.setToken(token)` 存储 token
3. `getAdminInfo()` → `counterStore.setAdminInfo(data)` 存储管理员信息
4. 若 `getAdminInfo` 失败 → `clearToken()` + 跳转登录页

路由守卫：已登录访问 `/admin/login` → 重定向 `/dashboard`；无 token 访问需认证页 → 直接跳转登录页（无 toast）。

## 错误处理模式

axios 响应拦截器有两层检查：HTTP 状态码（401/5xx）+ 业务 `code !== 200`。业务错误会 `toast(message, 'error')` 后 reject 一个带 `isBusinessError` 标记的 Error，后续 `.catch` 中不应重复提示。401 和 5xx 的 toast 有 2s 去重冷却。

## SCSS 约定

主题色 `$orange: #ff6b35` 在多个组件中各自定义（非全局变量），修改时需同步更新。组件使用 BEM 风格命名。

## 未完成功能

- `/roles` 和 `/settings` 路由目前指向 `Dashboard.vue`（占位）
- AppSidebar 中注释掉了「地址管理」和「系统设置」菜单项

## 交互要求

1. 你在处理所有问题时，**全程思考过程必须使用中文**（包括需求分析、逻辑拆解、方案选择、步骤推导等所有内部推理环节）;
2. 最终输出的所有回答内容（包括文字解释、代码注释、步骤说明等）必须全部使用中文，仅代码语法本身的英文关键词除外。
