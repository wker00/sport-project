<script setup>
import { ref, computed, onMounted, watch, nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getProductDetail, addCart, getCategoryList, getAddressList, buyNow, addAddress, getMyCoupons, getProductReviews } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import DOMPurify from 'dompurify'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const store = useCounterStore()

const product = ref(null)
const loading = ref(true)
const notFound = ref(false)
const quantity = ref(1)
const selectedSpecs = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const showBuyNowDialog = ref(false)
const remark = ref('')
const submitting = ref(false)
const showAddAddress = ref(false)
const newAddressFormRef = ref(null)
const newAddressSubmitting = ref(false)
const coupons = ref([])
const selectedCouponId = ref(null)

const emptyNewAddress = () => ({
  receiverName: '', phone: '', province: '', city: '', district: '', address: '', isDefault: 0
})
const newAddress = reactive(emptyNewAddress())

const newAddressRules = {
  receiverName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}
const categories = ref([])
const reviews = ref([])
const reviewsLoading = ref(false)

const levelMap = {
  1: { name: '普通会员', icon: 'ph-fill ph-crown-simple', cls: 'vip-regular' },
  2: { name: '银卡会员', icon: 'ph-fill ph-shield-star', cls: 'vip-silver' },
  3: { name: '金卡会员', icon: 'ph-fill ph-crown', cls: 'vip-gold' },
  4: { name: '钻石会员', icon: 'ph-fill ph-trophy', cls: 'vip-diamond' },
  5: { name: '黑金会员', icon: 'ph-fill ph-crown', cls: 'vip-black-gold' },
}

function getLevelInfo(level) { return levelMap[level] || levelMap[1] }

const categoryMap = computed(() => {
  const map = {}
  categories.value.forEach((c) => { map[c.id] = c.name })
  return map
})

const categoryName = computed(() => {
  if (!product.value) return ''
  return categoryMap.value[product.value.categoryId] || ''
})

const specs = computed(() => {
  if (!product.value?.specs) return { specGroups: [], specItems: [] }
  try {
    const parsed = typeof product.value.specs === 'string' ? JSON.parse(product.value.specs) : product.value.specs
    if (Array.isArray(parsed)) return { specGroups: parsed, specItems: [] }
    return {
      specGroups: parsed.specGroups || [],
      specItems: parsed.specItems || []
    }
  } catch (e) { console.error(e); return { specGroups: [], specItems: [] } }
})

const specItems = computed(() => specs.value.specItems)

const currentSpecKey = computed(() => selectedSpecs.value.filter(Boolean).join(' / '))

const currentPrice = computed(() => {
  if (!product.value) return 0
  const items = specItems.value
  if (!items || items.length === 0) return product.value.price
  const matched = items.find(item => item.specs === currentSpecKey.value)
  return matched && matched.price != null ? matched.price : product.value.price
})

const currentOriginalPrice = computed(() => {
  if (!product.value) return 0
  const items = specItems.value
  if (!items || items.length === 0) return product.value.originalPrice
  const matched = items.find(item => item.specs === currentSpecKey.value)
  if (matched && matched.originalPrice != null) return matched.originalPrice
  return product.value.originalPrice || product.value.price
})

const currentStock = computed(() => {
  if (!product.value) return 0
  const items = specItems.value
  if (!items || items.length === 0) return product.value.stock
  const matched = items.find(item => item.specs === currentSpecKey.value)
  return matched && matched.stock != null ? matched.stock : product.value.stock
})

const ratingStars = computed(() => {
  if (!product.value) return ''
  const score = product.value.ratingScore || 5
  const full = Math.floor(score)
  const half = score - full >= 0.5 ? 1 : 0
  return '★'.repeat(full) + (half ? '½' : '') + '☆'.repeat(5 - full - half)
})

function selectSpec(specIndex, option) { selectedSpecs.value[specIndex] = option }
function isSpecActive(specIndex, option) { return selectedSpecs.value[specIndex] === option }

function changeQty(delta) {
  const newVal = quantity.value + delta
  if (newVal >= 1 && newVal <= currentStock.value && newVal <= 99) quantity.value = newVal
}

function sanitizeQty() {
  let val = quantity.value
  if (typeof val !== 'number' || isNaN(val) || val < 1) val = 1
  if (val > currentStock.value) val = currentStock.value
  if (val > 99) val = 99
  quantity.value = val
}

watch(quantity, () => sanitizeQty())

watch(selectedSpecs, () => {
  quantity.value = 1
}, { deep: true })

