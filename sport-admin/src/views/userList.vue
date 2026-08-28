<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getUserList, getUserDetail, resetUserPassword } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'

const store = useCounterStore()

const keyword = ref('')
const users = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)

const total = computed(() => filteredUsers.value.length)

const filteredUsers = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return users.value
  return users.value.filter(
    (u) =>
      (u.username && u.username.toLowerCase().includes(kw)) ||
      (u.nickname && u.nickname.toLowerCase().includes(kw)) ||
      (u.phone && u.phone.includes(kw))
  )
})

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const levelMap = {
  1: { text: '普通会员', className: 'vip-regular' },
  2: { text: '银卡会员', className: 'vip-silver' },
  3: { text: '金卡会员', className: 'vip-gold' },
  4: { text: '钻石会员', className: 'vip-diamond' },
  5: { text: '黑金会员', className: 'vip-black-gold' },
}

const isSuperAdmin = computed(() => store.adminInfo?.role === 1)

const maskPhone = (phone) => {
  if (!phone || phone.length < 7) return phone || '-'
  return phone.slice(0, 3) + '****' + phone.slice(-4)
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data.data || []
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
}

const handleReset = () => {
  keyword.value = ''
  page.value = 1
}

const handlePageChange = (p) => {
  page.value = p
}

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentUser = ref(null)

const handleView = async (row) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await getUserDetail(row.id)
    currentUser.value = res.data.data
  } catch {
    currentUser.value = row
  } finally {
    detailLoading.value = false
  }
}

const handleResetPwd = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定将用户「${row.nickname || row.username}」的密码重置为 123456？`,
      '确认重置',
      { confirmButtonText: '确认重置', cancelButtonText: '取消', type: 'warning' }
    )
    await resetUserPassword(row.id)
    toast('密码已重置为 123456', 'success')
  } catch {
  }
}

onMounted(fetchUsers)
</script>

<template>
  <div class="page">
    <h1 class="page__title">用户列表</h1>
    <p class="page__desc">管理平台所有注册用户</p>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名 / 昵称 / 手机号" clearable class="toolbar__input"
          @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table :data="pagedUsers" v-loading="loading" stripe class="members-table" empty-text="暂无会员数据">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="34" :src="row.avatar">
              <el-icon :size="18">
                <UserFilled />
              </el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="120" align="center">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" align="center">
          <template #default="{ row }">
            {{ maskPhone(row.phone) }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" align="center">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="等级" width="110" align="center">
          <template #default="{ row }">
            <span class="user-level" :class="levelMap[row.userLevel]?.className">
              {{ levelMap[row.userLevel]?.text || '未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="pointsBalance" label="积分" width="80" align="center" />
        <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              <el-button v-if="isSuperAdmin" type="warning" link size="small"
                @click="handleResetPwd(row)">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next, total"
          background @current-change="handlePageChange" />
      </div>
    </div>
  </div>

  <!-- 详情弹窗 -->
  <el-dialog v-model="detailVisible" title="会员详情" width="520px" :close-on-click-modal="false">
    <el-skeleton v-if="detailLoading" :rows="6" animated />
    <div v-else-if="currentUser" class="detail">
      <div class="detail__avatar">
        <el-avatar :size="64" :src="currentUser.avatar">
          <el-icon :size="32">
            <UserFilled />
          </el-icon>
        </el-avatar>
      </div>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ maskPhone(currentUser.phone) }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">
          <span class="user-level" :class="levelMap[currentUser.userLevel]?.className">
            {{ levelMap[currentUser.userLevel]?.text || '未知' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="积分余额">{{ currentUser.pointsBalance ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentUser.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-dialog>
</template>

<style lang="scss" scoped>
@use '@/assets/level.scss' as *;

$text: #334155;
$text-muted: #94a3b8;

.page {
  &__title {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: -0.3px;
    margin-bottom: 8px;
  }

  &__desc {
    font-size: 15px;
    color: $text-muted;
    margin: 0 0 24px;
  }
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  &__input {
    width: 300px;
  }
}

.members-table {
  width: 100%;

  :deep(.cell) {
    cursor: context-menu;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail {
  &__avatar {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
  }
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 2px;

  .el-button {
    margin: 0;
  }
}


</style>
