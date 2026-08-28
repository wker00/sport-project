<script setup>
import { ref, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { createProduct, getProductDetail, updateProduct, uploadProductImage, getCategoryList } from '@/api/manager'

const props = defineProps({
  visible: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  productId: { type: [Number, String, null], default: null },
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const fileInputRef = ref(null)
const uploading = ref(false)

const defaultForm = () => ({
  name: '',
  categoryId: null,
  price: 0.01,
  originalPrice: 0.01,
  stock: 0,
  badge: '',
  isOn: true,
  image: '',
  subtitle: '',
  detail: '',
})

const form = ref(defaultForm())

const categories = ref([])

// ── 规格编辑数据 ──
const specGroups = ref([])
const specItems = ref([])

// 批量设置用
const batchPrice = ref(0)
const batchOriginalPrice = ref(0)
const batchStock = ref(0)

// 新规格组名称输入
const newGroupName = ref('')

// 各规格组的新选项输入（用 key-value 存储）
const newOptionTexts = ref({})

// 计算属性：是否有规格
const hasSpecs = computed(() => specGroups.value.length > 0 && specItems.value.length > 0)

// ── 规格工具函数 ──

// 计算笛卡尔积
function cartesianProduct(groups) {
  if (!groups || groups.length === 0) return []
  return groups
    .filter(g => g.name && g.options.length > 0)
    .reduce(
      (acc, group) => {
        const result = []
        acc.forEach(item => {
          group.options.forEach(opt => {
            result.push([...item, opt])
          })
        })
        return result
      },
      [[]]
    )
    .map(combo => combo.join(' / '))
}

// 解析 specs JSON 字符串为 { specGroups, specItems }
function parseSpecs(specsStr) {
  if (!specsStr) return { specGroups: [], specItems: [] }
  try {
    const obj = typeof specsStr === 'string' ? JSON.parse(specsStr) : specsStr
    // 兼容旧格式（数组）
    if (Array.isArray(obj)) {
      return {
        specGroups: obj.map(g => ({ name: g.name, options: g.options || [] })),
        specItems: [],
      }
    }
    return {
      specGroups: obj.specGroups || [],
      specItems: obj.specItems || [],
    }
  } catch {
    return { specGroups: [], specItems: [] }
  }
}

// 添加规格组
const addSpecGroup = () => {
  const name = newGroupName.value.trim()
  if (!name) {
    ElMessage.warning('请输入规格组名称')
    return
  }
  if (specGroups.value.some(g => g.name === name)) {
    ElMessage.warning('规格组名称不能重复')
    return
  }
  specGroups.value.push({ name, options: [] })
  newGroupName.value = ''
}

// 删除规格组
const removeSpecGroup = (index) => {
  specGroups.value.splice(index, 1)
}

// 添加选项到规格组
const addOption = (groupIndex) => {
  const key = `group_${groupIndex}`
  const text = (newOptionTexts.value[key] || '').trim()
  if (!text) return
  if (specGroups.value[groupIndex].options.includes(text)) {
    ElMessage.warning('选项不能重复')
    return
  }
  specGroups.value[groupIndex].options.push(text)
  newOptionTexts.value[key] = ''
}

// 删除选项
const removeOption = (groupIndex, optionIndex) => {
  specGroups.value[groupIndex].options.splice(optionIndex, 1)
}

// 批量设置规格价格和库存
const applyBatchSpecs = () => {
  specItems.value.forEach(item => {
    item.price = batchPrice.value
    item.originalPrice = batchOriginalPrice.value
    item.stock = batchStock.value
  })
}

// specGroups 变化时自动重新生成 specItems（保留已有价格/库存）
watch(
  specGroups,
  () => {
    const keys = cartesianProduct(specGroups.value)
    const oldMap = {}
    specItems.value.forEach(item => {
      oldMap[item.specs] = item
    })
    specItems.value = keys.map(key => ({
      specs: key,
      price: oldMap[key]?.price ?? batchPrice.value,
      originalPrice: oldMap[key]?.originalPrice ?? batchOriginalPrice.value,
      stock: oldMap[key]?.stock ?? batchStock.value,
    }))
  },
  { deep: true }
)

// specItems 变化时同步价格和库存到基本信息区域（供禁用字段正确显示）
watch(
  specItems,
  (items) => {
    if (items.length > 0) {
      form.value.price = Math.min(...items.map(i => i.price ?? Infinity))
      form.value.originalPrice = Math.min(...items.map(i => i.originalPrice ?? Infinity))
      form.value.stock = items.reduce((sum, i) => sum + (i.stock || 0), 0)
    }
  },
  { deep: true }
)

// ── 规格组输入框回车添加选项 ──
const handleOptionKeydown = (groupIndex, event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    addOption(groupIndex)
  }
}

// ── 表单操作 ──

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑商品' : '新增商品'))

const categoriesMap = computed(() => {
  const m = {}
  categories.value.forEach(c => { m[c.id] = c.name })
  return m
})

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data?.data || []
  } catch {
    categories.value = []
  }
}

