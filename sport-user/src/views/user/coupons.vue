<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMyCoupons, getCouponList, claimCoupon } from '@/api/manager'
import { toast } from '@/utils/toast'

const myCoupons = ref([])
const availableCoupons = ref([])
const activeTab = ref('my')
const couponsFilter = ref('ALL')
const claiming = ref(false)

const statusMap = { 0: '未使用', 1: '已使用', 2: '已过期' }
const typeMap = { 1: '满减券', 2: '折扣券' }

const filters = [
  { key: 'ALL', label: '全部' },
  { key: 0, label: '未使用' },
  { key: 1, label: '已使用' },
  { key: 2, label: '已过期' },
]

function formatDate(d) {
  if (!d) return ''
  const t = new Date(d)
  return `${t.getFullYear()}-${String(t.getMonth() + 1).padStart(2, '0')}-${String(t.getDate()).padStart(2, '0')}`
}

function couponConditionText(c) {
  if (!c.minAmount) return '无门槛'
  return `满¥${c.minAmount}可用`
}

function effectiveStatus(c) {
  if (c.status !== 0) return c.status
  if (c.endTime && new Date(c.endTime) < new Date()) return 2
  return 0
}

const filteredMyCoupons = computed(() => {
  const list = myCoupons.value.map(c => ({ ...c, _status: effectiveStatus(c) }))
  if (couponsFilter.value === 'ALL') return list
  return list.filter(c => c._status === couponsFilter.value)
})

async function fetchMyCoupons() {
  try {
    const res = await getMyCoupons()
    if (res.data.code === 200) myCoupons.value = res.data.data || []
  } catch (e) { console.error(e); myCoupons.value = [] }
}

async function fetchAvailableCoupons() {
  try {
    const res = await getCouponList()
    if (res.data.code === 200) availableCoupons.value = res.data.data || []
  } catch (e) { console.error(e); availableCoupons.value = [] }
}

onMounted(async () => {
  await Promise.all([fetchMyCoupons(), fetchAvailableCoupons()])
})

async function handleClaim(coupon) {
  if (coupon.stock !== undefined && coupon.stock <= 0) {
    toast('库存不足', 'warning')
    return
  }
  claiming.value = true
  try {
    const res = await claimCoupon({ couponId: coupon.id })
    if (res.data.code === 200) {
      if(coupon.pointsCost > 0) {
        toast('兑换成功', 'success')
      } else {
        toast('领取成功', 'success')
      }
      await Promise.all([fetchMyCoupons(), fetchAvailableCoupons()])
    } else {
      if(coupon.pointsCost > 0) {
        toast(res.data.message || '兑换失败', 'error')
      } else {
        toast(res.data.message || '领取失败', 'error')
      }
    }
  } catch (e) {
    console.error(e)
    if(coupon.pointsCost > 0) {
      toast('兑换失败', 'error')
    } else {
      toast('领取失败', 'error')
    }
  }
  claiming.value = false
}

function isClaimed(coupon) {
  return myCoupons.value.some(c => c.couponId === coupon.id)
}

function validityPeriod(c) {
  if (c.startTime && c.endTime) {
    return `${formatDate(c.startTime)} ~ ${formatDate(c.endTime)}`
  }
  return ''
}
</script>

