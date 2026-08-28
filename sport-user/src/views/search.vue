<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchProduct } from '@/api/manager'
import { useCounterStore } from '@/stores/counter'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const store = useCounterStore()

const products = ref([])
const loading = ref(false)
const keyword = ref('')
const activeSort = ref('综合')
const currentPage = ref(1)
const pageSize = 12
const sortParams = ref({})

const totalCount = computed(() => products.value.length)
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 1)

const pagedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return products.value.slice(start, start + pageSize)
})

const displayKeyword = computed(() => keyword.value || route.query.keyword || '')

async function fetchProducts() {
  const kw = route.query.keyword
  if (!kw) { products.value = []; return }
  keyword.value = kw
  loading.value = true
  currentPage.value = 1
  try {
    const res = await searchProduct(kw, sortParams.value)
    if (res.data.code === 200) products.value = res.data.data || []
    else products.value = []
  } catch (e) { console.error(e); products.value = [] }
  loading.value = false
}

function switchSort(type) {
  if (type === '综合') { activeSort.value = '综合'; sortParams.value = {}; fetchProducts(); return }
  if (activeSort.value === type) {
    const newDir = sortParams.value.sortDir === 'asc' ? 'desc' : 'asc'
    sortParams.value = { ...sortParams.value, sortDir: newDir }
  } else {
    activeSort.value = type
    sortParams.value = type === '价格' ? { sortBy: 'price', sortDir: 'asc' } : { sortBy: 'salesCount', sortDir: 'desc' }
  }
  fetchProducts()
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.query.keyword, () => fetchProducts())
onMounted(() => fetchProducts())
</script>

<template>
  <div class="search-page">
    <div class="page-inner">
      <div class="search-header">
        <button class="back-icon" @click="router.go(-1)"><i class="ph ph-arrow-left"></i></button>
        <h1 class="search-title">搜索 "<span>{{ displayKeyword }}</span>"</h1>
        <span class="search-count">共 <span>{{ totalCount }}</span> 件商品</span>
      </div>

      <div class="search-tabs">
        <button v-for="sort in ['综合', '销量', '价格']" :key="sort" class="sort-tab" :class="{ active: activeSort === sort }"
          @click="switchSort(sort)">
          {{ sort }}
          <span v-if="activeSort === sort && sortParams.sortDir" class="sort-arr">{{ sortParams.sortDir === 'asc' ? '↑'
            : '↓' }}</span>
        </button>
      </div>

      <div class="results-area">
        <div v-if="loading" class="results-grid">
          <div v-for="n in 6" :key="n" class="sk-shell">
            <div class="sk-core">
              <div class="sk-img"></div>
              <div class="sk-body">
                <div class="sk-line w-80"></div>
                <div class="sk-line w-50"></div>
                <div class="sk-line w-60"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="products.length === 0" class="no-results">
          <i class="ph ph-magnifying-glass"></i>
          <h2>未找到相关商品</h2>
          <p>试试其他搜索关键词，或浏览商品分类</p>
          <router-link to="/category" class="browse-btn">浏览全部商品</router-link>
        </div>

        <template v-else>
          <div class="results-grid">
            <div v-for="product in pagedProducts" :key="product.id" class="result-shell">
              <div class="result-core">
                <router-link :to="`/product/${product.id}`" class="result-img">
                  <span v-if="product.badge" class="result-badge">{{ product.badge }}</span>
                  <img :src="product.image" :alt="product.name" loading="lazy" />
                </router-link>
                <div class="result-info">
                  <router-link :to="`/product/${product.id}`">
                    <h3>{{ product.name }}</h3>
                  </router-link>
                  <div class="result-footer">
                    <span class="result-price">¥{{ product.price }}</span>
                    <span class="result-sales">已售 {{ product.salesCount || 0 }}</span>
                  </div>
                  <router-link :to="`/product/${product.id}`" class="cart-add-btn">
                    <i class="ph ph-eye"></i> 查看详情
                  </router-link>
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
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.search-page {
  padding-top: 100px;
  padding-bottom: 100px;
}

