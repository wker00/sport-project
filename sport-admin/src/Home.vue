<script setup>
import { ref } from 'vue'
import AppHeader from '@/components/AppHeader.vue'
import AppSidebar from '@/components/AppSidebar.vue'

const collapsed = ref(false)

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <div class="admin-layout">
    <AppSidebar :collapsed="collapsed" />

    <div class="admin-main" :class="{ 'admin-main--expanded': collapsed }">
      <AppHeader :collapsed="collapsed" @toggle-collapse="toggleCollapse" />
      <main class="admin-content">
        <router-view />
      </main>

    </div>
  </div>
</template>

<style lang="scss">
$sidebar-width: 240px;
$sidebar-collapsed: 64px;

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
}

#app {
  height: 100%;
}

.admin-layout {
  min-height: 100dvh;
  background: #f8fafc;
}

.admin-main {
  margin-left: $sidebar-width;
  min-height: 100dvh;
  transition: margin-left 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;

  &--expanded {
    margin-left: $sidebar-collapsed;
  }
}

.admin-content {
  flex: 1;
  padding: 28px 32px;
}
</style>
