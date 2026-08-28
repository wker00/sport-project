<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getAdminList, adminRegister, updateAdminInfo, updateAdminInfoById, deleteAdmin, enableAdmin, disableAdmin, resetAdminPassword } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'
import { encryptPassword } from '@/utils/rsa'

const store = useCounterStore()

const keyword = ref('')
const admins = ref([])
const loading = ref(false)

const filteredAdmins = computed(() => {
  const currentId = store.adminInfo?.id
  const list = currentId ? admins.value.filter((a) => a.id !== currentId) : admins.value
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return list
  return list.filter(
    (a) =>
      (a.username && a.username.toLowerCase().includes(kw)) ||
      (a.nickname && a.nickname.toLowerCase().includes(kw))
  )
})

const fetchAdmins = async () => {
  loading.value = true
  try {
    const res = await getAdminList()
    admins.value = res.data.data || []
  } catch {
    admins.value = []
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  keyword.value = ''
}

// 添加管理员
const createVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  role: 2,
})

const createRules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 3, max: 20, message: '3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' },
  ],
}

const createRef = ref(null)

const handleCreate = () => {
  createForm.value = { username: '', password: '', nickname: '', phone: '', email: '', role: 2 }
  createVisible.value = true
}

const submitCreate = async () => {
  const valid = await createRef.value.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    await adminRegister({
      ...createForm.value,
      password: encryptPassword(createForm.value.password),
    })
    toast('创建成功')
    createVisible.value = false
    await fetchAdmins()
  } catch {
  } finally {
    createLoading.value = false
  }
}

// 编辑管理员
const editVisible = ref(false)
const editLoading = ref(false)

const viewVisible = ref(false)
const viewData = ref(null)
const editForm = ref({
  nickname: '',
  phone: '',
  email: '',
  role: 2,
})
const editAdminId = ref(null)
const editOriginalRole = ref(null)

const editRef = ref(null)

const handleEnable = async (row) => {
  try {
    await ElMessageBox.confirm(`启用管理员「${row.username}」？`, '确认启用', {
      confirmButtonText: '确认启用',
      cancelButtonText: '取消',
      type: 'info',
    })
    await enableAdmin(row.id)
    toast('启用成功')
    await fetchAdmins()
  } catch { /* 取消或失败静默 */ }
}