function checkSpecsSelected() {
  if (specs.value.specGroups.length === 0) return true
  return specs.value.specGroups.length === selectedSpecs.value.filter(Boolean).length
}

const subtotal = computed(() => product.value ? currentPrice.value * quantity.value : 0)

const levelDiscountMap = {
  1: { name: '普通会员', rate: 0 },
  2: { name: '银卡会员', rate: 5 },
  3: { name: '金卡会员', rate: 10 },
  4: { name: '钻石会员', rate: 15 },
  5: { name: '黑金会员', rate: 20 },
}

const userLevel = computed(() => store.userInfo?.userLevel || 1)
const memberDiscountRate = computed(() => levelDiscountMap[userLevel.value]?.rate || 0)
const levelInfo = computed(() => levelDiscountMap[userLevel.value] || levelDiscountMap[1])
const memberDiscountAmount = computed(() => {
  if (memberDiscountRate.value <= 0) return 0
  return Math.round(subtotal.value * memberDiscountRate.value) / 100
})
const subtotalAfterMember = computed(() => subtotal.value - memberDiscountAmount.value)

const discountAmount = computed(() => {
  if (!selectedCouponId.value) return 0
  const coupon = coupons.value.find(c => String(c.id) === String(selectedCouponId.value))
  if (!coupon) return 0
  if (subtotalAfterMember.value < Number(coupon.minAmount || 0)) return 0
  if (Number(coupon.type) === 2) return Math.round(subtotalAfterMember.value * (10 - Number(coupon.value)) / 10 * 100) / 100
  return Number(coupon.value) || 0
})

const finalTotal = computed(() => {
  const total = subtotal.value - memberDiscountAmount.value - discountAmount.value
  return total < 0 ? 0 : total
})

function handleCouponClick(c) {
  if (subtotalAfterMember.value < Number(c.minAmount || 0)) {
    toast(`满¥${c.minAmount}可用，当前小计¥${subtotalAfterMember.value}，还差¥${(Number(c.minAmount) - subtotalAfterMember.value).toFixed(2)}`, 'warning')
    return
  }
  selectedCouponId.value = selectedCouponId.value === c.id ? null : c.id
}

function addressLabel(a) { return [a.province, a.city, a.district, a.address].filter(Boolean).join(' ') }

async function fetchAddresses() {
  try {
    const res = await getAddressList()
    if (res.data.code === 200) {
      addresses.value = res.data.data || []
      const def = addresses.value.find(a => a.isDefault === 1)
      if (def) selectedAddressId.value = def.id
      else if (addresses.value.length > 0) selectedAddressId.value = addresses.value[0].id
    }
  } catch (e) { console.error(e) }
}

async function handleAddCart() {
  if (!store.token) { toast('请先登录', 'error'); router.push('/user/login'); return }
  if (!product.value) return
  if (!checkSpecsSelected()) { toast('请选择商品规格', 'warning'); return }
  try {
    const specStr = selectedSpecs.value.filter(Boolean).join(' / ')
    const res = await addCart({ productId: product.value.id, quantity: quantity.value, spec: specStr || undefined })
    if (res.data.code === 200) { toast('已加入购物车', 'success'); store.fetchCartList() }
  } catch (e) { console.error(e); toast('加入购物车失败', 'error') }
}

async function handleBuyNow() {
  if (!store.token) { toast('请先登录后购买', 'error'); return }
  if (!product.value) return
  if (!checkSpecsSelected()) { toast('请选择完整的商品规格', 'warning'); return }
  remark.value = ''
  showAddAddress.value = false
  Object.assign(newAddress, emptyNewAddress())
  selectedCouponId.value = null
  try {
    const res = await getMyCoupons()
    if (res.data.code === 200) coupons.value = (res.data.data || []).filter(c => c.status === 0 && (!c.endTime || new Date(c.endTime) >= new Date()))
  } catch (e) { console.error(e) }
  await fetchAddresses()
  showBuyNowDialog.value = true
}

async function submitBuyNowOrder() {
  if (!selectedAddressId.value) { toast('请选择收货地址', 'warning'); return }
  if (submitting.value) return
  submitting.value = true
  const specStr = selectedSpecs.value.filter(Boolean).join(' / ')
  try {
    const res = await buyNow({ productId: product.value.id, quantity: quantity.value, spec: specStr || undefined, addressId: selectedAddressId.value, remark: remark.value, couponId: selectedCouponId.value || undefined })
    if (res.data.code === 200) {
      showBuyNowDialog.value = false
      await ElMessageBox.alert('订单已提交，请尽快完成支付', '下单成功', { confirmButtonText: '查看订单', type: 'success', lockScroll: false })
      router.push('/user/orders')
    } else { toast(res.data.message || '下单失败', 'error') }
  } catch (e) { console.error(e); toast('下单失败', 'error') }
  submitting.value = false
}

