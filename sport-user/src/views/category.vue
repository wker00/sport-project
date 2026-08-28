<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getCategoryList, getProductList } from '@/api/manager'

const router = useRouter()
const route = useRoute()

const products = ref([])
let cachedCategories = null
const categories = ref(cachedCategories || [])
const activeCategoryId = ref('')
const activeSort = ref('综合')
const loading = ref(true)
const currentPage = ref(1)
const pageSize = 12
const sortParams = ref({})

const categoryMap = computed(() => {
  const map = {}
  categories.value.forEach((c) => { map[c.id] = c.name })
  return map
})

const totalCount = computed(() => products.value.length)
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 1)

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return products.value.slice(start, start + pageSize)
})

function getCategoryName(id) { return categoryMap.value[id] || '' }

async function fetchProducts() {
  loading.value = true
  currentPage.value = 1
  const start = Date.now()
  try {
    const params = { ...sortParams.value }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const res = await getProductList(params)
    if (res.data.code === 200) products.value = res.data.data
  } catch (e) { console.error(e); products.value = [] }
  const elapsed = Date.now() - start
  const delay = Math.max(0, 500 - elapsed)
  if (delay) {
    setTimeout(() => { loading.value = false }, delay)
  } else {
    loading.value = false
  }
}

function switchCategory(id) {
  activeCategoryId.value = id
  activeSort.value = '综合'
  sortParams.value = {}
  fetchProducts()
}

function switchSort(type) {
  if (type === '综合') {
    activeSort.value = '综合'
    sortParams.value = {}
    fetchProducts()
    return
  }
  if (activeSort.value === type) {
    const newDir = sortParams.value.sortDir === 'asc' ? 'desc' : 'asc'
    sortParams.value = { ...sortParams.value, sortDir: newDir }
  } else {
    activeSort.value = type
    sortParams.value = type === '价格'
      ? { sortBy: 'price', sortDir: 'asc' }
      : { sortBy: 'salesCount', sortDir: 'desc' }
  }
  fetchProducts()
}

function handleBuy(product) { router.push(`/product/${product.id}`) }

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(async () => {
  try {
    if (!cachedCategories) {
      const catRes = await getCategoryList()
      if (catRes.data.code === 200) cachedCategories = catRes.data.data
    }
    categories.value = cachedCategories
  } catch (e) { console.error(e) }
  if (route.params.categoryId) activeCategoryId.value = Number(route.params.categoryId)
  await fetchProducts()
})
</script>

<template>
  <div class="category-page">
    <div class="page-inner">
      <button class="back-btn" @click="router.go(-1)">
          <i class="ph ph-arrow-left"></i>
        </button>
      <div class="page-header">
        <div>
          <h1>商品列表</h1>
          <p class="count">共找到 <span>{{ totalCount }}</span> 件商品</p>
        </div>
        <div class="sort-bar">
          <button v-for="sort in ['综合', '销量', '价格']" :key="sort" class="sort-pill"
            :class="{ active: activeSort === sort }" @click="switchSort(sort)">
            {{ sort }}
            <span v-if="activeSort === sort && sortParams.sortDir" class="sort-arrow">{{ sortParams.sortDir === 'asc' ?
              '↑' : '↓' }}</span>
          </button>
        </div>
      </div>

      <div class="category-tabs">
        <button class="cat-tab" :class="{ active: activeCategoryId === '' }" @click="switchCategory('')">全部</button>
        <button v-for="cat in categories" :key="cat.id" class="cat-tab" :class="{ active: activeCategoryId === cat.id }"
          @click="switchCategory(cat.id)">{{ cat.name }}</button>
      </div>

      <div v-if="loading" class="product-grid">
        <div v-for="n in 8" :key="n" class="skeleton-shell">
          <div class="skeleton-core">
            <div class="sk-img"></div>
            <div class="sk-body">
              <div class="sk-line w-70"></div>
              <div class="sk-line w-40"></div>
              <div class="sk-line w-30"></div>
              <div class="sk-footer">
                <div class="sk-line w-35"></div>
                <div class="sk-btn"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <i class="ph ph-package"></i>
        <p>暂无商品</p>
      </div>

      <div v-else class="product-grid">
        <div v-for="product in pagedProducts" :key="product.id" class="product-shell">
          <div class="product-core">
            <router-link :to="`/product/${product.id}`" class="product-img">
              <span v-if="product.badge" class="prod-badge">{{ product.badge }}</span>
              <img :src="product.image" :alt="product.name" loading="lazy" />
            </router-link>
            <div class="product-info">
              <router-link :to="`/product/${product.id}`">
                <h3>{{ product.name }}</h3>
              </router-link>
              <p class="prod-category">{{ getCategoryName(product.categoryId) }}</p>
              <p class="prod-sales">已售 {{ product.salesCount }} 件</p>
              <div class="prod-footer">
                <div class="prod-price">
                  <span class="price-curr">¥{{ product.price }}</span>
                  <span v-if="product.originalPrice" class="price-orig">¥{{ product.originalPrice }}</span>
                </div>
                <button class="prod-btn" @click="handleBuy(product)">查看
                  <span class="btn-icon-wrap sm">
                    <i class="ph ph-arrow-right"></i>
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :class="{ disabled: currentPage === 1 }" :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"><i class="ph ph-caret-left"></i></button>
        <button v-for="page in totalPages" :key="page" class="page-btn" :class="{ active: currentPage === page }"
          @click="goToPage(page)">{{ page }}</button>
        <button class="page-btn" :class="{ disabled: currentPage === totalPages }"
          :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)"><i
            class="ph ph-caret-right"></i></button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.category-page {
  padding-top: 100px;
  padding-bottom: 80px;
}

