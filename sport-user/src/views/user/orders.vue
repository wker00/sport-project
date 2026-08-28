<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, payOrder, cancelOrder, confirmOrder, reviewOrder, refundOrder, getUserInfo } from '@/api/manager'
import { toast } from '@/utils/toast'
import { useCounterStore } from '@/stores/counter'

const orders = ref([])
const orderStatusFilter = ref('ALL')
const detailDialogVisible = ref(false)
const currentOrder = ref(null)
const store = useCounterStore()
const router = useRouter()
const refreshOrders = inject('refreshOrders', null)
const reviewDialogVisible = ref(false)
const reviewForm = ref({ orderItemId: null, rating: 5, reviewContent: '' })
const reviewingOrderId = ref(null)
const loading = ref(true)

const refundDialogVisible = ref(false)
const refundForm = ref({ refundReason: '' })
const refundingOrderId = ref(null)

const statusMap = {
  0: '待付款', 1: '待发货', 2: '运输中', 3: '待收货',
  4: '待评价', 5: '已完成', 6: '已取消'
}
const refundStatusMap = {
  1: '退款申请中', 2: '退款已通过', 3: '退款已拒绝'
}
const statusClass = {
  0: 'pending', 1: 'paid', 2: 'shipped', 3: 'shipped',
  4: 'paid', 5: 'completed', 6: 'cancelled'
}
const filters = [
  { key: 'ALL', label: '全部' },
  { key: 0, label: '待付款' },
  { key: 1, label: '待发货' },
  { key: 2, label: '运输中' },
  { key: 3, label: '待收货' },
  { key: 4, label: '待评价' },
  { key: 5, label: '已完成' },
  { key: 6, label: '已取消' },
]

const filteredOrders = computed(() => {
  if (orderStatusFilter.value === 'ALL') return orders.value
  return orders.value.filter(o => o.status === orderStatusFilter.value)
})

const unreviewedItems = computed(() => {
  if (!currentOrder.value) return []
  return currentOrder.value.items.filter(i => i.rating == null)
})