async function submitNewAddress() {
  try { await newAddressFormRef.value.validate() } catch { return }
  if (newAddressSubmitting.value) return
  newAddressSubmitting.value = true
  try {
    const res = await addAddress({ receiverName: newAddress.receiverName.trim(), phone: newAddress.phone.trim(), province: newAddress.province.trim(), city: newAddress.city.trim(), district: newAddress.district.trim(), address: newAddress.address.trim(), isDefault: newAddress.isDefault })
    if (res.data.code === 200) {
      toast('地址添加成功', 'success')
      showAddAddress.value = false
      Object.assign(newAddress, emptyNewAddress())
      await fetchAddresses()
      const newId = res.data.data?.id
      if (newId) selectedAddressId.value = newId
    } else { toast(res.data.message || '添加失败', 'error') }
  } catch (e) { console.error(e); toast('添加失败', 'error') }
  newAddressSubmitting.value = false
}

async function fetchProductReviews() {
  if (!product.value) return
  reviewsLoading.value = true
  try {
    const res = await getProductReviews(product.value.id)
    if (res.data.code === 200) reviews.value = res.data.data || []
  } catch (e) { console.error(e) }
  reviewsLoading.value = false
}

onMounted(async () => {
  try {
    const catRes = await getCategoryList()
    if (catRes.data.code === 200) categories.value = catRes.data.data
  } catch (e) { console.error(e) }
  try {
    const res = await getProductDetail(route.params.id)
    if (res.data.code === 200 && res.data.data) {
      product.value = res.data.data
      await fetchProductReviews()
    } else { notFound.value = true }
  } catch (e) { console.error(e); notFound.value = true }
  loading.value = false
})
</script>

