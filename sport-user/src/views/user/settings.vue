<script setup>
import { ref, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { updatePassword, getUserInfo, updateUserInfo, uploadAvatar } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'
import { encryptPassword } from '@/utils/rsa'

const router = useRouter()
const store = useCounterStore()

const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const uploadingAvatar = ref(false)

const editForm = reactive({ nickname: '', email: '', phone: '' })

function openEditDialog() {
  editForm.nickname = store.userInfo.nickname || ''
  editForm.email = store.userInfo.email || ''
  editForm.phone = store.userInfo.phone || ''
  editDialogVisible.value = true
}

async function fetchUserInfo() {
  try {
    const res = await getUserInfo()
    if (res.data.code === 200) {
      store.setUserInfo(res.data.data)
    }
  } catch (e) { console.error(e) }
}

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploadingAvatar.value = true
  try {
    const res = await uploadAvatar(file)
    if (res.data.code === 200) {
      toast('头像更新成功', 'success')
      await fetchUserInfo()
    } else {
      toast(res.data.msg || '上传失败', 'error')
    }
  } catch {
    toast('头像上传失败', 'error')
  }
  uploadingAvatar.value = false
}

async function submitEdit() {
  const payload = {}
  const nickname = editForm.nickname.trim()
  const email = editForm.email.trim()
  const phone = editForm.phone.trim()

  if (nickname) payload.nickname = nickname
  if (email) payload.email = email
  if (phone) payload.phone = phone

  if (Object.keys(payload).length === 0) {
    toast('请至少填写一项', 'warning')
    return
  }

  editSubmitting.value = true
  try {
    const res = await updateUserInfo(payload)
    if (res.data.code === 200) {
      toast('资料更新成功', 'success')
      editDialogVisible.value = false
      await fetchUserInfo()
    } else {
      toast(res.data.message || '更新失败', 'error')
    }
  } catch {
    toast('资料更新失败', 'error')
  }
  editSubmitting.value = false
}

const passwordDialogVisible = ref(false)
const pwdSubmitting = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

function openPwdDialog() {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordDialogVisible.value = true
  nextTick(() => passwordFormRef.value?.clearValidate())
}

async function submitPassword() {
  if (!passwordFormRef.value) return
  try { await passwordFormRef.value.validate() } catch { return }
  pwdSubmitting.value = true
  try {
    const res = await updatePassword({
      oldPassword: encryptPassword(passwordForm.value.oldPassword),
      newPassword: encryptPassword(passwordForm.value.newPassword),
      confirmPassword: encryptPassword(passwordForm.value.confirmPassword)
    })
    if (res.data.code === 200) {
      store.clearToken()
      router.push({ name: 'index' })
      toast('密码修改成功，请重新登录', 'success')
      passwordDialogVisible.value = false
    } else {
      toast(res.data.message || '修改失败', 'error')
    }
  } catch {
    toast('密码修改失败', 'error')
  }
  pwdSubmitting.value = false
}
</script>

