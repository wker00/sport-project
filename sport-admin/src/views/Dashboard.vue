<script setup>
import { ref, watch, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTransition } from '@vueuse/core'
import { getDashboardData, getOrderList, getCategoryList, getCouponList, getPointsGiftsList, getDailyStatistics, getReviewList } from '@/api/manager'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const isDashboard = computed(() => route.name === 'Dashboard')

const pageTitle = {
  Dashboard: '首页',
  Products: '商品列表',
  Categories: '分类管理',
  Orders: '订单管理',
  Members: '会员管理',
  Admins: '管理员管理',
  Roles: '角色权限',
  Settings: '系统设置',
}

const title = pageTitle[route.name] || '首页'

const stats = ref(null)
const loading = ref(true)
const orders = ref([])
const ordersLoading = ref(true)
const extraLoading = ref(true)
const categoryCount = ref(0)
const couponCount = ref(0)
const pointsGiftsCount = ref(0)
const orderCount = ref(0)
const reviews = ref([])

const sourceUsers = ref(0)
const sourceProducts = ref(0)
const sourceOrders = ref(0)
const sourceAmount = ref(0)

const animatedUsers = useTransition(sourceUsers, { duration: 1200 })
const animatedProducts = useTransition(sourceProducts, { duration: 1200 })
const animatedOrders = useTransition(sourceOrders, { duration: 1200 })
const animatedAmount = useTransition(sourceAmount, { duration: 1200 })

const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '运输中', type: 'info' },
  3: { text: '待收货', type: 'warning' },
  4: { text: '待评价', type: 'primary' },
  5: { text: '已完成', type: 'success' },
  6: { text: '已取消', type: 'info' },
}

const orderStats = computed(() => {
  const counts = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0, 6: 0 }
  orders.value.forEach((o) => { if (o.status !== undefined) counts[o.status]++ })
  return counts
})

const refundPendingOrders = computed(() =>
  orders.value.filter((o) => o.refundStatus === 1).length
)

const unrepliedReviews = computed(() =>
  reviews.value.filter((r) => !r.replyContent).length
)

const hasPendingTasks = computed(() =>
  (orderStats.value[1] || 0) + (orderStats.value[2] || 0) +
  refundPendingOrders.value + unrepliedReviews.value > 0
)

const quickActions = [
  { path: '/orders', icon: 'List', label: '订单管理', color: '#3b82f6' },
  { path: '/userList', icon: 'UserFilled', label: '用户列表', color: '#10b981' },
  { path: '/points-gifts', icon: 'Present', label: '积分商品', color: '#ff6b35' },
  { path: '/adminList', icon: 'User', label: '管理员列表', color: '#f59e0b' },
]

const enterDashboard = async () => {
  loading.value = true
  ordersLoading.value = true
  extraLoading.value = true
  try {
    const [resStats, resOrders, resCats, resCoupons, resGifts, resReviews] = await Promise.all([
      getDashboardData(),
      getOrderList(),
      getCategoryList(),
      getCouponList(),
      getPointsGiftsList(),
      getReviewList(),
    ])
    stats.value = resStats.data.data
    sourceUsers.value = stats.value?.totalUsers ?? 0
    sourceProducts.value = stats.value?.totalProducts ?? 0
    sourceOrders.value = stats.value?.totalOrders ?? 0
    sourceAmount.value = stats.value?.todayOrderAmount ?? 0
    orders.value = resOrders.data.data || []
    categoryCount.value = (resCats.data.data || []).length
    couponCount.value = (resCoupons.data.data || []).length
    pointsGiftsCount.value = (resGifts.data.data || []).length
    orderCount.value = (resOrders.data.data || []).length
    reviews.value = resReviews.data.data || []
  } catch {
    stats.value = null
    orders.value = []
  } finally {
    loading.value = false
    ordersLoading.value = false
    extraLoading.value = false
    syncTodayAmount()
  }
}

watch(() => route.name, async (name) => {
  if (name === 'Dashboard') {
    await enterDashboard()
  }
}, { immediate: true })