const handleDisable = async (row) => {
  if (row.id === store.adminInfo?.id) {
    toast('不能禁用当前管理员', 'warning')
    return
  }
  try {
    await ElMessageBox.confirm(`禁用管理员「${row.username}」后其将立即下线，确定？`, '确认禁用', {
      confirmButtonText: '确认禁用',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await disableAdmin(row.id)
    toast('禁用成功')
    await fetchAdmins()
  } catch { /* 取消或失败静默 */ }
}

const handleResetPassword = async (row) => {
  if (row.id === store.adminInfo?.id) {
    toast('不能重置当前管理员的密码', 'warning')
    return
  }
  try {
    await ElMessageBox.confirm(
      `将管理员「${row.username}」的密码重置为默认密码 123456，重置后其将立即下线，确定？`,
      '确认重置密码',
      {
        confirmButtonText: '确认重置',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await resetAdminPassword(row.id)
    toast('密码已重置为 123456')
    await fetchAdmins()
  } catch { /* 取消或失败静默 */ }
}

const handleDelete = async (row) => {
  if (row.id === store.adminInfo?.id) {
    toast('不能删除当前管理员', 'warning')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除管理员「${row.username}」？此操作不可撤销。`, '确认删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteAdmin(row.id)
    toast('删除成功')
    await fetchAdmins()
  } catch {
    // 取消或失败均静默
  }
}

const handleEdit = (row) => {
  if (!isSuperAdmin.value && row.id !== store.adminInfo?.id) {
    toast('只能修改当前管理员的信息', 'warning')
    return
  }
  editAdminId.value = row.id
  editOriginalRole.value = row.role
  editForm.value = {
    nickname: row.nickname || '',
    phone: row.phone || '',
    email: row.email || '',
    role: row.role || 2,
  }
  editVisible.value = true
}

const handleView = (row) => {
  viewData.value = row
  viewVisible.value = true
}

const submitEdit = async () => {
  const valid = await editRef.value.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    if (editAdminId.value && editAdminId.value !== store.adminInfo?.id) {
      await updateAdminInfoById(editAdminId.value, editForm.value)
    } else {
      await updateAdminInfo(editForm.value)
    }
    toast('更新成功')
    editVisible.value = false
    await fetchAdmins()
  } catch {
  } finally {
    editLoading.value = false
  }
}

const statusMap = {
  0: { text: '禁用', type: 'danger' },
  1: { text: '启用', type: 'success' },
}

const roleMap = {
  1: { text: '超级管理员', type: 'danger' },
  2: { text: '普通管理员', type: 'primary' },
}

const isSuperAdmin = computed(() => store.adminInfo?.role === 1)

const canEditRole = computed(() =>
  isSuperAdmin.value &&
  editAdminId.value &&
  editAdminId.value !== store.adminInfo?.id &&
  editOriginalRole.value !== 1
)

onMounted(fetchAdmins)
</script>

<template>
  <div class="page">
    <h1 class="page__title">管理员列表</h1>
    <p class="page__desc">管理系统管理员账号与信息</p>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索用户名 / 昵称" clearable class="toolbar__input"
          @keyup.enter="fetchAdmins" />
        <el-button type="primary" @click="fetchAdmins">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button v-if="isSuperAdmin" type="primary" class="toolbar__add" @click="handleCreate">
          + 添加管理员
        </el-button>
      </div>

      <el-table :data="filteredAdmins" v-loading="loading" stripe class="admins-table" empty-text="暂无管理员数据">
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
        <el-table-column prop="username" label="登录账号" min-width="120" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="120" align="center" />
        <el-table-column prop="phone" label="手机号" min-width="130" align="center">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200" align="center">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="roleMap[row.role]?.type || 'info'" size="small">
              {{ roleMap[row.role]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" align="center">
              {{ statusMap[row.status]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" align="center">
          <template #default="{ row }">
            {{ row.lastLoginTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginIp" label="最后登录IP" width="150" align="center">
          <template #default="{ row }">
            {{ row.lastLoginIp || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <template v-if="isSuperAdmin">
                <el-button v-if="row.status === 0" type="success" link size="small" @click="handleEnable(row)">
                  启用
                </el-button>
                <el-button v-else type="warning" link size="small" @click="handleDisable(row)">
                  禁用
                </el-button>
                <el-button type="primary" link size="small" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button type="danger" link size="small" @click="handleDelete(row)">
                  删除
                </el-button>
                <el-button type="warning" link size="small" @click="handleResetPassword(row)">
                  重置密码
                </el-button>
              </template>
              <el-button v-else type="info" link size="small" @click="handleView(row)">
                查看
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>

  <!-- 添加管理员弹窗 -->
  <el-dialog v-model="createVisible" title="添加管理员" width="520px" :close-on-click-modal="false">
    <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="80px" label-position="right">
      <el-form-item label="登录账号" prop="username">
        <el-input v-model="createForm.username" placeholder="3-20 个字符" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="createForm.password" type="password" show-password placeholder="至少 6 位" />
      </el-form-item>
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="createForm.nickname" placeholder="可选" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="createForm.phone" placeholder="可选" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="createForm.email" placeholder="可选" />
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="createForm.role" style="width:100%">
          <el-option :value="1" label="超级管理员" />
          <el-option :value="2" label="普通管理员" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="createLoading" @click="submitCreate">
        确认创建
      </el-button>
    </template>
  </el-dialog>

  <!-- 编辑管理员弹窗 -->
  <el-dialog v-model="editVisible" :title="editAdminId && editAdminId !== store.adminInfo?.id ? '编辑管理员' : '编辑信息'"
    width="520px" :close-on-click-modal="false">
    <el-form ref="editRef" :model="editForm" label-width="80px" label-position="right">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="editForm.nickname" placeholder="3-20 个字符" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="editForm.phone" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="editForm.email" />
      </el-form-item>
      <el-form-item v-if="canEditRole" label="角色" prop="role">
        <el-select v-model="editForm.role" style="width:100%">
          <el-option :value="1" label="超级管理员" />
          <el-option :value="2" label="普通管理员" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="editLoading" @click="submitEdit">
        确认更新
      </el-button>
    </template>
  </el-dialog>

  <!-- 查看管理员弹窗 -->
  <el-dialog v-model="viewVisible" title="管理员详情" width="520px" :close-on-click-modal="false">
    <div class="detail-items">
      <div class="detail-item">
        <span class="detail-item__label">昵称</span>
        <span class="detail-item__value">{{ viewData?.nickname || '-' }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-item__label">手机号</span>
        <span class="detail-item__value">{{ viewData?.phone || '-' }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-item__label">邮箱</span>
        <span class="detail-item__value">{{ viewData?.email || '-' }}</span>
      </div>
      <div class="detail-item">
        <span class="detail-item__label">角色</span>
        <el-tag :type="viewData?.role === 1 ? 'danger' : 'primary'" size="small">
          {{ viewData?.role === 1 ? '超级管理员' : '普通管理员' }}
        </el-tag>
      </div>
      <div class="detail-item">
        <span class="detail-item__label">状态</span>
        <el-tag :type="viewData?.status === 1 ? 'success' : 'danger'" size="small">
          {{ viewData?.status === 1 ? '启用' : '禁用' }}
        </el-tag>
      </div>
      <div class="detail-item">
        <span class="detail-item__label">创建时间</span>
        <span class="detail-item__value">{{ viewData?.createTime || '-' }}</span>
      </div>
    </div>
    <template #footer>
      <el-button @click="viewVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
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
    width: 280px;
  }

  &__add {
    margin-left: auto;
  }
}

.admins-table {
  width: 100%;

  :deep(.cell) {
    cursor: context-menu;
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

.detail-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;

  &__label {
    width: 80px;
    color: #64748b;
    font-size: 14px;
    flex-shrink: 0;
  }

  &__value {
    color: #0f172a;
    font-size: 14px;
  }
}
</style>