<template>
  <div class="glass-card">
    <div class="glass-card-header">
      <h3><i class="ph ph-gear"></i> 账号设置</h3>
    </div>
    <div class="glass-card-body">
      <div class="settings-list">
        <div class="setting-row glass-inner" @click="openEditDialog">
          <div class="setting-icon"><i class="ph ph-user-circle"></i></div>
          <div class="setting-info">
            <h4>编辑资料</h4>
            <p>修改昵称、邮箱、手机号、头像</p>
          </div>
          <i class="ph ph-caret-right setting-arrow"></i>
        </div>
        <div class="setting-row glass-inner" @click="openPwdDialog">
          <div class="setting-icon"><i class="ph ph-lock-key"></i></div>
          <div class="setting-info">
            <h4>修改密码</h4>
            <p>定期更换密码保障账号安全</p>
          </div>
          <i class="ph ph-caret-right setting-arrow"></i>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="editDialogVisible" title="编辑个人资料" width="440px" :lock-scroll="false" destroy-on-close>
    <div class="edit-dialog-body">
      <div class="avatar-edit-row">
        <div class="avatar-edit-preview">
          <img v-if="store.userInfo.avatar" :src="store.userInfo.avatar" alt="头像" />
          <i v-else class="ph ph-user"></i>
        </div>
        <label class="avatar-upload-btn">
          <i class="ph ph-camera"></i>
          {{ uploadingAvatar ? '上传中...' : '更换头像' }}
          <input type="file" accept="image/*" :disabled="uploadingAvatar" hidden @change="handleAvatarUpload" />
        </label>
      </div>
      <div class="form-group">
        <label>昵称</label>
        <input v-model="editForm.nickname" type="text" placeholder="请输入昵称" maxlength="20" />
      </div>
      <div class="form-group">
        <label>邮箱</label>
        <input v-model="editForm.email" type="email" placeholder="请输入邮箱" />
      </div>
      <div class="form-group">
        <label>手机号</label>
        <input v-model="editForm.phone" type="text" placeholder="请输入手机号" maxlength="11" />
      </div>
    </div>
    <template #footer>
      <el-button type="default" @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit" :loading="editSubmitting">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px" :lock-scroll="false" destroy-on-close>
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" size="large">
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" autocomplete="off" placeholder="请输入当前密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password autocomplete="off" placeholder="至少6位" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password autocomplete="off" placeholder="再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="default" @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPassword" :loading="pwdSubmitting">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.glass-card {
  @include solid-card;
  border-radius: $radius-xl;
  margin-bottom: 24px;
  overflow: hidden;

  .glass-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 0.5px solid $border-subtle;

    h3 {
      font-size: 18px;
      font-weight: 700;
      color: $text-primary;
      display: flex;
      align-items: center;
      gap: 8px;
      i { color: $accent-energy; }
    }
  }

  .glass-card-body {
    padding: 20px 24px;
  }
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.glass-inner {
  background: $bg-subtle;
  border: 0.5px solid $border-subtle;
}

.setting-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.5s $transition-premium;

  &:hover {
    background: $bg-card-hover;
    border-color: $border-accent;
    transform: translateX(6px);
  }

  .setting-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    background: $accent-energy-soft;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: $accent-energy;
    flex-shrink: 0;
  }

  .setting-info {
    flex: 1;

    h4 { font-size: 15px; font-weight: 600; color: $text-primary; margin-bottom: 2px; }
    p { font-size: 13px; color: $text-muted; }
  }

  .setting-arrow {
    font-size: 16px;
    color: $text-muted;
    transition: all 0.3s $transition-premium;
  }
}

.edit-dialog-body {
  .avatar-edit-row {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;

    .avatar-edit-preview {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      background: linear-gradient(135deg, $accent-energy, $accent-gradient-light);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: #fff;
      overflow: hidden;

      img { width: 100%; height: 100%; object-fit: cover; }
    }

    .avatar-upload-btn {
      @include btn-base;
      padding: 8px 16px;
      background: $bg-card-hover;
      border: 0.5px solid $border-subtle;
      border-radius: 10px;
      font-size: 13px;
      color: $text-secondary;
      display: inline-flex;
      align-items: center;
      gap: 6px;

      &:hover {
        background: $bg-elevated;
        color: $text-primary;
      }
    }
  }

  .form-group {
    margin-bottom: 18px;

    label {
      display: block;
      font-size: 13px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 8px;
    }

    input {
      width: 100%;
      padding: 12px 16px;
      background: $bg-card;
      border: 0.5px solid $border-subtle;
      border-radius: 10px;
      font-size: 14px;
      color: $text-primary;
      outline: none;
      transition: border-color 0.3s $transition-premium;
      box-sizing: border-box;

      &:focus { border-color: $accent-energy; }
      &::placeholder { color: $text-muted; }
    }
  }
}
</style>