<template>
  <div class="product-page">
    <div class="product-container">
      <div v-if="loading" class="loading-state">
        <i class="ph ph-spinner ph-spin"></i>
        <p>加载中...</p>
      </div>

      <div v-else-if="notFound" class="empty-state">
        <i class="ph ph-package"></i>
        <p>商品不存在或已下架</p>
        <router-link to="/category" class="back-link">返回商品列表</router-link>
      </div>

      <template v-else-if="product">
        <button class="back-btn" @click="router.go(-1)">
          <i class="ph ph-arrow-left"></i>
        </button>

        <div class="breadcrumb">
          <router-link to="/index">首页</router-link>
          <i class="ph ph-caret-right"></i>
          <router-link :to="`/category/${product.categoryId}`">{{ categoryName || '商品分类' }}</router-link>
          <i class="ph ph-caret-right"></i>
          <span>{{ product.name }}</span>
        </div>

        <div class="product-main">
          <div class="product-gallery double-bezel-outer">
            <div class="double-bezel-inner">
              <img :src="product.image" :alt="product.name" loading="lazy" />
            </div>
          </div>

          <div class="product-info">
            <div class="product-title-row">
              <span class="product-title">{{ product.name }}</span>
              <span v-if="product.badge" class="product-badge">{{ product.badge }}</span>
            </div>
            <p v-if="product.subtitle" class="product-subtitle">{{ product.subtitle }}</p>

            <div class="rating-row">
              <span class="rating-stars">{{ ratingStars }}</span>
              <span class="rating-info">销量 {{ product.salesCount || 0 }}</span>
            </div>

            <div class="product-price">
              <span class="current-price">¥{{ currentPrice }}</span>
              <span v-if="currentOriginalPrice && currentOriginalPrice !== currentPrice" class="original-price">¥{{ currentOriginalPrice }}</span>
            </div>

            <div class="product-options">
              <div v-for="(spec, si) in specs.specGroups" :key="si" class="option-group">
                <div class="option-label">{{ spec.name }}：</div>
                <div class="option-values">
                  <button v-for="opt in spec.options" :key="opt" class="option-btn"
                    :class="{ active: isSpecActive(si, opt) }" @click="selectSpec(si, opt)">{{ opt }}</button>
                </div>
              </div>

              <div v-if="checkSpecsSelected() && specItems.length > 0" class="option-group stock-info">
                <div class="option-label">库存：</div>
                <div class="stock-value">
                  <span v-if="currentStock > 0" class="stock-available">有货（剩余 {{ currentStock }} 件）</span>
                  <span v-else class="stock-out">缺货</span>
                </div>
              </div>

              <div class="option-group">
                <div class="option-label">数量：</div>
                <div class="quantity-selector">
                  <button class="qty-btn" :disabled="quantity <= 1" @click="changeQty(-1)"><i
                      class="ph ph-minus"></i></button>
                  <input type="text" class="qty-input" v-model.number="quantity" @blur="sanitizeQty" />
                  <button class="qty-btn" :disabled="quantity >= currentStock || quantity >= 99" @click="changeQty(1)"><i
                      class="ph ph-plus"></i></button>
                </div>
              </div>
            </div>

            <div class="action-buttons">
              <button class="btn-buy btn-trail" :disabled="currentStock <= 0" @click="handleBuyNow">
                <i class="ph ph-lightning"></i> {{ currentStock <= 0 ? '已售罄' : '立即购买' }}
                <span class="btn-inner-icon"><i class="ph ph-arrow-right"></i></span>
              </button>
              <button class="btn-add-cart btn-trail-secondary" :disabled="currentStock <= 0" @click="handleAddCart">
                <i class="ph ph-shopping-cart"></i> {{ currentStock <= 0 ? '已售罄' : '加入购物车' }}
                <span class="btn-inner-icon"></span>
              </button>
            </div>
          </div>
        </div>

        <div class="product-desc glass-panel">
          <h3>商品详情</h3>
          <div v-if="product.detail" class="detail-content" v-html="DOMPurify.sanitize(product.detail)"></div>
          <p v-else class="detail-placeholder">暂无商品详情</p>
        </div>

        <div class="product-reviews glass-panel">
          <h3>用户评价 ({{ reviews.length }})</h3>

          <div v-if="reviewsLoading" class="reviews-loading">
            <i class="ph ph-spinner ph-spin"></i> 加载中...
          </div>
          <div v-else-if="reviews.length === 0" class="reviews-empty">
            <i class="ph ph-chat-circle"></i> 暂无评价
          </div>

          <div v-else class="reviews-list">
            <div v-for="r in reviews" :key="r.id" class="review-item">
              <div class="review-header">
                <el-avatar :size="36" v-if="r.avatar" :src="r.avatar" />
                <el-avatar v-else :size="36"
                  style="background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff;">
                  {{ r.nickname?.[0] || '?' }}
                </el-avatar>
                <div class="review-user">
                  <div class="review-name-row">
                    <span class="review-name">{{ r.nickname || '匿名用户' }}</span>
                    <span v-if="r.userLevel" :class="['review-vip-badge', getLevelInfo(r.userLevel).cls]">
                      <i :class="getLevelInfo(r.userLevel).icon"></i> {{ getLevelInfo(r.userLevel).name }}
                    </span>
                  </div>
                  <span class="review-time">{{ r.reviewTime }}</span>
                </div>
                <el-rate :model-value="r.rating" disabled size="small" />
              </div>
              <div class="review-content">{{ r.reviewContent }}</div>
              <div v-if="r.replyContent" class="review-reply">
                <span class="reply-time">{{ r.replyTime }}</span>
                <span class="reply-label">商家回复：{{ r.replyContent }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>

  <el-dialog v-model="showBuyNowDialog" title="订单确认" width="560px" :close-on-click-modal="false" destroy-on-close>
    <div class="buynow-body">
      <div class="buynow-section">
        <div class="section-title">
          <i class="ph ph-map-pin"></i> 收货地址
          <button v-if="!showAddAddress && addresses.length > 0" class="add-addr-inline" @click="showAddAddress = true">
            <i class="ph ph-plus"></i> 新建
          </button>
        </div>
        <div v-if="addresses.length === 0 && !showAddAddress" class="addr-empty">
          <p>暂无收货地址，请先添加</p>
          <button class="addr-goto-btn" @click="showAddAddress = true"><i class="ph ph-plus"></i> 添加地址</button>
        </div>
        <div v-else-if="showAddAddress" class="address-form-inline">
          <el-form :model="newAddress" :rules="newAddressRules" ref="newAddressFormRef" label-position="top">
            <div class="form-row">
              <el-form-item prop="receiverName" label="收件人">
                <el-input v-model="newAddress.receiverName" placeholder="请输入收件人姓名" maxlength="20" />
              </el-form-item>
              <el-form-item prop="phone" label="手机号">
                <el-input v-model="newAddress.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item prop="province" label="省份">
                <el-input v-model="newAddress.province" placeholder="如：广东省" />
              </el-form-item>
              <el-form-item prop="city" label="城市">
                <el-input v-model="newAddress.city" placeholder="如：深圳市" />
              </el-form-item>
              <el-form-item prop="district" label="区县">
                <el-input v-model="newAddress.district" placeholder="如：南山区" />
              </el-form-item>
            </div>
            <el-form-item prop="address" label="详细地址">
              <el-input v-model="newAddress.address" type="textarea" placeholder="街道、门牌号、楼层等" :rows="2"
                maxlength="200" />
            </el-form-item>
            <el-form-item><el-checkbox v-model="newAddress.isDefault" :true-value="1" :false-value="0">设为默认地址</el-checkbox></el-form-item>
            <div class="form-actions-inline">
              <button type="button" class="cancel-form-btn"
                @click="showAddAddress = false; Object.assign(newAddress, emptyNewAddress())">取消</button>
              <button type="button" class="save-form-btn" :disabled="newAddressSubmitting" @click="submitNewAddress">
                <i class="ph ph-check-circle"></i> {{ newAddressSubmitting ? '保存中...' : '保存' }}
              </button>
            </div>
          </el-form>
        </div>
        <div v-else class="addr-list">
          <div v-for="a in addresses" :key="a.id" class="addr-card" :class="{ active: selectedAddressId === a.id }"
            @click="selectedAddressId = a.id">
            <div class="addr-radio">
              <span v-if="selectedAddressId === a.id" class="radio-dot active"></span>
              <span v-else class="radio-dot"></span>
            </div>
            <div class="addr-info">
              <div class="addr-name">
                <strong>{{ a.receiverName }}</strong>
                <span class="addr-phone">{{ a.phone }}</span>
                <span v-if="a.isDefault === 1" class="def-tag">默认</span>
              </div>
              <div class="addr-detail">{{ addressLabel(a) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="buynow-section">
        <div class="section-title"><i class="ph ph-package"></i> 商品</div>
        <div class="buynow-item">
          <img :src="product.image" :alt="product.name" />
          <div class="buynow-item-info">
            <div class="buynow-item-name">{{ product.name }}</div>
            <div v-if="selectedSpecs.filter(Boolean).length" class="buynow-item-spec">{{
              selectedSpecs.filter(Boolean).join(' / ') }}</div>
          </div>
          <div class="buynow-item-price">¥{{ currentPrice }}</div>
          <div class="buynow-item-qty">×{{ quantity }}</div>
        </div>
      </div>

      <div v-if="coupons.length > 0" class="buynow-section">
        <div class="section-title"><i class="ph ph-ticket"></i> 优惠券</div>
        <div class="available-coupons">
          <div v-for="c in coupons" :key="c.id" class="coupon-tag"
            :class="{ active: selectedCouponId === c.id, disabled: subtotalAfterMember < Number(c.minAmount || 0) }"
            @click="handleCouponClick(c)">
            <i class="ph ph-tag"></i> {{ c.name }}
          </div>
        </div>
      </div>

      <div class="buynow-section">
        <div class="section-title"><i class="ph ph-note-pencil"></i> 备注</div>
        <el-input class="remark-input" v-model="remark" placeholder="选填" maxlength="50" show-word-limit />
      </div>
    </div>

    <template #footer>
      <div class="buynow-footer">
        <div class="buynow-total">
          <div class="discount-info">
            <span v-if="memberDiscountAmount > 0" class="discount-hint"><i class="ph ph-crown"></i> 会员折扣 ({{
              levelInfo.name }} {{ memberDiscountRate }}%) -¥{{ memberDiscountAmount.toFixed(2) }}</span>
            <span v-if="discountAmount > 0" class="discount-hint">优惠券 -¥{{ discountAmount.toFixed(2) }}</span>
          </div>
          <div class="total-row">
            <span>合计：</span>
            <span class="total-amount">¥{{ finalTotal.toFixed(2) }}</span>
          </div>
        </div>
        <div class="buynow-actions">
          <button class="cancel-btn" @click="showBuyNowDialog = false" :disabled="submitting">取消</button>
          <button class="submit-btn" :disabled="!selectedAddressId || submitting" @click="submitBuyNowOrder">
            <i class="ph ph-check-circle"></i> {{ submitting ? '提交中...' : '提交订单' }}
          </button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;
@use '@/assets/level.scss' as *;

.product-page {
  padding-top: 100px;
  padding-bottom: 80px;
}

.product-container {
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;
}

.loading-state {
  text-align: center;
  padding: 120px 0;
  color: $text-muted;

  i {
    font-size: 48px;
    display: block;
    margin-bottom: 16px;
  }

  p {
    font-size: 16px;
  }
}

.empty-state {
  text-align: center;
  padding: 120px 0;
  color: $text-muted;

  i {
    font-size: 64px;
    display: block;
    margin-bottom: 16px;
  }

  p {
    font-size: 16px;
    margin-bottom: 24px;
  }
}

.back-btn {
  @include btn-base;
  width: 36px;
  height: 36px;
  margin-bottom: 32px;
  border-radius: 10px;
  background: $bg-card;
  border: 0.5px solid $border-subtle;
  color: $text-secondary;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &:hover {
    background: $bg-elevated;
    color: $text-primary;
  }
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: $accent-energy;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s $transition-premium;

  &:hover {
    gap: 12px;
  }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: $text-muted;
  margin-bottom: 24px;

  a {
    color: $text-muted;
    transition: color 0.3s;

    &:hover {
      color: $accent-energy;
    }
  }

  i {
    font-size: 12px;
  }

  span {
    color: $text-secondary;
  }
}

.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
}

.double-bezel-outer {
  @include double-bezel($outer-radius: $radius-2xl, $gap: 0.375rem);
}

.double-bezel-inner {
  aspect-ratio: 1;
  overflow: hidden;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.8s $transition-premium;
  }

  &:hover img {
    transform: scale(1.04);
  }
}

.product-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;

  .product-title {
    font-size: 32px;
    font-weight: 800;
    letter-spacing: -0.02em;
    line-height: 1.2;
    color: $text-primary;
  }

  .product-badge {
    display: inline-block;
    padding: 6px 14px;
    background: $accent-energy;
    border-radius: 999px;
    font-size: 11px;
    font-weight: 700;
    color: #fff;
    flex-shrink: 0;
  }
}

