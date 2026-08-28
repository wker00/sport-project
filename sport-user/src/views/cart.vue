<script setup>
import { ref, computed, onMounted, nextTick, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { updateCart, deleteCartItem, clearCart, checkCartItem, getMyCoupons, getAddressList, createOrder, addAddress } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'

const router = useRouter()
const store = useCounterStore()
const coupons = ref([])
const loading = ref(true)
const selectedCouponId = ref(null)
const updatingIds = ref(new Set())
const addresses = ref([])
const selectedAddressId = ref(null)
const showCheckoutDialog = ref(false)
const remark = ref('')
const submitting = ref(false)
const showAddAddress = ref(false)
const newAddressFormRef = ref(null)
const newAddressSubmitting = ref(false)

const emptyNewAddress = () => ({ receiverName: '', phone: '', province: '', city: '', district: '', address: '', isDefault: 0 })
const newAddress = reactive(emptyNewAddress())
const newAddressRules = {
  receiverName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const allChecked = computed({
  get: () => store.cartItems.length > 0 && store.cartItems.every(i => i.checked === 1),
  set: async (val) => {
    const nv = val ? 1 : 0
    await Promise.all(store.cartItems.map(item => checkCartItem(item.id, nv).then(() => { item.checked = nv })))
  }
})
const selectedItems = computed(() => store.cartItems.filter(i => i.checked === 1))
const subtotal = computed(() => selectedItems.value.reduce((sum, i) => sum + i.price * i.quantity, 0))
const totalCount = computed(() => selectedItems.value.reduce((sum, i) => sum + i.quantity, 0))

const levelMap = { 1: { name: '普通会员', rate: 0 }, 2: { name: '银卡会员', rate: 5 }, 3: { name: '金卡会员', rate: 10 }, 4: { name: '钻石会员', rate: 15 }, 5: { name: '黑金会员', rate: 20 } }
const userLevel = computed(() => store.userInfo?.userLevel || 1)
const memberDiscountRate = computed(() => levelMap[userLevel.value]?.rate || 0)
const levelInfo = computed(() => levelMap[userLevel.value] || levelMap[1])
const memberDiscountAmount = computed(() => { if (memberDiscountRate.value <= 0) return 0; return Math.round(subtotal.value * memberDiscountRate.value) / 100 })
const subtotalAfterMember = computed(() => subtotal.value - memberDiscountAmount.value)
const discountAmount = computed(() => {
  if (!selectedCouponId.value) return 0
  const coupon = coupons.value.find(c => String(c.id) === String(selectedCouponId.value))
  if (!coupon) return 0
  if (subtotalAfterMember.value < Number(coupon.minAmount || 0)) return 0
  if (Number(coupon.type) === 2) return Math.round(subtotalAfterMember.value * (10 - Number(coupon.value)) / 10 * 100) / 100
  return Number(coupon.value) || 0
})
const finalTotal = computed(() => { const t = subtotal.value - memberDiscountAmount.value - discountAmount.value; return t < 0 ? 0 : t })
const canCheckout = computed(() => selectedItems.value.length > 0)

async function loadCart() {
  loading.value = true
  try {
    if (store.cartItems.length === 0 && store.token) await store.fetchCartList()
    const res = await getMyCoupons()
    if (res.data.code === 200) coupons.value = (res.data.data || []).filter(c => c.status === 0 && (!c.endTime || new Date(c.endTime) >= new Date()))
  } catch (e) { console.error(e) }
  loading.value = false
}

async function updateQuantity(item, delta) {
  const nq = item.quantity + delta
  if (nq < 1) return
  if (item.stock != null && nq > item.stock) { toast(`库存不足，最多可购买${item.stock}件`, 'warning'); return }
  if (nq > 99) return
  if (updatingIds.value.has(item.id)) return
  updatingIds.value.add(item.id)
  item.quantity = nq
  try { await updateCart(item.id, { quantity: nq, spec: item.spec, checked: item.checked }) }
  catch { item.quantity -= delta }
  updatingIds.value.delete(item.id)
}

async function sanitizeQty(item) {
  let v = item.quantity
  if (typeof v !== 'number' || isNaN(v) || v < 1) v = 1
  if (item.stock != null && v > item.stock) v = item.stock
  if (v > 99) v = 99
  if (v !== item.quantity) { item.quantity = v; try { await updateCart(item.id, { quantity: v, spec: item.spec, checked: item.checked }) } catch (e) { console.error(e) } }
}

async function handleCheck(item) {
  const nc = item.checked === 1 ? 0 : 1
  try { await checkCartItem(item.id, nc); item.checked = nc }
  catch (e) { console.error(e); toast('操作失败', 'error') }
}

async function handleDelete(item) {
  try {
    await ElMessageBox.confirm('确定要删除这件商品吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', lockScroll: false })
  } catch { return }
  try {
    await deleteCartItem(item.id)
    store.setCartItems(store.cartItems.filter(i => i.id !== item.id))
    toast('已删除', 'success')
  } catch (e) { console.error(e); toast('删除失败', 'error') }
}

async function handleClear() {
  if (store.cartItems.length === 0) return
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', lockScroll: false })
  } catch { return }
  try {
    await clearCart()
    store.setCartItems([])
    toast('已清空', 'success')
  } catch (e) { console.error(e); toast('清空失败', 'error') }
}