const formatAmount = (v) => '¥' + (v ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const yesterdayStats = computed(() => {
  const list = dailyStats.value
  if (list.length < 2) return null
  return list[list.length - 2]
})

const amountChangePercent = computed(() => {
  if (!yesterdayStats.value || !stats.value) return null
  const today = stats.value.todayOrderAmount ?? 0
  const yesterday = yesterdayStats.value.orderAmount ?? 0
  if (yesterday === 0) return null
  return ((today - yesterday) / yesterday * 100).toFixed(1)
})

const amountTrend = computed(() => {
  const v = Number(amountChangePercent.value)
  if (v > 0) return 'up'
  if (v < 0) return 'down'
  return 'flat'
})

const todayNewUsers = computed(() => {
  const list = dailyStats.value
  if (!list.length) return 0
  return list[list.length - 1]?.newUsers ?? 0
})

const todayNewOrders = computed(() => {
  const list = dailyStats.value
  if (!list.length) return 0
  return list[list.length - 1]?.newOrders ?? 0
})

// 每日趋势图表
const chartRef = ref(null)
const dailyStats = ref([])
const chartLoading = ref(true)
let chartInstance = null

const fmtDate = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const syncTodayAmount = () => {
  if (!stats.value || dailyStats.value.length === 0) return
  const today = fmtDate(new Date())
  const last = dailyStats.value[dailyStats.value.length - 1]
  if (last.statDate === today) {
    last.payAmount = stats.value.todayOrderAmount ?? 0
  }
}

const fetchDailyStats = async () => {
  chartLoading.value = true
  const end = new Date()
  const start = new Date(end.getFullYear(), end.getMonth(), 1)
  try {
    const res = await getDailyStatistics(fmtDate(start), fmtDate(end))
    dailyStats.value = (res.data.data || []).reverse()
  } catch {
    dailyStats.value = []
  } finally {
    chartLoading.value = false
    syncTodayAmount()
    updateChart()
  }
}

const initChart = () => {
  if (!chartRef.value) return
  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

watch(dailyStats, (val) => {
  if (val.length > 0) {
    nextTick(() => {
      if (!chartInstance) {
        initChart()
      } else {
        updateChart()
      }
    })
  }
})

const updateChart = () => {
  if (!chartInstance || dailyStats.value.length === 0) return
  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: (params) => {
        const idx = params[0].dataIndex
        const stat = dailyStats.value[idx]
        if (!stat) return ''
        return `<strong>${params[0].axisValue}</strong><br/>` +
          `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#ff6b35;margin-right:5px"></span>支付金额：<span style="color:#ff6b35">¥${(stat.payAmount ?? 0).toFixed(2)}</span><br/>` +
          `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#3b82f6;margin-right:5px"></span>新订单数：${stat.newOrders}`
      },
    },
    legend: {
      data: ['支付金额'],
      top: 0,
    },
    grid: {
      left: 50,
      right: 60,
      bottom: 20,
      top: 40,
    },
    xAxis: {
      type: 'category',
      data: dailyStats.value.map((d) => d.statDate),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
    },
    yAxis: [
      {
        type: 'value',
        name: '支付金额 (元)',
        splitLine: { lineStyle: { color: '#f1f5f9' } },
      },
    ],
    series: [
      {
        name: '支付金额',
        type: 'line',
        data: dailyStats.value.map((d) => d.payAmount),
        lineStyle: { color: '#ff6b35', width: 2.5 },
        itemStyle: { color: '#ff6b35' },
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
      },
    ],
  })
}

const handleResize = () => { chartInstance?.resize() }

