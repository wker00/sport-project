<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getReviewList, getReviewDetail, replyReview, deleteReview } from '@/api/manager'
import { toast } from '@/utils/toast'

const reviews = ref([])
const loading = ref(false)
const ratingFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)

const ratingOptions = [
  { label: '全部', value: 'all' },
  { label: '1星', value: 1 },
  { label: '2星', value: 2 },
  { label: '3星', value: 3 },
  { label: '4星', value: 4 },
  { label: '5星', value: 5 },
]

const filteredReviews = computed(() => {
  if (ratingFilter.value === 'all') return reviews.value
  return reviews.value.filter((r) => r.rating === ratingFilter.value)
})

const total = computed(() => filteredReviews.value.length)

const pagedReviews = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredReviews.value.slice(start, start + pageSize.value)
})

const fetchReviews = async (resetPage = false) => {
  if (resetPage) page.value = 1
  loading.value = true
  try {
    const res = await getReviewList()
    reviews.value = res.data.data || []
  } catch {
    reviews.value = []
  } finally {
    loading.value = false
    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (page.value > maxPage) page.value = maxPage
  }
}

watch(ratingFilter, () => {
  page.value = 1
})

const handlePageChange = (p) => {
  page.value = p
}

const truncateText = (text, max = 30) => {
  if (!text) return '-'
  return text.length > max ? text.slice(0, max) + '...' : text
}

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const currentReview = ref(null)

const handleView = async (row) => {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await getReviewDetail(row.id)
    currentReview.value = res.data.data
  } catch {
    currentReview.value = row
  } finally {
    detailLoading.value = false
  }
}

// 回复弹窗
const replyVisible = ref(false)
const replyLoading = ref(false)
const replyForm = ref({ id: null, replyContent: '' })

const handleReply = (row) => {
  replyForm.value = { id: row.id, replyContent: row.replyContent || '' }
  replyVisible.value = true
}