async function fetchAddresses() {
  try { const res = await getAddressList(); if (res.data.code === 200) { addresses.value = res.data.data || []; const def = addresses.value.find(a => a.isDefault === 1); if (def) selectedAddressId.value = def.id; else if (addresses.value.length > 0) selectedAddressId.value = addresses.value[0].id } } catch (e) { console.error(e) }
}

function addressLabel(a) { return [a.province, a.city, a.district, a.address].filter(Boolean).join(' ') }

function handleCheckout() {
  if (!canCheckout.value) { toast('请选择要结算的商品', 'warning'); return }
  remark.value = ''; showAddAddress.value = false; Object.assign(newAddress, emptyNewAddress())
  showCheckoutDialog.value = true
  nextTick(fetchAddresses)
}

async function submitNewAddress() {
  try { await newAddressFormRef.value.validate() } catch { return }
  if (newAddressSubmitting.value) return; newAddressSubmitting.value = true
  try {
    const res = await addAddress({ receiverName: newAddress.receiverName.trim(), phone: newAddress.phone.trim(), province: newAddress.province.trim(), city: newAddress.city.trim(), district: newAddress.district.trim(), address: newAddress.address.trim(), isDefault: newAddress.isDefault })
    if (res.data.code === 200) {
      toast('地址添加成功', 'success'); showAddAddress.value = false; Object.assign(newAddress, emptyNewAddress()); await fetchAddresses()
      const newId = res.data.data?.id; if (newId) selectedAddressId.value = newId
    } else toast(res.data.message || '添加失败', 'error')
  } catch (e) { console.error(e); toast('添加失败', 'error') }
  newAddressSubmitting.value = false
}

async function submitOrder() {
  if (!selectedAddressId.value) { toast('请选择收货地址', 'warning'); return }
  if (submitting.value) return; submitting.value = true
  try {
    const res = await createOrder({ addressId: selectedAddressId.value, remark: remark.value, cartItemIds: selectedItems.value.map(i => i.id), couponId: selectedCouponId.value || undefined })
    if (res.data.code === 200) {
      showCheckoutDialog.value = false
      store.setCartItems(store.cartItems.filter(i => i.checked !== 1))
      await ElMessageBox.alert('订单已提交，请尽快完成支付', '下单成功', { confirmButtonText: '查看订单', type: 'success', lockScroll: false })
      router.push('/user/orders')
    } else toast(res.data.message || '下单失败', 'error')
  } catch (e) { console.error(e) }
  submitting.value = false
}

function handleCouponClick(c) {
  if (subtotalAfterMember.value < Number(c.minAmount || 0)) { toast(`满¥${c.minAmount}可用`, 'warning'); return }
  selectedCouponId.value = selectedCouponId.value === c.id ? null : c.id
}

onMounted(loadCart)
</script>

