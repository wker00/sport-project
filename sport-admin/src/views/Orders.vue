<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, shipOrder, refundOrder, deliverOrder } from '@/api/manager'
import { toast } from '@/utils/toast'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref('all')
const searchNo = ref('')
const page = ref(1)
const pageSize = ref(10)

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '运输中', value: 2 },
  { label: '待收货', value: 3 },
  { label: '待评价', value: 4 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 },
]

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '运输中', type: 'info' },
  3: { text: '待收货', type: 'warning' },
  4: { text: '待评价', type: 'primary' },
  5: { text: '已完成', type: 'success' },
  6: { text: '已取消', type: 'info' },
}

const levelMap = {
  1: { text: '普通会员', className: 'vip-regular' },
  2: { text: '银卡会员', className: 'vip-silver' },
  3: { text: '金卡会员', className: 'vip-gold' },
  4: { text: '钻石会员', className: 'vip-diamond' },
  5: { text: '黑金会员', className: 'vip-black-gold' },
}

const filteredOrders = computed(() => {
  let list = orders.value
  if (searchNo.value.trim()) {
    const kw = searchNo.value.trim().toLowerCase()
    list = list.filter((o) => o.orderNo && o.orderNo.toLowerCase().includes(kw))
  }
  return list
})

const total = computed(() => filteredOrders.value.length)

const pagedOrders = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

const fetchOrders = async (resetPage = false) => {
  if (resetPage) page.value = 1
  loading.value = true
  try {
    const res = await getOrderList(statusFilter.value === 'all' ? null : statusFilter.value)
    orders.value = res.data.data || []
  } catch {
    orders.value = []
  } finally {
    loading.value = false
    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (page.value > maxPage) page.value = maxPage
  }
}

watch(statusFilter, () => {
  page.value = 1
  fetchOrders(true)
})

const handleSearch = () => {
  page.value = 1
}

const handlePageChange = (p) => {
  page.value = p
}

const formatAmount = (v) => '¥' + (v ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentOrder = ref(null)

const handleView = async (row) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await getOrderDetail(row.id)
    currentOrder.value = res.data.data
  } catch {
    currentOrder.value = row
  } finally {
    detailLoading.value = false
  }
}

// 发货
const shipVisible = ref(false)
const shipLoading = ref(false)
const shipForm = ref({ orderId: null, expressCompany: '', expressNo: '' })

const expressCompanies = [
  '顺丰快递', '圆通快递', '中通快递', '韵达快递', '申通快递',
  '百世快递', '极兔快递', '京东快递', '德邦快递', '邮政EMS',
]

const handleShip = (row) => {
  shipForm.value = { orderId: row.id, expressCompany: '', expressNo: '' }
  shipVisible.value = true
}

const submitShip = async () => {
  if (!shipForm.value.expressCompany || !shipForm.value.expressNo) {
    toast('请填写快递公司和运单号', 'warning')
    return
  }
  shipLoading.value = true
  try {
    await shipOrder(shipForm.value)
    toast('发货成功')
    shipVisible.value = false
    await fetchOrders()
  } catch {
  } finally {
    shipLoading.value = false
  }
}

// 确认送达
const deliverLoading = ref(false)

const handleDeliver = async (row) => {
  try {
    await ElMessageBox.confirm(`确定确认订单「${row.orderNo}」已送达？`, '确认送达', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'info',
    })
    deliverLoading.value = true
    await deliverOrder(row.id)
    toast('已确认送达')
    await fetchOrders()
  } catch {
  } finally {
    deliverLoading.value = false
  }
}

// 退款
const refundVisible = ref(false)
const refundForm = ref({ orderId: null, refundStatus: 2, refundReason: '' })

const handleRefund = (row) => {
  refundForm.value = { orderId: row.id, refundStatus: 2, refundReason: '' }
  refundVisible.value = true
}

const submitRefund = async () => {
  refundLoading.value = true
  try {
    await refundOrder(refundForm.value)
    toast(refundForm.value.refundStatus === 2 ? '退款已通过' : '退款已拒绝')
    refundVisible.value = false
    await fetchOrders()
  } catch {
  } finally {
    refundLoading.value = false
  }
}

const refundLoading = ref(false)

onMounted(() => fetchOrders(true))
</script>

