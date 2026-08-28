import { createRouter, createWebHistory } from 'vue-router'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    {
      path: '/user/login',
      name: 'login',
      component: () => import('@/views/login.vue'),
    },
    {
      path: '/user/register',
      name: 'register',
      component: () => import('@/views/register.vue'),
    },
    {
      path: '/',
      component: () => import('@/Home.vue'),
      redirect: '/index',
      children: [
        {
          path: '/index',
          name: 'index',
          component: () => import('@/views/index.vue'),
        },
        {
          path: '/category/:categoryId?',
          name: 'category',
          component: () => import('@/views/category.vue'),
        },
        {
          path: '/product/:id(\\d+)',
          name: 'product',
          component: () => import('@/views/product.vue'),
        },
        {
          path: '/cart',
          name: 'cart',
          component: () => import('@/views/cart.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/search',
          name: 'search',
          component: () => import('@/views/search.vue'),
        },
        {
          path: '/pointsMall',
          name: 'pointsMall',
          component: () => import('@/views/pointsMall.vue'),
        },
        {
          path: '/user',
          component: () => import('@/views/user/home.vue'),
          redirect: '/user/points',
          meta: {
            requiresAuth: true
          },
          children: [
            {
              path: '/user/orders',
              name: 'orders',
              component: () => import('@/views/user/orders.vue')
            },
            {
              path: '/user/points',
              name: 'points',
              component: () => import('@/views/user/points.vue')
            },
            {
              path: '/user/coupons',
              name: 'coupons',
              component: () => import('@/views/user/coupons.vue')
            },
            {
              path: '/user/address',
              name: 'address',
              component: () => import('@/views/user/address.vue')
            },
            {
              path: '/user/settings',
              name: 'settings',
              component: () => import('@/views/user/settings.vue')
            },
          ],
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/404.vue')
    },
  ],
})

router.beforeEach((to, from) => {
  const { token } = useCounterStore()

  if ((to.name === 'login' || to.name === 'register') && token) {
    return { name: 'index' }
  }

  if (to.meta.requiresAuth && !token) {
    toast('请先登录', 'error')
    return { name: 'index' }
  }

  return true

})

export default router
