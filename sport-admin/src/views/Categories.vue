<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getCategoryList, getCategoryDetail, createCategory, updateCategory, deleteCategory } from '@/api/manager'
import { toast } from '@/utils/toast'

const categories = ref([])
const loading = ref(false)

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data.data || []
  } catch {
    categories.value = []
  } finally {
    loading.value = false
  }
}

// 创建
const createVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({ name: '', icon: '', description: '', sortOrder: 0 })
const createFile = ref(null)
const createPreview = ref('')
const createRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}
const createRef = ref(null)

const handleCreate = () => {
  createForm.value = { name: '', icon: '', description: '', sortOrder: 0 }
  createFile.value = null
  createPreview.value = ''
  createVisible.value = true
}

const onCreateFileChange = (file) => {
  createFile.value = file.raw
  createPreview.value = URL.createObjectURL(file.raw)
}

const onCreateFileRemove = () => {
  if (createPreview.value) URL.revokeObjectURL(createPreview.value)
  createFile.value = null
  createPreview.value = ''
}

const submitCreate = async () => {
  const valid = await createRef.value.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    const fd = new FormData()
    fd.append('category', new Blob([JSON.stringify(createForm.value)], { type: 'application/json' }))
    if (createFile.value) fd.append('file', createFile.value)
    await createCategory(fd)
    toast('创建成功')
    createVisible.value = false
    await fetchCategories()
  } catch {
  } finally {
    createLoading.value = false
  }
}

// 编辑
const editVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({ id: null, name: '', icon: '', description: '', sortOrder: 0 })
const editFile = ref(null)
const editPreview = ref('')
const editRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}
const editRef = ref(null)

const handleEdit = (row) => {
  editForm.value = {
    id: row.id,
    name: row.name || '',
    icon: row.icon || '',
    description: row.description || '',
    sortOrder: row.sortOrder ?? 0,
  }
  editFile.value = null
  editPreview.value = row.image || ''
  editVisible.value = true
}

const onEditFileChange = (file) => {
  editFile.value = file.raw
  editPreview.value = URL.createObjectURL(file.raw)
}

const onEditFileRemove = () => {
  if (editPreview.value && editFile.value) URL.revokeObjectURL(editPreview.value)
  editFile.value = null
  editPreview.value = ''
}

const submitEdit = async () => {
  const valid = await editRef.value.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    const fd = new FormData()
    fd.append('category', new Blob([JSON.stringify(editForm.value)], { type: 'application/json' }))
    if (editFile.value) fd.append('file', editFile.value)
    await updateCategory(editForm.value.id, fd)
    toast('更新成功')
    editVisible.value = false
    await fetchCategories()
  } catch {
  } finally {
    editLoading.value = false
  }
}

// 查看
const detailVisible = ref(false)
const detailData = ref(null)

const handleView = async (row) => {
  try {
    const res = await getCategoryDetail(row.id)
    detailData.value = res.data.data
    detailVisible.value = true
  } catch { toast('加载详情失败', 'error') }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '确认删除', {
      confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning',
    })
    await deleteCategory(row.id)
    toast('删除成功')
    await fetchCategories()
  } catch { /* 静默 */ }
}

onMounted(fetchCategories)
</script>

<template>
  <div class="page">
    <div class="page__header">
      <div>
        <h1 class="page__title">分类管理</h1>
        <p class="page__desc">管理商品分类</p>
      </div>
      <el-button type="primary" @click="handleCreate">+ 创建分类</el-button>
    </div>

    <div class="card">
      <el-table :data="categories" v-loading="loading" stripe class="cats-table" empty-text="暂无分类数据">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <el-image v-if="row.image" :src="row.image" fit="cover" class="product-img">
              <template #error>
                <el-icon :size="20"><Picture /></el-icon>
              </template>
            </el-image>
            <el-icon v-else :size="20" class="img-placeholder"><Picture /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="icon" label="图标标识" width="120" />
        <el-table-column prop="description" label="描述" min-width="220" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="info" link size="small" @click="handleView(row)">查看</el-button>
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 创建弹窗 -->
    <el-dialog v-model="createVisible" title="创建分类" width="520px" :close-on-click-modal="false">
      <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="100px" label-position="right">
        <el-form-item label="名称" prop="name">
          <el-input v-model="createForm.name" placeholder="如 篮球" />
        </el-form-item>
        <el-form-item label="分类图片">
          <div class="upload-wrap">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="onCreateFileChange"
              accept="image/*">
              <el-button type="primary">选择图片</el-button>
            </el-upload>
            <div v-if="createPreview" class="upload-preview">
              <el-image :src="createPreview" fit="cover" class="upload-preview__img" />
              <el-button type="danger" size="small" circle class="upload-preview__remove" @click="onCreateFileRemove">
                <el-icon>
                  <Close />
                </el-icon>
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input v-model="createForm.icon" placeholder="可选" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit
            resize="none" placeholder="可选" class="textarea-fixed textarea-fixed--sm" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="createForm.sortOrder" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="detailVisible" title="分类详情" width="520px" :close-on-click-modal="false">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="名称">{{ detailData.name }}</el-descriptions-item>
        <el-descriptions-item label="图片">
          <el-image v-if="detailData.image" :src="detailData.image" fit="cover" style="width:80px;height:80px;border-radius:8px">
            <template #error>
              <el-icon :size="28"><Picture /></el-icon>
            </template>
          </el-image>
          <el-icon v-else :size="28" class="img-placeholder"><Picture /></el-icon>
        </el-descriptions-item>
        <el-descriptions-item label="图标标识">{{ detailData.icon || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ detailData.sortOrder }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑分类" width="520px" :close-on-click-modal="false">
      <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="100px" label-position="right">
        <el-form-item label="名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="分类图片">
          <div class="upload-wrap">
            <el-upload action="#" :auto-upload="false" :show-file-list="false" :on-change="onEditFileChange"
              accept="image/*">
              <el-button type="primary">选择图片</el-button>
            </el-upload>
            <div v-if="editPreview" class="upload-preview">
              <el-image :src="editPreview" fit="cover" class="upload-preview__img" />
              <el-button type="danger" size="small" circle class="upload-preview__remove" @click="onEditFileRemove">
                <el-icon>
                  <Close />
                </el-icon>
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input v-model="editForm.icon" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit
            resize="none" class="textarea-fixed textarea-fixed--sm" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="editForm.sortOrder" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">确认更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
$text-muted: #94a3b8;

.page {
  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 24px;
  }

  &__title {
    font-size: 26px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: -0.3px;
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 15px;
    color: $text-muted;
    margin: 0;
  }
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.cats-table {
  width: 100%;
}

:deep(.el-table .el-table__body .el-table__row) {
  height: 50px;
}

.product-img {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  background: #f1f5f9;
}

.textarea-fixed {
  :deep(.el-textarea__inner) {
    resize: none;
    box-sizing: border-box;
    overflow-y: auto;
    line-height: 1.5;
  }

  &--sm :deep(.el-textarea__inner) {
    height: 72px;
    min-height: 72px;
    max-height: 72px;
  }
}

.text-muted {
  color: #94a3b8;
  font-size: 13px;
}

.img-placeholder {
  color: #94a3b8;
}

.upload-wrap {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-preview {
  position: relative;
  display: inline-block;

  &__img {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
  }

  &__remove {
    position: absolute;
    top: -8px;
    right: -8px;
  }
}

.action-group {
  display: flex;
  justify-content: center;
  gap: 2px;

  .el-button {
    margin: 0;
  }
}
</style>
