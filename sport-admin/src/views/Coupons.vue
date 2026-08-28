<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getCouponList, getCouponDetail, createCoupon, updateCoupon, deleteCoupon, getCouponRecords } from '@/api/manager'
import { toast } from '@/utils/toast'

const keyword = ref('')
const coupons = ref([])
const loading = ref(false)

const couponStatus = (row) => {
  if (new Date(row.endTime).getTime() < Date.now()) return { text: '已过期', type: 'danger' }
  if ((row.stock ?? 0) <= 0) return { text: '已领完', type: 'info' }
  return { text: '正常', type: 'success' }
}

const typeMap = { 1: '满减券', 2: '折扣券' }

const filteredCoupons = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return coupons.value
  return coupons.value.filter((c) => c.name && c.name.toLowerCase().includes(kw))
})

const fetchCoupons = async () => {
  loading.value = true
  try {
    const res = await getCouponList()
    coupons.value = res.data.data || []
  } catch {
    coupons.value = []
  } finally {
    loading.value = false
  }
}

const handleReset = () => { keyword.value = '' }

// 创建
const createVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({
  name: '', type: 1, value: null, minAmount: 0, pointsCost: 0, stock: null,
  startTime: '', endTime: '',
})
// 创建表单验证规则（静态）
const createRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  value: [{ required: true, message: '请输入面值', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

// 折扣选项
const discountOptions = [9.5, 9, 8.5, 8, 7.5, 7, 6.5, 6, 5.5, 5, 4.5, 4, 3.5, 3, 2.5, 2, 1.5, 1]

// 动态 label / placeholder / min / max
const createValueLabel = computed(() => createForm.value.type === 2 ? '折扣' : '面值')
const createValuePlaceholder = computed(() => createForm.value.type === 2 ? '如 8.5 = 8.5折' : '如 10.00')

// 切换类型时重置 value
watch(() => createForm.value.type, () => { createForm.value.value = null })

const createRef = ref(null)

const handleCreate = () => {
  createForm.value = { name: '', type: 1, value: null, minAmount: 0, pointsCost: 0, stock: null, startTime: '', endTime: '' }
  createVisible.value = true
  nextTick(() => { createRef.value?.clearValidate() })
}

const submitCreate = async () => {
  if (createForm.value.type === 2 && (createForm.value.value == null || createForm.value.value < 1 || createForm.value.value > 9.9)) {
    toast('请选择优惠券信息', 'error')
    return
  }
  const valid = await createRef.value.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    await createCoupon(createForm.value)
    toast('创建成功')
    createVisible.value = false
    await fetchCoupons()
  } catch {
  } finally {
    createLoading.value = false
  }
}

// 编辑
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({ name: '', type: 1, value: null, minAmount: 0, pointsCost: 0, stock: null, startTime: '', endTime: '' })
// 编辑表单验证规则（静态）
const editRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  value: [{ required: true, message: '请输入面值', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
}

// 动态 label / placeholder / min / max
const editValueLabel = computed(() => editForm.value.type === 2 ? '折扣' : '面值')
const editValuePlaceholder = computed(() => editForm.value.type === 2 ? '如 8.5 = 8.5折' : '如 10.00')

// 编辑弹窗：切换类型时重置 value
watch(() => editForm.value.type, () => { editForm.value.value = null })

const editRef = ref(null)

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name || '',
    type: row.type ?? 1,
    value: row.value ?? null,
    minAmount: row.minAmount ?? 0,
    pointsCost: row.pointsCost ?? 0,
    stock: row.stock ?? null,
    startTime: row.startTime || '',
    endTime: row.endTime || '',
  }
  editVisible.value = true
  nextTick(() => { editRef.value?.clearValidate() })
}

