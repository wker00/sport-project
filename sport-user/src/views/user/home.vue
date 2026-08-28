<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { userLogout, getOrderList, getUserInfo } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'

const router = useRouter()
const store = useCounterStore()
const { userInfo } = storeToRefs(store)
const orders = ref([])
const loading = ref(true)

const levelMap = {
  1: { name: '普通会员', icon: 'ph-fill ph-crown-simple', color: '#94a3b8', cls: 'vip-regular' },
  2: { name: '银卡会员', icon: 'ph-fill ph-shield-star', color: '#6b7280', cls: 'vip-silver' },
  3: { name: '金卡会员', icon: 'ph-fill ph-crown', color: '#d97706', cls: 'vip-gold' },
  4: { name: '钻石会员', icon: 'ph-fill ph-trophy', color: '#0891b2', cls: 'vip-diamond' },
  5: { name: '黑金会员', icon: 'ph-fill ph-crown', color: '#f59e0b', cls: 'vip-black-gold' },
}

const vipLevel = computed(() => levelMap[userInfo.value.userLevel] || levelMap[1])
const displayName = computed(() => userInfo.value.nickname || '')
const sidebarOrderCount = computed(() =>
  orders.value.filter(o => o.status !== 4 && o.status !== 5 && o.status !== 6).length
)

async function fetchOrders() {
  try {
    const res = await getOrderList()
    if (res.data.code === 200) orders.value = res.data.data || []
  } catch (e) { console.error(e); orders.value = [] }
}

async function handleLogout() {
  try { await userLogout() } catch (e) { console.error(e) }
  store.clearToken()
  router.push({ name: 'index' })
}

async function confirmLogout() {
  try {
    ElMessageBox.confirm('确定退出登录？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', lockScroll: false
    }).then(() => handleLogout()).catch(() => { })
  } catch (e) { console.error(e) }
}

async function refreshUserInfo() {
  try {
    const res = await getUserInfo()
    if (res.data.code === 200) store.setUserInfo(res.data.data)
  } catch (e) { console.error(e) }
}

provide('refreshOrders', fetchOrders)

onMounted(async () => {
  loading.value = true
  await Promise.all([fetchOrders(), refreshUserInfo()])
  loading.value = false
})
</script>

<template>
  <div class="user-page">
    <div class="user-container">
      <div class="user-header double-bezel-outer">
        <div class="double-bezel-inner">
          <div class="user-avatar-shell">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="头像" class="avatar-img" />
            <span v-else>{{ userInfo.nickname?.[0] || "?" }}</span>
          </div>
          <div class="user-info">
            <h1 class="user-name">{{ displayName }}</h1>
            <p class="user-id"><i class="ph ph-phone"></i> {{ userInfo.phone ? userInfo.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '未绑定' }}</p>
            <span class="user-level" :class="vipLevel.cls">
              <i :class="vipLevel.icon"></i>
              {{ vipLevel.name }}
            </span>
          </div>
          <div class="user-stats">
            <router-link to="/user/orders" class="stat-item">
              <div class="stat-value" :style="{ color: vipLevel.color }">{{ orders.length }}</div>
              <div class="stat-label">我的订单</div>
            </router-link>
            <router-link to="/user/points" class="stat-item">
              <div class="stat-value" :style="{ color: vipLevel.color }">{{ (userInfo.pointsBalance || 0).toLocaleString() }}</div>
              <div class="stat-label">我的积分</div>
            </router-link>
          </div>
        </div>
      </div>

      <div class="user-tabs">
        <router-link to="/user/points" active-class="active">
          <i class="ph ph-coin"></i> 我的积分
        </router-link>
        <router-link to="/user/orders" active-class="active">
          <i class="ph ph-shopping-cart"></i> 我的订单
          <span v-if="sidebarOrderCount > 0" class="tab-badge">{{ sidebarOrderCount }}</span>
        </router-link>
        <router-link to="/user/coupons" active-class="active">
          <i class="ph ph-ticket"></i> 优惠券
        </router-link>
        <router-link to="/user/address" active-class="active">
          <i class="ph ph-map-pin"></i> 收货地址
        </router-link>
        <router-link to="/user/settings" active-class="active">
          <i class="ph ph-gear"></i> 账号设置
        </router-link>
      </div>

      <div class="user-grid">
        <div class="user-sidebar">
          <div class="sidebar-shell">
            <nav class="sidebar-nav">
              <router-link to="/user/points" active-class="active">
                <i class="ph ph-coin"></i> 我的积分
              </router-link>
              <router-link to="/user/orders" active-class="active">
                <i class="ph ph-shopping-cart"></i> 我的订单
                <span v-if="sidebarOrderCount > 0" class="badge">{{ sidebarOrderCount }}</span>
              </router-link>
              <router-link to="/user/coupons" active-class="active">
                <i class="ph ph-ticket"></i> 优惠券
              </router-link>
              <router-link to="/user/address" active-class="active">
                <i class="ph ph-map-pin"></i> 收货地址
              </router-link>
              <router-link to="/user/settings" active-class="active">
                <i class="ph ph-gear"></i> 账号设置
              </router-link>
              <a class="nav-logout" @click="confirmLogout">
                <i class="ph ph-sign-out"></i> 退出登录
              </a>
            </nav>
          </div>
        </div>
        <div class="user-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;