<template>
  <div class="cart-page">
    <div class="page-inner">
      <div class="page-header">
        <h1>购物车</h1><span v-if="!loading" class="item-count">共 {{ store.cartItems.length }} 件商品</span>
      </div>

      <div v-if="loading" class="cart-layout">
        <div class="cart-items">
          <div class="skeleton-item" v-for="n in 3" :key="n">
            <div class="sk-check"></div>
            <div class="sk-img"></div>
            <div class="sk-info">
              <div class="sk-line w-50"></div>
              <div class="sk-line w-30"></div>
            </div>
          </div>
        </div>
        <div class="order-summary">
          <div class="sk-summary">
            <div class="sk-line w-40"></div>
            <div class="sk-line w-60"></div>
            <div class="sk-line w-50"></div>
            <div class="sk-divider"></div>
            <div class="sk-line w-45 h-7"></div>
            <div class="sk-line w-80 h-8"></div>
          </div>
        </div>
      </div>

      <template v-else-if="store.cartItems.length === 0">
        <div class="empty-cart"><i class="ph ph-shopping-cart"></i>
          <h2>购物车是空的</h2>
          <p>快去挑选喜欢的运动装备吧</p><button class="btn-shop" @click="router.push('/category')">去逛逛</button>
        </div>
      </template>

      <div v-else class="cart-layout">
        <div class="cart-items-shell">
          <div class="cart-items-core">
            <div class="cart-header">
              <label class="select-all" @click="allChecked = !allChecked">
                <span class="ck-box" :class="{ checked: allChecked }"><i v-if="allChecked"
                    class="ph ph-check"></i></span> 全选
              </label>
              <button class="clear-btn" @click="handleClear"><i class="ph ph-trash"></i> 清空购物车</button>
            </div>

            <div v-for="item in store.cartItems" :key="item.id" class="cart-item"
              :class="{ disabled: updatingIds.has(item.id) }">
              <div class="item-select" :class="{ checked: item.checked === 1 }" @click="handleCheck(item)"><i
                  v-if="item.checked === 1" class="ph ph-check"></i></div>
              <div class="item-img"><img :src="item.productImage" :alt="item.productName" loading="lazy" /></div>
              <div class="item-info">
                <h3>{{ item.productName }}</h3>
                <div v-if="item.spec" class="item-spec">{{ item.spec }}</div>
              </div>
              <div class="item-right">
                <div class="item-price"><span class="price">¥{{ item.price }}</span></div>
                <div class="qty-ctrl">
                  <button class="qty-minus" :disabled="item.quantity <= 1 || updatingIds.has(item.id)"
                    @click="updateQuantity(item, -1)"><i class="ph ph-minus"></i></button>
                  <input type="text" v-model.number="item.quantity" @blur="sanitizeQty(item)" />
                  <button class="qty-plus" :disabled="item.quantity >= 99 || updatingIds.has(item.id)"
                    @click="updateQuantity(item, 1)"><i class="ph ph-plus"></i></button>
                </div>
                <button class="del-btn" @click="handleDelete(item)"><i class="ph ph-trash"></i></button>
              </div>
            </div>
          </div>
        </div>

        <div class="order-summary">
          <div class="summary-shell">
            <div class="summary-core">
              <h3 class="summary-title">订单信息</h3>
              <div class="summary-row"><span>商品总价</span><span>¥{{ subtotal.toFixed(2) }}</span></div>
              <div v-if="memberDiscountAmount > 0" class="summary-row disc"><span><i class="ph ph-crown"></i> 会员折扣 ({{
                levelInfo.name }} {{ memberDiscountRate }}%)</span><span>-¥{{ memberDiscountAmount.toFixed(2)
                  }}</span>
              </div>
              <div class="summary-row disc"><span>优惠折扣</span><span>{{ discountAmount > 0 ? '-¥' +
                discountAmount.toFixed(2) :
                '¥0.00' }}</span></div>
              <div class="summary-row"><span>运费</span><span>免运费</span></div>
              <div class="summary-divider"></div>
              <div class="summary-total"><span>合计</span><span>¥{{ finalTotal.toFixed(2) }}</span></div>

              <div v-if="coupons.length > 0" class="coupon-section">
                <div class="coupon-header"><i class="ph ph-ticket"></i><span>优惠券</span></div>
                <div class="coupon-list">
                  <div v-for="c in coupons" :key="c.id" class="coupon-tag"
                    :class="{ active: selectedCouponId === c.id, disabled: subtotalAfterMember < Number(c.minAmount || 0) }"
                    @click="handleCouponClick(c)"><i class="ph ph-tag"></i> {{ c.name }}</div>
                </div>
              </div>

              <button class="checkout-btn" :class="{ disabled: !canCheckout }" :disabled="!canCheckout"
                @click="handleCheckout">
                <i class="ph ph-credit-card"></i> 去结算
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="showCheckoutDialog" title="订单确认" width="560px" :close-on-click-modal="false" destroy-on-close>
    <div class="checkout-body">
      <div class="checkout-section">
        <div class="section-title">
          <i class="ph ph-map-pin"></i> 收货地址
          <button v-if="!showAddAddress && addresses.length > 0" class="add-addr-inline" @click="showAddAddress = true">
            <i class="ph ph-plus"></i> 新建
          </button>
        </div>
        <div v-if="addresses.length === 0 && !showAddAddress" class="a-empty">
          <p>暂无收货地址</p><button class="a-add-btn" @click="showAddAddress = true"><i class="ph ph-plus"></i> 添加地址</button>
        </div>
        <div v-else-if="showAddAddress" class="address-form-inline">
          <el-form :model="newAddress" :rules="newAddressRules" ref="newAddressFormRef" label-position="top">
            <div class="form-row"><el-form-item prop="receiverName" label="收件人"><el-input
                  v-model="newAddress.receiverName" placeholder="请输入收件人姓名" maxlength="20" /></el-form-item><el-form-item
                prop="phone" label="手机号"><el-input v-model="newAddress.phone" placeholder="请输入手机号"
                  maxlength="11" /></el-form-item></div>
            <div class="form-row"><el-form-item prop="province" label="省份"><el-input v-model="newAddress.province"
                  placeholder="如：广东省" /></el-form-item><el-form-item prop="city" label="城市"><el-input
                  v-model="newAddress.city" placeholder="如：深圳市" /></el-form-item><el-form-item prop="district"
                label="区县"><el-input v-model="newAddress.district" placeholder="如：南山区" /></el-form-item></div>
            <el-form-item prop="address" label="详细地址"><el-input v-model="newAddress.address" type="textarea"
                placeholder="街道、门牌号、楼层等" :rows="2" maxlength="200" /></el-form-item>
            <el-form-item><el-checkbox v-model="newAddress.isDefault" :true-value="1"
                :false-value="0">设为默认地址</el-checkbox></el-form-item>
            <div class="form-actions-inline"><button type="button" class="cancel-form-btn"
                @click="showAddAddress = false; Object.assign(newAddress, emptyNewAddress())">取消</button><button
                type="button" class="save-form-btn" :disabled="newAddressSubmitting" @click="submitNewAddress"><i
                  class="ph ph-check-circle"></i> {{ newAddressSubmitting ? '保存中...' : '保存' }}</button></div>
          </el-form>
        </div>
        <div v-else class="address-list">
          <div v-for="a in addresses" :key="a.id" class="address-card" :class="{ active: selectedAddressId === a.id }"
            @click="selectedAddressId = a.id">
            <div class="a-radio"><span v-if="selectedAddressId === a.id" class="r-dot active"></span><span v-else
                class="r-dot"></span></div>
            <div class="a-info">
              <div class="a-name"><strong>{{ a.receiverName }}</strong><span class="a-phone">{{ a.phone }}</span><span
                  v-if="a.isDefault === 1" class="def-tag">默认</span></div>
              <div class="a-detail">{{ addressLabel(a) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="checkout-section">
        <div class="section-title"><i class="ph ph-package"></i> 商品清单</div>
        <div class="checkout-items">
          <div v-for="item in selectedItems" :key="item.id" class="checkout-item">
            <img :src="item.productImage" :alt="item.productName" />
            <div class="ci-info">
              <div class="ci-name">{{ item.productName }}</div>
              <div v-if="item.spec" class="ci-spec">{{ item.spec }}</div>
            </div>
            <div class="ci-price">¥{{ item.price }}</div>
            <div class="ci-qty">×{{ item.quantity }}</div>
          </div>
        </div>
      </div>

      <div class="checkout-section">
        <div class="section-title"><i class="ph ph-note-pencil"></i> 订单备注</div>
        <el-input class="remark-input" v-model="remark" placeholder="选填：备注信息（如配送时间要求等）" maxlength="50"
          show-word-limit />
      </div>
    </div>
    <template #footer>
      <div class="checkout-footer">
        <div class="co-total"><span>合计：</span><span class="co-amount">¥{{ finalTotal.toFixed(2) }}</span></div>
        <div class="co-actions"><button class="co-cancel" @click="showCheckoutDialog = false"
            :disabled="submitting">取消</button><button class="co-submit" :disabled="!selectedAddressId || submitting"
            @click="submitOrder"><i class="ph ph-check-circle"></i> {{ submitting ? '提交中...' : '提交订单' }}</button></div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.cart-page {
  padding-top: 100px;
  padding-bottom: 100px;
}

.page-inner {
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 40px;
  display: flex;
  align-items: flex-end;
  gap: 16px;

  h1 {
    font-size: clamp(32px, 4vw, 48px);
    font-weight: 800;
    letter-spacing: -0.02em;
    margin: 0;
    color: $text-primary;
  }

  .item-count {
    font-size: 14px;
    color: $text-muted;
    padding-bottom: 6px;
  }
}

.cart-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 32px;
  align-items: start;
}

@keyframes sk-pulse {
  0% {
    opacity: 0.6;
  }

  50% {
    opacity: 0.2;
  }

  100% {
    opacity: 0.6;
  }
}

.skeleton-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-bottom: 0.5px solid $border-subtle;
  animation: sk-pulse 1.8s ease-in-out infinite;
}