const submitEdit = async () => {
  if (editForm.value.type === 2 && (editForm.value.value == null || editForm.value.value < 1 || editForm.value.value > 9.9)) {
    toast('请选择 1～9.9 的折扣', 'error')
    return
  }
  const valid = await editRef.value.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    await updateCoupon(editForm.value.id, editForm.value)
    toast('更新成功')
    editVisible.value = false
    await fetchCoupons()
  } catch {
  } finally {
    editLoading.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除优惠券「${row.name}」？`, '确认删除', {
      confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning',
    })
    await deleteCoupon(row.id)
    toast('删除成功')
    await fetchCoupons()
  } catch { /* 静默 */ }
}

// 发放记录
const recordsVisible = ref(false)
const recordsLoading = ref(false)
const records = ref([])
const recordTitle = ref('')

const handleRecords = async (row) => {
  recordTitle.value = `「${row.name}」发放记录`
  records.value = []
  recordsVisible.value = true
  recordsLoading.value = true
  try {
    const res = await getCouponRecords(row.id)
    records.value = res.data.data || []
  } catch {
    records.value = []
  } finally {
    recordsLoading.value = false
  }
}

// 查看详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const handleView = async (row) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await getCouponDetail(row.id)
    detailData.value = res.data.data
  } catch {
    detailData.value = null
  } finally {
    detailLoading.value = false
  }
}

const recordStatusMap = {
  0: { text: '未使用', type: 'primary' },
  1: { text: '已使用', type: 'success' },
  2: { text: '已过期', type: 'info' },
}

onMounted(fetchCoupons)
</script>

<template>
  <div class="page">
    <h1 class="page__title">优惠券管理</h1>
    <p class="page__desc">管理优惠券模板与发放记录</p>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索优惠券名称" clearable class="toolbar__input" />
        <el-button type="primary" @click="fetchCoupons">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" class="toolbar__add" @click="handleCreate">
          + 创建优惠券
        </el-button>
      </div>

      <el-table :data="filteredCoupons" v-loading="loading" stripe class="coupons-table" empty-text="暂无优惠券数据">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="name" label="名称" min-width="160" align="center" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            {{ typeMap[row.type] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="面值" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.type === 2">{{ row.value }}折</span>
            <span v-else>¥{{ row.value?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最低消费" width="110" align="center">
          <template #default="{ row }">
            ¥{{ (row.minAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="积分兑换成本" width="90" align="center">
          <template #default="{ row }">
            {{ row.pointsCost ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" align="center" />
        <el-table-column label="有效期" min-width="260" align="center">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="couponStatus(row).type" size="small">
              {{ couponStatus(row).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="操作" width="250" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="success" link size="small" @click="handleView(row)">
                查看
              </el-button>
              <el-button type="primary" link size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                删除
              </el-button>
              <el-button type="info" link size="small" @click="handleRecords(row)">
                记录
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建弹窗 -->
    <el-dialog v-model="createVisible" title="创建优惠券" width="560px" :close-on-click-modal="false">
      <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="110px" label-position="right">
        <el-form-item label="类型" prop="type">
          <el-select v-model="createForm.type" style="width:100%">
            <el-option :value="1" label="满减券" />
            <el-option :value="2" label="折扣券" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="createForm.name" placeholder="如 满100减10" />
        </el-form-item>
        <el-form-item :label="createValueLabel" prop="value">
          <el-input-number v-if="createForm.type === 1" v-model="createForm.value" :min="0"
            :precision="2" :step="1" style="width:100%" placeholder="如 10.00" />
          <el-select v-else v-model="createForm.value" style="width:100%" placeholder="请选择折扣">
            <el-option v-for="d in discountOptions" :key="d" :value="d" :label="d + ' 折'" />
          </el-select>
        </el-form-item>
        <el-form-item label="最低消费时可用" prop="minAmount">
          <el-input-number v-model="createForm.minAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="积分兑换成本" prop="pointsCost">
          <el-input-number v-model="createForm.pointsCost" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="createForm.stock" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="createForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm"
            placeholder="选择开始时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="createForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm"
            placeholder="选择结束时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">
          确认创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑优惠券" width="560px" :close-on-click-modal="false">
      <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="100px" label-position="right">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="editForm.type" style="width:100%">
            <el-option :value="1" label="满减券" />
            <el-option :value="2" label="折扣券" />
          </el-select>
        </el-form-item>
        <el-form-item :label="editValueLabel" prop="value">
          <el-input-number v-if="editForm.type === 1" v-model="editForm.value" :min="0"
            :precision="2" :step="1" style="width:100%" placeholder="如 10.00" />
          <el-select v-else v-model="editForm.value" style="width:100%" placeholder="请选择折扣">
            <el-option v-for="d in discountOptions" :key="d" :value="d" :label="d + ' 折'" />
          </el-select>
        </el-form-item>
        <el-form-item label="最低消费" prop="minAmount">
          <el-input-number v-model="editForm.minAmount" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="积分成本" prop="pointsCost">
          <el-input-number v-model="editForm.pointsCost" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="editForm.stock" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="editForm.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm"
            placeholder="选择开始时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="editForm.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm"
            placeholder="选择结束时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">
          确认更新
        </el-button>
      </template>
    </el-dialog>

    <!-- 发放记录弹窗 -->
    <el-dialog v-model="recordsVisible" :title="recordTitle" width="700px" :close-on-click-modal="false">
      <el-table :data="records" v-loading="recordsLoading" stripe empty-text="暂无发放记录">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="username" label="用户" min-width="120" />
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="recordStatusMap[row.status]?.type || 'info'" size="small">
              {{ recordStatusMap[row.status]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="使用订单" width="100" align="center">
          <template #default="{ row }">{{ row.orderId || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="领取时间" width="170" />
        <el-table-column prop="useTime" label="使用时间" width="170">
          <template #default="{ row }">{{ row.useTime || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" title="优惠券详情" width="640px" :close-on-click-modal="false">
      <el-descriptions v-loading="detailLoading" :column="2" border class="detail-descriptions">
        <el-descriptions-item label="名称" :span="2">{{ detailData?.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeMap[detailData?.type] || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ detailData?.stock }}</el-descriptions-item>
        <el-descriptions-item label="面值">
          <span v-if="detailData?.type === 2">{{ detailData?.value }}折</span>
          <span v-else>¥{{ detailData?.value?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="最低消费">¥{{ (detailData?.minAmount || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="积分成本">{{ detailData?.pointsCost ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="有效期" :span="2">{{ detailData?.startTime }} ~ {{ detailData?.endTime
        }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
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

.coupons-table {
  width: 100%;
}

:deep(.el-table .el-table__body .el-table__row) {
  height: 50px;
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 2px;

  .el-button {
    margin: 0;
  }
}

.detail-descriptions {
  :deep(.el-descriptions__cell) {
    font-size: 15px;
  }

  :deep(.el-descriptions__label) {
    font-size: 14px;
  }
}
</style>
