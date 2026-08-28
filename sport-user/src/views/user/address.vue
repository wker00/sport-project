<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/manager'
import { toast } from '@/utils/toast'

const addresses = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const emptyForm = () => ({
  receiverName: '', phone: '', province: '', city: '', district: '', address: '', isDefault: 0
})

const form = reactive(emptyForm())

async function fetchAddresses() {
  loading.value = true
  try {
    const res = await getAddressList()
    if (res.data.code === 200) addresses.value = res.data.data || []
  } catch { addresses.value = [] }
  loading.value = false
}

function openAddDialog() {
  editingId.value = null
  Object.assign(form, emptyForm())
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate?.())
}

function openEditDialog(a) {
  editingId.value = a.id
  Object.assign(form, {
    receiverName: a.receiverName, phone: a.phone,
    province: a.province, city: a.city, district: a.district,
    address: a.address, isDefault: a.isDefault || 0
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate?.())
}

function validateForm() {
  if (!form.receiverName.trim()) { toast('请输入收件人姓名', 'warning'); return false }
  if (!form.phone.trim() || !/^1\d{10}$/.test(form.phone.trim())) { toast('请输入正确的手机号', 'warning'); return false }
  if (!form.province.trim()) { toast('请输入省份', 'warning'); return false }
  if (!form.city.trim()) { toast('请输入城市', 'warning'); return false }
  if (!form.district.trim()) { toast('请输入区县', 'warning'); return false }
  if (!form.address.trim()) { toast('请输入详细地址', 'warning'); return false }
  return true
}

async function submitForm() {
  if (!validateForm() || submitting.value) return
  submitting.value = true
  try {
    const payload = {
      receiverName: form.receiverName.trim(),
      phone: form.phone.trim(),
      province: form.province.trim(),
      city: form.city.trim(),
      district: form.district.trim(),
      address: form.address.trim(),
      isDefault: form.isDefault
    }
    let res
    if (editingId.value) {
      res = await updateAddress(editingId.value, payload)
    } else {
      res = await addAddress(payload)
    }
    if (res.data.code === 200) {
      toast(editingId.value ? '地址已更新' : '地址添加成功', 'success')
      dialogVisible.value = false
      await fetchAddresses()
    } else {
      toast(res.data.message || '操作失败', 'error')
    }
  } catch { toast('操作失败', 'error') }
  submitting.value = false
}

async function handleDelete(a) {
  try {
    await ElMessageBox.confirm(`确定删除「${a.receiverName}」的收货地址？`, '删除地址', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', lockScroll: false,
    })
    const res = await deleteAddress(a.id)
    if (res.data.code === 200) {
      toast('已删除', 'success')
      await fetchAddresses()
    } else {
      toast(res.data.message || '删除失败', 'error')
    }
  } catch (e) { console.error(e) }
}

async function handleSetDefault(a) {
  if (a.isDefault === 1) return
  try {
    const res = await setDefaultAddress(a.id)
    if (res.data.code === 200) {
      toast('已设为默认地址', 'success')
      await fetchAddresses()
    } else {
      toast(res.data.message || '操作失败', 'error')
    }
  } catch { toast('操作失败', 'error') }
}

function fullAddress(a) {
  return [a.province, a.city, a.district, a.address].filter(Boolean).join(' ')
}

onMounted(fetchAddresses)
</script>