.product-subtitle {
  font-size: 16px;
  color: $text-secondary;
  margin-bottom: 20px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 0.5px solid $border-subtle;

  .rating-stars {
    color: #fbbf24;
    font-size: 16px;
    letter-spacing: 2px;
  }

  .rating-info {
    font-size: 14px;
    color: $text-secondary;
  }
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;

  .current-price {
    font-size: 40px;
    font-weight: 800;
    color: $price-color;
    letter-spacing: -0.02em;
  }

  .original-price {
    font-size: 18px;
    color: $price-muted;
    text-decoration: line-through;
  }
}

.option-group {
  margin-bottom: 20px;
}

.option-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: $text-primary;
}

.option-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.option-btn {
  @include btn-base;
  min-width: 48px;
  height: 48px;
  padding: 0 16px;
  background: $bg-card;
  border: 0.5px solid $border-subtle;
  border-radius: 12px;
  color: $text-secondary;
  font-size: 14px;
  font-weight: 500;

  &:hover {
    background: $bg-elevated;
    border-color: $border-accent;
    color: $accent-energy;
  }

  &.active {
    background: $accent-energy-soft;
    border-color: $accent-energy;
    color: $accent-energy;
  }
}

.quantity-selector {
  display: flex;
  align-items: center;
  background: $bg-card;
  border: 0.5px solid $border-subtle;
  border-radius: 12px;
  overflow: hidden;
  width: fit-content;
}

