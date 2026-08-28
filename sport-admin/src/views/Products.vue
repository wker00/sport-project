<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getProductList, searchProduct, deleteProduct, getProductByCategory, getCategoryList } from '@/api/manager'
import { toast } from '@/utils/toast'
import ProductFormDialog from '@/components/ProductFormDialog.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const products = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const categories = ref([])
const selectedCategory = ref('all')

const productFormVisible = ref(false)
const productFormMode = ref('create')
const editProductId = ref(null)

const serverTotal = ref(0)

const badgeMap = {
  '热卖': { type: 'danger' },
  '新品': { type: 'success' },
  '推荐': { type: 'warning' },
}

const isServerPaging = computed(() => selectedCategory.value === 'all' && !keyword.value.trim())

const tableData = computed(() => {
  if (isServerPaging.value) return products.value
  const start = (page.value - 1) * pageSize.value
  return products.value.slice(start, start + pageSize.value)
})

const total = computed(() => isServerPaging.value ? serverTotal.value : products.value.length)

const rowIndex = (i) => (page.value - 1) * pageSize.value + i + 1

const syncRouteQuery = () => {
  const query = {}
  if (page.value > 1) query.page = String(page.value)
  const kw = keyword.value.trim()
  if (kw) query.keyword = kw
  if (selectedCategory.value !== 'all') query.categoryId = String(selectedCategory.value)
  router.replace({ query })
}

const restoreFromRoute = () => {
  const { page: qPage, keyword: qKeyword, categoryId } = route.query
  if (qPage) {
    const p = Number(qPage)
    if (p > 0) page.value = p
  }
  if (typeof qKeyword === 'string') keyword.value = qKeyword
  if (categoryId) {
    const id = Number(categoryId)
    if (!Number.isNaN(id)) selectedCategory.value = id
  } else {
    selectedCategory.value = 'all'
  }
}

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
  if (page.value > maxPage) page.value = maxPage
}

const fetchProducts = async (resetPage = false) => {
  if (resetPage) page.value = 1
  loading.value = true
  try {
    if (keyword.value.trim()) {
      const catId = selectedCategory.value !== 'all' ? selectedCategory.value : undefined
      const res = await searchProduct(keyword.value.trim(), catId)
      products.value = res.data.data || []
    } else if (selectedCategory.value !== 'all') {
      const res = await getProductByCategory(selectedCategory.value)
      products.value = res.data.data || []
    } else {
      const res = await getProductList({ page: page.value, size: pageSize.value })
      const data = res.data.data || {}
      products.value = data.records || []
      serverTotal.value = data.total || 0
    }
  } catch {
    products.value = []
    serverTotal.value = 0
  } finally {
    loading.value = false
    const prevPage = page.value
    clampPage()
    syncRouteQuery()
    if (isServerPaging.value && page.value !== prevPage) {
      await fetchProducts()
    }
  }
}

const fetchCategories = async () => {
  try { const res = await getCategoryList(); categories.value = res.data.data || [] }
  catch { categories.value = [] }
}

const handleSearch = () => fetchProducts(true)

const handleCategoryChange = () => fetchProducts(true)

const handleReset = () => {
  keyword.value = ''
  selectedCategory.value = 'all'
  fetchProducts(true)
}

const handlePageChange = (p) => {
  page.value = p
  if (isServerPaging.value) fetchProducts()
  else syncRouteQuery()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除商品「${row.name}」？`, '确认删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteProduct(row.id)
    toast('删除成功')
    await fetchProducts()
    if (isServerPaging.value && products.value.length === 0 && page.value > 1) {
      page.value--
      await fetchProducts()
    }
  } catch { }
}

const handleEdit = (row) => {
  editProductId.value = row.id
  productFormMode.value = 'edit'
  productFormVisible.value = true
}

const handleCreate = () => {
  editProductId.value = null
  productFormMode.value = 'create'
  productFormVisible.value = true
}

const handleFormSuccess = () => {
  fetchProducts()
}

const formatAmount = (v) => '¥' + (v ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })

const getSpecCount = (specsStr) => {
  if (!specsStr) return 0
  try {
    const parsed = JSON.parse(specsStr)
    if (Array.isArray(parsed)) return 0
    return parsed.specItems?.length || 0
  } catch { return 0 }
}

const getPriceRange = (row) => {
  try {
    const parsed = JSON.parse(row.specs)
    if (Array.isArray(parsed) || !parsed.specItems?.length) {
      return formatAmount(row.price)
    }
    const prices = parsed.specItems.map(i => i.price).sort((a, b) => a - b)
    const min = prices[0]
    const max = prices[prices.length - 1]
    if (min === max) return formatAmount(min)
    return `${formatAmount(min)}~${formatAmount(max)}`
  } catch {
    return formatAmount(row.price)
  }
}

onMounted(async () => {
  restoreFromRoute()
  await fetchCategories()
  await fetchProducts()
})
</script>

<template>
  <div class="page">
    <h1 class="page__title">商品列表</h1>
    <p class="page__desc">管理平台所有商品</p>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable class="toolbar__input"
          @keyup.enter="handleSearch" />
        <el-select v-model="selectedCategory" placeholder="按分类筛选" style="width:160px"
          @change="handleCategoryChange">
          <el-option value="all" label="全部分类" />
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="primary" class="toolbar__add" @click="handleCreate">
          + 添加商品
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe class="products-table" empty-text="暂无商品数据">
        <el-table-column type="index" :index="rowIndex" label="#" width="55" align="center" />
        <el-table-column label="图片" width="75" align="center">
          <template #default="{ row }">
            <el-image :src="row.image" fit="cover" class="product-img">
              <template #error>
                <el-icon :size="20">
                  <Picture />
                </el-icon>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" min-width="180">
          <template #default="{ row }">
            <div class="product-name">{{ row.name }}</div>
            <div v-if="row.subtitle" class="product-subtitle">{{ row.subtitle }}</div>
          </template>
        </el-table-column>
        <el-table-column label="规格" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="getSpecCount(row.specs) > 0" size="small" type="info">
              {{ getSpecCount(row.specs) }}种规格
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="160" align="center">
          <template #default="{ row }">
            <div class="product-price">
              <span class="product-price__current">{{ getPriceRange(row) }}</span>
              <span v-if="row.originalPrice && row.originalPrice !== row.price" class="product-price__original">
                {{ formatAmount(row.originalPrice) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="70" align="center" />
        <el-table-column prop="salesCount" label="销量" width="70" align="center" />
        <el-table-column label="标签" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.badge" :type="badgeMap[row.badge]?.type || 'info'" size="small">
              {{ row.badge }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isOn === 1 ? 'success' : 'info'" size="small">
              {{ row.isOn === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next, total"
          background @current-change="handlePageChange" />
      </div>
    </div>
  </div>

  <ProductFormDialog v-model:visible="productFormVisible" :mode="productFormMode" :product-id="editProductId"
    @success="handleFormSuccess" />
</template>

<style lang="scss" scoped>
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
    margin: 0 0 24px;
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

  &__input {
    width: 280px;
  }

  &__add {
    margin-left: auto;
  }
}

.products-table {
  width: 100%;
}

.product-img {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-name {
  font-weight: 500;
  color: #0f172a;
  font-size: 14px;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 12px;
  color: $text-muted;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 240px;
}

.product-price {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;

  &__current {
    font-weight: 600;
    color: #ff6b35;
    font-size: 14px;
  }

  &__original {
    font-size: 12px;
    color: $text-muted;
    text-decoration: line-through;
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
</style>