<template>
  <div class="glass-card">
    <div class="glass-card-header">
      <h3><i class="ph ph-map-pin"></i> 收货地址</h3>
      <el-button type="primary" class="btn-trail-sm" @click="openAddDialog">
        <i class="ph ph-plus"></i>添加地址
      </el-button>
    </div>
    <div class="glass-card-body">
      <div v-if="loading" class="loading-state"><i class="ph ph-spinner ph-spin"></i> 加载中...</div>
      <div v-else-if="addresses.length === 0" class="empty-state">
        <i class="ph ph-map-pin"></i>
        <p>暂无收货地址</p>
      </div>
      <div v-else class="address-list">
        <div v-for="a in addresses" :key="a.id" class="address-card glass-inner">
          <div class="address-top">
            <div class="address-info">
              <span class="addr-name">{{ a.receiverName }}</span>
              <span class="addr-phone">{{ a.phone }}</span>
              <span v-if="a.isDefault === 1" class="default-badge">默认</span>
            </div>
            <div class="address-detail">{{ fullAddress(a) }}</div>
          </div>
          <div class="address-actions">
            <button v-if="a.isDefault !== 1" class="action-btn" @click="handleSetDefault(a)">
              <i class="ph ph-check-circle"></i> 设为默认
            </button>
            <button class="action-btn" @click="openEditDialog(a)">
              <i class="ph ph-pencil-line"></i> 编辑
            </button>
            <button class="action-btn danger" @click="handleDelete(a)">
              <i class="ph ph-trash"></i> 删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '添加地址'" width="480px" :lock-scroll="false" destroy-on-close>
    <div class="address-form">
      <div class="form-row">
        <div class="form-group flex-1">
          <label>收件人 <span class="required">*</span></label>
          <input v-model="form.receiverName" placeholder="请输入收件人姓名" maxlength="20" />
        </div>
        <div class="form-group flex-1">
          <label>手机号 <span class="required">*</span></label>
          <input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-group flex-1">
          <label>省份 <span class="required">*</span></label>
          <input v-model="form.province" placeholder="如：广东省" />
        </div>
        <div class="form-group flex-1">
          <label>城市 <span class="required">*</span></label>
          <input v-model="form.city" placeholder="如：深圳市" />
        </div>
        <div class="form-group flex-1">
          <label>区县 <span class="required">*</span></label>
          <input v-model="form.district" placeholder="如：南山区" />
        </div>
      </div>
      <div class="form-group">
        <label>详细地址 <span class="required">*</span></label>
        <textarea v-model="form.address" placeholder="街道、门牌号、楼层等" rows="2" maxlength="200"></textarea>
      </div>
      <div class="form-group checkbox-row">
        <label class="checkbox-label">
          <input type="checkbox" v-model="form.isDefault" :true-value="1" :false-value="0" />
          <span class="checkbox-custom"><i v-if="form.isDefault === 1" class="ph ph-check"></i></span>
          设为默认地址
        </label>
      </div>
    </div>
    <template #footer>
      <el-button type="default" class="dialog-btn" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" class="dialog-btn" :disabled="submitting" @click="submitForm">
        <i class="ph ph-check-circle"></i> {{ submitting ? '保存中...' : '保存' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

h1,h2,h3{
  cursor: context-menu;
}

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

.btn-trail-sm {
  width: 100px;
  height: 40px;
  background: $accent-energy;
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.4s;

  &:hover {
    box-shadow: 0 6px 24px $accent-energy-glow;
    transform: translateY(-2px);
  }
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: $text-muted;
  font-size: 14px;
  i { margin-right: 8px; }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  i { font-size: 48px; color: $text-muted; margin-bottom: 16px; }
  p { font-size: 14px; color: $text-muted; }
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  padding: 20px 24px;
  border-radius: 16px;
  transition: all 0.5s $transition-premium;
  cursor: context-menu;

  &:hover {
    border-color: $border-accent;
    transform: translateY(-2px);
  }
}

.glass-inner {
  background: $bg-subtle;
  border: 0.5px solid $border-subtle;
}

.address-top { margin-bottom: 16px; }

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .addr-name { font-size: 15px; font-weight: 700; color: $text-primary; }
  .addr-phone { font-size: 14px; color: $text-secondary; }
  .default-badge {
    padding: 2px 8px;
    border-radius: 4px;
    background: $vitality-soft;
    color: $accent-vitality;
    font-size: 11px;
    font-weight: 600;
  }
}

.address-detail {
  font-size: 13px;
  color: $text-muted;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 0.5px solid $border-subtle;
}

.action-btn {
  @include btn-base;
  padding: 6px 14px;
  background: transparent;
  border: 0.5px solid transparent;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  color: $text-secondary;
  display: flex;
  align-items: center;
  gap: 6px;

  &:hover {
    background: $bg-card-hover;
    border-color: $border-subtle;
  }

  &.danger:hover {
    background: rgba($color-danger, 0.1);
    color: $color-danger;
  }
}

.address-form {
  .form-row { display: flex; gap: 12px; }
  .flex-1 { flex: 1; }

  .form-group {
    margin-bottom: 18px;

    label {
      display: block;
      font-size: 13px;
      font-weight: 600;
      color: $text-secondary;
      margin-bottom: 8px;
      .required { color: $color-danger; }
    }

    input, textarea {
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
      font-family: inherit;

      &:focus { border-color: $accent-energy; }
      &::placeholder { color: $text-muted; }
    }

    textarea { resize: none; }
  }

  .checkbox-row { margin-bottom: 0; }

  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;

    input[type="checkbox"] { display: none; }
  }

  .checkbox-custom {
    width: 20px;
    height: 20px;
    border-radius: 6px;
    border: 1.5px solid $border-subtle;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #fff;
    transition: all 0.3s $transition-premium;
  }

  input[type="checkbox"]:checked + .checkbox-custom {
    background: $accent-energy;
    border-color: $accent-energy;
  }
}

.dialog-btn {
  width: 100px;
  height: 40px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
}
</style>