@use '@/assets/level.scss' as *;

p,h1{
  cursor: context-menu;
}

.user-page {
  padding-top: 100px;
  min-height: 100dvh;
}

.user-container {
  max-width: 1400px;
  margin: 0 auto;
  width: 92%;
}

.double-bezel-outer {
  @include double-bezel($outer-radius: $radius-2xl, $gap: 0.375rem);
  margin-bottom: 32px;
}

.double-bezel-inner {
  padding: 40px;
}

.user-header {
  .user-avatar-shell {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: linear-gradient(135deg, $accent-energy, $accent-gradient-light);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    font-weight: 800;
    color: #fff;
    flex-shrink: 0;
    overflow: hidden;

    .avatar-img { width: 100%; height: 100%; object-fit: cover; }
  }

  .user-info {
    flex: 1;

    .user-name {
      font-size: 26px;
      font-weight: 800;
      margin-bottom: 8px;
      color: $text-primary;
      letter-spacing: -0.02em;
    }

    .user-id {
      font-size: 13px;
      color: $text-muted;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 6px;
      i { font-size: 14px; }
    }
  }

  .double-bezel-inner {
    display: flex;
    align-items: center;
    gap: 32px;
  }

  .user-stats {
    display: flex;
    gap: 40px;

    .stat-item {
      text-align: center;
      cursor: pointer;
      transition: transform 0.5s $transition-premium;
      text-decoration: none;

      &:hover { transform: translateY(-4px); }

      .stat-value {
        font-size: 28px;
        font-weight: 800;
        margin-bottom: 4px;
        transition: color 0.4s;
      }

      .stat-label {
        font-size: 13px;
        color: $text-secondary;
      }
    }
  }
}

.user-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
}

.sidebar-shell {
  @include solid-card;
  border-radius: $radius-xl;
  overflow: hidden;
  position: sticky;
  top: 120px;
}

.sidebar-nav {
  padding: 12px;

  a {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 12px;
    font-size: 14px;
    color: $text-secondary;
    transition: all 0.5s $transition-premium;
    cursor: pointer;
    text-decoration: none;

    &:hover {
      background: $bg-card-hover;
      color: $text-primary;
      transform: translateX(4px);
    }

    &.active {
      background: $accent-energy-soft;
      color: $accent-energy;
    }

    i { font-size: 20px; }

    .badge {
      margin-left: auto;
      padding: 2px 8px;
      background: $accent-energy;
      border-radius: 999px;
      font-size: 11px;
      font-weight: 700;
      color: #fff;
    }
  }

  .nav-logout {
    @include btn-base;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 12px;
    font-size: 14px;
    color: $color-danger;
    margin-top: 8px;
    border-top: 0.5px solid $border-subtle;

    &:hover {
      background: rgba($color-danger, 0.1);
      color: $color-danger;
    }

    i { font-size: 20px; }
  }
}

.user-content { min-width: 0; }

.user-tabs {
  display: none;
  gap: 8px;
  margin-bottom: 24px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  white-space: nowrap;
  flex-wrap: nowrap;

  &::-webkit-scrollbar { display: none; }

  a {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 18px;
    border-radius: 999px;
    font-size: 13px;
    font-weight: 600;
    color: $text-secondary;
    white-space: nowrap;
    flex-shrink: 0;
    text-decoration: none;
    transition: all 0.4s $transition-premium;

    &:hover {
      background: $bg-card-hover;
    }

    &.active {
      background: $accent-energy-soft;
      color: $accent-energy;
    }

    i { font-size: 16px; }

    .tab-badge {
      padding: 1px 6px;
      background: $accent-energy;
      border-radius: 999px;
      font-size: 10px;
      color: #fff;
      font-weight: 700;
    }
  }

  @include respond(lg) {
    display: flex;
  }
}

@include respond(lg) {
  .user-grid { grid-template-columns: 1fr; }
  .user-sidebar { display: none; }
}

@include respond(md) {
  .user-page { padding-top: 84px; }

  .user-header .double-bezel-inner {
    flex-direction: column;
    text-align: center;
  }

  .user-header .user-stats { justify-content: center; }
}
</style>