.qty-btn {
  @include btn-base;
  width: 48px;
  height: 48px;
  background: transparent;
  border: none;
  color: $text-secondary;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover:not(:disabled) {
    background: $accent-energy-soft;
    color: $accent-energy;
  }
}

.qty-input {
  width: 60px;
  height: 48px;
  background: transparent;
  border: none;
  border-left: 0.5px solid $border-subtle;
  border-right: 0.5px solid $border-subtle;
  color: $text-primary;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  outline: none;
  -moz-appearance: textfield;

  &::-webkit-inner-spin-button,
  &::-webkit-outer-spin-button {
    -webkit-appearance: none;
    margin: 0;
  }
}

.stock-info {
  display: flex;
}

.stock-value {
  font-size: 14px;
  font-weight: 500;
}

.stock-available {
  color: #22c55e;
}

.stock-out {
  color: #ef4444;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.btn-buy,
.btn-add-cart {
  @include btn-base;
  flex: 1;
  padding: 18px 32px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;

  &:active:not(:disabled) {
    transform: scale(0.96);
  }
}

.btn-buy {
  background: $accent-energy;
  border: none;
  color: #fff;
  transition-property: transform, box-shadow, color, border-color, opacity;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 40px $accent-energy-glow;
    @include btn-hover-gradient-primary;
  }
}