onMounted(() => {
  fetchDailyStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<template>
  <div v-if="!isDashboard" class="page">
    <h1 class="page__title">{{ title }}</h1>
    <p class="page__desc">欢迎使用 SportZone 管理平台</p>
  </div>

  <div v-else class="dashboard">
    <div class="dashboard__header">
      <div>
        <p class="dashboard__greeting">欢迎回来</p>
      </div>
      <span class="dashboard__date">{{ new Date().toLocaleDateString('zh-CN') }}</span>
    </div>

    <div class="stats">
      <el-skeleton v-if="loading" :count="4" animated class="stats__skeleton">
        <template #template>
          <el-skeleton-item variant="rect" class="stats__skeleton-card" />
        </template>
      </el-skeleton>
      <template v-else-if="stats">
        <div class="stats__card">
          <el-statistic :value="animatedUsers">
            <template #prefix>
              <div class="stats__icon stats__icon--orange">
                <el-icon><UserFilled /></el-icon>
              </div>
            </template>
            <template #title>用户总数</template>
          </el-statistic>
          <div v-if="todayNewUsers" class="stats__change stats__change--orange">
            今日 +{{ todayNewUsers }}
          </div>
        </div>
        <div class="stats__card">
          <el-statistic :value="animatedProducts">
            <template #prefix>
              <div class="stats__icon stats__icon--blue">
                <el-icon><Goods /></el-icon>
              </div>
            </template>
            <template #title>商品总数</template>
          </el-statistic>
        </div>
        <div class="stats__card">
          <el-statistic :value="animatedOrders">
            <template #prefix>
              <div class="stats__icon stats__icon--green">
                <el-icon><List /></el-icon>
              </div>
            </template>
            <template #title>订单总数</template>
          </el-statistic>
          <div v-if="todayNewOrders" class="stats__change stats__change--green">
            今日 +{{ todayNewOrders }}
          </div>
        </div>
        <div class="stats__card stats__card--amount">
          <el-statistic :value="animatedAmount" :formatter="formatAmount">
            <template #prefix>
              <div class="stats__icon stats__icon--purple">
                <el-icon><Money /></el-icon>
              </div>
            </template>
            <template #title>今日成交额</template>
          </el-statistic>
          <div v-if="amountChangePercent !== null" class="stats__change" :class="'stats__change--' + amountTrend">
            较昨日
            <el-icon v-if="amountTrend === 'up'"><Top /></el-icon>
            <el-icon v-if="amountTrend === 'down'"><Bottom /></el-icon>
            {{ Math.abs(amountChangePercent) }}%
          </div>
        </div>
      </template>
    </div>

    <div class="stats stats--secondary">
      <el-skeleton v-if="ordersLoading" :count="4" animated class="stats__skeleton">
        <template #template>
          <el-skeleton-item variant="rect" class="stats__skeleton-card" />
        </template>
      </el-skeleton>
      <template v-else>
        <div class="stats__card stats__card--sm">
          <div class="stats__tag stats__tag--warning">{{ orderStats[0] }}</div>
          <div class="stats__info">
            <span class="stats__label">待付款</span>
          </div>
        </div>
        <div class="stats__card stats__card--sm">
          <div class="stats__tag stats__tag--primary">{{ orderStats[1] }}</div>
          <div class="stats__info">
            <span class="stats__label">待发货</span>
          </div>
        </div>
        <div class="stats__card stats__card--sm">
          <div class="stats__tag stats__tag--warning">{{ orderStats[3] }}</div>
          <div class="stats__info">
            <span class="stats__label">待收货</span>
          </div>
        </div>
        <div class="stats__card stats__card--sm">
          <div class="stats__tag stats__tag--success">{{ orderStats[5] }}</div>
          <div class="stats__info">
            <span class="stats__label">已完成</span>
          </div>
        </div>
      </template>
    </div>

    <div class="dashboard__grid dashboard__grid--triple">
      <div class="dashboard__card pending">
        <h3 class="section-title">待处理任务</h3>
        <el-skeleton v-if="loading" :count="3" animated class="pending__skeleton">
          <template #template>
            <el-skeleton-item variant="rect" class="pending__skeleton-item" />
          </template>
        </el-skeleton>
        <template v-else-if="hasPendingTasks">
          <div v-if="orderStats[1]" class="pending__item">
            <div class="pending__left">
              <span class="pending__count">{{ orderStats[1] }}</span>
              <span class="pending__label">待发货订单</span>
            </div>
            <el-link type="primary" underline="never" @click="router.push('/orders')">去发货</el-link>
          </div>
          <div v-if="orderStats[2]" class="pending__item">
            <div class="pending__left">
              <span class="pending__count">{{ orderStats[2] }}</span>
              <span class="pending__label">运输中订单</span>
            </div>
            <el-link type="primary" underline="never" @click="router.push('/orders')">确认送达</el-link>
          </div>
          <div v-if="refundPendingOrders" class="pending__item">
            <div class="pending__left">
              <span class="pending__count">{{ refundPendingOrders }}</span>
              <span class="pending__label">退款申请</span>
            </div>
            <el-link type="primary" underline="never" @click="router.push('/orders')">去处理</el-link>
          </div>
          <div v-if="unrepliedReviews" class="pending__item">
            <div class="pending__left">
              <span class="pending__count">{{ unrepliedReviews }}</span>
              <span class="pending__label">未回复评价</span>
            </div>
            <el-link type="primary" underline="never" @click="router.push('/reviews')">去回复</el-link>
          </div>
        </template>
        <el-empty v-else description="暂无待处理" :image-size="60" />
      </div>

      <div class="dashboard__card overview">
        <h3 class="section-title">运营数据概览</h3>
        <el-skeleton v-if="extraLoading" :count="5" animated class="overview__skeleton">
          <template #template>
            <el-skeleton-item variant="rect" class="overview__skeleton-item" />
          </template>
        </el-skeleton>
        <div v-else class="overview__grid">
          <div class="overview__item">
            <span class="overview__value">{{ orderCount }}</span>
            <span class="overview__label">订单数量</span>
          </div>

          <div class="overview__item">
            <span class="overview__value">{{ pointsGiftsCount }}</span>
            <span class="overview__label">积分商品</span>
          </div>

          <div class="overview__item">
            <span class="overview__value">{{ categoryCount }}</span>
            <span class="overview__label">分类</span>
          </div>

          <div class="overview__item">
            <span class="overview__value">{{ couponCount }}</span>
            <span class="overview__label">优惠券</span>
          </div>
        </div>
      </div>

      <div class="dashboard__card quick">
        <h3 class="section-title">快捷操作</h3>
        <div class="quick__grid">
          <div v-for="item in quickActions" :key="item.path" class="quick__card" @click="router.push(item.path)">
            <div class="quick__icon" :style="{ background: item.color + '1A', color: item.color }">
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
            </div>
            <span class="quick__label">{{ item.label }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="dashboard__card trend">
      <h3 class="section-title">当月趋势</h3>
      <el-skeleton v-if="chartLoading" :count="4" animated class="trend__skeleton" />
      <div v-else-if="dailyStats.length > 0" ref="chartRef" class="trend__chart"></div>
      <el-empty v-else description="暂无统计数据" :image-size="60" />
    </div>

    <div class="orders">
      <h3 class="section-title">最近订单</h3>
      <el-skeleton v-if="ordersLoading" :count="4" animated class="orders__skeleton">
        <template #template>
          <el-skeleton-item variant="rect" class="orders__skeleton-row" />
        </template>
      </el-skeleton>
      <el-table v-else :data="orders.slice(0, 8)" stripe class="orders__table" empty-text="暂无订单数据">
        <el-table-column prop="orderNo" label="订单号" width="300" align="center" />
        <el-table-column prop="payAmount" label="金额" width="120" align="center">
          <template #default="{ row }">
            <span class="orders__amount">{{ formatAmount(row.payAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'" size="small">
              {{ statusMap[row.status]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" align="center" />
      </el-table>
    </div>
  </div>
</template>

<style lang="scss" scoped>
$orange: #ff6b35;
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
    margin: 0;
  }
}

.dashboard {
  margin: 0 auto;
  max-width: 1400px;

  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 28px;
  }

  &__title {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: -0.3px;
    margin: 0 0 6px;
  }

  &__greeting {
    font-size: 15px;
    color: $text-muted;
    margin: 0;
  }

  &__date {
    font-size: 13px;
    color: $text-muted;
    padding: 4px 12px;
    background: #fff;
    border-radius: 6px;
    border: 1px solid #e2e8f0;
  }
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  span{
    cursor: context-menu;
  }

  &__skeleton {
    display: contents;

    &-card {
      height: 110px;
      border-radius: 12px;
    }
  }

  &__card {
    background: #fff;
    border-radius: 14px;
    padding: 20px 24px;
    border: 1px solid #f1f5f9;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      border-color: #e2e8f0;
    }

    :deep(.el-statistic) {
      --el-statistic-content-font-size: 24px;
      --el-statistic-content-color: #0f172a;
    }

    :deep(.el-statistic__head) {
      margin-bottom: 12px;
    }

    :deep(.el-statistic__content) {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    &--amount :deep(.el-statistic) {
      --el-statistic-content-color: #ff6b35;
    }
  }

  &__icon {
    width: 46px;
    height: 46px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 22px;
    }

    &--orange {
      background: rgba($orange, 0.12);
      color: $orange;
    }

    &--blue {
      background: rgba(#3b82f6, 0.12);
      color: #3b82f6;
    }

    &--green {
      background: rgba(#10b981, 0.12);
      color: #10b981;
    }

    &--purple {
      background: rgba(#8b5cf6, 0.12);
      color: #8b5cf6;
    }
  }

  &__change {
    font-size: 12px;
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 2px;

    &--up {
      color: #10b981;
    }

    &--down {
      color: #ef4444;
    }

    &--flat {
      color: $text-muted;
    }

    &--info {
      color: $text-muted;
    }

    &--orange {
      color: #ff6b35;
    }

    &--green {
      color: #10b981;
    }
  }

  &__info {
    display: flex;
    flex-direction: column;
  }

  &__label {
    font-size: 13px;
    color: $text-muted;
    margin-top: 2px;
  }

  &--secondary {
    margin-bottom: 24px;

    .stats__card {
      padding: 14px 20px;
    }

    .stats__card--sm {
      display: flex;
      align-items: center;
      gap: 14px;
    }

    .stats__label {
      margin-top: 0;
    }
  }

  &__tag {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 700;
    flex-shrink: 0;

    &--warning {
      background: rgba(#e6a23c, 0.12);
      color: #e6a23c;
    }

    &--primary {
      background: rgba($orange, 0.12);
      color: $orange;
    }

    &--success {
      background: rgba(#10b981, 0.12);
      color: #10b981;
    }

    &--danger {
      background: rgba(#f56c6c, 0.12);
      color: #f56c6c;
    }
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 16px;
}

.dashboard__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  &--triple {
    grid-template-columns: 1fr 1fr 1fr;
  }
}

.dashboard__card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.pending {
  &__skeleton {
    display: flex;
    flex-direction: column;
    gap: 12px;

    &-item {
      height: 56px;
      border-radius: 10px;
    }
  }

  &__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 0;

    &+& {
      border-top: 1px solid #f1f5f9;
    }
  }

  &__left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__count {
    font-size: 20px;
    font-weight: 700;
    color: $orange;
    min-width: 32px;
  }

  &__label {
    font-size: 14px;
    color: $text;
  }
}

.quick {
  &__grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-top: 30px;
  }

  &__card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid transparent;

    &:hover {
      background: #f8fafc;
      border-color: #e2e8f0;
    }

    &:active {
      transform: scale(0.97);
    }
  }

  &__icon {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .el-icon {
      font-size: 20px;
    }
  }

  &__label {
    font-size: 14px;
    font-weight: 500;
    color: $text;
  }
}

.overview {
  &__skeleton {
    display: flex;
    flex-direction: column;
    gap: 8px;

    &-item {
      height: 28px;
      border-radius: 6px;
    }
  }

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-top: 30px;
  }

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px;
    border-radius: 10px;
    background: #f8fafc;
    transition: background 0.2s;

    &:hover {
      background: #f1f5f9;
    }
  }

  &__value {
    font-size: 20px;
    font-weight: 700;
    color: #0f172a;
    line-height: 1.2;
  }

  &__label {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 2px;
  }
}

.trend {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  margin-top: 20px;

  &__skeleton {
    display: flex;
    flex-direction: column;
    gap: 12px;

    :deep(.el-skeleton__item) {
      height: 60px;
      border-radius: 10px;
    }
  }

  &__chart {
    width: 100%;
    height: 320px;
  }
}

.orders {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-top: 20px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  span{
    cursor: context-menu;
  }
  &__skeleton {
    display: flex;
    flex-direction: column;
    gap: 10px;

    &-row {
      height: 40px;
      border-radius: 6px;
    }
  }

  &__table {
    width: 100%;
    min-height: 200px;

    :deep(.el-table__row) {
      height: 50px;
    }
  }

  &__amount {
    font-weight: 600;
    color: #ff6b35;
  }
}

@media (max-width: 968px) {
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard__grid {
    grid-template-columns: 1fr;
  }

  .quick__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