.sk-check {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: $bg-card;
  flex-shrink: 0;
}

.sk-img {
  width: 100px;
  height: 100px;
  border-radius: 14px;
  background: rgba(255, 107, 53, 0.04);
  flex-shrink: 0;
}

.sk-info {
  flex: 1;
}

.sk-line {
  height: 13px;
  border-radius: 8px;
  background: $bg-card;
  margin-bottom: 10px;

  &.w-50 {
    width: 50%;
  }

  &.w-40 {
    width: 40%;
  }

  &.w-30 {
    width: 30%;
  }

  &.w-60 {
    width: 60%;
  }

  &.w-45 {
    width: 45%;
  }

  &.w-80 {
    width: 80%;
  }

  &.h-7 {
    height: 24px;
  }

  &.h-8 {
    height: 28px;
  }
}

.sk-divider {
  height: 1px;
  background: $border-subtle;
  margin: 16px 0;
}

.sk-summary {
  @include solid-card;
  border-radius: $radius-xl;
  padding: 1.5px;

  >div {
    padding: 24px;
  }
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;

  i {
    font-size: 80px;
    color: $text-muted;
    margin-bottom: 24px;
  }

  h2 {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 12px;
    color: $text-secondary;
  }

  p {
    color: $text-secondary;
    margin-bottom: 32px;
    font-size: 15px;
  }
}