const loadProduct = async (id) => {
  if (!id) return
  try {
    const res = await getProductDetail(id)
    const data = res.data?.data
    if (!data) {
      ElMessage.error('商品不存在或已被删除')
      return
    }
    form.value = {
      name: data.name || '',
      categoryId: data.categoryId ?? null,
      price: data.price ?? 0.01,
      originalPrice: data.originalPrice ?? 0.01,
      stock: data.stock ?? 0,
      salesCount: data.salesCount ?? 0,
      badge: data.badge || '',
      isOn: data.isOn === 1,
      image: data.image || '',
      subtitle: data.subtitle || '',
      detail: data.detail || '',
    }
    // 解析规格
    const parsed = parseSpecs(data.specs)
    specGroups.value = parsed.specGroups
    specItems.value = parsed.specItems
  } catch {
    ElMessage.error('加载商品信息失败')
  }
}

const handleImageClick = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  uploading.value = true
  try {
    const res = await uploadProductImage(file)
    const url = res.data?.data
    if (!url) {
      ElMessage.error('上传失败，未获取到图片地址')
      return
    }
    form.value.image = url
    if (props.mode === 'edit' && props.productId) {
      await updateProduct(props.productId, { image: url })
    }
    ElMessage.success('图片上传成功')
  } catch {
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploading.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

const handleRemoveImage = () => {
  form.value.image = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const resetForm = () => {
  form.value = defaultForm()
  specGroups.value = []
  specItems.value = []
  batchPrice.value = 0
  batchOriginalPrice.value = 0
  batchStock.value = 0
  newGroupName.value = ''
  newOptionTexts.value = {}
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  try {
    const productData = { ...form.value }

    // 组装 specs JSON
    if (specGroups.value.length > 0 && specItems.value.length > 0) {
      productData.specs = JSON.stringify({
        specGroups: specGroups.value,
        specItems: specItems.value,
      })
    } else {
      productData.specs = ''
    }

    if (productData.badge == null) {
      productData.badge = ''
    }

    if (props.mode === 'edit' && props.productId) {
      await updateProduct(props.productId, productData)
      ElMessage.success('更新成功')
    } else {
      await createProduct(productData)
      ElMessage.success('新增成功')
    }
    emit('update:visible', false)
    emit('success')
  } catch (err) {
    // 业务错误已在 axios 拦截器中提示，此处不再重复
    if (!err.isBusinessError) {
      ElMessage.error(err?.response?.data?.message || '操作失败，请重试')
    }
  }
}

watch(
  () => props.visible,
  async (val) => {
    if (val) {
      resetForm()
      await nextTick()
      formRef.value?.clearValidate()
      await fetchCategories()
      if (props.mode === 'edit' && props.productId) {
        await loadProduct(props.productId)
      }
    } else {
      resetForm()
    }
  }
)
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="$emit('update:visible', $event)"
    :title="dialogTitle"
    width="740px"
    top="5vh"
    destroy-on-close
    class="product-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="{
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
        price: [
          { required: true, message: '请输入商品价格', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value === null || value === undefined) {
              callback(new Error('请输入商品价格'))
            } else if (value <= 0) {
              callback(new Error('商品价格必须大于0'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ],
        stock: [
          { required: true, message: '请输入商品库存', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value === null || value === undefined) {
              callback(new Error('请输入商品库存'))
            } else if (value < 0) {
              callback(new Error('商品库存不能为负数'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ],
      }"
      label-width="90px"
      label-position="right"
    >
      <div class="form-section">
        <div class="form-section__title">基本信息</div>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" type="textarea" :rows="2" placeholder="商品副标题（选填）" maxlength="200" show-word-limit />
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="form-section__title">商品规格</div>
        <p class="form-section__desc">定义规格组（如颜色、尺码），系统自动生成所有规格组合并设定价格和库存</p>

        <div v-if="specGroups.length === 0" class="spec-empty">
          <span>暂未设置规格，商品将以统一价格销售</span>
        </div>

        <div v-for="(group, gi) in specGroups" :key="gi" class="spec-group">
          <div class="spec-group__header">
            <el-input
              v-model="group.name"
              size="small"
              class="spec-group__name"
              placeholder="规格组名称"
            />
            <el-button type="danger" link size="small" @click="removeSpecGroup(gi)">删除规格组</el-button>
          </div>
          <div class="spec-group__options">
            <el-tag
              v-for="(opt, oi) in group.options"
              :key="oi"
              closable
              class="spec-option-tag"
              @close="removeOption(gi, oi)"
            >{{ opt }}</el-tag>
            <el-input
              v-model="newOptionTexts[`group_${gi}`]"
              size="small"
              class="spec-option-input"
              :placeholder="`添加${group.name}规格名称`"
              @keydown="handleOptionKeydown(gi, $event)"
            >
              <template #append>
                <el-button @click="addOption(gi)">+</el-button>
              </template>
            </el-input>
          </div>
        </div>

        <div class="spec-add-group">
          <el-input
            v-model="newGroupName"
            size="small"
            class="spec-add-group__input"
            placeholder="输入规格组名称，如：颜色"
            @keydown.enter.prevent="addSpecGroup"
          />
          <el-button size="small" @click="addSpecGroup">+ 添加规格组</el-button>
        </div>

        <template v-if="specItems.length > 0">
          <div class="spec-batch">
            <span class="spec-batch__label">批量设置：</span>
            <el-input-number v-model="batchPrice" :min="0" :precision="2" size="small" controls-position="right" class="spec-batch__input" placeholder="价格" />
            <el-input-number v-model="batchOriginalPrice" :min="0" :precision="2" size="small" controls-position="right" class="spec-batch__input" placeholder="原价" />
            <el-input-number v-model="batchStock" :min="0" size="small" controls-position="right" class="spec-batch__input" placeholder="库存" />
            <el-button type="primary" size="small" @click="applyBatchSpecs">应用到全部</el-button>
          </div>

          <el-table :data="specItems" border size="small" class="spec-table" max-height="320">
            <el-table-column prop="specs" label="规格名称" min-width="180" />
            <el-table-column label="价格" width="140" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.price"
                  :min="0"
                  :precision="2"
                  size="small"
                  controls-position="right"
                  class="spec-table__number"
                />
              </template>
            </el-table-column>
            <el-table-column label="原价" width="140" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.originalPrice"
                  :min="0"
                  :precision="2"
                  size="small"
                  controls-position="right"
                  class="spec-table__number"
                />
              </template>
            </el-table-column>
            <el-table-column label="库存" width="120" align="center">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.stock"
                  :min="0"
                  size="small"
                  controls-position="right"
                  class="spec-table__number"
                />
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>

      <div class="form-section">
        <div class="form-section__title">价格与库存</div>
        <div v-if="hasSpecs" class="form-section__tip">
          <el-alert title="当商品设置了规格时，价格和库存将由规格自动计算，此处设置将被忽略" type="info" show-icon :closable="false" />
        </div>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" controls-position="right" style="width: 200px" :disabled="hasSpecs" />
        </el-form-item>
        <el-form-item label="划线价格">
          <el-input-number v-model="form.originalPrice" :min="0.01" :precision="2" controls-position="right" style="width: 200px" :disabled="hasSpecs" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" controls-position="right" style="width: 200px" :disabled="hasSpecs" />
        </el-form-item>
        <el-form-item v-if="mode === 'edit'" label="销量">
          <el-input-number v-model="form.salesCount" :min="0" controls-position="right" style="width: 200px" :disabled="hasSpecs" />
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="form-section__title">营销信息</div>
        <el-form-item label="商品标签">
          <el-select v-model="form.badge" clearable placeholder="选择标签（选填）" style="width: 200px">
            <el-option label="热卖" value="热卖" />
            <el-option label="新品" value="新品" />
            <el-option label="推荐" value="推荐" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品状态">
          <el-switch v-model="form.isOn" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="form-section__title">商品图片</div>
        <div class="image-uploader">
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none;"
            @change="handleFileChange"
          />
          <div v-if="form.image" class="image-uploader__preview">
            <img :src="form.image" alt="商品图片" />
            <div class="image-uploader__mask">
              <el-button type="primary" link @click.stop="handleImageClick">更换</el-button>
              <el-button type="danger" link @click.stop="handleRemoveImage">删除</el-button>
            </div>
          </div>
          <div v-else class="image-uploader__placeholder" @click="handleImageClick">
            <el-icon :size="28" color="#c0c4cc"><Picture /></el-icon>
            <span v-if="uploading">上传中...</span>
            <span v-else>点击上传图片</span>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="form-section__title">商品详情</div>
        <el-input
          v-model="form.detail"
          type="textarea"
          :rows="6"
          placeholder="请输入商品详情（选填）"
        />
      </div>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submitForm">
        {{ mode === 'edit' ? '保存修改' : '确认新增' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.product-form-dialog {
  :deep(.el-dialog) {
    border-radius: 14px;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    padding: 20px 24px 16px;
    margin: 0;
    border-bottom: 1px solid #f1f5f9;
  }

  :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
  }

  :deep(.el-dialog__body) {
    padding: 20px 24px;
    max-height: 72vh;
    overflow-y: auto;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px 20px;
    border-top: 1px solid #f1f5f9;
  }
}

.form-section {
  margin-bottom: 24px;

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 14px;
  }

  &__desc {
    font-size: 13px;
    color: #94a3b8;
    margin: -8px 0 12px;
  }

  &__tip {
    margin-bottom: 12px;
  }
}

.image-uploader {
  width: 200px;
  height: 200px;

  &__preview {
    width: 100%;
    height: 100%;
    border-radius: 10px;
    overflow: hidden;
    position: relative;
    border: 1px solid #e2e8f0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }
  }

  &__mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    opacity: 0;
    transition: opacity 0.2s;

    .el-button {
      color: #fff;
    }

    :deep(.el-button--primary) {
      color: #409eff;
    }

    :deep(.el-button--danger) {
      color: #f56c6c;
    }
  }

  &:hover &__mask {
    opacity: 1;
  }

  &__placeholder {
    width: 100%;
    height: 100%;
    border: 2px dashed #e2e8f0;
    border-radius: 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: #f8fafc;

    &:hover {
      border-color: #409eff;
      background: #ecf5ff;
    }

    span {
      font-size: 13px;
      color: #94a3b8;
    }
  }
}

/* ── 规格编辑器样式 ── */

.spec-empty {
  padding: 24px;
  text-align: center;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #e2e8f0;
  margin-bottom: 16px;

  span {
    font-size: 13px;
    color: #94a3b8;
  }
}

.spec-group {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 10px;
  border: 1px solid #f1f5f9;

  &__header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }

  &__name {
    width: 140px;
  }

  &__options {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    align-items: center;
  }
}

.spec-option-tag {
  margin: 0;
}

.spec-option-input {
  width: 160px;
}

.spec-add-group {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  margin-bottom: 16px;

  &__input {
    width: 200px;
  }
}

.spec-batch {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #bae6fd;

  &__label {
    font-size: 13px;
    color: #1e293b;
    font-weight: 500;
    white-space: nowrap;
  }

  &__input {
    width: 110px;
  }
}

.spec-table {
  width: 100%;

  &__number {
    width: 100px;
  }
}
</style>
