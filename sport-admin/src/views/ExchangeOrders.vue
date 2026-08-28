<script setup>
import { ref, computed, onMounted } from 'vue'
import { getExchangeOrderList, shipExchangeOrder } from '@/api/manager'
import { toast } from '@/utils/toast'

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)

const searchOrderNo = ref('')
const searchUsername = ref('')
const searchStatus = ref('')

const detailVisible = ref(false)
const currentOrder = ref(null)

const shipVisible = ref(false)
const shipForm = ref({ logisticsCompany: '', logisticsNo: '' })
const shippingId = ref(null)
const shipLoading = ref(false)

const statusMap = {
  0: { label: '待发货', type: 'warning' },
  1: { label: '已发货', type: 'primary' },
  2: { label: '已完成', type: 'success' }
}

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    if (searchOrderNo.value && !order.orderNo.includes(searchOrderNo.value)) return false
    if (searchUsername.value && !order.username.includes(searchUsername.value)) return false
    const sv = searchStatus.value
    if (sv !== '' && sv !== undefined && sv !== null && order.status !== Number(sv)) return false
    return true
  })
})

const total = computed(() => filteredOrders.value.length)

const pagedOrders = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredOrders.value.slice(start, start + pageSize.value)
})

const formatAddress = (row) => {
  return [row.province, row.city, row.district, row.address].filter(Boolean).join(' ')
}

const fetchOrders = async () => {
  loading.value = true
  page.value = 1
  try {
    const res = await getExchangeOrderList()
    orders.value = res.data.data || []
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
}

const handleReset = () => {
  searchOrderNo.value = ''
  searchUsername.value = ''
  searchStatus.value = ''
  page.value = 1
}

const showDetail = (order) => {
  currentOrder.value = order
  detailVisible.value = true
}

const openShip = (order) => {
  shippingId.value = order.id
  shipForm.value = { logisticsCompany: '', logisticsNo: '' }
  shipVisible.value = true
}

const confirmShip = async () => {
  if (!shipForm.value.logisticsCompany || !shipForm.value.logisticsNo) {
    toast('请填写完整的物流信息', 'warning')
    return
  }
  shipLoading.value = true
  try {
    await shipExchangeOrder(shippingId.value, shipForm.value)
    toast('发货成功', 'success')
    shipVisible.value = false
    fetchOrders()
  } catch {
  } finally {
    shipLoading.value = false
  }
}

onMounted(fetchOrders)
</script>

<template>
  <div class="page">
    <h1 class="page__title">兑换记录</h1>
    <p class="page__desc">用户积分商品兑换记录</p>

    <div class="card card--search">
      <el-form :inline="true" class="search-form">
        <el-form-item label="兑换单号">
          <el-input v-model="searchOrderNo" placeholder="输入单号搜索" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="用户">
          <el-input v-model="searchUsername" placeholder="输入用户名搜索" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchStatus" placeholder="全部状态" clearable style="width: 120px;">
            <el-option label="待发货" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card">
      <el-table :data="pagedOrders" v-loading="loading" stripe empty-text="暂无兑换记录" style="cursor: context-menu;">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="orderNo" label="兑换单号" width="200" align="center" />
        <el-table-column prop="username" label="用户" width="110" align="center" />
        <el-table-column label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="gift-info">
              <el-image v-if="row.giftImage" :src="row.giftImage" class="gift-img" />
              <span>{{ row.giftName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="消耗积分" width="100" align="center">
          <template #default="{ row }">
            <span class="points-price">{{ row.pointsPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type" size="small" effect="dark">
              {{ statusMap[row.status]?.label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="160">
          <template #default="{ row }">
            <div>{{ row.receiverName }}</div>
            <div class="text-muted">{{ row.phone }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="兑换时间" width="170" align="center" />
        <el-table-column label="操作" width="170" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" size="small" @click.stop="openShip(row)">发货</el-button>
            <span v-else-if="row.status === 1" class="logistics-text">待收货</span>
            <span v-else class="text-muted">已完成</span>
            <el-button type="text" size="small" @click.stop="showDetail(row)" style="margin-left: 4px;">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total"
          layout="prev, pager, next, total" background @current-change="page = $event" />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="兑换订单详情" width="600px" :close-on-click-modal="false">
      <template v-if="currentOrder">
        <div class="detail-header">
          <el-image v-if="currentOrder.giftImage" :src="currentOrder.giftImage" class="detail-img" />
          <div class="detail-header-info">
            <div class="detail-order-no">{{ currentOrder.orderNo }}</div>
            <el-tag :type="statusMap[currentOrder.status]?.type" size="small" effect="dark">
              {{ statusMap[currentOrder.status]?.label }}
            </el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border class="detail-body">
          <el-descriptions-item label="用户名" :span="1">{{ currentOrder.username }}</el-descriptions-item>
          <el-descriptions-item label="消耗积分" :span="1">
            <span class="points-price">{{ currentOrder.pointsPrice }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="商品名称" :span="2">{{ currentOrder.giftName }}</el-descriptions-item>
          <el-descriptions-item label="收货人" :span="1">{{ currentOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话" :span="1">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ formatAddress(currentOrder) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.logisticsCompany" label="物流公司" :span="1">{{ currentOrder.logisticsCompany }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.logisticsNo" label="物流单号" :span="1">{{ currentOrder.logisticsNo }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.shipTime" label="发货时间" :span="1">{{ currentOrder.shipTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.completeTime" label="完成时间" :span="1">{{ currentOrder.completeTime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentOrder.remark" label="备注" :span="2">{{ currentOrder.remark }}</el-descriptions-item>
          <el-descriptions-item label="兑换时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <el-dialog v-model="shipVisible" title="兑换订单发货" width="450px" :close-on-click-modal="false">
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="物流公司" required>
          <el-input v-model="shipForm.logisticsCompany" placeholder="请输入物流公司名称" />
        </el-form-item>
        <el-form-item label="物流单号" required>
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
$text-muted: #94a3b8;
$orange: #ff6b35;

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

  &--search {
    margin-bottom: 16px;
    padding: 16px 24px;

    :deep(.el-form) {
      margin-bottom: 0;
    }

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.gift-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.gift-img {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.points-price {
  font-weight: 600;
  color: $orange;
}

.text-muted {
  color: $text-muted;
  font-size: 13px;
}

.logistics-text {
  font-size: 12px;
  color: #475569;
  line-height: 1.5;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table .el-table__body .el-table__row) {
  height: 52px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.detail-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.detail-header-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-order-no {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.detail-body {
  :deep(.el-descriptions__label) {
    color: #64748b;
    font-weight: 500;
  }
}
</style>