<template>
  <div class="page">
    <div class="page__header">
      <div>
        <h1 class="page__title">订单管理</h1>
        <p class="page__desc">查看和处理平台所有订单</p>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="toolbar__tabs">
          <el-radio-group v-model="statusFilter" size="small">
            <el-radio-button v-for="opt in statusOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
        <el-input v-model="searchNo" placeholder="搜索订单号" clearable class="toolbar__input" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="pagedOrders" v-loading="loading" stripe class="orders-table" empty-text="暂无订单数据">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="orderNo" label="订单号" width="220" align="center" />
        <el-table-column label="金额" width="160" align="center">
          <template #default="{ row }">
            <div class="order-amount">
              <span class="order-amount__pay">{{ formatAmount(row.payAmount) }}</span>
              <span v-if="row.discountAmount" class="order-amount__discount">
                已减 {{ formatAmount(row.discountAmount) }}
              </span>
              <span v-if="row.levelDiscount" class="order-amount__discount">
                会员减 {{ formatAmount(row.levelDiscount) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.text || '未知' }}
            </el-tag>
            <div v-if="row.refundStatus" class="refund-hint">
              {{ { 1: '退款申请中', 2: '已通过', 3: '已拒绝' }[row.refundStatus] || '' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" align="center">
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="物流" width="160" align="center">
          <template #default="{ row }">
            <div v-if="row.expressCompany" class="express-info">
              <span>{{ row.expressCompany }}</span>
              <span v-if="row.expressNo" class="express-info__no">{{ row.expressNo }}</span>
            </div>
            <span v-else class="text-muted">未发货</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="primary" link size="small" @click="handleView(row)">
                详情
              </el-button>
              <el-button v-if="row.status === 1" type="success" link size="small" @click="handleShip(row)">
                发货
              </el-button>
              <el-button v-if="row.status === 2" type="info" link size="small" :loading="deliverLoading"
                @click="handleDeliver(row)">
                确认送达
              </el-button>
              <el-button v-if="row.refundStatus === 1" type="warning" link size="small" @click="handleRefund(row)">
                退款
              </el-button>
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
  <el-dialog v-model="detailVisible" title="订单详情" width="640px" :close-on-click-modal="false">
    <el-skeleton v-if="detailLoading" :rows="8" animated />
    <div v-else-if="currentOrder" class="detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号" :span="2">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusMap[currentOrder.status]?.type || 'info'" size="small">
            {{ statusMap[currentOrder.status]?.text || '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款状态">
          <span v-if="currentOrder.refundStatus">
            {{ { 1: '退款申请中', 2: '已通过', 3: '已拒绝' }[currentOrder.refundStatus] }}
          </span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="实付金额" :span="2">
          <span class="detail__amount">{{ formatAmount(currentOrder.payAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="优惠券折扣">
          {{ currentOrder.discountAmount ? formatAmount(currentOrder.discountAmount) : '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="会员折扣">
          {{ currentOrder.levelDiscount ? formatAmount(currentOrder.levelDiscount) : '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="总金额" :span="2">
          {{ formatAmount(currentOrder.totalAmount) }}
        </el-descriptions-item>
        <el-descriptions-item label="快递公司" v-if="currentOrder.expressCompany">
          {{ currentOrder.expressCompany }}
        </el-descriptions-item>
        <el-descriptions-item label="运单号" v-if="currentOrder.expressNo">
          {{ currentOrder.expressNo }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentOrder.remark || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="发货时间" v-if="currentOrder.deliveryTime">
          {{ currentOrder.deliveryTime }}
        </el-descriptions-item>
        <el-descriptions-item label="签收时间" v-if="currentOrder.signTime">
          {{ currentOrder.signTime }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div v-if="currentOrder.items?.length" class="detail__items-section" style="margin-bottom: 10px;">
        <h4 class="detail__sub-title">商品明细</h4>
        <el-table :data="currentOrder.items" border size="small" class="detail__items-table">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="detail__product">
                <img v-if="row.productImage" :src="row.productImage" class="detail__product-img" />
                <div class="detail__product-info">
                  <span class="detail__product-name">{{ row.productName }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="规格" prop="spec" width="100" align="center">
            <template #default="{ row }">{{ row.spec || '-' }}</template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="right">
            <template #default="{ row }">{{ formatAmount(row.price) }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
          <el-table-column label="小计" width="120" align="right">
            <template #default="{ row }">
              <span class="detail__subtotal">{{ formatAmount(row.price * row.quantity) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="currentOrder.address" class="detail__address-section" style="margin-bottom: 10px;">
        <h4 class="detail__sub-title">收货地址</h4>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="收件人">{{ currentOrder.address.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.address.phone }}</el-descriptions-item>
          <el-descriptions-item label="地址">
            {{ currentOrder.address.province }}{{ currentOrder.address.city }}{{ currentOrder.address.district }}{{ currentOrder.address.address }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div v-if="currentOrder.user" class="detail__user-section">
        <h4 class="detail__sub-title">用户信息</h4>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="用户名">{{ currentOrder.user.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentOrder.user.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentOrder.user.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentOrder.user.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="会员等级">
            <tag class="user-level" :class="levelMap[currentOrder.user.userLevel]?.className">
              {{ levelMap[currentOrder.user.userLevel]?.text || '未知' }}
            </tag>
          </el-descriptions-item>
          <el-descriptions-item label="积分余额">{{ currentOrder.user.pointsBalance ?? 0 }}</el-descriptions-item>
        </el-descriptions>
      </div>
  
      <div v-if="currentOrder.refundReason" class="detail__refund-reason">
        <strong>退款原因：</strong>{{ currentOrder.refundReason }}
      </div>
    </div>
  </el-dialog>

  <!-- 发货弹窗 -->
  <el-dialog v-model="shipVisible" title="发货" width="420px" :close-on-click-modal="false">
    <el-form :model="shipForm" label-width="100px" label-position="right">
      <el-form-item label="快递公司" required>
        <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width:100%">
          <el-option v-for="item in expressCompanies" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="运单号" required>
        <el-input v-model="shipForm.expressNo" placeholder="如：SF1234567890" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="shipVisible = false">取消</el-button>
      <el-button type="primary" :loading="shipLoading" @click="submitShip">
        确认发货
      </el-button>
    </template>
  </el-dialog>

  <!-- 退款弹窗 -->
  <el-dialog v-model="refundVisible" title="处理退款" width="420px" :close-on-click-modal="false">
    <el-form :model="refundForm" label-width="100px" label-position="right">
      <el-form-item label="处理结果" required>
        <el-radio-group v-model="refundForm.refundStatus">
          <el-radio :value="2">通过退款</el-radio>
          <el-radio :value="3">拒绝退款</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="原因说明">
        <el-input v-model="refundForm.refundReason" type="textarea" :rows="3" maxlength="200" resize="none"
          placeholder="可选" class="detail__fixed-textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="refundVisible = false">取消</el-button>
      <el-button type="primary" :loading="refundLoading" @click="submitRefund">
        确认处理
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
@use '@/assets/level.scss' as *;

$text-muted: #94a3b8;

.page {
  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 24px;
  }

  &__title {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: -0.3px;
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 15px;
    color: $text-muted;
    margin: 0;
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
  flex-wrap: wrap;

  &__input {
    width: 220px;
    margin-left: auto;
  }
}

.orders-table {
  width: 100%;

  :deep(.el-table__row) {
    height: 50px;
  }
}

.order-amount {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;

  &__pay {
    font-weight: 600;
    color: #ff6b35;
  }

  &__discount {
    font-size: 11px;
    color: #10b981;
  }
}

.refund-hint {
  font-size: 11px;
  color: #f56c6c;
  margin-top: 2px;
}

.express-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 13px;

  &__no {
    font-size: 12px;
    color: $text-muted;
  }
}

.text-muted {
  color: $text-muted;
  font-size: 13px;
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 2px;

  .el-button {
    margin: 0;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}


.detail {
  &__amount {
    font-size: 18px;
    font-weight: 700;
    color: #ff6b35;
  }

  &__sub-title {
    font-size: 14px;
    font-weight: 600;
    color: #0f172a;
    margin: 16px 0 10px;
  }

  &__items-section {
    margin-top: 4px;
  }

  &__items-table {
    :deep(.el-table__header th) {
      background: #f8fafc;
      font-weight: 600;
      color: #475569;
    }
  }

  &__product {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  &__product-img {
    width: 40px;
    height: 40px;
    border-radius: 6px;
    object-fit: cover;
    flex-shrink: 0;
    background: #f1f5f9;
  }

  &__product-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
  }

  &__product-name {
    font-size: 13px;
    font-weight: 500;
    color: #0f172a;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__product-review {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  &__review-text {
    font-size: 12px;
    color: $text-muted;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 160px;
  }

  &__subtotal {
    font-weight: 600;
    color: #ff6b35;
  }

  &__user-section {
    margin-top: 16px;
  }

  &__fixed-textarea {
    :deep(.el-textarea__inner) {
      resize: none;
      height: 72px;
      min-height: 72px;
      max-height: 72px;
      overflow-y: auto;
    }
  }

  &__refund-reason {
    margin-top: 16px;
    padding: 12px 16px;
    background: #fff5f5;
    border-radius: 8px;
    font-size: 13px;
    color: #c53030;
    line-height: 1.5;
  }
}
</style>
