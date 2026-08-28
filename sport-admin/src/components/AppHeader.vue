<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCounterStore } from '@/stores/counter'
import { ElMessageBox } from 'element-plus'
import { adminLogout } from '@/api/manager'
import { toast } from '@/utils/toast'

defineProps({
  collapsed: Boolean
})

const emit = defineEmits(['toggle-collapse'])
const router = useRouter()
const counterStore = useCounterStore()

const admin = computed(
  () => counterStore.adminInfo
)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch { return }
  try {
     await adminLogout() 
    } catch {}
  counterStore.clearToken()
  toast('退出成功')
  router.push({ name: 'login' })
}
</script>

<template>
  <header class="app-header">
    <div class="header__left">
      <el-button text class="header__collapse-btn" @click="emit('toggle-collapse')">
        <el-icon size="20">
          <Expand v-if="collapsed" />
          <Fold v-else />
        </el-icon>
      </el-button>
    </div>

    <div class="header__right">
      <el-dropdown trigger="click" placement="bottom-end">
        <div class="header__user">
          <el-avatar v-if="admin.avatar" :size="32" :src="admin.avatar" class="header__avatar"></el-avatar>
          <el-avatar v-else :size="32" class="header__avatar">
            <el-icon>
              <UserFilled />
            </el-icon>
          </el-avatar>

          <span class="header__username">{{ admin.nickname }}</span>
          <el-icon class="header__arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/profile')">
              <el-icon>
                <User />
              </el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item @click="handleLogout" class="logout-item">
              <span class="logout-icon">
                <el-icon>
                  <SwitchButton />
                </el-icon>
              </span>
              <span class="logout-text">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style lang="scss" scoped>
$orange: #ff6b35;

.app-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header__left {
  display: flex;
  align-items: center;
}

.header__collapse-btn {
  font-size: 18px;
  color: #64748b;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: #f1f5f9;
    color: $orange;
  }
}

.header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header__user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background: #f8fafc;
  }
}

.header__avatar {
  background: rgba($orange, 0.12);
  color: $orange;
  flex-shrink: 0;
}

.header__username {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.header__arrow {
  font-size: 12px;
  color: #94a3b8;
}

.logout-item {
  .logout-icon,
  .logout-text {
    color: #f56c6c !important;
  }
  .logout-icon {
    display: inline-flex;
    align-items: center;
  }
}
</style>