.btn-shop {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 12px 20px;
  background: $accent-energy;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba($accent-energy, 0.25);
  }
}

.cart-items-shell {
  @include solid-card;
  border-radius: $radius-squircle;
  padding: 1.5px;
}

.cart-items-core {
  border-radius: calc($radius-squircle - 0.375rem);
  overflow: hidden;
  background: transparent;
}

.cart-header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 0.5px solid $border-subtle;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: $text-secondary;
  user-select: none;
}

.ck-box {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  border: 2px solid $border-medium;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.3s;
  flex-shrink: 0;

  &.checked {
    background: $accent-energy;
    border-color: $accent-energy;
    color: #fff;
  }

  i {
    font-size: 13px;
    font-weight: 700;
  }
}

.clear-btn {
  margin-left: auto;
  padding: 8px 16px;
  background: transparent;
  border: 0.5px solid $border-glass;
  border-radius: 8px;
  color: $text-muted;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: $bg-card;
    color: $text-muted;
  }
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 0.5px solid $border-subtle;
  transition: background 0.3s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: $bg-barely;
  }

  &.disabled {
    opacity: 0.5;
    pointer-events: none;
  }
}

.item-select {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  border: 2px solid $border-medium;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;

  &.checked {
    background: $accent-energy;
    border-color: $accent-energy;
    color: #fff;
  }

  i {
    font-size: 13px;
    font-weight: 700;
  }
}