.btn-add-cart {
  background: $bg-card;
  border: 0.5px solid $border-subtle;
  color: $text-primary;

  &:hover {
    background: $bg-elevated;
    border-color: $border-accent;
    color: $accent-energy;
    transform: translateY(-2px);
  }
}

.glass-panel {
  @include solid-card;
  border-radius: $radius-xl;
  margin-top: 48px;
  padding: 32px;

  h3 {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 0.5px solid $border-subtle;
    color: $text-primary;
  }
}

.detail-content {
  color: $text-secondary;
  line-height: 1.8;
  font-size: 15px;

  :deep(img) {
    max-width: 100%;
    border-radius: $radius-xl;
    margin: 16px 0;
  }

  :deep(p) {
    margin-bottom: 12px;
  }
}

.detail-placeholder {
  color: $text-muted;
  font-size: 14px;
  padding: 24px 0;
}

.reviews-loading,
.reviews-empty {
  text-align: center;
  padding: 40px 0;
  color: $text-muted;
  font-size: 14px;

  i {
    font-size: 32px;
    display: block;
    margin-bottom: 12px;
  }
}

.reviews-list {
  display: flex;
  flex-direction: column;
}

.review-item {
  padding: 24px 0;
  border-bottom: 0.5px solid $border-subtle;

  &:last-child {
    border-bottom: none;
  }
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-user {
  flex: 1;
}

.review-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
}

.review-vip-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  white-space: nowrap;

  i {
    margin-top: 2px;
    font-size: 11px;
  }
}

.review-time {
  font-size: 12px;
  color: $text-muted;
}

.review-content {
  max-width: 420px;
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.7;
  overflow-wrap: break-word;
  word-break: break-word;
  padding-left: 48px;
}

.review-reply {
  margin-top: 12px;
  padding: 12px 16px;
  background: $bg-card;
  border-radius: 10px;
  font-size: 13px;
  color: $text-secondary;
  line-height: 1.6;
  overflow-wrap: break-word;
  word-break: break-word;
  position: relative;
  max-width: 420px;
  margin-left: 48px;
  border: 0.5px solid $border-subtle;

  &::before {
    content: '';
    position: absolute;
    top: -8px;
    left: 20px;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-bottom: 8px solid $bg-card;
  }

  .reply-time {
    display: block;
    font-size: 12px;
    color: $text-muted;
    margin-bottom: 4px;
  }

  .reply-label {
    color: $accent-energy;
    font-weight: 600;
  }
}

// Buy now dialog
.buynow-section {
  margin-bottom: 20px;
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

.addr-empty {
  text-align: center;
  padding: 20px;
  background: $bg-card;
  border-radius: 12px;
  border: 0.5px solid $border-subtle;

  p {
    font-size: 13px;
    color: $text-muted;
    margin-bottom: 12px;
  }
}

.addr-goto-btn {
  @include btn-base;
  padding: 8px 20px;
  background: $accent-energy-soft;
  border: none;
  border-radius: 8px;
  color: $accent-energy;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &:hover {
    background: $accent-energy;
    color: #fff;
  }
}

.address-form-inline {
  overflow: hidden;
  background: $bg-card;
  border-radius: 12px;
  padding: 20px;
  border: 0.5px solid $border-subtle;

  .form-row {
    display: flex;
    gap: 12px;

    >div {
      flex: 1;
    }
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 600;
    color: $text-secondary;
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background: $bg-card;
    border: 0.5px solid $border-subtle;
    box-shadow: none;
  }

  :deep(.el-textarea__inner) {
    border-radius: 8px;
    background: $bg-card;
    font-family: inherit;
  }
}

.form-actions-inline {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.cancel-form-btn {
  @include btn-base;
  padding: 8px 20px;
  background: transparent;
  border: 0.5px solid $border-subtle;
  border-radius: 8px;
  color: $text-secondary;
  font-size: 13px;
  font-weight: 600;
}

.save-form-btn {
  @include btn-base;
  padding: 8px 20px;
  background: $accent-energy;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &:hover:not(:disabled) {
    box-shadow: 0 4px 12px $accent-energy-glow;
  }
}

.addr-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.addr-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: $bg-card;
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s $transition-premium;

  &:hover {
    background: $bg-card-hover;
  }

  &.active {
    border-color: $accent-energy;
    background: $accent-energy-soft;
  }
}

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
    box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.9);
  }
}

