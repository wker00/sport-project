<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCounterStore } from '@/stores/counter'
import { useThemeSwitch } from '@/composables/useThemeSwitch'

const store = useCounterStore()
const router = useRouter()
const isDark = computed(() => store.theme === 'dark')
const { handleToggleTheme } = useThemeSwitch()

const transitionName = ref('')

const authRoutes = ['login', 'register']
router.beforeEach((to, from) => {
  if (authRoutes.includes(to.name) && authRoutes.includes(from.name)) {
    transitionName.value = 'auth-slide'
  } else {
    transitionName.value = ''
  }
})

let resizeObserver = null

watch(() => store.theme, (val) => {
  document.documentElement.setAttribute('data-theme', val)
}, { immediate: true })

onMounted(() => {
  const vh = window.innerHeight * 0.01
  document.documentElement.style.setProperty('--vh', `${vh}px`)
  resizeObserver = new ResizeObserver(() => {
    document.documentElement.style.setProperty('--vh', `${window.innerHeight * 0.01}px`)
  })
  resizeObserver.observe(document.documentElement)
})

onUnmounted(() => {
  if (resizeObserver) resizeObserver.disconnect()
})
</script>

<template>
  <div id="app">
    <div class="noise-overlay" aria-hidden="true"></div>
    <div class="mesh-bg" aria-hidden="true"></div>
      <button class="theme-toggle-fixed" :aria-label="isDark ? '切换到亮色模式' : '切换到暗色模式'" @click="handleToggleTheme($event)">
      <i :class="isDark ? 'ph ph-sun' : 'ph ph-moon'"></i>
    </button>
    <router-view v-slot="{ Component, route }">
      <Transition :name="transitionName" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </Transition>
    </router-view>
  </div>
</template>

<style>
:root {
  --accent: #ff6b35;
  --accent-rgb: 255, 107, 53;
  --vitality: #00d4aa;
  --transition-premium: cubic-bezier(0.32, 0.72, 0, 1);
}

html {
  scrollbar-gutter: stable;
  overflow-y: auto;
  background: var(--bg-primary);
  transition: background-color 0.5s cubic-bezier(0.32, 0.72, 0, 1);
}

html.theme-changing,
html.theme-changing *,
html.theme-changing *::before,
html.theme-changing *::after {
  transition: background-color 0.5s cubic-bezier(0.32, 0.72, 0, 1),
    color 0.5s cubic-bezier(0.32, 0.72, 0, 1),
    border-color 0.5s cubic-bezier(0.32, 0.72, 0, 1),
    box-shadow 0.5s cubic-bezier(0.32, 0.72, 0, 1) !important;
}


*::before,
*::after {
  box-sizing: border-box;
}

a {
  text-decoration: none;
  color: inherit;
}

body {
  font-family: 'Plus Jakarta Sans', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
  background: var(--bg-primary);
  color: var(--text-primary);
  min-height: 100vh;
  min-height: calc(var(--vh, 1vh) * 100);
}


.noise-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  pointer-events: none;
  opacity: 0.018;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 256px 256px;
}

.mesh-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}

.mesh-bg::before {
  content: "";
  position: absolute;
  width: 800px;
  height: 800px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--mesh-glow-1) 0%, transparent 70%);
  top: -200px;
  right: -200px;
  filter: blur(120px);
  animation: mesh-float 30s ease-in-out infinite;
}

.mesh-bg::after {
  content: "";
  position: absolute;
  width: 600px;
  height: 600px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--mesh-glow-2) 0%, transparent 70%);
  bottom: -150px;
  left: -150px;
  filter: blur(100px);
  animation: mesh-float 35s ease-in-out infinite reverse;
}

@keyframes mesh-float {

  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }

  33% {
    transform: translate(30px, -20px) scale(1.05);
  }

  66% {
    transform: translate(-20px, 15px) scale(0.95);
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--scrollbar-thumb);
  border-radius: 999px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--scrollbar-thumb-hover);
}

button {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  font-family: inherit;
}

button:active:not(:disabled) {
  transform: scale(0.97);
}

button:focus-visible {
  outline: 2px solid #ff6b35;
  outline-offset: 2px;
}

button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  pointer-events: none;
}

::selection {
  background: rgba(255, 107, 53, 0.3);
  color: #fff;
}

@keyframes skeleton-pulse {
  0% {
    opacity: 0.6;
  }

  50% {
    opacity: 0.2;
  }

  100% {
    opacity: 0.6;
  }
}

.theme-toggle-fixed {
  position: fixed;
  top: 22px;
  right: 16px;
  z-index: 1001;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 0.5px solid var(--nav-border);
  background: var(--nav-bg);
  backdrop-filter: blur(32px);
  -webkit-backdrop-filter: blur(32px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 18px;
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.32, 0.72, 0, 1);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);

  &:hover {
    color: var(--accent-energy);
  }

  i {
    transition: transform 0.5s cubic-bezier(0.32, 0.72, 0, 1);
  }

  &:hover i {
    transform: rotate(15deg) scale(1.1);
  }
}

@media (max-width: 1024px) {
  .theme-toggle-fixed {
    top: auto;
    bottom: 24px;
    right: 24px;
    width: 44px;
    height: 44px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  }
}

@media (max-width: 640px) {
  .theme-toggle-fixed {
    bottom: 16px;
    right: 16px;
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

::view-transition-old(root) {
  animation: none;
  mix-blend-mode: normal;
}

::view-transition-new(root) {
  animation: none;
  mix-blend-mode: normal;
}

.auth-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.32, 0.72, 0, 1);
}

.auth-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.32, 0.72, 0, 1);
}

.auth-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
  filter: blur(4px);
}

.auth-slide-leave-from {
  opacity: 1;
  transform: translateX(0);
  filter: blur(0);
}

.auth-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
  filter: blur(4px);
}
</style>
