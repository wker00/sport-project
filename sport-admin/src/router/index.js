import { createRouter, createWebHistory } from 'vue-router'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/admin/login',
      name: 'login',
      component: () => import('@/views/login.vue'),
    },
    {
      path: '/',
      component: () => import('@/Home.vue'),
      redirect: '/dashboard',
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'), //首页
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'), //个人中心页
        },
        {
          path: 'products',
          name: 'Products',
          component: () => import('@/views/Products.vue'), //商品列表页
        },
        {
          path: 'categories',
          name: 'Categories',
          component: () => import('@/views/Categories.vue'), //分类列表页
        },
        {
          path: 'orders',
          name: 'Orders',
          component: () => import('@/views/Orders.vue'), //订单列表页
        },
        {
          path: 'coupons',
          name: 'Coupons',
          component: () => import('@/views/Coupons.vue'), //优惠券管理页
        },
        {
          path: 'points-gifts',
          name: 'PointsGifts',
          component: () => import('@/views/PointsGifts.vue'), //积分商品页
        },
        {
          path: 'userList',
          name: 'UserList',
          component: () => import('@/views/userList.vue'), //用户列表页 
        },
        {
          path: 'adminList',
          name: 'AdminList',
          component: () => import('@/views/adminList.vue'), //管理员列表页
        },
        {
          path: 'roles',
          name: 'Roles',
          component: () => import('@/views/Dashboard.vue'), //角色权限页
        },
        {
          path: 'exchange-orders',
          name: 'ExchangeOrders',
          component: () => import('@/views/ExchangeOrders.vue'),
        },
        {
          path: 'reviews',
          name: 'Reviews',
          component: () => import('@/views/Reviews.vue'), //评价管理页
        },
        {
          path: 'operate-logs',
          name: 'OperateLogs',
          component: () => import('@/views/OperateLogs.vue'), //操作日志页
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/Dashboard.vue'), //系统设置
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
    },
  ],
})

router.beforeEach((to, from) => {
  const { token } = useCounterStore()

  if (to.name === 'login' && token) {
    return { name: 'Dashboard' }
  }

  if (to.meta.requiresAuth && !token) {
    return { name: 'login' }
  }

  return true

})

export default router