const submitReply = async () => {
  if (!replyForm.value.replyContent.trim()) {
    toast('请输入回复内容', 'warning')
    return
  }
  replyLoading.value = true
  try {
    await replyReview(replyForm.value.id, { replyContent: replyForm.value.replyContent })
    toast('回复成功')
    replyVisible.value = false
    await fetchReviews()
  } catch {
  } finally {
    replyLoading.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.nickname || row.username}」对「${row.productName}」的评价？删除后用户可重新评价。`,
      '确认删除',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteReview(row.id)
    toast('删除成功')
    await fetchReviews()
  } catch {
  }
}

onMounted(() => fetchReviews(true))
</script>

<template>
  <div class="page">
    <h1 class="page__title">评价管理</h1>
    <p class="page__desc">查看和管理用户商品评价</p>

    <div class="card">
      <div class="toolbar">
        <div class="toolbar__tabs">
          <el-radio-group v-model="ratingFilter" size="small">
            <el-radio-button v-for="opt in ratingOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-table :data="pagedReviews" v-loading="loading" stripe class="reviews-table" empty-text="暂无评价数据">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column label="图片" width="75" align="center">
          <template #default="{ row }">
            <el-image :src="row.productImage" fit="cover" class="product-info__img">
              <template #error>
                <el-icon :size="20">
                  <Picture />
                </el-icon>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" prop="productName" min-width="200" align="center">
        </el-table-column>
        <el-table-column label="用户" width="120" align="center">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.avatar">
                <el-icon :size="12">
                  <UserFilled />
                </el-icon>
              </el-avatar>
              <span>{{ row.nickname || row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="120" align="center">
          <template #default="{ row }">
            <div class="rating">
              <el-icon v-for="i in 5" :key="i" class="rating__star"
                :class="{ 'rating__star--active': i <= row.rating }">
                <StarFilled />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="200" align="center">
          <template #default="{ row }">
            <span class="review-text" :title="row.reviewContent">{{ truncateText(row.reviewContent) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="回复状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.replyContent" type="success" size="small">已回复</el-tag>
            <el-tag v-else type="info" size="small">未回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewTime" label="评价时间" width="170" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="primary" link size="small" @click="handleView(row)">
                详情
              </el-button>
              <el-button type="primary" link size="small" @click="handleReply(row)">
                {{ row.replyContent ? '修改回复' : '回复' }}
              </el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next, total"
          background @current-change="handlePageChange" />
      </div>
    </div>
  </div>

  <!-- 详情弹窗 -->
  <el-dialog v-model="detailVisible" title="评价详情" width="560px" :close-on-click-modal="false">
    <el-skeleton v-if="detailLoading" :rows="6" animated />
    <div v-else-if="currentReview" class="detail">
      <div class="detail__header">
        <div class="detail__product">
          <el-image :src="currentReview.productImage" fit="cover" class="detail__product-img">
            <template #error>
              <el-icon :size="20">
                <Picture />
              </el-icon>
            </template>
          </el-image>
          <span class="detail__product-name">{{ currentReview.productName }}</span>
        </div>
        <div class="detail__user">
          <el-avatar :size="36" :src="currentReview.avatar">
            <el-icon :size="18">
              <UserFilled />
            </el-icon>
          </el-avatar>
          <div class="detail__user-info">
            <span class="detail__nickname">{{ currentReview.nickname || currentReview.username }}</span>
            <span class="detail__time">{{ currentReview.reviewTime }}</span>
          </div>
        </div>
      </div>
      <div class="detail__rating">
        <el-icon v-for="i in 5" :key="i" class="rating__star rating__star--lg"
          :class="{ 'rating__star--active': i <= currentReview.rating }">
          <StarFilled />
        </el-icon>
      </div>
      <p class="detail__content">{{ currentReview.reviewContent || '暂无评价内容' }}</p>
      <div v-if="currentReview.reviewImages" class="detail__images">
        <el-image v-for="(img, idx) in JSON.parse(currentReview.reviewImages)" :key="idx" :src="img" fit="cover"
          :preview-src-list="JSON.parse(currentReview.reviewImages)" :initial-index="idx" class="detail__image" />
      </div>
      <el-divider v-if="currentReview.replyContent" />
      <div v-if="currentReview.replyContent" class="detail__reply">
        <span class="detail__reply-label">商家回复：</span>
        <p class="detail__reply-content">{{ currentReview.replyContent }}</p>
        <span class="detail__reply-time">{{ currentReview.replyTime }}</span>
      </div>
    </div>
  </el-dialog>

  <!-- 回复弹窗 -->
  <el-dialog v-model="replyVisible" title="回复评价" width="520px" :close-on-click-modal="false">
    <el-input v-model="replyForm.replyContent" type="textarea" :rows="4" placeholder="请输入回复内容，最多 500 个字符"
      maxlength="500" show-word-limit />
    <template #footer>
      <el-button @click="replyVisible = false">取消</el-button>
      <el-button type="primary" :loading="replyLoading" @click="submitReply">
        确认回复
      </el-button>
    </template>
  </el-dialog>
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
}

.reviews-table {
  width: 100%;

  .product-info__img {
    width: 44px;
    height: 44px;
    border-radius: 6px;
    background: #f1f5f9;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  span {
    cursor: context-menu;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;

  &__img {
    width: 44px;
    height: 44px;
    border-radius: 6px;
    flex-shrink: 0;
  }

  &__name {
    font-size: 14px;
    font-weight: 500;
    color: #0f172a;
    text-align: left;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
  font-size: 14px;
  color: #0f172a;
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
  justify-content: center;

  &__star {
    font-size: 14px;
    color: #d1d5db;

    &--active {
      color: #f59e0b;
    }

    &--lg {
      font-size: 20px;
    }
  }
}

.review-text {
  font-size: 14px;
  color: #334155;
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 2px;

  .el-button {
    margin: 0;
  }
}

// 详情弹窗
.detail {
  &__header {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;
  }

  &__product {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f8fafc;
    border-radius: 10px;
  }

  &__product-img {
    width: 44px;
    height: 44px;
    border-radius: 6px;
    background: #f1f5f9;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__product-name {
    font-size: 14px;
    font-weight: 600;
    color: #0f172a;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  &__user-info {
    display: flex;
    flex-direction: column;
  }

  &__nickname {
    font-size: 14px;
    font-weight: 500;
    color: #0f172a;
  }

  &__time {
    font-size: 12px;
    color: $text-muted;
  }

  &__rating {
    display: flex;
    gap: 4px;
    margin-bottom: 12px;
  }

  &__content {
    font-size: 14px;
    color: #334155;
    line-height: 1.6;
    margin: 0 0 12px;
  }

  &__images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 12px;
  }

  &__image {
    width: 80px;
    height: 80px;
    border-radius: 8px;
  }

  &__reply {
    background: #f8fafc;
    border-radius: 10px;
    padding: 14px;
  }

  &__reply-label {
    font-size: 13px;
    font-weight: 600;
    color: #0f172a;
  }

  &__reply-content {
    font-size: 13px;
    color: #334155;
    line-height: 1.6;
    margin: 6px 0 4px;
  }

  &__reply-time {
    font-size: 12px;
    color: $text-muted;
  }
}
</style>
