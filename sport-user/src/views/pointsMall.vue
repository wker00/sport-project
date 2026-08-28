<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessageBox } from 'element-plus'
import { useCounterStore } from '@/stores/counter'
import { getPointsBalance, getPointsGifts, exchangeGift, getExchangeOrders, getUserInfo, confirmExchangeOrder } from '@/api/manager'
import { getCouponList, claimCoupon, getMyCoupons } from '@/api/manager'
import { getSigninStatus, signin } from '@/api/manager'
import { getAddressList, addAddress } from '@/api/manager'
import { toast } from '@/utils/toast'

const router = useRouter()
const store = useCounterStore()
const { userInfo } = storeToRefs(store)

const pointsBalance = ref(0)
const gifts = ref([])
const exchangeOrders = ref([])
const availableCoupons = ref([])
const myCoupons = ref([])
const loading = ref(true)
const activeTab = ref('gifts')
const signinStatus = ref({ signedIn: false, streakDays: 0, todayBonus: 20 })
const signingIn = ref(false)
const exchangeDialogVisible = ref(false)
const exchangeGiftData = ref(null)
const addressList = ref([])
const selectedAddressId = ref(null)
const exchangeRemark = ref('')
const submitLoading = ref(false)
const confirmingOrderId = ref(null)
const showAddAddress = ref(false)
const addressFormRef = ref(null)
const addressSubmitting = ref(false)
const addressForm = reactive({
  receiverName: '', phone: '', province: '', city: '', district: '', address: '', isDefault: 0,
})
const addressFormRules = {
  receiverName: [{ required: true, message: '请输入收件人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  province: [{ required: true, message: '请选择省份', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  district: [{ required: true, message: '请选择区县', trigger: 'change' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
}

const isLoggedIn = computed(() => !!store.token)

const totalConsumption = computed(() => userInfo.value.totalConsumption || 0)

const levelConfig = [
  { id: 1, name: '普通', threshold: 0 },
  { id: 2, name: '银卡', threshold: 2000 },
  { id: 3, name: '金卡', threshold: 7000 },
  { id: 4, name: '钻石', threshold: 17000 },
  { id: 5, name: '黑金', threshold: 67000 },
]

const currentLevel = computed(() => {
  const id = userInfo.value.userLevel || 1
  return levelConfig.find(l => l.id === id) || levelConfig[0]
})

const nextLevel = computed(() => {
  const idx = levelConfig.findIndex(l => l.id === currentLevel.value.id)
  return idx < levelConfig.length - 1 ? levelConfig[idx + 1] : null
})

const progressPercent = computed(() => {
  if (!nextLevel.value) return 100
  const cur = Math.max(totalConsumption.value, currentLevel.value.threshold)
  return Math.min(((cur - currentLevel.value.threshold) / (nextLevel.value.threshold - currentLevel.value.threshold)) * 100, 100)
})

const consumptionToNext = computed(() => nextLevel.value ? Math.max(0, nextLevel.value.threshold - totalConsumption.value) : 0)

const orderStatusMap = { 0: '待发货', 1: '已发货', 2: '已完成' }

function formatDateTime(d) {
  if (!d) return ''
  const t = new Date(d)
  const pad = (n) => String(n).padStart(2, '0')
  return `${t.getFullYear()}-${pad(t.getMonth() + 1)}-${pad(t.getDate())} ${pad(t.getHours())}:${pad(t.getMinutes())}`
}

async function fetchSigninStatus() {
  if (!isLoggedIn.value) return
  try {
    const res = await getSigninStatus()
    if (res.data.code === 200) signinStatus.value = res.data.data || { signedIn: false, streakDays: 0, todayBonus: 20 }
  } catch { }
}

async function handleSignin() {
  if (signingIn.value) return
  signingIn.value = true
  try {
    const res = await signin()
    if (res.data.code === 200) {
      const data = res.data.data
      let msg = `签到成功，获得 ${data.pointsEarned} 积分`
      if (data.bonusPoints > 0) msg += `\n连续 ${data.streakDays} 天额外奖励 ${data.bonusPoints} 积分`
      msg += `\n当前总积分：${data.totalBalance.toLocaleString()}`
      await ElMessageBox.alert(msg, '签到成功', {
        confirmButtonText: '太棒了', type: 'success', lockScroll: false,
      })
      signinStatus.value.signedIn = true
      signinStatus.value.streakDays = data.streakDays
      pointsBalance.value = data.totalBalance
    } else {
      toast(res.data.message || '签到失败', 'error')
    }
  } catch (e) { console.error(e) }
  signingIn.value = false
}

async function fetchAll() {
  loading.value = true
  try {
    const calls = [getPointsGifts()]
    if (isLoggedIn.value) {
      calls.push(
        getPointsBalance().then(r => { if (r.data.code === 200) pointsBalance.value = r.data.data || 0 }),
        getExchangeOrders().then(r => { if (r.data.code === 200) exchangeOrders.value = r.data.data || [] }),
        getMyCoupons().then(r => { if (r.data.code === 200) myCoupons.value = r.data.data || [] }),
        getUserInfo().then(r => { if (r.data.code === 200) store.setUserInfo(r.data.data) }),
      )
    }
    calls.push(
      getCouponList().then(r => { if (r.data.code === 200) availableCoupons.value = r.data.data || [] })
    )
    const [giftRes] = await Promise.all(calls)
    if (giftRes.data.code === 200) gifts.value = giftRes.data.data || []
  } catch (e) { console.error(e) }
  loading.value = false
}

onMounted(() => { fetchAll(); fetchSigninStatus() })

function isCouponClaimed(coupon) { return myCoupons.value.some(c => c.couponId === coupon.id) }
function couponValueText(c) { if (c.type === 1) return `¥${c.value}`; return `${c.value}折` }
function couponConditionText(c) {
  if (c.type === 1) return c.minAmount ? `满${c.minAmount}元减${c.value}元` : `减${c.value}元`
  return c.minAmount ? `满${c.minAmount}元可用` : '无门槛'
}

async function handleExchange(gift) {
  if (!isLoggedIn.value) { toast('请先登录', 'error'); return }
  if (gift.stock !== undefined && gift.stock <= 0) { toast('库存不足', 'warning'); return }
  try {
    const res = await getAddressList()
    if (res.data.code !== 200) {
      toast(res.data.message || '获取地址失败', 'error')
      return
    }
    if (!res.data.data || res.data.data.length === 0) {
      await ElMessageBox.confirm('您还没有收货地址，请先添加地址', '提示', {
        confirmButtonText: '去添加', cancelButtonText: '取消', type: 'warning', lockScroll: false,
      })
      router.push('/user/address')
      return
    }
    addressList.value = res.data.data
    exchangeGiftData.value = gift
    selectedAddressId.value = res.data.data.find(a => a.isDefault === 1)?.id || res.data.data[0]?.id
    exchangeRemark.value = ''
    exchangeDialogVisible.value = true
  } catch (e) {
    console.error(e); toast('获取地址失败，请稍后重试', 'error')
  }
}

async function submitExchange() {
  if (!selectedAddressId.value) { toast('请选择收货地址', 'warning'); return }
  try {
    await ElMessageBox.confirm('确认兑换后订单将不可取消，请确认收货地址相关信息无误', '提示', {
      confirmButtonText: '确认兑换', cancelButtonText: '取消', type: 'warning', lockScroll: false,
    })
  } catch { return }
  submitLoading.value = true
  try {
    const res = await exchangeGift({
      giftId: exchangeGiftData.value.id,
      addressId: selectedAddressId.value,
      remark: exchangeRemark.value || undefined,
    })
    if (res.data.code === 200) {
      toast('兑换成功！', 'success')
      exchangeDialogVisible.value = false
      await fetchAll()
    } else {
      toast(res.data.message || '兑换失败', 'error')
    }
  } catch { }
  submitLoading.value = false
}

async function submitNewAddress() {
  if (!addressFormRef.value) return
  try { await addressFormRef.value.validate() } catch { return }
  addressSubmitting.value = true
  try {
    const res = await addAddress({ ...addressForm })
    if (res.data.code === 200) {
      toast('地址添加成功', 'success')
      showAddAddress.value = false
      addressForm.receiverName = ''; addressForm.phone = ''
      addressForm.province = ''; addressForm.city = ''
      addressForm.district = ''; addressForm.address = ''
      addressForm.isDefault = 0
      const addrRes = await getAddressList()
      if (addrRes.data.code === 200) {
        addressList.value = addrRes.data.data || []
        const newId = res.data.data?.id
        if (newId) selectedAddressId.value = newId
      }
    } else {
      toast(res.data.message || '添加地址失败', 'error')
    }
  } catch { toast('添加地址失败', 'error') }
  addressSubmitting.value = false
}

async function handleConfirmExchange(order) {
  if (confirmingOrderId.value) return
  try {
    await ElMessageBox.confirm(`确定已收到「${order.giftName}」？`, '确认收货', {
      confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'info', lockScroll: false,
    })
    confirmingOrderId.value = order.id
    const res = await confirmExchangeOrder(order.id)
    if (res.data.code === 200) { toast('确认收货成功', 'success'); await fetchAll() }
    else { toast(res.data.message || '操作失败', 'error') }
  } catch { }
  confirmingOrderId.value = null
}

async function handleClaimCoupon(coupon) {
  if (!isLoggedIn.value) { toast('请先登录', 'error'); return }
  try {
    await ElMessageBox.confirm(`确定花费 ${coupon.pointsCost} 积分兑换「${coupon.name}」？`, '确认兑换', {
      confirmButtonText: '确认兑换', cancelButtonText: '取消', type: 'info', lockScroll: false,
    })
    const res = await claimCoupon({ couponId: coupon.id })
    if (res.data.code === 200) { toast('兑换成功，已放入我的优惠券', 'success'); await fetchAll() }
    else { toast(res.data.message || '兑换失败', 'error') }
  } catch { }
}
</script>

<template>
  <div class="points-page">
    <div class="points-hero">
      <div class="points-hero-glow g1"></div>
      <div class="points-hero-glow g2"></div>
      <div class="hero-deco d1"></div>
      <div class="hero-deco d2"></div>
      <div class="points-hero-inner">
        <div class="hero-top">
          <div class="hero-top-left">
            <div class="hero-icon-badge">
              <i class="ph-fill ph-coin"></i>
            </div>
            <div class="hero-text">
              <h1>积分商城</h1>
              <p>运动积分当钱花，兑换超多运动好物</p>
            </div>
          </div>
          <div class="hero-top-right">
            <div v-if="isLoggedIn" class="hero-balance">
              <span class="big-num">{{ pointsBalance.toLocaleString() }}</span>
              <span class="label">可用积分</span>
            </div>
          </div>
        </div>

        <div class="hero-stats">
          <div class="stats-row">
            <span v-if="isLoggedIn" class="stat-item"><i class="ph ph-arrow-clockwise"></i> 已兑换 {{ exchangeOrders.length
              }} 件</span>
            <span class="stat-item"><i class="ph ph-gift"></i> {{ gifts.length }} 种商品</span>
          </div>
          <div class="hero-actions">
            <button v-if="!isLoggedIn" class="hero-btn" @click="$router.push('/user/login')">
              <span class="btn-label">登录查看</span>
              <span class="btn-icon-wrap"><i class="ph ph-sign-in"></i></span>
            </button>
            <template v-else>
              <button v-if="!signinStatus.signedIn" class="hero-btn signin-btn" :disabled="signingIn"
                @click="handleSignin">
                <span class="btn-icon-wrap"><i class="ph ph-calendar-check"></i></span>
                <span class="btn-label">{{ signingIn ? '签到中...' : '点击签到' }}</span>
              </button>
              <button v-else class="hero-btn signed-btn">
                <span class="btn-icon-wrap"><i class="ph-fill ph-calendar-check"></i></span>
                <span class="btn-label">已签到 {{ signinStatus.streakDays }} 天</span>
              </button>
            </template>
          </div>
        </div>

        <div v-if="isLoggedIn" class="hero-level">
          <div class="level-badge"><span class="level-name">{{ currentLevel.name }}会员</span></div>
          <div class="level-progress-wrap">
            <div class="level-bar-track">
              <div class="level-bar-fill" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <span v-if="nextLevel" class="level-text">距{{ nextLevel.name }}会员还需花费 <strong>￥{{
              consumptionToNext.toLocaleString()
                }}</strong></span>
            <span v-else class="level-text">已达最高等级</span>
          </div>
        </div>
      </div>
    </div>

    <div class="points-rules">
      <div class="rule-card glass-inner">
        <div class="rule-icon"><i class="ph ph-shopping-cart"></i></div>
        <h4>购物积分</h4>
        <p>每消费1元可获得10积分，订单完成后自动到账</p>
      </div>
      <div class="rule-card glass-inner">
        <div class="rule-icon vitality"><i class="ph ph-calendar-check"></i></div>
        <h4>签到积分</h4>
        <p>每日签到可获得20-50积分，连续签到7天额外奖励100积分</p>
        <div v-if="isLoggedIn" class="rule-extra">
          <template v-if="signinStatus.signedIn">
            <span class="rule-badge signed">已签到 {{ signinStatus.streakDays }} 天</span>
          </template>
          <template v-else>
            <span class="rule-badge unsign" @click="handleSignin">今日签到可得 {{ signinStatus.todayBonus }} 积分 →</span>
          </template>
        </div>
      </div>
      <div class="rule-card glass-inner">
        <div class="rule-icon"><i class="ph ph-users"></i></div>
        <h4>评价积分</h4>
        <p>订单确认收货后评价商品可获得相应积分奖励</p>
      </div>
    </div>

    <div class="points-layout">
      <aside class="points-sidebar">
        <div class="sidebar-shell">
          <div class="sidebar-header">积分分类</div>
          <nav class="sidebar-nav">
            <a :class="{ active: activeTab === 'gifts' }" @click="activeTab = 'gifts'"><i class="ph ph-gift"></i>
              积分商品</a>
            <a :class="{ active: activeTab === 'coupons' }" @click="activeTab = 'coupons'"><i class="ph ph-ticket"></i>
              优惠券</a>
            <a v-if="isLoggedIn" :class="{ active: activeTab === 'exchange' }" @click="activeTab = 'exchange'"><i
                class="ph ph-arrow-clockwise"></i> 兑换记录</a>
          </nav>
        </div>
      </aside>

      <div class="points-content">
        <template v-if="loading">
          <div class="empty-state"><i class="ph ph-spinner ph-spin"></i>
            <p>加载中...</p>
          </div>
        </template>

        <template v-else-if="activeTab === 'gifts'">
          <template v-if="gifts.length === 0">
            <div class="empty-state"><i class="ph ph-gift"></i>
              <p>暂无积分商品</p>
            </div>
          </template>
          <div v-else class="products-grid">
            <div v-for="g in gifts" :key="g.id" class="product-card glass-inner">
              <div class="product-image">
                <img v-if="g.image" :src="g.image" :alt="g.name" />
                <i v-else class="ph ph-gift"></i>
              </div>
              <div class="product-info">
                <h4>{{ g.name }}</h4>
                <p class="product-desc">{{ g.description ? g.description : '暂无描述' }}</p>
                <div class="points-cost"><i class="ph-fill ph-coin"></i><span class="num">{{ g.pointsPrice }}
                    <span>积分</span></span></div>
                <div class="stock-info">剩余 <span>{{ g.stock }}</span> 件</div>
                <button class="exchange-btn" :disabled="g.stock <= 0" @click="handleExchange(g)">{{ g.stock <= 0 ? '已兑完'
                  : '立即兑换' }}</button>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeTab === 'coupons'">
          <template v-if="availableCoupons.length === 0">
            <div class="empty-state"><i class="ph ph-ticket"></i>
              <p>暂无可用优惠券</p>
            </div>
          </template>
          <div v-else class="coupons-scroll">
            <div v-for="c in availableCoupons" :key="c.id" class="coupon-card glass-inner">
              <div class="coupon-banner">
                <div class="value">{{ couponValueText(c) }}</div>
                <div class="desc">{{ couponConditionText(c) }}</div>
              </div>
              <div class="coupon-info">
                <div class="coupon-name">{{ c.name }}</div>
                <div class="coupon-points">需 <span>{{ c.pointsCost }}</span> 积分</div>
                <div class="coupon-stock" v-if="c.stock !== undefined">剩余 {{ c.stock }} 张</div>
                <button class="coupon-btn" :class="{ owned: isCouponClaimed(c) }"
                  :disabled="isCouponClaimed(c) || c.stock <= 0" @click="handleClaimCoupon(c)">
                  {{ isCouponClaimed(c) ? '已领取' : c.stock <= 0 ? '已领完' : '立即兑换' }} </button>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="activeTab === 'exchange'">
          <div class="section-header">
            <h2>兑换记录</h2>
          </div>
          <template v-if="exchangeOrders.length === 0">
            <div class="empty-state"><i class="ph ph-arrow-clockwise"></i>
              <p>暂无兑换记录</p>
            </div>
          </template>
          <div v-else class="history-table glass-inner">
            <div class="history-header">
              <span>商品</span><span>状态</span><span>积分</span><span>单号</span><span>兑换时间</span><span>操作</span>
            </div>
            <div v-for="o in exchangeOrders" :key="o.id" class="history-row">
              <div class="history-type expense">
                <div class="history-image">
                  <img v-if="o.giftImage" :src="o.giftImage" alt="">
                  <i v-else class="ph ph-gift"></i>
                </div>
                <div class="history-gift-info">
                  <span class="gift-name">{{ o.giftName }}</span>
                </div>
              </div>
              <span class="history-status">
                <span :class="['status-tag', `s-${o.status}`]">{{ orderStatusMap[o.status] || '未知' }}</span>
              </span>
              <span class="history-points expense">-{{ o.pointsPrice }}</span>
              <span class="history-no">{{ o.orderNo }}</span>
              <span class="history-time">{{ formatDateTime(o.createTime) }}</span>
              <span class="history-action">
                <button v-if="o.status === 1" class="confirm-btn" :disabled="confirmingOrderId === o.id"
                  @click="handleConfirmExchange(o)">
                  {{ confirmingOrderId === o.id ? '处理中...' : '确认收货' }}
                </button>
                <span v-else-if="o.status === 2" class="done-text">已完成</span>
                <span v-else class="done-text">—</span>
              </span>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>

  <el-dialog v-model="exchangeDialogVisible" title="确认兑换" width="520px" :close-on-click-modal="false" lock-scroll>
    <div v-if="exchangeGiftData" class="exchange-dialog-body">
      <div class="exchange-gift-preview">
        <div class="exchange-gift-img">
          <img v-if="exchangeGiftData.image" :src="exchangeGiftData.image" :alt="exchangeGiftData.name">
          <i v-else class="ph ph-gift"></i>
        </div>
        <div class="exchange-gift-info">
          <h4>{{ exchangeGiftData.name }}</h4>
          <p class="exchange-desc">{{ exchangeGiftData.description || '暂无描述' }}</p>
          <div class="exchange-cost"><i class="ph-fill ph-coin"></i> 需要 <strong>{{ exchangeGiftData.pointsPrice
              }}</strong> 积分</div>
        </div>
      </div>

      <div class="exchange-section">
        <div class="exchange-section-title">
          <i class="ph ph-map-pin"></i> 收货地址
          <button v-if="!showAddAddress && addressList.length > 0" class="add-addr-inline" @click="showAddAddress = true">
            <i class="ph ph-plus-circle"></i> 新建
          </button>
        </div>

        <div v-if="showAddAddress" class="exchange-address-form">
          <el-form ref="addressFormRef" :model="addressForm" :rules="addressFormRules" label-width="70px" size="small">
            <el-form-item label="收件人" prop="receiverName">
              <el-input v-model="addressForm.receiverName" placeholder="收件人姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="addressForm.phone" placeholder="手机号" maxlength="11" />
            </el-form-item>
            <el-form-item label="所在地区" prop="district">
              <div class="region-row">
                <el-input v-model="addressForm.province" placeholder="省" />
                <el-input v-model="addressForm.city" placeholder="市" />
                <el-input v-model="addressForm.district" placeholder="区" />
              </div>
            </el-form-item>
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="addressForm.address" placeholder="街道/门牌号/小区名" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="addressForm.isDefault" :true-value="1" :false-value="0">设为默认地址</el-checkbox>
            </el-form-item>
            <div class="form-actions">
              <button class="form-btn cancel" type="button" @click="showAddAddress = false">取消</button>
              <button class="form-btn submit" type="button" :disabled="addressSubmitting" @click="submitNewAddress">
                {{ addressSubmitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </el-form>
        </div>

        <template v-if="!showAddAddress">
          <div v-if="addressList.length === 0" class="exchange-empty-address">
            <p>暂无地址，请先添加地址</p>
          </div>
          <div v-else class="exchange-address-list">
            <div v-for="addr in addressList" :key="addr.id"
              :class="['exchange-address-item', { active: selectedAddressId === addr.id }]"
              @click="selectedAddressId = addr.id">
              <div class="exchange-addr-radio">
                <span :class="['radio-dot', { active: selectedAddressId === addr.id }]"></span>
              </div>
              <div class="exchange-addr-info">
                <div class="exchange-addr-name">
                  <span>{{ addr.receiverName }}</span>
                  <span class="addr-phone">{{ addr.phone }}</span>
                  <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
                </div>
                <div class="exchange-addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.address }}
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <div class="exchange-section">
        <div class="exchange-section-title"><i class="ph ph-note-pencil"></i> 备注（可选）</div>
        <el-input class="remark-input" v-model="exchangeRemark" placeholder="填写备注信息..." maxlength="50" show-word-limit />
      </div>
    </div>

    <template #footer>
      <div class="exchange-dialog-footer">
        <button class="dialog-btn cancel" @click="exchangeDialogVisible = false">取消</button>
        <button class="dialog-btn confirm" :disabled="submitLoading" @click="submitExchange">
          {{ submitLoading ? '兑换中...' : '兑换' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

p,
h1,
h2,
h3,
h4,
h5 {
  cursor: context-menu;
}

.points-page {
  padding-top: 120px;
  padding-bottom: 100px;
  max-width: 1400px;
  margin: 0 auto;
  width: 92%;
}

.points-hero {
  position: relative;
  background: var(--border-glass);
  border-radius: $radius-squircle;
  padding: 4px;
  box-shadow: inset 0 0 0 0.5px var(--border-glass);
  margin-bottom: 48px;
  overflow: hidden;

  .points-hero-glow {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
    filter: blur(60px);
    z-index: 0;

    &.g1 {
      width: 300px;
      height: 300px;
      background: $accent-energy-soft;
      left: -80px;
      bottom: -60px;
    }

    &.g2 {
      width: 200px;
      height: 200px;
      background: rgba(0, 212, 170, 0.06);
      left: 50%;
      top: -40px;
    }
  }

  .hero-deco {
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
    z-index: 0;

    &.d1 {
      width: 160px;
      height: 160px;
      border: 1px solid $border-subtle;
      top: -40px;
      left: 35%;
      animation: hero-float 8s ease-in-out infinite;
    }

    &.d2 {
      width: 50px;
      height: 50px;
      border: 1px solid $border-subtle;
      top: 18%;
      right: 12%;
      animation: hero-float 7s ease-in-out infinite 1.5s;
    }
  }

  .points-hero-inner {
    position: relative;
    z-index: 1;
    border-radius: calc(#{$radius-squircle} - 6px);
    backdrop-filter: blur(20px);
    box-shadow:
      inset 0 1px 0 var(--bg-glass-hover),
      var(--shadow-glass);
    padding: 40px;
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .hero-top {
    display: flex;
    justify-content: space-between;
    gap: 48px;
  }

  .hero-top-left {
    display: flex;
    align-items: center;
    gap: 20px;
    flex: 1;
    min-width: 0;
  }

  .hero-top-right {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-shrink: 0;
    cursor: context-menu;
  }

  .hero-icon-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 50%;
    background: linear-gradient(135deg, $accent-energy, #ff8c5a);
    font-size: 26px;
    color: #fff;
    box-shadow: 0 8px 24px $accent-energy-glow;
    flex-shrink: 0;
    transition: transform 0.5s $transition-premium;

    &:hover {
      transform: scale(1.05) rotate(-8deg);
    }
  }

  .hero-text {
    h1 {
      font-size: 36px;
      font-weight: 800;
      letter-spacing: -0.03em;
      color: $text-primary;
      margin-bottom: 6px;
    }

    p {
      font-size: 15px;
      color: $text-secondary;
      line-height: 1.6;
      max-width: 40ch;
    }
  }

  .hero-balance {
    display: flex;
    align-items: baseline;
    gap: 12px;

    .big-num {
      font-size: 60px;
      font-weight: 800;
      color: $accent-energy;
      letter-spacing: -0.02em;
      line-height: 1;
      font-variant-numeric: tabular-nums;
    }

    .label {
      font-size: 16px;
      font-weight: 600;
      color: $text-muted;
    }
  }

  .hero-stats {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
  }

  .stats-row {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    cursor: context-menu;

    .stat-item {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 6px 16px;
      background: $bg-card;
      backdrop-filter: blur(4px);
      border-radius: 20px;
      font-size: 13px;
      color: $text-secondary;
      font-weight: 500;
      border: 0.5px solid $border-subtle;
      transition: all 0.4s $transition-premium;

      &:hover {
        border-color: $border-accent;
        transform: translateY(-1px);
      }

      i {
        font-size: 15px;
        color: $text-muted;
      }
    }
  }

  .hero-level {
    display: flex;
    align-items: center;
    gap: 20px;
    padding-top: 20px;
    border-top: 0.5px solid $border-subtle;
    cursor: context-menu;

    .level-badge {
      flex-shrink: 0;

      .level-name {
        font-size: 14px;
        font-weight: 700;
        color: $text-primary;
        white-space: nowrap;
      }
    }

    .level-progress-wrap {
      flex: 1;
      min-width: 0;

      .level-bar-track {
        height: 6px;
        background: $bg-card-hover;
        border-radius: 3px;
        overflow: hidden;

        .level-bar-fill {
          height: 100%;
          background: linear-gradient(90deg, $accent-energy, #ff8c5a);
          border-radius: 3px;
          transition: width 0.8s $transition-premium;
        }
      }

      .level-text {
        display: block;
        margin-top: 6px;
        font-size: 12px;
        color: $text-muted;
        font-weight: 500;

        strong {
          color: $text-secondary;
          font-weight: 700;
        }
      }
    }
  }

  .hero-actions {
    flex-shrink: 0;

    .hero-btn {
      @include btn-base;
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 20px 8px 24px;
      background: rgba($accent-energy, 0.12);
      border: 0.5px solid $border-accent;
      border-radius: 12px;
      color: $accent-energy;
      font-size: 14px;
      font-weight: 700;
      transition: all 0.5s $transition-premium;

      .btn-label {
        position: relative;
        z-index: 1;
      }

      .btn-icon-wrap {
        @include btn-inner-icon;
        background: rgba($accent-energy, 0.15);
        font-size: 14px;
        transition: all 0.5s $transition-premium;
      }

      &:hover:not(:disabled) {
        box-shadow: 0 8px 32px $accent-energy-glow;

        .btn-icon-wrap {
          transform: translateX(2px) scale(1.05);
        }
      }

      &:disabled {
        opacity: 0.3;
        cursor: not-allowed;
        pointer-events: none;
      }
    }

    .signin-btn {
      background: rgba(255, 107, 53, 0.15);
      font-size: 15px;
      animation: pulse-glow 2s ease-in-out infinite;

      &:hover {
        background: $accent-energy;
        background: rgba(255, 107, 53, 0.15);
        transform: translateY(-2px) scale(1.02);
      }
    }

    @keyframes pulse-glow {

      0%,
      100% {
        box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.3);
      }

      50% {
        box-shadow: 0 0 24px 6px rgba(255, 255, 255, 0.12);
      }
    }

    .signed-btn {
      background: rgba($accent-vitality, 0.12);
      border-color: rgba($accent-vitality, 0.2);
      color: #0aa889;
      animation: none;
      cursor: context-menu;

      &:hover {
        background: rgba($accent-vitality, 0.15) !important;
        box-shadow: none !important;

        .btn-icon-wrap {
          transform: translateY(0px) !important;
        }
      }

      .btn-icon-wrap {
        background: rgba($accent-vitality, 0.15);
        color: #0aa889;
      }

    }
  }
}

@keyframes pulse-glow {

  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(255, 107, 53, 0.3);
  }

  50% {
    box-shadow: 0 0 24px 6px rgba(255, 107, 53, 0.12);
  }
}

@keyframes hero-float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-14px);
  }
}

.points-rules {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 48px;

  .rule-card {
    padding: 24px;
    border-radius: $radius-xl;
    text-align: center;
    transition: all 0.5s $transition-premium;

    &:hover {
      transform: translateY(-4px);
      border-color: $border-accent;
    }

    .rule-icon {
      width: 60px;
      height: 60px;
      margin: 0 auto 16px;
      border-radius: 50%;
      background: $accent-energy-soft;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: $accent-energy;

      &.vitality {
        background: rgba($accent-vitality, 0.1);
        color: $accent-vitality;
      }
    }

    h4 {
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 8px;
      color: $text-primary;
    }

    p {
      font-size: 13px;
      color: $text-secondary;
      line-height: 1.6;
      margin-bottom: 12px;
    }

    .rule-extra {
      margin-top: 4px;
    }

    .rule-badge {
      display: inline-block;
      padding: 4px 14px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;

      &.signed {
        background: $vitality-soft;
        color: #0aa889;
        cursor: default;
      }

      &.unsign {
        background: $accent-energy-soft;
        color: $accent-energy;

        &:hover {
          background: $accent-energy;
          color: #fff;
        }
      }
    }
  }
}

.points-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 32px;
}

.glass-inner {
  background: $bg-subtle;
  border: 0.5px solid $border-subtle;
}

.sidebar-shell {
  @include solid-card;
  border-radius: $radius-xl;
  overflow: hidden;
  position: sticky;
  top: 100px;

  .sidebar-header {
    padding: 16px 20px;
    border-bottom: 0.5px solid $border-subtle;
    font-size: 15px;
    font-weight: 600;
    color: $text-primary;
  }

  .sidebar-nav {
    padding: 12px;

    a {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 14px 16px;
      border-radius: 12px;
      font-size: 14px;
      color: $text-secondary;
      cursor: pointer;
      transition: all 0.5s $transition-premium;

      &:hover {
        background: $bg-card-hover;
        color: $text-primary;
        transform: translateX(4px);
      }

      &.active {
        background: $accent-energy-soft;
        color: $accent-energy;
      }

      i {
        font-size: 20px;
      }
    }
  }
}

.points-content {
  min-width: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    font-size: 24px;
    font-weight: 700;
    color: $text-primary;
  }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 48px;

  .product-card {
    background: var(--bg-card);
    border-radius: $radius-xl;
    overflow: hidden;
    transition: all 0.5s $transition-premium;
    cursor: context-menu;

    &:hover {
      transform: translateY(-6px);
      border-color: $accent-energy;
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
    }

    .product-image {
      height: 160px;
      background: linear-gradient(135deg, rgba(255, 107, 53, 0.08), rgba(0, 212, 170, 0.05));
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 56px;
      color: $accent-energy;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .product-info {
      padding: 20px;
    }

    .product-info h4 {
      font-size: 15px;
      font-weight: 700;
      margin-bottom: 8px;
      color: $text-primary;
    }

    .product-desc {
      font-size: 12px;
      color: $text-muted;
      margin-bottom: 8px;
      line-height: 1.4;
    }

    .points-cost {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 8px;

      i {
        font-size: 18px;
        color: $accent-energy;
      }

      .num {
        font-size: 20px;
        font-weight: 700;
        color: $accent-energy;

        span {
          font-size: 12px;
          font-weight: 500;
        }
      }
    }

    .stock-info {
      font-size: 12px;
      color: $text-muted;
      margin-bottom: 16px;

      span {
        color: $accent-energy;
        font-weight: 600;
      }
    }

    .exchange-btn {
      @include btn-base;
      transition-property: transform, box-shadow, color, border-color, opacity;
      width: 100%;
      padding: 12px;
      background: $accent-energy;
      border: none;
      border-radius: 10px;
      color: #fff;
      font-size: 13px;
      font-weight: 700;

      &:hover:not(:disabled) {
        box-shadow: 0 8px 24px $accent-energy-glow;
        @include btn-hover-gradient-primary;
      }

      &:disabled {
        background: $bg-card-hover;
        color: $text-muted;
        box-shadow: none;
      }
    }
  }
}

.coupons-scroll {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  cursor: context-menu;

  .coupon-card {
    flex-shrink: 0;
    width: 220px;
    border-radius: $radius-xl;
    overflow: hidden;
    transition: all 0.5s $transition-premium;

    &:hover {
      transform: translateY(-4px);
      border-color: $border-accent;
    }

    .coupon-banner {
      padding: 20px;
      text-align: center;
      background: linear-gradient(135deg, rgba(0, 212, 170, 0.08), rgba(255, 107, 53, 0.06));

      .value {
        font-size: 32px;
        font-weight: 800;
        color: $accent-vitality;
      }

      .desc {
        font-size: 12px;
        color: $text-secondary;
        margin-top: 4px;
      }
    }

    .coupon-info {
      padding: 16px;
    }

    .coupon-name {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 8px;
      color: $text-primary;
    }

    .coupon-points {
      font-size: 12px;
      color: $text-muted;
      margin-bottom: 4px;

      span {
        color: $accent-vitality;
        font-weight: 600;
      }
    }

    .coupon-stock {
      font-size: 11px;
      color: $text-muted;
      margin-bottom: 12px;
    }

    .coupon-btn {
      @include btn-base;
      width: 100%;
      padding: 10px;
      background: rgba(0, 212, 170, 0.1);
      border: 0.5px solid rgba(0, 212, 170, 0.2);
      border-radius: 8px;
      color: $accent-vitality;
      font-size: 12px;
      font-weight: 700;

      &:hover:not(:disabled) {
        @include btn-hover-gradient-vitality;
      }

      &.owned {
        background: $bg-card;
        border-color: $border-subtle;
        color: $text-muted;
      }
    }
  }
}

.history-table {
  border-radius: $radius-xl;
  overflow: hidden;

  .history-header {
    display: grid;
    grid-template-columns: 2fr 0.8fr 0.8fr 1.2fr 1fr 1fr;
    padding: 16px 24px;
    background: $bg-subtle;
    font-size: 13px;
    font-weight: 600;
    color: $text-muted;
    border-bottom: 0.5px solid $border-subtle;
  }

  .history-row {
    display: grid;
    grid-template-columns: 2fr 0.8fr 0.8fr 1.2fr 1fr 1fr;
    padding: 16px 24px;
    border-bottom: 0.5px solid $border-subtle;
    font-size: 14px;
    color: $text-secondary;
    align-items: center;
    transition: background 0.3s;
    cursor: context-menu;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: $bg-subtle;
    }
  }

  .history-type {
    display: flex;
    align-items: center;
    gap: 10px;

    i {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
    }

    &.expense i {
      background: $accent-energy-soft;
      color: $accent-energy;
    }

    .history-image {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      overflow: hidden;
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .history-time {
    color: $text-muted;
    font-size: 13px;
  }

  .history-no {
    font-size: 12px;
    color: $text-muted;
  }

  .history-points {
    font-weight: 700;
  }

  .history-points.expense {
    color: $accent-energy;
  }

  .history-status {
    .status-tag {
      display: inline-block;
      padding: 3px 10px;
      border-radius: 999px;
      font-size: 12px;
      font-weight: 600;

      &.s-0 {
        background: $accent-energy-soft;
        color: $accent-energy;
      }

      &.s-1 {
        background: rgba(#3b82f6, 0.12);
        color: #3b82f6;
      }

      &.s-2 {
        background: rgba($accent-vitality, 0.12);
        color: $accent-vitality;
      }
    }
  }

  .history-gift-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .gift-name {
      font-weight: 600;
    }

    .gift-logistics {
      font-size: 11px;
      color: $text-muted;
    }
  }

  .history-action {
    .confirm-btn {
      @include btn-base;
      padding: 6px 14px;
      border: none;
      border-radius: 8px;
      font-size: 12px;
      font-weight: 600;
      background: #3b82f6;
      color: #fff;
      transition: all 0.3s $transition-premium;

      &:hover:not(:disabled) {
        box-shadow: 0 4px 16px rgba(#3b82f6, 0.3);
        transform: translateY(-1px);
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .done-text {
      font-size: 12px;
      color: $text-muted;
    }
  }
}

.exchange-dialog-body {
  .exchange-gift-preview {
    display: flex;
    gap: 20px;
    padding: 20px;
    background: $bg-subtle;
    border-radius: $radius-xl;
    margin-bottom: 24px;
    border: 0.5px solid $border-subtle;

    .exchange-gift-img {
      width: 80px;
      height: 80px;
      border-radius: 12px;
      overflow: hidden;
      flex-shrink: 0;
      background: $accent-energy-soft;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
      color: $accent-energy;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .exchange-gift-info {
      flex: 1;
      min-width: 0;

      h4 {
        font-size: 16px;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .exchange-desc {
        font-size: 13px;
        color: $text-secondary;
        margin-bottom: 8px;
      }

      .exchange-cost {
        font-size: 14px;
        color: $text-muted;

        i {
          color: $accent-energy;
        }

        strong {
          color: $accent-energy;
          font-weight: 700;
        }
      }
    }
  }

  .exchange-section {
    margin-bottom: 20px;

    .exchange-section-title {
      font-size: 14px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 6px;

      i {
        color: $accent-energy;
      }
    }

    .remark-input {
  width: 60%;

  :deep(.el-input__wrapper) {
    background: $bg-card;
    border: 0.5px solid $border-subtle;
    border-radius: 10px;
    box-shadow: none;
    padding: 4px 14px;
    transition: border-color 0.3s;

    &:hover,
    &.is-focus {
      border-color: $accent-energy;
    }
  }

  :deep(.el-input__inner) {
    color: $text-primary;
    font-size: 13px;

    &::placeholder {
      color: $text-muted;
    }
  }

  :deep(.el-input__count-inner) {
    background: transparent;
    padding: 0;
  }
}

    .exchange-empty-address {
      padding: 20px;
      text-align: center;
      color: $text-muted;
      font-size: 13px;
      background: $bg-subtle;
      border-radius: 12px;
      border: 0.5px solid $border-subtle;
    }

    .exchange-address-list {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .exchange-address-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 14px 16px;
        border-radius: 12px;
        border: 0.5px solid $border-subtle;
        cursor: pointer;
        transition: all 0.3s $transition-premium;

        &:hover {
          border-color: $border-accent;
          background: $bg-subtle;
        }

        &.active {
          border-color: $accent-energy;
          background: $accent-energy-soft;
        }

        .exchange-addr-radio {
          padding-top: 2px;

          .radio-dot {
            display: block;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            border: 2px solid $border-subtle;
            transition: all 0.3s $transition-premium;

            &.active {
              border-color: $accent-energy;
              background: $accent-energy;
              box-shadow: inset 0 0 0 3px #fff;
            }
          }
        }

        .exchange-addr-info {
          flex: 1;
          min-width: 0;

          .exchange-addr-name {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 600;
            color: $text-primary;
            margin-bottom: 4px;

            .addr-phone {
              font-weight: 400;
              color: $text-secondary;
            }

            .default-tag {
              font-size: 11px;
              font-weight: 600;
              padding: 1px 6px;
              border-radius: 4px;
              background: $accent-energy-soft;
              color: $accent-energy;
            }
          }

          .exchange-addr-detail {
            font-size: 13px;
            color: $text-secondary;
          }
        }
      }
    }
  }

  :deep(.el-input) {
    --el-input-border-color: $border-subtle;
    --el-input-hover-border-color: $accent-energy;
    --el-input-focus-border-color: $accent-energy;
    --el-input-border-radius: 10px;
  }
}

.add-addr-inline {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px dashed $border-accent;
  border-radius: 8px;
  background: transparent;
  color: $accent-energy;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s $transition-premium;

  &:hover {
    background: $accent-energy-soft;
    border-style: solid;
  }

  i { font-size: 14px; }
}

.exchange-address-form {
  overflow: hidden;
  padding: 16px;
  background: $bg-subtle;
  border-radius: 12px;
  border: 0.5px solid $border-subtle;
  margin-bottom: 12px;

  .region-row {
    display: grid;
    grid-template-columns: 1fr 1fr 1fr;
    gap: 8px;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 8px;

    .form-btn {
      @include btn-base;
      padding: 8px 20px;
      border-radius: 8px;
      font-size: 13px;
      font-weight: 600;

      &.cancel {
        background: $bg-card;
        border: 0.5px solid $border-subtle;
        color: $text-secondary;

        &:hover { background: $bg-elevated; color: $text-primary; }
      }

      &.submit {
        background: $accent-energy;
        border: none;
        color: #fff;

        &:hover:not(:disabled) { @include btn-hover-gradient-primary; }

        &:disabled { opacity: 0.5; cursor: not-allowed; }
      }
    }
  }

  :deep(.el-form-item) { margin-bottom: 14px; }
  :deep(.el-form-item__label) { font-weight: 600; color: $text-secondary; }
  :deep(.el-checkbox__label) { font-size: 13px; }
}

.exchange-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .dialog-btn {
    @include btn-base;
    padding: 10px 24px;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;

    &.cancel {
      background: $bg-card;
      border: 0.5px solid $border-subtle;
      color: $text-secondary;

      &:hover {
        background: $bg-elevated;
        color: $text-primary;
      }
    }

    &.confirm {
      background: $accent-energy;
      border: none;
      color: #fff;

      &:hover:not(:disabled) {
        box-shadow: 0 8px 24px $accent-energy-glow;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
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

@include respond(xl) {
  .points-layout {
    grid-template-columns: 1fr;
  }

  .points-sidebar {
    display: none;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .points-rules {
    grid-template-columns: 1fr;
  }
}

@include respond(md) {
  .points-page {
    padding-top: 100px;
  }

  .points-hero {
    .points-hero-inner {
      padding: 28px 20px;
      gap: 20px;
      text-align: center;
    }

    .hero-top {
      flex-direction: column;
      align-items: center;
      gap: 24px;
    }

    .hero-top-left {
      flex-direction: column;
      text-align: center;
      gap: 12px;
    }

    .hero-top-right {
      align-items: center;
    }

    .hero-icon-badge {
      width: 40px;
      height: 40px;
      font-size: 20px;
    }

    .hero-text {
      h1 {
        font-size: 28px;
      }

      p {
        font-size: 14px;
        max-width: none;
      }
    }

    .hero-balance {
      justify-content: center;

      .big-num {
        font-size: 44px;
      }

      .label {
        font-size: 14px;
      }
    }

    .stats-row {
      justify-content: center;
    }

    .hero-level {
      flex-direction: column;
      gap: 12px;
      text-align: center;

      .level-badge .level-name {
        font-size: 13px;
      }
    }

    .hero-actions .hero-btn {
      padding: 10px 16px 10px 20px;
      font-size: 13px;
      @include touch-target(36px);
    }
  }

  .products-grid {
    grid-template-columns: 1fr;
  }

  .product-image {
    height: 140px;
  }

  .product-info {
    padding: 16px;
  }

  .sidebar-shell {
    position: static;
  }

  .history-header {
    display: none;
  }

  .history-row {
    grid-template-columns: 1fr !important;
    gap: 6px;
    padding: 14px 18px !important;
  }

  .history-type {
    font-size: 14px;
    font-weight: 600;
  }

  .history-no {
    font-size: 11px;
  }

  .history-time {
    font-size: 12px;
  }

  .exchange-btn {
    padding: 10px;
    font-size: 12px;
  }
}

@include respond(sm) {
  .hero-text h1 {
    font-size: 24px;
  }

  .hero-balance .big-num {
    font-size: 36px;
  }

  .rule-card {
    padding: 20px 16px;
  }

  .products-grid .product-card .product-image {
    height: 120px;
  }
}
</style>
