<script setup>
import { useRouter, useRoute } from 'vue-router'

defineProps({
  collapsed: Boolean
})

const router = useRouter()
const route = useRoute()

const menuGroups = [
  {
    label: '概览',
    children: [
      { path: '/dashboard', icon: 'HomeFilled', label: '首页' },
    ],
  },
  {
    label: '商城管理',
    children: [
      { path: '/products', icon: 'Goods', label: '商品管理' },
      { path: '/categories', icon: 'Collection', label: '分类管理' },
      { path: '/orders', icon: 'List', label: '订单管理' },
      { path: '/coupons', icon: 'Ticket', label: '优惠券管理' },
      { path: '/points-gifts', icon: 'Present', label: '积分商品管理' },
      { path: '/exchange-orders', icon: 'Document', label: '积分商品兑换管理' },
      
    ],
  },
  {
    label: '用户管理',
    children: [
      { path: '/userList', icon: 'UserFilled', label: '用户管理' },
      { path: '/reviews', icon: 'ChatDotRound', label: '评价管理' },
    ],
  },
  {
    label: '系统管理',
    children: [
      { path: '/adminList', icon: 'User', label: '管理员管理' },
      { path: '/operate-logs', icon: 'Monitor', label: '管理员操作日志' },
      // { path: '/roles', icon: 'Key', label: '角色权限' },
    ],
  },
]

const isActive = (path) => route.path === path || route.path.startsWith(path + '/')

const handleMenuClick = (path) => {
  router.push(path)
}
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--collapsed': collapsed }">
    <div class="sidebar__logo">
      <span class="sidebar__logo-icon" />
      <transition name="fade">
        <span v-show="!collapsed" class="sidebar__logo-text">后台管理</span>
      </transition>
    </div>

    <el-menu
      :default-active="route.path"
      :collapse="collapsed"
      class="sidebar__menu"
      background-color="#0f172a"
      text-color="rgba(255,255,255,0.6)"
      active-text-color="#ff6b35"
    >
      <template v-for="group in menuGroups" :key="group.label">
        <div v-show="!collapsed" class="sidebar__group-label">{{ group.label }}</div>

        <el-menu-item
          v-for="item in group.children"
          :key="item.path"
          :index="item.path"
          @click="handleMenuClick(item.path)"
        >
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>
            <span>{{ item.label }}</span>
          </template>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- <div class="sidebar__footer">
      <el-menu
        :default-active="route.path"
        :collapse="collapsed"
        class="sidebar__menu sidebar__menu--footer"
        background-color="#0f172a"
        text-color="rgba(255,255,255,0.6)"
        active-text-color="#ff6b35"
      >
        <el-menu-item index="/settings" @click="handleMenuClick('/settings')">
          <el-icon>
            <Setting />
          </el-icon>
          <template #title>
            <span>系统设置</span>
          </template>
        </el-menu-item>
      </el-menu>
    </div> -->
  </aside>
</template>

<style lang="scss" scoped>
$sidebar-width: 240px;
$sidebar-collapsed: 64px;
$dark: #0f172a;
$orange: #ff6b35;

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: $sidebar-width;
  height: 100dvh;
  background: $dark;
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: width 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;

  &--collapsed {
    width: $sidebar-collapsed;
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__logo-icon {
    flex-shrink: 0;
    width: 32px;
    height: 32px;
    background: $orange;
    border-radius: 8px;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      inset: 3px;
      background: $dark;
      border-radius: 5px;
    }
  }

  &__logo-text {
    font-size: 18px;
    font-weight: 700;
    color: #fff;
    letter-spacing: -0.3px;
    white-space: nowrap;
  }

  &__group-label {
    font-size: 11px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.25);
    text-transform: uppercase;
    letter-spacing: 1.2px;
    padding: 16px 20px 6px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__menu {
    flex: 1;
    border-right: none;
    overflow-y: auto;
    overflow-x: hidden;
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
      display: none;
    }

    &--footer {
      flex: 0 0 auto;
      border-top: 1px solid rgba(255, 255, 255, 0.06);
    }

    .el-menu-item {
      margin: 2px 8px;
      border-radius: 8px;
      width: auto;
      height: 42px;
      line-height: 42px;
      transition: all 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.06);
      }

      &.is-active {
        background: rgba($orange, 0.12);
      }
    }

    .el-icon {
      font-size: 18px;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