.page-inner {
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;

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
}

.page-header {
  margin-bottom: 32px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;

  h1 {
    font-size: 32px;
    font-weight: 800;
    letter-spacing: -0.02em;
    color: $text-primary;
    margin: 0;
  }

  .count {
    font-size: 14px;
    color: $text-muted;
    margin-top: 8px;

    span {
      color: $accent-energy;
      font-weight: 600;
    }
  }
}

.sort-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}

.sort-pill {
  padding: 9px 20px;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  border-radius: 999px;
  color: $text-secondary;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  &:hover {
    background: $accent-energy-soft;
    border-color: $border-accent;
    color: $accent-energy;
  }

  &.active {
    background: $accent-energy;
    border-color: $accent-energy;
    color: #fff;
  }

  .sort-arrow {
    margin-left: 4px;
    font-size: 11px;
  }
}

.category-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 32px;
  border-bottom: 0.5px solid $border-subtle;
  padding-bottom: 16px;
  flex-wrap: wrap;
}

.cat-tab {
  padding: 9px 22px;
  background: transparent;
  border: 0.5px solid transparent;
  border-radius: 999px;
  color: $text-secondary;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  &:hover {
    background: $bg-card;
    color: $text-secondary;
  }

  &.active {
    background: $accent-energy-soft;
    border-color: $border-accent;
    color: $accent-energy;
  }
}

@keyframes sk-pulse {
  0% {
    opacity: 1;
  }

  50% {
    opacity: 0.15;
  }

  100% {
    opacity: 1;
  }
}

.skeleton-shell {
  @include solid-card;
  border-radius: $radius-xl;
  padding: 1.5px;
}

.skeleton-core {
  border-radius: calc($radius-xl - 0.375rem);
  overflow: hidden;
  background: $bg-subtle;
}

.sk-img {
  height: 200px;
  background: $border-medium;
  animation: sk-pulse 1.8s ease-in-out infinite;
}

.sk-body {
  padding: 20px;
}

.sk-line {
  height: 13px;
  border-radius: 8px;
  background: $border-medium;
  margin-bottom: 12px;
  animation: sk-pulse 1.8s ease-in-out infinite;
}

.sk-line.w-70 {
  width: 70%;
}

.sk-line.w-40 {
  width: 40%;
}

.sk-line.w-30 {
  width: 30%;
}

.sk-line.w-35 {
  width: 35%;
}

.sk-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.sk-btn {
  width: 80px;
  height: 36px;
  border-radius: 10px;
  background: $border-medium;
  animation: sk-pulse 1.8s ease-in-out infinite;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: $text-muted;

  i {
    font-size: 64px;
    display: block;
    margin-bottom: 16px;
  }

  p {
    font-size: 16px;
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-shell {
  @include solid-card;
  padding: 1.5px;

  span,p{
  }

  &:hover {
    @include card-hover;
  }
}

.product-core {
  border-radius: calc($radius-xl - 0.375rem);
  overflow: hidden;
  background: transparent;
  transition: all 0.5s $transition-premium;

  &:hover {
    .product-img img {
      transform: scale(1.05);
    }
  }
}

.product-img {
  position: relative;
  height: 200px;
  display: block;
  overflow: hidden;
  background: rgba($accent-energy, 0.03);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s $transition-premium;
    display: block;
  }
}

.prod-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 5px 12px;
  background: $accent-energy;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  z-index: 2;
}

.product-info {
  background-color: rgba(255, 255, 255, 0.1);
  padding: 20px;

  h3 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 6px;
    color: $text-primary;
    transition: color 0.3s;

    &:hover {
      color: $accent-energy;
    }
  }
}

.prod-category {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 2px;
}

.prod-sales {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 12px;
}

.prod-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.prod-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-curr {
  font-size: 20px;
  font-weight: 700;
  color: $price-color;
}

.price-orig {
  font-size: 12px;
  color: $price-muted;
  text-decoration: line-through;
}

.prod-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: $accent-energy;
  border: none;
  border-radius: 25px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  .btn-icon-wrap {
    @include btn-inner-icon;

    &.sm {
      width: 28px;
      height: 28px;
      font-size: 12px;
    }
  }

  &:hover {
    background: #e55a2b;
    box-shadow: 0 4px 16px rgba($accent-energy, 0.25);

    .btn-icon-wrap.sm {
      transform: translateX(2px);
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 48px;
}

.page-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 14px;
  border-radius: 12px;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  color: $text-secondary;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  &:hover:not(.disabled):not(.active) {
    border-color: $border-hover;
    color: $text-secondary;
  }

  &.active {
    background: $accent-energy;
    border-color: $accent-energy;
    color: #fff;
  }

  &.disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

@include respond(xl) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@include respond(lg) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@include respond(md) {
  .category-page {
    padding-top: 100px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;

    h1 {
      font-size: 26px;
    }
  }

  .category-tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    gap: 8px;
    padding-bottom: 12px;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  .cat-tab {
    white-space: nowrap;
    flex-shrink: 0;
    padding: 9px 18px;
    font-size: 13px;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }

  .product-img {
    height: 180px;
  }

  .product-info {
    padding: 16px;
  }

  .prod-btn {
    @include touch-target(36px);
  }
}
</style>