function formatPrice(v) { return Number(v || 0).toFixed(2) }
function formatDate(d) {
  if (!d) return ''
  const t = new Date(d)
  return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')} ${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`
}
function formatDateShort(d) {
  if (!d) return ''
  const t = new Date(d)
  return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`
}
function statusText(s) { return statusMap[s] || '未知' }
function itemsCount(order) { return order.items ? order.items.length : 0 }

onMounted(async () => {
  loading.value = true
  const start = Date.now()
  const res = await getOrderList()
  if (res.data.code === 200) orders.value = res.data.data || []
  const elapsed = Date.now() - start
  const delay = Math.max(0, 500 - elapsed)
  if (delay) {
    setTimeout(() => { loading.value = false }, delay)
  } else {
    loading.value = false
  }
})

async function handlePay(order) {
  try {
    await ElMessageBox.confirm(`确认支付订单 ${order.orderNo}？`, '提示', {
      confirmButtonText: '确认支付', cancelButtonText: '取消', type: 'warning', lockScroll: false
    })
    const res = await payOrder(order.id)
    if (res.data.code === 200) {
      toast('支付成功', 'success')
      order.status = 1
      refreshOrders?.()
    } else {
      toast(res.data.message || '支付失败', 'error')
    }
  } catch (e) { console.error(e) }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm(`确定取消订单 ${order.orderNo}？`, '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', lockScroll: false
    })
    const res = await cancelOrder(order.id)
    if (res.data.code === 200) {
      toast('订单已取消', 'success')
      order.status = 6
      refreshOrders?.()
    } else {
      toast(res.data.message || '取消失败', 'error')
    }
  } catch (e) { console.error(e) }
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm('确认已收到货？', '提示', {
      confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'warning', lockScroll: false
    })
    const res = await confirmOrder(order.id)
    if (res.data.code === 200) {
      toast('已确认收货', 'success')
      order.status = 4
      refreshOrders?.()
      const userRes = await getUserInfo()
      if (userRes.data.code === 200) {
        store.setUserInfo(userRes.data.data)
      }
    } else {
      toast(res.data.message || '操作失败', 'error')
    }
  } catch (e) { console.error(e) }
}

function openRefund(order) {
  refundingOrderId.value = order.id
  refundForm.value = { refundReason: '' }
  refundDialogVisible.value = true
}

async function submitRefund() {
  try {
    await ElMessageBox.confirm('确认申请退款？退款后无法撤销', '提示', {
      confirmButtonText: '确认退款', cancelButtonText: '取消', type: 'warning', lockScroll: false
    })
    const res = await refundOrder(refundingOrderId.value, {
      refundReason: refundForm.value.refundReason.trim() || undefined
    })
    if (res.data.code === 200) {
      toast('退款申请已提交', 'success')
      refundDialogVisible.value = false
      const order = orders.value.find(o => o.id === refundingOrderId.value)
      if (order) order.refundStatus = 1
      refreshOrders?.()
    } else {
      toast(res.data.message || '退款申请失败', 'error')
    }
  } catch (e) { console.error(e) }
}

async function openDetail(order) {
  const res = await getOrderDetail(order.id)
  if (res.data.code === 200) {
    currentOrder.value = res.data.data
    detailDialogVisible.value = true
  } else {
    toast('获取订单详情失败', 'error')
  }
}

function openReview(order) {
  reviewingOrderId.value = order.id
  reviewForm.value = { orderItemId: null, rating: 5, reviewContent: '' }
  reviewDialogVisible.value = true
  if (!currentOrder.value || currentOrder.value.id !== order.id) {
    getOrderDetail(order.id).then(res => {
      if (res.data.code === 200) currentOrder.value = res.data.data
    })
  }
}

async function submitReview() {
  if (!reviewForm.value.orderItemId) { toast('请选择要评价的商品', 'warning'); return }
  if (!reviewForm.value.reviewContent.trim()) { toast('请填写评价内容', 'warning'); return }
  const data = {
    orderItemId: reviewForm.value.orderItemId,
    rating: reviewForm.value.rating,
    reviewContent: reviewForm.value.reviewContent.trim(),
  }
  const res = await reviewOrder(reviewingOrderId.value, data)
  if (res.data.code === 200) {
    toast('评价成功', 'success')
    reviewDialogVisible.value = false
    const order = orders.value.find(o => o.id === reviewingOrderId.value)
    if (order && unreviewedItems.value.length <= 1) { order.status = 5 }
    refreshOrders?.()
    const userRes = await getUserInfo()
    if (userRes.data.code === 200) { store.setUserInfo(userRes.data.data) }
  } else {
    toast(res.data.message || '评价失败', 'error')
  }
}
</script>

<template>
  <div class="glass-card">
    <div class="glass-card-header">
      <h3><i class="ph ph-shopping-cart"></i> 我的订单</h3>
    </div>
    <div class="glass-card-body">
      <div class="pill-tabs">
        <button v-for="f in filters" :key="f.key" :class="{ active: orderStatusFilter === f.key }"
          @click="orderStatusFilter = f.key">{{ f.label }}</button>
      </div>

      <template v-if="loading">
        <div class="skeleton-list">
          <div class="skeleton-order-item glass-inner" v-for="n in 3" :key="n">
            <div class="skeleton-image"></div>
            <div class="skeleton-info">
              <div class="sk-line sk-title"></div>
              <div class="sk-line sk-subtitle"></div>
            </div>
            <div class="skeleton-right">
              <div class="sk-line sk-price"></div>
              <div class="sk-line sk-status"></div>
              <div class="sk-line sk-action"></div>
            </div>
          </div>
        </div>
      </template>
      <template v-else-if="filteredOrders.length === 0">
        <div class="empty-state">
          <i class="ph ph-package"></i>
          <p>{{ orderStatusFilter === 'ALL' ? '暂无订单' : '暂无此类订单' }}</p>
        </div>
      </template>
      <div v-else class="order-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-item glass-inner">
          <div class="order-image">
            <img v-if="order.items && order.items[0] && order.items[0].productImage" :src="order.items[0].productImage"
              alt="商品" />
            <i v-else class="ph ph-package"></i>
            <span v-if="itemsCount(order) > 1" class="order-image-badge">{{ itemsCount(order) }}</span>
          </div>
          <div class="order-info">
            <h4>{{ order.items && order.items[0] ? order.items[0].productName : '商品' }}
              <span v-if="itemsCount(order) > 1"> 等{{ itemsCount(order) }}件商品</span>
            </h4>
            <p>{{ formatDateShort(order.createTime) }} | {{ order.orderNo }}</p>
          </div>
          <div class="order-right">
            <div class="order-price-wrap">
              <span class="order-price">¥{{ formatPrice(order.payAmount) }}</span>
              <span v-if="order.totalAmount > order.payAmount" class="order-original">¥{{ formatPrice(order.totalAmount)
                }}</span>
            </div>
            <span :class="['order-status', statusClass[order.status] || 'pending']">{{ statusText(order.status)
              }}</span>
            <div class="order-actions">

              <span v-if="order.status === 0" class="order-action primary" @click="handlePay(order)">立即支付</span>
              <span v-if="order.status === 0" class="order-action" @click="handleCancel(order)">取消订单</span>
              <span v-if="order.status === 3" class="order-action confirm" @click="handleConfirm(order)">确认收货</span>
              <span v-if="order.status === 4" class="order-action primary" @click="openReview(order)">去评价</span>

              <span v-if="order.status >= 1 && order.status <= 3 && !order.refundStatus" class="order-action refund"
                @click="openRefund(order)">申请退款</span>
              <span v-if="order.refundStatus" :class="['order-action', 'refund-status-' + order.refundStatus]">
                {{ refundStatusMap[order.refundStatus] }}
              </span>
              <span class="order-action view" @click="openDetail(order)">查看详情</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="detailDialogVisible" title="订单详情" width="720px" :lock-scroll="false" destroy-on-close>
    <template v-if="currentOrder">
      <div class="detail-header">
        <div class="detail-header-left">
          <span class="detail-order-no">订单号：{{ currentOrder.orderNo }}</span>
          <span :class="['order-status', statusClass[currentOrder.status] || 'pending']">{{
            statusText(currentOrder.status) }}</span>
        </div>
        <span class="detail-date">{{ formatDate(currentOrder.createTime) }}</span>
      </div>
      <div class="detail-section">
        <h4 class="detail-section-title">商品信息</h4>
        <div v-for="item in currentOrder.items" :key="item.id" class="detail-item">
          <div class="detail-item-img"><img :src="item.productImage" :alt="item.productName" /></div>
          <div class="detail-item-info">
            <router-link :to="`/product/${item.productId}`" class="detail-item-link"
              @click="detailDialogVisible = false">
              {{ item.productName }}
            </router-link>
            <p v-if="item.spec" class="detail-item-spec">规格：{{ item.spec }}</p>
          </div>
          <div class="detail-item-right">
            <span class="detail-item-price">¥{{ formatPrice(item.price) }}</span>
            <span class="detail-item-qty">×{{ item.quantity }}</span>
          </div>
        </div>
      </div>
      <div class="detail-section">
        <h4 class="detail-section-title">价格明细</h4>
        <div class="detail-summary">
          <div class="summary-row"><span>商品金额</span><span>¥{{ formatPrice(currentOrder.totalAmount) }}</span></div>
          <div class="summary-row" v-if="currentOrder.levelDiscount"><span><i class="ph ph-crown"></i> 会员折扣</span><span
              style="color:var(--accent-energy)">-¥{{ formatPrice(currentOrder.levelDiscount) }}</span></div>
          <div class="summary-row" v-if="currentOrder.discountAmount"><span>优惠减免</span><span
              style="color:var(--accent-vitality)">-¥{{ formatPrice(currentOrder.discountAmount) }}</span></div>
          <div class="summary-row total"><span>实付金额</span><span style="color:var(--price-color)">¥{{
            formatPrice(currentOrder.payAmount) }}</span></div>
        </div>
      </div>
      <div class="detail-section" v-if="currentOrder.remark">
        <h4 class="detail-section-title">备注信息</h4>
        <p class="detail-remark">{{ currentOrder.remark }}</p>
      </div>
      <div class="detail-section" v-if="currentOrder.expressCompany">
        <h4 class="detail-section-title">物流信息</h4>
        <div class="detail-summary">
          <div class="summary-row"><span>快递公司</span><span>{{ currentOrder.expressCompany }}</span></div>
          <div class="summary-row" v-if="currentOrder.expressNo"><span>快递单号</span><span>{{ currentOrder.expressNo
              }}</span></div>
          <div class="summary-row" v-if="currentOrder.deliveryTime"><span>发货时间</span><span>{{
            formatDate(currentOrder.deliveryTime) }}</span></div>
          <div class="summary-row" v-if="currentOrder.signTime"><span>签收时间</span><span>{{
            formatDate(currentOrder.signTime) }}</span></div>
        </div>
      </div>
      <div class="detail-section" v-if="currentOrder.refundStatus">
        <h4 class="detail-section-title">退款信息</h4>
        <div class="detail-summary">
          <div class="summary-row"><span>退款状态</span><span>{{ {
            1: '退款申请中', 2: '退款已通过', 3: '退款已拒绝'
          }[currentOrder.refundStatus] || '' }}</span></div>
          <div class="summary-row" v-if="currentOrder.refundReason"><span>退款原因</span><span>{{ currentOrder.refundReason
              }}</span></div>
        </div>
      </div>
      <template v-for="item in currentOrder.items" :key="'review-' + item.id">
        <div class="detail-section" v-if="item.rating">
          <h4 class="detail-section-title">评价 — {{ item.productName }}</h4>
          <div class="detail-summary">
            <div class="summary-row"><span>评分</span><el-rate :model-value="item.rating" disabled size="small" /></div>
            <div class="summary-row" v-if="item.reviewContent"><span>评价</span><span>{{ item.reviewContent }}</span>
            </div>
            <div class="summary-row" v-if="item.replyContent"><span>商家回复</span><span
                style="color:var(--accent-energy)">{{ item.replyContent }}</span></div>
          </div>
        </div>
      </template>
    </template>
  </el-dialog>

  <el-dialog v-model="reviewDialogVisible" title="评价商品" width="520px" :lock-scroll="false" destroy-on-close>
    <div class="review-form">
      <div class="review-section">
        <div class="section-title"><i class="ph ph-package"></i> 选择商品</div>
        <el-select v-model="reviewForm.orderItemId" placeholder="请选择要评价的商品" style="width:100%;">
          <el-option v-for="item in unreviewedItems" :key="item.id"
            :label="item.productName + (item.spec ? ' (' + item.spec + ')' : '')" :value="item.id" />
        </el-select>
      </div>
      <div class="review-section">
        <div class="section-title"><i class="ph ph-star"></i> 评分</div>
        <el-rate v-model="reviewForm.rating" :texts="['极差', '较差', '一般', '满意', '非常满意']" show-text />
      </div>
      <div class="review-section">
        <div class="section-title"><i class="ph ph-note-pencil"></i> 评价内容</div>
        <el-input v-model="reviewForm.reviewContent" type="textarea" maxlength="500" placeholder="分享您的使用体验（最多500字）"
          show-word-limit />
      </div>
    </div>
    <template #footer>
      <el-button @click="reviewDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitReview">提交评价</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="refundDialogVisible" title="申请退款" width="480px" :lock-scroll="false" destroy-on-close>
    <div class="review-form">
      <div class="review-section">
        <div class="section-title"><i class="ph ph-package"></i> 退款原因</div>
        <el-input v-model="refundForm.refundReason" type="textarea" maxlength="255" placeholder="请填写退款原因（可选，最多255字）"
          show-word-limit />
      </div>
    </div>
    <template #footer>
      <el-button @click="refundDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitRefund">提交退款申请</el-button>
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

      i {
        color: $accent-energy;
      }
    }
  }

  .glass-card-body {
    padding: 20px 24px;
  }
}

.pill-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;

  button {
    @include btn-base;
    padding: 6px 16px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;
    color: $text-secondary;
    background: $bg-card;
    border: 0.5px solid $border-subtle;

    &:hover {
      border-color: $border-accent;
      color: $accent-energy;
    }

    &.active {
      background: $accent-energy;
      border-color: $accent-energy;
      color: #fff;
    }
  }
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.glass-inner {
  background: $bg-subtle;
  border: 0.5px solid $border-subtle;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  transition: all 0.5s $transition-premium;
  cursor: pointer;

  &:hover {
    background: $bg-card-hover;
    border-color: $border-accent;
    transform: translateX(8px);
  }

  .order-image {
    width: 64px;
    height: 64px;
    border-radius: 10px;
    overflow: hidden;
    background: $accent-energy-soft;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: $accent-energy;
    flex-shrink: 0;
    position: relative;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .order-image-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      min-width: 20px;
      height: 20px;
      border-radius: 999px;
      background: $accent-energy;
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 5px;
    }
  }

  .order-info {
    flex: 1;
    min-width: 0;
    cursor: context-menu;

    h4 {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 4px;
      color: $text-primary;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    p {
      font-size: 12px;
      color: $text-muted;
    }
  }

  .order-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;

    .order-price-wrap {
      text-align: right;

      .order-price {
        display: block;
        font-size: 15px;
        font-weight: 700;
        color: $price-color;
      }

      .order-original {
        display: block;
        font-size: 11px;
        color: $text-muted;
        text-decoration: line-through;
      }
    }

    .order-actions {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .order-action {
        font-size: 12px;
        font-weight: 600;
        color: $text-muted;
        padding: 4px 10px;
        border-radius: 6px;
        text-align: center;
        white-space: nowrap;

        &:hover {
          background: $bg-card-hover;
        }

        &.confirm {
          color: $accent-vitality;

          &:hover {
            background: rgba($accent-vitality, 0.1);
          }
        }

        &.primary {
          color: #fff;
          background: $accent-energy;
        }

        &.view {
          color: $text-secondary;
          // border: 0.5px solid $border-subtle;

          &:hover {
            border-color: $accent-energy;
            color: $accent-energy;
            background: $accent-energy-soft;
          }
        }
      }

      .refund {
        color: $color-warning;
        border: 0.5px solid transparent;

        &:hover {
          background: color-mix(in srgb, var(--color-warning) 12%, transparent);
          border-color: color-mix(in srgb, var(--color-warning) 30%, transparent);
        }
      }
    }
  }
}

.order-status {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;

  &.pending {
    background: $accent-energy-soft;
    color: $accent-energy;
  }

  &.paid {
    background: rgba($accent-energy, 0.12);
    color: $accent-energy;
  }

  &.shipped {
    background: rgba($color-info, 0.15);
    color: $color-info;
  }

  &.completed {
    background: rgba($accent-vitality, 0.15);
    color: $accent-vitality;
  }

  &.cancelled {
    background: rgba(148, 163, 184, 0.15);
    color: $text-muted;
  }
}

.order-action.refund {

  &.refund-status-1 {
    color: $color-warning;
    cursor: default;

    &:hover {
      background: none;
    }
  }

  &.refund-status-2 {
    color: $accent-vitality;
    cursor: default;

    &:hover {
      background: none;
    }
  }

  &.refund-status-3 {
    color: $color-danger;
    cursor: default;

    &:hover {
      background: none;
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: $text-muted;

  i {
    display: block;
    margin: 0 auto 16px;
    font-size: 48px;
  }

  p {
    font-size: 14px;
  }
}

@keyframes sk-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.35; }
}

.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
}

.skeleton-image {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: $border-subtle;
  flex-shrink: 0;
  animation: sk-pulse 1.6s ease-in-out infinite;
}

.skeleton-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.sk-line {
  height: 12px;
  border-radius: 6px;
  background: $border-subtle;
  animation: sk-pulse 1.6s ease-in-out infinite;

  &.sk-title {
    width: 55%;
    height: 14px;
  }

  &.sk-subtitle {
    width: 38%;
  }

  &.sk-price {
    width: 60px;
    height: 14px;
  }

  &.sk-status {
    width: 48px;
  }

  &.sk-action {
    width: 56px;
    margin-top: 2px;
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .detail-header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .detail-order-no {
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
  }

  .detail-date {
    font-size: 12px;
    color: $text-muted;
  }
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 0.5px solid $border-subtle;

  &:last-child {
    border-bottom: none;
    margin-bottom: 0;
    padding-bottom: 0;
  }

  .detail-section-title {
    font-size: 13px;
    font-weight: 700;
    color: $text-secondary;
    margin-bottom: 12px;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .detail-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;

    &:not(:last-child) {
      border-bottom: 0.5px solid $border-subtle;
    }

    .detail-item-img {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      overflow: hidden;
      flex-shrink: 0;
      background: $bg-card;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .detail-item-info {
      flex: 1;
      min-width: 0;
    }

    .detail-item-name {
      font-size: 13px;
      font-weight: 600;
      color: $text-primary;
    }

    .detail-item-link {
      text-decoration: none;
      transition: color 0.3s;

      &:hover {
        color: $accent-energy;
      }
    }

    .detail-item-spec {
      font-size: 11px;
      color: $text-muted;
      margin-top: 2px;
    }

    .detail-item-right {
      text-align: right;
      flex-shrink: 0;
    }

    .detail-item-price {
      display: block;
      font-size: 13px;
      font-weight: 700;
      color: $price-color;
    }

    .detail-item-qty {
      font-size: 11px;
      color: $text-muted;
    }
  }

  .detail-summary {
    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 6px 0;
      font-size: 13px;
      color: $text-secondary;

      &.total {
        border-top: 0.5px solid $border-subtle;
        margin-top: 4px;
        padding-top: 10px;
        font-weight: 700;
        font-size: 15px;
      }
    }
  }
}

.review-form {
  .review-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 14px;

    i {
      font-size: 18px;
      color: $accent-energy;
    }
  }

  :deep(.el-select__wrapper) {
    background: $bg-card;
    border: 0.5px solid $border-subtle;
    border-radius: 10px;
    box-shadow: none !important;
    padding: 6px 12px;
    min-height: 40px;
    transition-property: border-color, box-shadow;
    transition-duration: 0.25s;
    transition-timing-function: cubic-bezier(0.32, 0.72, 0, 1);

    &:hover {
      border-color: $accent-energy;
    }

    &.is-focused {
      border-color: $accent-energy;
      box-shadow: 0 0 0 3px $accent-energy-soft !important;
    }
  }

  :deep(.el-select__placeholder) {
    color: $text-muted;
    font-size: 13px;
  }

  :deep(.el-select__selected-item) {
    color: $text-primary;
    font-size: 13px;
    font-weight: 500;
  }

  :deep(.el-select__caret) {
    color: $text-muted;
    font-size: 16px;
    transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);

    &.is-reverse {
      transform: rotate(180deg);
    }
  }

  :deep(.el-textarea__inner) {
    background: $bg-card;
    border: 0.5px solid $border-subtle;
    border-radius: 10px;
    box-shadow: none;
    color: $text-primary;
    font-size: 13px;
    transition: border-color 0.3s;
    height: 120px;
    resize: none;

    &::placeholder {
      color: $text-muted;
    }

    &:hover,
    &:focus {
      border-color: $accent-energy;
    }
  }

  :deep(.el-input__count) {
    background: transparent;
  }

  :deep(.el-input__count-inner) {
    background: transparent;
    padding: 0;
  }

  :deep(.el-rate__text) {
    font-size: 13px;
    color: $text-secondary;
  }

}
</style>
