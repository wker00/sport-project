<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getAdminInfo, updateAdminInfo, uploadAdminAvatar, updateAdminPassword } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'
import { encryptPassword } from '@/utils/rsa'

const store = useCounterStore()
const router = useRouter()

const admin = ref(null)
const loading = ref(true)
const submitting = ref(false)
const editable = ref(false)
const formRef = ref(null)

const form = ref({ nickname: '', phone: '', email: '' })

const avatarUrl = ref('')
const avatarFile = ref(null)

const rules = {
  nickname: [
    { min: 3, max: 20, message: '昵称 3-20 个字符', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
}

const pwdVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdRef = ref(null)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const statusTag = computed(() => {
  if (!admin.value) return {}
  return admin.value.status === 1
    ? { type: 'success', text: '正常' }
    : { type: 'danger', text: '禁用' }
})

const fetchProfile = async () => {
  loading.value = true
  try {
    const res = await getAdminInfo()
    admin.value = res.data.data
    avatarUrl.value = admin.value.avatar || ''
    form.value = {
      nickname: admin.value.nickname || '',
      phone: admin.value.phone || '',
      email: admin.value.email || '',
    }
  } catch {
    admin.value = null
  } finally {
    loading.value = false
  }
}

const handleAvatarChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return false
  if (!file.type.startsWith('image/')) { toast('只能上传图片文件', 'warning'); return false }
  if (file.size / 1024 / 1024 > 10) { toast('图片大小不能超过 10MB', 'warning'); return false }
  avatarFile.value = file
  avatarUrl.value = URL.createObjectURL(file)
  return false
}

const submitAvatar = async () => {
  if (!avatarFile.value) return
  try {
    const res = await uploadAdminAvatar(avatarFile.value)
    const url = res.data.data || res.data.message
    if (!url) {
      toast('头像上传失败', 'error')
      return
    }
    avatarUrl.value = url
    avatarFile.value = null
    store.setAdminInfo({ ...store.adminInfo, avatar: url })
    toast('头像更新成功')
  } catch {
    avatarUrl.value = admin.value?.avatar || ''
    avatarFile.value = null
  }
}

const cancelEdit = () => {
  form.value = {
    nickname: admin.value?.nickname || '',
    phone: admin.value?.phone || '',
    email: admin.value?.email || '',
  }
  editable.value = false
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateAdminInfo(form.value)
    store.setAdminInfo({ ...store.adminInfo, ...form.value })
    toast('保存成功')
    editable.value = false
    await fetchProfile()
  } catch (e) {
    toast(e.response?.data?.message || '保存失败', 'error')
  } finally {
    submitting.value = false
  }
}

const handleSubmitPwd = async () => {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  pwdSubmitting.value = true
  try {
    await updateAdminPassword({
      oldPassword: encryptPassword(pwdForm.value.oldPassword),
      newPassword: encryptPassword(pwdForm.value.newPassword),
      confirmPassword: encryptPassword(pwdForm.value.confirmPassword),
    })
    toast('密码修改成功，请重新登录', 'success')
    pwdVisible.value = false
    store.clearToken()
    router.push('/admin/login')
  } catch {
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(fetchProfile)
</script>

<template>
  <div class="page">
    <h1 class="page__title">个人中心</h1>
    <p class="page__desc">管理您的个人信息和头像</p>

    <div v-loading="loading" class="profile-layout">
      <div class="profile-layout__left">
        <div class="card card--avatar">
          <div class="avatar-section">
            <el-avatar :size="96" :src="avatarUrl" class="avatar-section__img">
              <el-icon :size="40">
                <UserFilled />
              </el-icon>
            </el-avatar>
            <div class="avatar-section__name">{{ admin?.nickname || admin?.username }}</div>
            <div class="avatar-section__uname">@{{ admin?.username }}</div>
          </div>
          <div class="avatar-actions">
            <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleAvatarChange">
              <el-button type="primary" plain size="small">{{ avatarUrl? '更换头像' : '上传头像'}}</el-button>
            </el-upload>
            <el-button v-if="avatarFile" type="primary" size="small" :loading="submitting" @click="submitAvatar">
              确认上传
            </el-button>
          </div>
        </div>

        <div class="card card--meta">
          <div class="meta-row">
            <span class="meta-row__label">用户名</span>
            <span class="meta-row__value">{{ admin?.username }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-row__label">角色</span>
            <el-tag :type="admin?.role === 1 ? 'danger' : 'primary'" size="small">
              {{ admin?.role === 1 ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </div>
          <div class="meta-row">
            <span class="meta-row__label">状态</span>
            <el-tag :type="statusTag.type" size="small">{{ statusTag.text }}</el-tag>
          </div>
          <div class="meta-row">
            <span class="meta-row__label">上次登录</span>
            <span class="meta-row__value">{{ admin?.lastLoginTime || '-' }}</span>
          </div>
          <div class="meta-row">
            <span class="meta-row__label">创建时间</span>
            <span class="meta-row__value">{{ admin?.createTime || '-' }}</span>
          </div>
        </div>
      </div>

      <div class="profile-layout__right">
        <div class="card">
          <h2 class="card__title">编辑资料</h2>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" label-position="right">
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="3-20 个字符" maxlength="20" show-word-limit :disabled="!editable" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" :disabled="!editable" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱地址" maxlength="100" :disabled="!editable" />
            </el-form-item>
            <el-form-item>
              <el-button v-if="!editable" type="primary" @click="editable = true">修改</el-button>
              <template v-else>
                <el-button type="primary" :loading="submitting" @click="submitForm">保存修改</el-button>
                <el-button @click="cancelEdit">取消</el-button>
              </template>
            </el-form-item>
          </el-form>
        </div>

        <div class="card">
          <h2 class="card__title">安全设置</h2>
          <el-button type="warning" plain @click="pwdVisible = true">修改密码</el-button>
          <p class="pwd-hint">修改密码后将立即退出登录，需要重新登录</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="pwdVisible" title="修改密码" width="420px" :close-on-click-modal="false">
    <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="80px" label-position="right">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="pwdVisible = false">取消</el-button>
      <el-button type="primary" :loading="pwdSubmitting" @click="handleSubmitPwd">确认修改</el-button>
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

.profile-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;

  &__left {
    width: 320px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  &__right {
    flex: 1;
    min-width: 0;
    max-width: 500px;
  }
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #0f172a;
    margin: 0 0 20px;
  }
}

.avatar-section {
  text-align: center;
  padding: 8px 0 16px;

  &__img {
    background: rgba(#ff6b35, 0.12);
    color: #ff6b35;
    margin-bottom: 12px;
  }

  &__name {
    font-size: 16px;
    font-weight: 600;
    color: #0f172a;
  }

  &__uname {
    font-size: 13px;
    color: $text-muted;
    margin-top: 2px;
  }
}

.pwd-hint {
  font-size: 12px;
  color: #94a3b8;
  margin: 8px 0 0;
}

.avatar-actions {
  display: flex;
  justify-content: center;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;

  &+& {
    border-top: 1px solid #f1f5f9;
  }

  &__label {
    font-size: 13px;
    color: $text-muted;
  }

  &__value {
    font-size: 13px;
    color: #334155;
    font-weight: 500;
  }
}
</style>