<template>
  <div class="glass-card">
    <div class="glass-card-header">
      <h3><i class="ph ph-ticket"></i> 优惠券</h3>
      <div class="tab-switch">
        <button :class="{ active: activeTab === 'my' }" @click="activeTab = 'my'">我的优惠券</button>
        <button :class="{ active: activeTab === 'available' }" @click="activeTab = 'available'">可领取/兑换</button>
      </div>
    </div>
    <div class="glass-card-body">
      <template v-if="activeTab === 'my'">
        <div class="pill-tabs">
          <button v-for="f in filters" :key="f.key" :class="{ active: couponsFilter === f.key }"
            @click="couponsFilter = f.key">{{ f.label }}</button>
        </div>
        <template v-if="filteredMyCoupons.length === 0">
          <div class="empty-state">
            <i class="ph ph-ticket"></i>
            <p>{{ couponsFilter === 'ALL' ? '暂无优惠券，去领取/兑换一些吧' : '暂无此类优惠券' }}</p>
          </div>
        </template>
        <div v-else class="coupon-list">
          <div v-for="c in filteredMyCoupons" :key="c.id" :class="['coupon-item glass-inner', { disabled: c._status !== 0 }]">
            <div class="coupon-left" :class="{ discount: c.type === 2, used: c.status !== 0 }">
              <div class="coupon-amount">
                <span v-if="c.type === 1" class="coupon-symbol">¥</span>
                <span v-if="c.type === 1">{{ c.value }}</span>
                <span v-if="c.type === 2">{{ c.value }}<span class="coupon-symbol">折</span></span>
              </div>
              <div class="coupon-condition">{{ couponConditionText(c) }}</div>
            </div>
            <div class="coupon-divider"></div>
            <div class="coupon-right">
              <h4>{{ c.name || '优惠券' }}</h4>
              <div class="coupon-meta">
                <span class="coupon-type-tag">{{ typeMap[c.type] || '' }}</span>
                <span class="coupon-period">{{ validityPeriod(c) }}</span>
              </div>
              <span v-if="c._status === 0" class="status-tag usable">可使用</span>
              <span v-else-if="c._status === 1" class="status-tag used">已使用</span>
              <span v-else-if="c._status === 2" class="status-tag expired">已过期</span>
            </div>
          </div>
        </div>
      </template>

      <template v-else>
        <template v-if="availableCoupons.length === 0">
          <div class="empty-state">
            <i class="ph ph-gift"></i>
            <p>暂无可领取/兑换的优惠券</p>
          </div>
        </template>
        <div v-else class="coupon-list">
          <div v-for="c in availableCoupons" :key="c.id" :class="['coupon-item glass-inner', { disabled: c.stock <= 0 }]">
            <div class="coupon-left" :class="{ discount: c.type === 2 }">
              <div class="coupon-amount">
                <span v-if="c.type === 1" class="coupon-symbol">¥</span>
                <span v-if="c.type === 1">{{ c.value }}</span>
                <span v-if="c.type === 2">{{ c.value }}<span class="coupon-symbol">折</span></span>
              </div>
              <div class="coupon-condition">{{ couponConditionText(c) }}</div>
            </div>
            <div class="coupon-divider"></div>
            <div class="coupon-right">
              <h4>{{ c.name || '优惠券' }}</h4>
              <div class="coupon-meta">
                <span class="coupon-type-tag">{{ typeMap[c.type] || '' }}</span>
                <span v-if="c.pointsCost" class="coupon-cost">需 {{ c.pointsCost }} 积分</span>
                <span v-else class="coupon-cost">免费领取</span>
              </div>
              <p class="coupon-period">{{ validityPeriod(c) }}</p>
              <div class="coupon-footer">
                <span class="coupon-stock">剩余 {{ c.stock }} 张</span>
                <button v-if="isClaimed(c)" class="claim-btn claimed" disabled>已领取</button>
                <button v-else-if="c.stock <= 0" class="claim-btn sold-out" disabled>已领完</button>
                <button v-else-if="c.pointsCost" class="claim-btn" :disabled="claiming" @click="handleClaim(c)">{{ claiming ? '兑换中...' : '立即兑换'}}</button>
                <button v-else class="claim-btn" :disabled="claiming" @click="handleClaim(c)">
                  {{ claiming ? '领取中...' : '立即领取'}}
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
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

    .tab-switch {
      display: flex;
      gap: 4px;
      background: $bg-card;
      border-radius: 10px;
      padding: 3px;
      border: 0.5px solid $border-subtle;

      button {
        padding: 7px 16px;
        border: none;
        border-radius: 8px;
        font-size: 13px;
        font-weight: 600;
        color: $text-secondary;
        background: transparent;
        cursor: pointer;
        transition: all 0.3s $transition-premium;

        &:hover { color: $text-primary; }

        &.active {
          background: $accent-energy;
          color: #fff;
        }
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
      background: $accent-energy-soft;
      border-color: $border-accent;
      color: $accent-energy;
    }
  }
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .coupon-item {
    display: flex;
    border-radius: 16px;
    overflow: hidden;
    transition: all 0.5s $transition-premium;
    cursor: context-menu;

    &:hover {
      transform: translateX(8px);
    }

    &.disabled { opacity: 0.45; }

    .coupon-left {
      width: 140px;
      padding: 24px 20px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      background: linear-gradient(135deg, $accent-energy, #e85d26);

      &.discount { background: linear-gradient(135deg, #3b82f6, #6366f1); }
      &.used { background: linear-gradient(135deg, #475569, #64748b); }

      .coupon-amount {
        font-size: 32px;
        font-weight: 800;
        color: #fff;
        line-height: 1;

        .coupon-symbol { font-size: 16px; }
      }

      .coupon-condition {
        margin-top: 8px;
        font-size: 11px;
        color: $text-primary;
      }
    }

    .coupon-divider {
      width: 0;
      border-left: 2px dashed $border-subtle;
    }

    .coupon-right {
      flex: 1;
      padding: 20px 24px;
      display: flex;
      flex-direction: column;
      justify-content: center;
      gap: 6px;

      h4 {
        font-size: 15px;
        font-weight: 600;
        color: $text-primary;
      }

      .coupon-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
      }

      .coupon-type-tag {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 11px;
        font-weight: 600;
        background: $accent-energy-soft;
        color: $accent-energy;
      }

      .coupon-cost {
        font-size: 11px;
        color: $text-muted;
      }

      .coupon-period {
        font-size: 11px;
        color: $text-muted;
      }

      .coupon-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 4px;

        .coupon-stock { font-size: 11px; color: $text-muted; }

        .claim-btn {
          @include btn-base;
          padding: 6px 18px;
          border: none;
          border-radius: 999px;
          font-size: 12px;
          font-weight: 600;
          background: $accent-energy;
          color: #fff;

          &:hover:not(:disabled) {
            @include btn-hover-gradient-primary;
            box-shadow: 0 4px 16px $accent-energy-glow;
          }

          &.claimed {
            background: $bg-card;
            color: $text-muted;
            border: 0.5px solid $border-subtle;
          }

          &.sold-out { background: #475569; }
        }
      }

      .status-tag {
        align-self: flex-start;
        padding: 3px 10px;
        border-radius: 999px;
        font-size: 11px;
        font-weight: 600;

        &.expired, &.used {
          background: rgba(148, 163, 184, 0.15);
          color: $text-muted;
        }

        &.usable {
          background: $accent-energy-soft;
          color: $accent-energy;
        }
      }
    }
  }
}

.glass-inner {
  background: $bg-subtle;
  border: 0.5px solid $border-subtle;
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

  p { font-size: 14px; }
}
</style>