.item-img {
  width: 100px;
  height: 100px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(255, 107, 53, 0.03);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s $transition-premium;
  }

  &:hover img {
    transform: scale(1.05);
  }
}

.item-info {
  flex: 1;
  min-width: 0;

  h3 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 6px;
    color: $text-primary;
  }

  .item-spec {
    font-size: 13px;
    color: $text-muted;
  }
}

.item-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.item-price .price {
  font-size: 18px;
  font-weight: 700;
  color: $price-color;
  white-space: nowrap;
}

.qty-ctrl {
  display: flex;
  align-items: center;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  border-radius: 10px;
  overflow: hidden;

  button {
    width: 34px;
    height: 34px;
    background: transparent;
    border: none;
    color: $text-secondary;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;

    &:hover:not(:disabled) {
      background: $accent-energy-soft;
      color: $accent-energy;
    }

    &:disabled {
      opacity: 0.3;
      cursor: not-allowed;
    }
  }

  input {
    width: 44px;
    height: 34px;
    background: transparent;
    border: none;
    color: $text-primary;
    font-size: 14px;
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
}

.del-btn {
  padding: 8px;
  background: transparent;
  border: none;
  color: $text-muted;
  font-size: 18px;
  cursor: pointer;

  &:hover {
    color: #ef4444;
  }
}

.order-summary {
  position: sticky;
  top: 100px;
}

.summary-shell {
  @include solid-card;
  border-radius: $radius-squircle;
  padding: 1.5px;
}

.summary-core {
  border-radius: calc($radius-squircle - 0.375rem);
  padding: 28px;
  background: transparent;
}

.summary-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
  color: $text-primary;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  font-size: 14px;

  span:first-child {
    color: $text-secondary;
  }

  span:last-child {
    color: $text-secondary;
  }

  &.disc span:last-child {
    color: $accent-energy;
  }

  i {
    margin-right: 4px;
  }
}

.summary-divider {
  height: 0.5px;
  background: $border-subtle;
  margin: 18px 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  span:first-child {
    font-size: 16px;
    font-weight: 600;
  }

  span:last-child {
    font-size: 28px;
    font-weight: 800;
    color: $price-color;
  }
}

.coupon-section {
  margin-top: 20px;
  padding: 18px;
  background: $bg-barely;
  border-radius: $radius-xl;
}

.coupon-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 14px;
  color: $accent-energy;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.coupon-tag {
  padding: 10px 14px;
  background: $accent-energy-soft;
  border: 1px dashed $border-accent;
  border-radius: 8px;
  font-size: 13px;
  color: $accent-energy;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;

  &.active {
    background: $accent-energy;
    color: #fff;
    border-style: solid;
  }

  &.disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.checkout-btn {
  width: 100%;
  padding: 16px;
  background: $accent-energy;
  border: none;
  border-radius: 14px;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.5s $transition-premium;

  &:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 12px 32px $accent-energy-glow;
  }
}

.checkout-body {
  max-height: 60vh;
  overflow-y: auto;
}

.checkout-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 16px;

  i {
    font-size: 18px;
    color: $accent-energy;
  }
}

.a-empty {
  text-align: center;
  padding: 20px;
  background: $bg-barely;
  border-radius: 12px;

  p {
    font-size: 13px;
    color: $text-muted;
    margin-bottom: 12px;
  }
}

.a-add-btn {
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
  cursor: pointer;

  &:hover {
    background: $accent-energy;
    color: #fff;
  }
}

.address-form-inline {
  overflow: hidden;
  background: $bg-barely;
  border-radius: 12px;
  padding: 20px;

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

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background: $bg-card;
  }
}

