<script setup>
import { ref, computed, onMounted, watch } from "vue"
import { useRouter } from "vue-router"
import { useCounterStore } from "@/stores/counter"

const router = useRouter()
const store = useCounterStore()
const searchKeyword = ref("")
const menuOpen = ref(false)

function doSearch() {
  const kw = searchKeyword.value.trim()
  if (!kw) return
  menuOpen.value = false
  router.push({ name: "search", query: { keyword: kw } })
}

const user = computed(() => store.userInfo)
const isLoggedIn = computed(() => !!store.token)

function toggleMenu() {
  menuOpen.value = !menuOpen.value
}

function closeMenu() {
  menuOpen.value = false
}

onMounted(() => {
  if (store.token && !store.cartItems.length) store.fetchCartList()
})

watch(menuOpen, (val) => {
  document.body.style.overflow = val ? "hidden" : ""
})
</script>

<template>
  <header class="fluid-island">
    <router-link to="/" class="fixed-logo" aria-label="SportZone 首页">
      <svg viewBox="0 0 40 40" width="34" height="34" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="20" cy="20" r="20" fill="#ff6b35" />
        <path d="M22.5 8L13 22h6l-2.5 10L27 18h-6l2.5-10z" fill="#fff" stroke="#fff" stroke-width="0.5"
          stroke-linejoin="round" />
      </svg>
    </router-link>
    <nav class="nav-pill" :class="{ 'menu-active': menuOpen }">
      <div class="nav-center">
        <router-link to="/index" class="nav-link" active-class="active">首页</router-link>
        <router-link to="/category" class="nav-link" active-class="active">商品分类</router-link>
        <router-link to="/pointsMall" class="nav-link" active-class="active">积分商城</router-link>
      </div>

      <div class="search-wrap">
        <input v-model="searchKeyword" class="search-field" type="text" placeholder="搜索运动装备..."
          @keyup.enter="doSearch" />
        <div class="search-trigger" @click="doSearch" aria-label="搜索">
          <i class="ph ph-magnifying-glass"></i>
        </div>
      </div>

      <div class="nav-right">

        <template v-if="isLoggedIn">
          <router-link to="/cart" class="cart-link" aria-label="购物车">
            <i class="ph ph-shopping-cart"></i>
            <span v-if="store.cartCount > 0" class="cart-count">{{ store.cartCount > 99 ? '99+' : store.cartCount
              }}</span>
          </router-link>
          <router-link to="/user" class="avatar-link">
            <div v-if="user.avatar" class="avatar" :style="{ backgroundImage: `url(${user.avatar})` }"></div>
            <div v-else class="avatar avatar-placeholder">
              {{ (store.userInfo.nickname || store.userInfo.username || '?')[0] }}
            </div>
          </router-link>
        </template>
        <router-link v-else to="/user/login" class="login-link">登录</router-link>

        <button class="hamburger" :class="{ 'is-active': menuOpen }" @click="toggleMenu" aria-label="菜单">
          <span class="hamburger-line top"></span>
          <span class="hamburger-line mid"></span>
          <span class="hamburger-line bot"></span>
        </button>
      </div>
    </nav>

    <Teleport to="body">
      <div class="menu-overlay" :class="{ 'is-open': menuOpen }" @click="closeMenu">
        <div class="menu-panel" @click.stop>
          <nav class="menu-links">
            <router-link to="/index" class="menu-link" :class="{ 'is-visible': menuOpen }" style="--delay: 0"
              @click="closeMenu">首页</router-link>
            <router-link to="/category" class="menu-link" :class="{ 'is-visible': menuOpen }" style="--delay: 1"
              @click="closeMenu">商品分类</router-link>
            <router-link to="/pointsMall" class="menu-link" :class="{ 'is-visible': menuOpen }" style="--delay: 2"
              @click="closeMenu">积分商城</router-link>
            <router-link v-if="isLoggedIn" to="/user/orders" class="menu-link" :class="{ 'is-visible': menuOpen }"
              style="--delay: 3" @click="closeMenu">我的订单</router-link>
            <router-link v-if="isLoggedIn" to="/user" class="menu-link" :class="{ 'is-visible': menuOpen }"
              style="--delay: 4" @click="closeMenu">个人中心</router-link>
          </nav>
        </div>
      </div>
    </Teleport>
  </header>
</template>

<style scoped lang="scss">
@use "@/assets/variables" as *;

.fluid-island {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  padding-top: 10px;
  pointer-events: none;
}

.nav-pill {
  pointer-events: all;
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 18px;
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  border: 0.5px solid $nav-border;
  border-radius: 25px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
  transition: all 0.5s $transition-premium;
  max-width: 1400px;

  &.menu-active {
    border-radius: 20px;
  }
}

.fixed-logo {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1001;
  display: flex;
  align-items: center;

  svg {
    display: block;
  }
}

.nav-center {
  display: flex;
  align-items: center;
  gap: 2px;
  margin: 0 12px;

  @include respond(lg) {
    display: none;
  }
}

