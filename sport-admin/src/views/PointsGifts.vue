<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getPointsGiftsList, getPointsGiftDetail, createPointsGift, updatePointsGift, deletePointsGift } from '@/api/manager'
import { toast } from '@/utils/toast'

const gifts = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = ref({
  name: '', image: '', pointsPrice: null, stock: null, description: '',
})

const giftImageFile = ref(null)
const giftImagePreview = ref('')

const displayImage = computed(() => giftImagePreview.value || form.value.image || '')

const rules = {
  name: [{ required: true, message: '请输入积分商品名称', trigger: 'blur' }],
  pointsPrice: [
    { required: true, message: '请输入积分价格', trigger: 'blur' },
    { type: 'number', min: 1, message: '积分价格必须大于 0', trigger: 'blur' },
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能为负数', trigger: 'blur' },
  ],
}

const total = computed(() => gifts.value.length)

const pagedGifts = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return gifts.value.slice(start, start + pageSize.value)
})

const clearImage = () => {
  if (giftImagePreview.value) URL.revokeObjectURL(giftImagePreview.value)
  giftImageFile.value = null
  giftImagePreview.value = ''
}

const resetForm = () => {
  form.value = { name: '', image: '', pointsPrice: null, stock: null, description: '' }
  clearImage()
}

const fetchGifts = async (resetPage = false) => {
  if (resetPage) page.value = 1
  loading.value = true
  try {
    const res = await getPointsGiftsList()
    gifts.value = res.data.data || []
  } catch {
    gifts.value = []
  } finally {
    loading.value = false
    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (page.value > maxPage) page.value = maxPage
  }
}

const handleCreate = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  form.value = {
    name: row.name || '',
    image: row.image || '',
    pointsPrice: row.pointsPrice ?? null,
    stock: row.stock ?? null,
    description: row.description || '',
  }
  clearImage()
  dialogVisible.value = true
}

// 查看
const detailVisible = ref(false)
const detailData = ref(null)

const handleView = async (row) => {
  try {
    const res = await getPointsGiftDetail(row.id)
    detailData.value = res.data.data
    detailVisible.value = true
  } catch { toast('加载详情失败', 'error') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除积分商品「${row.name}」？`, '确认删除', {
      confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning',
    })
    await deletePointsGift(row.id)
    toast('删除成功')
    await fetchGifts()
  } catch { }
}

const handleImageChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return false
  if (!file.type.startsWith('image/')) { toast('只能上传图片文件', 'warning'); return false }
  if (file.size / 1024 / 1024 > 10) { toast('图片大小不能超过 10MB', 'warning'); return false }
  giftImageFile.value = file
  giftImagePreview.value = URL.createObjectURL(file)
  form.value.image = ''
  return false
}

const removeImage = () => {
  clearImage()
  form.value.image = ''
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (giftImageFile.value) {
      const fd = new FormData()
      const giftData = { ...form.value }
      delete giftData.image
      fd.append('gift', new Blob([JSON.stringify(giftData)], { type: 'application/json' }))
      fd.append('file', giftImageFile.value)
      if (editingId.value) await updatePointsGift(editingId.value, fd)
      else await createPointsGift(fd)
    } else {
      if (editingId.value) await updatePointsGift(editingId.value, form.value)
      else await createPointsGift(form.value)
    }
    toast(editingId.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    await fetchGifts()
  } catch {
  } finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => { if (giftImagePreview.value) URL.revokeObjectURL(giftImagePreview.value) })

onMounted(() => fetchGifts(true))
</script>

<template>
  <div class="page">
    <h1 class="page__title">积分商品</h1>
    <p class="page__desc">管理积分商城中的兑换商品</p>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable class="toolbar__input" @keyup.enter="fetchGifts" />
        <el-button type="primary" @click="fetchGifts">搜索</el-button>
        <el-button @click="keyword = ''">重置</el-button>
        <el-button type="primary" class="toolbar__add" @click="handleCreate">+ 添加积分商品</el-button>
      </div>

      <el-table :data="pagedGifts" v-loading="loading" stripe empty-text="暂无积分商品数据">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column label="图片" width="75"  align="center">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" fit="cover" class="gift-img" />
            <el-icon v-else :size="20">
              <Picture />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" align="center"/>
        <el-table-column label="积分价格" width="120" align="center">
          <template #default="{ row }">
            <span class="points-price">{{ row.pointsPrice }} 积分</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip align="center"/>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center"/>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination :current-page="page" :page-size="pageSize" :total="total" layout="prev, pager, next, total"
          background @current-change="page = $event" />
      </div>
    </div>
  </div>

  <!-- 查看弹窗 -->
  <el-dialog v-model="detailVisible" title="积分商品详情" width="520px" :close-on-click-modal="false">
    <el-descriptions v-if="detailData" :column="1" border>
      <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
      <el-descriptions-item label="图片">
        <el-image v-if="detailData.image" :src="detailData.image" fit="cover" style="width:80px;height:80px;border-radius:6px" />
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="积分价格">{{ detailData.pointsPrice }}</el-descriptions-item>
      <el-descriptions-item label="库存">{{ detailData.stock }}</el-descriptions-item>
      <el-descriptions-item label="描述">{{ detailData.description || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
    </el-descriptions>
  </el-dialog>

  <el-dialog :model-value="dialogVisible" :title="editingId ? '编辑积分商品' : '添加积分商品'" width="520px"
    :close-on-click-modal="false" @close="clearImage(); dialogVisible = false">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" label-position="right">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="最多 100 个字符" maxlength="100" show-word-limit />
      </el-form-item>

      <el-form-item label="商品图片">
        <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" :on-change="handleImageChange">
          <el-button type="primary" plain>选择图片</el-button>
          <template #tip><span class="upload-tip">支持 JPG/PNG，最大 10MB</span></template>
        </el-upload>
        <div v-if="displayImage" class="img-preview">
          <el-image :src="displayImage" fit="cover" class="img-preview__el" />
          <el-button type="danger" link size="small" @click="removeImage">移除</el-button>
        </div>
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="积分价格" prop="pointsPrice">
            <el-input-number v-model="form.pointsPrice" :min="1" controls-position="right" class="full-width" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="form.stock" :min="0" controls-position="right" class="full-width" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit resize="none"
          class="textarea-fixed textarea-fixed--sm" placeholder="可选" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="clearImage(); dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ editingId ? '保存修改' : '确认创建' }}
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

  &__input {
    width: 280px;
  }

  &__add {
    margin-left: auto;
  }
}


.gift-img {
  width: 44px; height: 44px; border-radius: 6px; background: #f1f5f9;
}

:deep(.el-table .el-table__body .el-table__row) { height: 50px; }

.points-price {
  font-weight: 600;
  color: #ff6b35;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.full-width {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 8px;
}

:deep(.el-form-item__content){
  display: block;
}
.img-preview {

  margin-top: 10px;

  &__el {
    width: 120px;
    height: 120px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    display: block;
  }
}

.textarea-fixed {
  :deep(.el-textarea__inner) {
    resize: none;
    box-sizing: border-box;
    overflow-y: auto;
    line-height: 1.5;
  }

  &--sm :deep(.el-textarea__inner) {
    height: 96px;
    min-height: 96px;
    max-height: 96px;
  }
}
</style>