.form-actions-inline {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.cancel-form-btn {
  padding: 8px 20px;
  background: transparent;
  border: 0.5px solid $border-glass;
  border-radius: 8px;
  color: $text-secondary;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
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
  cursor: pointer;

  &:hover:not(:disabled) {
    box-shadow: 0 4px 12px $accent-energy-glow;
  }
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: $bg-barely;
  border: 1px solid transparent;
  border-radius: 12px;
  cursor: pointer;

  &:hover {
    background: $accent-energy-soft;
  }

  &.active {
    border-color: $border-accent;
    background: $accent-energy-soft;
  }
}

.a-radio {
  flex-shrink: 0;
}

.r-dot {
  display: block;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid $border-medium;

  &.active {
    border-color: $accent-energy;
    background: $accent-energy;
    box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.9);
  }
}

.a-info {
  flex: 1;
  min-width: 0;
}

.a-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;

  strong {
    font-size: 13px;
    color: $text-primary;
  }
}

.a-phone {
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

.a-detail {
  font-size: 12px;
  color: $text-muted;
  line-height: 1.4;
}

.checkout-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: $bg-barely;
  border-radius: 10px;

  img {
    width: 52px;
    height: 52px;
    border-radius: 8px;
    object-fit: cover;
    flex-shrink: 0;
  }
}

.ci-info {
  flex: 1;
  min-width: 0;
}

.ci-name {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ci-spec {
  font-size: 12px;
  color: $text-muted;
  margin-top: 2px;
}

.ci-price {
  font-size: 14px;
  font-weight: 700;
  color: $price-color;
  white-space: nowrap;
}

.ci-qty {
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

.co-total {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-size: 14px;
  color: $text-secondary;

  .co-amount {
    font-size: 24px;
    font-weight: 800;
    color: $price-color;
  }
}

.checkout-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.co-actions {
  display: flex;
  gap: 12px;
}

.co-cancel {
  padding: 10px 22px;
  background: transparent;
  border: 0.5px solid $border-glass;
  border-radius: 10px;
  color: $text-secondary;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.co-submit {
  padding: 10px 24px;
  background: $accent-energy;
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 8px 24px $accent-energy-glow;
  }
}

@include respond(lg) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .order-summary {
    position: static;
  }

  .summary-core {
    padding: 24px 20px;
  }

  .summary-total span:last-child {
    font-size: 24px;
  }
}

@include respond(md) {
  .cart-page {
    padding-top: 100px;
    padding-bottom: 80px;
  }

  .page-header {
    margin-bottom: 24px;
  }

  .page-header h1 {
    font-size: 28px;
  }

  .cart-header {
    padding: 16px;
  }

  .cart-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px;
    position: relative;
  }

  .item-select {
    position: absolute;
    top: 16px;
    left: 16px;
    z-index: 2;
  }

  .item-img {
    width: 80px;
    height: 80px;
    margin-left: 32px;
  }

  .item-info {
    flex: 1;
    min-width: calc(100% - 120px);
  }

  .item-info h3 {
    font-size: 14px;
  }

  .item-right {
    width: 100%;
    justify-content: space-between;
    padding-left: 32px;
  }

  .item-price .price {
    font-size: 16px;
  }

  .qty-ctrl button {
    width: 38px;
    height: 38px;
    @include touch-target(38px);
  }

  .qty-ctrl input {
    width: 40px;
    height: 38px;
  }

  .del-btn {
    padding: 8px 12px;
    font-size: 16px;
  }

  .summary-total span:last-child {
    font-size: 22px;
  }

  .checkout-btn {
    padding: 14px;
    font-size: 14px;
  }
}

@include respond(sm) {
  .cart-page {
    padding-bottom: 60px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .page-header .item-count {
    font-size: 13px;
  }

  .cart-item {
    gap: 10px;
    padding: 12px;
  }

  .item-img {
    width: 64px;
    height: 64px;
    border-radius: 10px;
  }

  .item-info h3 {
    font-size: 13px;
  }

  .item-spec {
    font-size: 12px;
  }

  .item-right {
    flex-wrap: wrap;
    gap: 8px;
  }

  .clear-btn {
    padding: 6px 12px;
    font-size: 12px;
  }

  .select-all {
    font-size: 13px;
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .address-form-inline {
    padding: 16px;
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

  i {
    font-size: 14px;
  }
}

:deep(.el-checkbox__label) {
  font-size: 13px;
}
</style>