.addr-info {
  flex: 1;
  min-width: 0;
}

.addr-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;

  strong {
    font-size: 13px;
    color: $text-primary;
  }
}

.addr-phone {
  font-size: 12px;
  color: $text-secondary;
}

.def-tag {
  padding: 1px 6px;
  border-radius: 4px;
  background: $vitality-soft;
  color: $accent-vitality;
  font-size: 10px;
  font-weight: 600;
}

.addr-detail {
  font-size: 12px;
  color: $text-muted;
  line-height: 1.4;
}

.buynow-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: $bg-card;
  border-radius: 10px;
  border: 0.5px solid $border-subtle;

  img {
    width: 52px;
    height: 52px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }
}

.buynow-item-info {
  flex: 1;
  min-width: 0;
}

.buynow-item-name {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.buynow-item-spec {
  font-size: 12px;
  color: $text-muted;
  margin-top: 2px;
}

.buynow-item-price {
  font-size: 14px;
  font-weight: 700;
  color: $price-color;
  white-space: nowrap;
}

.buynow-item-qty {
  font-size: 13px;
  color: $text-muted;
  white-space: nowrap;
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

.available-coupons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.coupon-tag {
  padding: 10px 22px;
  background: $accent-energy-soft;
  border: 1px dashed $border-accent;
  border-radius: 8px;
  font-size: 13px;
  color: $accent-energy;
  cursor: pointer;
  transition: all 0.3s $transition-premium;
  display: inline-flex;
  align-items: center;
  gap: 8px;

  i {
    font-size: 14px;
  }

  &.active {
    background: $accent-energy;
    color: #fff;
    border-style: solid;
  }

  &.disabled {
    opacity: 0.4;
    cursor: not-allowed;

    &:hover {
      background: $accent-energy-soft;
      color: $accent-energy;
      border-style: dashed;
    }
  }
}

.discount-hint {
  font-size: 12px;
  color: $accent-energy;
  font-weight: 600;
  margin-right: 8px;

  i {
    margin-right: 4px;
  }
}

.buynow-footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  flex-direction: column;
  gap: 12px;
}

.buynow-total {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .discount-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .total-row {
    display: flex;
    align-items: baseline;
    gap: 6px;
    font-size: 14px;
    color: $text-secondary;

    .total-amount {
      font-size: 22px;
      font-weight: 800;
      color: $price-color;
    }
  }
}

.buynow-actions {
  display: flex;
  gap: 10px;
  align-self: flex-end;
}

.cancel-btn {
  @include btn-base;
  padding: 10px 22px;
  background: transparent;
  border: 0.5px solid $border-subtle;
  border-radius: 10px;
  color: $text-secondary;
  font-size: 13px;
  font-weight: 600;
}

.submit-btn {
  @include btn-base;
  padding: 10px 24px;
  background: $accent-energy;
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 8px 24px $accent-energy-glow;
    @include btn-hover-gradient-primary;
  }
}

@include respond(lg) {
  .product-main {
    grid-template-columns: 1fr;
    gap: 40px;
  }

  .product-title-row .product-title {
    font-size: 28px;
  }
}

@include respond(md) {
  .product-page {
    padding-top: 100px;
  }

  .product-title-row .product-title {
    font-size: 24px;
  }

  .current-price {
    font-size: 32px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .btn-buy,
  .btn-add-cart {
    padding: 16px 24px;
    font-size: 14px;
  }

  .option-btn {
    height: 44px;
    min-width: 44px;
    @include touch-target(44px);
  }

  .qty-btn {
    width: 44px;
    height: 44px;
    @include touch-target(44px);
  }

  .qty-input {
    height: 44px;
  }

  .review-header {
    flex-wrap: wrap;
  }

  .review-content {
    padding-left: 0;
    max-width: none;
  }

  .review-reply {
    margin-left: 0;
    max-width: none;
  }

  .remark-input {
    width: 100%;
  }
}

@include respond(sm) {
  .product-title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .product-title-row .product-title {
    font-size: 20px;
  }

  .current-price {
    font-size: 28px;
  }

  .original-price {
    font-size: 14px;
  }

  .breadcrumb {
    font-size: 12px;
    flex-wrap: wrap;
  }

  .glass-panel {
    padding: 20px;
    margin-top: 32px;

    h3 {
      font-size: 18px;
    }
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
</style>