.nav-link {
  padding: 8px 16px;
  font-weight: 500;
  font-size: 13px;
  color: $text-secondary;
  border-radius: 999px;
  transition: all 0.4s $transition-premium;
  white-space: nowrap;
  transition: all 0.4s cubic-bezier(0.32, 0.72, 0, 1);

  &.active {
    color: $accent-energy;
    background-color: rgba(255, 107, 53, 0.1);
  }
  &:hover{
    color: $accent-energy;
    background-color: rgba(255, 107, 53, 0.1);
  }
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 5px;
  flex-shrink: 0;
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 4px 0 10px;
  height: 36px;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  border-radius: 999px;
  transition: all 0.4s $transition-premium;
  flex-shrink: 0;

  .search-field {
    width: 140px;
    background: transparent;
    border: none;
    outline: none;
    color: $text-primary;
    font-size: 13px;
    font-family: inherit;

    &::placeholder {
      color: $text-muted;
    }

    @include respond(lg) {
      width: 80px;
    }

    @include respond(md) {
      width: 60px;
    }
  }

  .search-trigger {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $text-muted;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.4s $transition-premium;
    flex-shrink: 0;

    &:hover {
      background: $accent-energy;
      color: #fff;
    }

    @include respond(md) {
      display: none;
    }
  }

  &:focus-within {
    border-color: $accent-energy;
  }

  @include respond(lg) {
    padding: 0 8px;
    gap: 4px;
  }

  @include respond(md) {
    padding: 0 6px;
    gap: 2px;
  }
}

.cart-link {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-muted;
  font-size: 18px;
  transition: all 0.4s $transition-premium;

  &:hover {
    color: $accent-energy;
    background: rgba($accent-energy, 0.1);
  }
}

.cart-count {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 16px;
  height: 16px;
  border-radius: 999px;
  background: $accent-energy;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.avatar-link {
  display: flex;
  align-items: center;
  border: 1.5px solid $border-medium;
  border-radius: 50%;
  transition: all 0.4s $transition-premium;

  &:hover {
    border-color: $accent-energy;
    box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
  }
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-size: cover;
  background-position: center;
}

.avatar-placeholder {
  background: linear-gradient(135deg, $accent-energy, #ff8c5a);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.login-link {
  padding: 7px 18px;
  background: rgba(255, 107, 53, 0.15);
  color: $accent-energy;
  font-size: 13px;
  font-weight: 600;
  border-radius: 999px;
  transition: all 0.4s $transition-premium;

  &:hover {
    background: $accent-energy;
    color: #fff;
    box-shadow: 0 4px 16px rgba(255, 107, 53, 0.3);
  }
}

// === Hamburger ===
.hamburger {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: transparent;
  border: none;
  cursor: pointer;
  display: none;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.4s $transition-premium;

  @include respond(lg) {
    display: flex;
    @include touch-target(36px);
  }

  &:hover {
    background: $bg-card-hover;
  }
}

.hamburger-line {
  display: block;
  width: 16px;
  height: 1.5px;
  background: $text-secondary;
  border-radius: 999px;
  transition: all 0.5s $transition-premium;
  transform-origin: center;
}

.hamburger.is-active {
  .top {
    transform: translateY(5.5px) rotate(45deg);
  }

  .mid {
    transform: scaleX(0);
    opacity: 0;
  }

  .bot {
    transform: translateY(-5.5px) rotate(-45deg);
  }
}

// === Menu Overlay ===
.menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 999;
  background: $overlay-bg;
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  pointer-events: none;
  transition: all 0.6s $transition-premium;

  &.is-open {
    opacity: 1;
    pointer-events: all;
  }
}

.menu-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  width: 100%;
  max-width: 400px;
  padding: 0 24px;
}

.menu-search {
  width: 100%;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $transition-premium;
  transition-delay: 0.05s;

  .is-open & {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-search-inner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  border-radius: 16px;
  transition: border-color 0.4s $transition-premium;

  &:focus-within {
    border-color: $accent-energy;
  }

  i {
    font-size: 20px;
    color: $text-muted;
    flex-shrink: 0;
  }

  input {
    flex: 1;
    background: transparent;
    border: none;
    outline: none;
    color: $text-primary;
    font-size: 16px;
    font-family: inherit;

    &::placeholder {
      color: $text-muted;
    }
  }
}

.menu-links {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.menu-link {
  font-size: 28px;
  font-weight: 700;
  color: $text-muted;
  letter-spacing: -0.02em;
  padding: 8px 0;
  transition: all 0.5s $transition-premium;
  opacity: 0;
  transform: translateY(30px);
  transition-delay: calc(var(--delay) * 0.08s);

  &.is-visible {
    opacity: 1;
    transform: translateY(0);
  }

  &:hover,
  &.router-link-active {
    color: #fff;
  }
}

.menu-footer {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $transition-premium;
  transition-delay: calc(var(--delay) * 0.08s);

  &.is-visible {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-login-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 36px;
  background: $accent-energy;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border-radius: 999px;
  transition: all 0.4s $transition-premium;

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 8px 24px rgba(255, 107, 53, 0.3);
  }
}

.menu-logout {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 36px;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  color: $text-muted;
  font-size: 14px;
  font-weight: 600;
  border-radius: 999px;
  transition: all 0.4s $transition-premium;

  &:hover {
    background: $bg-elevated;
    color: $text-primary;
  }
}

// hamburger visibility controlled by .hamburger default display:none + respond(lg) override
</style>