.page-inner {
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.back-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.4s $transition-premium;
  flex-shrink: 0;

  &:hover {
    background: $bg-elevated;
    color: $text-secondary;
  }
}

.search-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0;
  color: $text-primary;

  span {
    color: $accent-energy;
  }
}

.search-count {
  font-size: 14px;
  color: $text-muted;
  margin-left: auto;

  span {
    color: $text-secondary;
    font-weight: 600;
  }
}

.search-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 32px;
  border-bottom: 0.5px solid $border-subtle;
  padding-bottom: 16px;
}

.sort-tab {
  padding: 9px 22px;
  background: transparent;
  border: none;
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
    background: $accent-energy;
    color: #fff;
  }

  .sort-arr {
    margin-left: 4px;
    font-size: 11px;
  }
}

.results-area {
  min-height: 400px;
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

.sk-shell {
  @include solid-card;
  border-radius: $radius-xl;
  padding: 1.5px;
}

.sk-core {
  border-radius: calc($radius-xl - 0.375rem);
  overflow: hidden;
  background: $bg-subtle;
}

.sk-img {
  height: 180px;
  background: rgba($accent-energy, 0.04);
  animation: sk-pulse 1.8s ease-in-out infinite;
}

.sk-body {
  padding: 18px;
}

.sk-line {
  height: 13px;
  border-radius: 8px;
  background: $bg-card;
  margin-bottom: 12px;
  animation: sk-pulse 1.8s ease-in-out infinite;
}

.sk-line.w-80 {
  width: 80%;
}

.sk-line.w-60 {
  width: 60%;
}

.sk-line.w-50 {
  width: 50%;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.result-shell {
  @include solid-card;
  padding: 1.5px;

  &:hover {
    @include card-hover;
  }
}

.result-core {
  border-radius: calc($radius-xl - 0.375rem);
  overflow: hidden;
  background: transparent;
  transition: all 0.5s $transition-premium;

  &:hover {
    .result-img img {
      transform: scale(1.05);
    }
  }
}

.result-img {
  position: relative;
  height: 180px;
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

.result-badge {
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

.result-info {
  padding: 18px;

  h3 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 12px;
    color: $text-primary;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    transition: color 0.3s;

    &:hover {
      color: $accent-energy;
    }
  }
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-price {
  font-size: 20px;
  font-weight: 700;
  color: $price-color;
}

.result-sales {
  font-size: 12px;
  color: $text-muted;
}

.cart-add-btn {
  padding: 10px;
  background: $bg-card;
  border: 0.5px solid $border-glass;
  border-radius: 10px;
  color: $text-secondary;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  &:hover {
    background: $accent-energy;
    border-color: $accent-energy;
    color: #fff;
  }
}

.no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 40px;
  text-align: center;

  i {
    font-size: 72px;
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
  }
}

.browse-btn {
  display: inline-flex;
  padding: 12px 28px;
  background: $accent-energy;
  border: none;
  border-radius: 999px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  transition: all 0.4s $transition-premium;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba($accent-energy, 0.25);
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
  .results-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@include respond(md) {
  .search-page {
    padding-top: 100px;
  }

  .search-header {
    flex-wrap: wrap;
    gap: 12px;
  }

  .search-title {
    font-size: 22px;
  }

  .search-count {
    margin-left: 0;
    width: 100%;
  }

  .search-tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
    gap: 8px;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  .sort-tab {
    white-space: nowrap;
    flex-shrink: 0;
  }

  .results-grid {
    grid-template-columns: 1fr;
  }

  .result-img {
    height: 180px;
  }

  .result-info {
    padding: 14px;
  }

  .cart-add-btn {
    @include touch-target(36px);
  }
}
</style>
