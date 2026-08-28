<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { getOperateLogList, getOperateLogDetail, deleteOperateLog, cleanOperateLog } from '@/api/manager'
import { toast } from '@/utils/toast'

const loading = ref(false)
const orders = ref([])
const page = ref(1)
const pageSize = ref(10)
const serverTotal = ref(0)

const searchModule = ref('')
const searchDateRange = ref(null)

const detailVisible = ref(false)
const currentLog = ref(null)

const cleanDialogVisible = ref(false)
const cleanDate = ref('')

const moduleOptions = [
  { value: 'admin', label: '管理员' },
  { value: 'category', label: '分类管理' },
  { value: 'product', label: '商品管理' },
  { value: 'order', label: '订单管理' },
  { value: 'coupon', label: '优惠券管理' },
  { value: 'points_gift', label: '积分商品' },
  { value: 'user', label: '用户管理' },
  { value: 'review', label: '评价管理' },
]

const moduleMap = Object.fromEntries(moduleOptions.map(m => [m.value, m.label]))


const fetchLogs = async (resetPage = false) => {
  if (resetPage) page.value = 1
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize.value }
    if (searchModule.value) params.module = searchModule.value
    if (searchDateRange.value) {
      params.startDate = searchDateRange.value[0]
      params.endDate = searchDateRange.value[1]
    }
    const res = await getOperateLogList(params)
    const data = res.data.data || {}
    orders.value = data.records || []
    serverTotal.value = data.total || 0
  } catch {
    orders.value = []
    serverTotal.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => fetchLogs(true)

const handleReset = () => {
  searchModule.value = ''
  searchDateRange.value = null
  fetchLogs(true)
}

const handlePageChange = (p) => {
  page.value = p
  fetchLogs()
}

const showDetail = async (row) => {
  try {
    const res = await getOperateLogDetail(row.id)
    currentLog.value = res.data.data
    detailVisible.value = true
  } catch {
    toast('获取日志详情失败', 'error')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除该操作日志？`, '确认删除', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteOperateLog(row.id)
    toast('删除成功')
    fetchLogs()
  } catch { }
}

const openClean = () => {
  cleanDate.value = ''
  cleanDialogVisible.value = true
}

const confirmClean = async () => {
  if (!cleanDate.value) {
    toast('请选择日期', 'warning')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定清理 ${cleanDate.value} 之前的所有操作日志？此操作不可恢复。`,
      '确认清理',
      { confirmButtonText: '确认清理', cancelButtonText: '取消', type: 'warning' }
    )
    await cleanOperateLog(cleanDate.value)
    toast('清理成功')
    cleanDialogVisible.value = false
    fetchLogs(true)
  } catch { }
}

const formatParams = (params) => {
  if (!params) return '-'
  try {
    return JSON.stringify(JSON.parse(params), null, 2)
  } catch {
    return params
  }
}

onMounted(() => fetchLogs())
</script>

<template>
  <div class="page">
    <h1 class="page__title">操作日志</h1>
    <p class="page__desc">查看管理端所有操作记录</p>

    <div class="card card--search">
      <el-form :inline="true">
        <el-form-item label="模块">
          <el-select v-model="searchModule" placeholder="全部模块" clearable style="width: 150px;">
            <el-option v-for="m in moduleOptions" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="searchDateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD"
            style="width: 260px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="margin-left: auto;">
          <el-button type="danger" plain @click="openClean">清理日志</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card">
      <el-table :data="orders" v-loading="loading" stripe empty-text="暂无操作日志">
        <el-table-column type="index" :index="(i) => (page - 1) * pageSize + i + 1" label="#" width="55" align="center" />
        <el-table-column prop="username" label="管理员" width="110" align="center" />
        <el-table-column label="模块" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ moduleMap[row.module] || row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="80" align="center" />
        <el-table-column prop="description" label="描述" min-width="180" max-width="300" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="130" align="center" />
        <el-table-column label="耗时" width="70" align="center">
          <template #default="{ row }">
            <span>{{ row.costTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button type="text" size="small" @click="showDetail(row)">详情</el-button>
              <span class="action-divider" />
              <el-button type="text" size="small" style="color: #ef4444;" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="serverTotal > pageSize" class="pagination-wrap">
        <el-pagination :current-page="page" :page-size="pageSize" :total="serverTotal"
          layout="prev, pager, next, total" background @current-change="handlePageChange" />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="640px" :close-on-click-modal="false">
      <template v-if="currentLog">
        <el-descriptions :column="2" border class="detail-body">
          <el-descriptions-item label="ID" :span="1">{{ currentLog.id }}</el-descriptions-item>
          <el-descriptions-item label="管理员" :span="1">{{ currentLog.username }}</el-descriptions-item>
          <el-descriptions-item label="模块" :span="1">
            <el-tag size="small">{{ moduleMap[currentLog.module] || currentLog.module }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型" :span="1">{{ currentLog.type }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentLog.description }}</el-descriptions-item>
          <el-descriptions-item label="请求方法" :span="1">{{ currentLog.method }}</el-descriptions-item>
          <el-descriptions-item label="请求 URL" :span="1">{{ currentLog.url }}</el-descriptions-item>
          <el-descriptions-item label="IP" :span="1">{{ currentLog.ip }}</el-descriptions-item>
          <el-descriptions-item label="耗时" :span="1">{{ currentLog.costTime }}ms</el-descriptions-item>
          <el-descriptions-item label="结果" :span="1">
            <el-tag :type="currentLog.result === 'success' ? 'success' : 'danger'" size="small">
              {{ currentLog.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentLog.errorMsg" label="错误信息" :span="2">
            <span style="color: #ef4444;">{{ currentLog.errorMsg }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="请求参数" :span="2">
            <pre class="params-json">{{ formatParams(currentLog.params) }}</pre>
          </el-descriptions-item>
          <el-descriptions-item label="时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>

    <el-dialog v-model="cleanDialogVisible" title="清理操作日志" width="420px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="清理日期" required>
          <el-date-picker v-model="cleanDate" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <p class="clean-tip">将删除该日期之前的所有操作日志，建议定期清理 90 天前的日志。</p>
      <template #footer>
        <el-button @click="cleanDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmClean">确认清理</el-button>
      </template>
    </el-dialog>
  </div>
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

  &--search {
    margin-bottom: 16px;
    padding: 16px 24px;

    :deep(.el-form) {
      margin-bottom: 0;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
    }

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-table .el-table__body .el-table__row) {
  height: 52px;
}

.params-json {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 12px;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.detail-body {
  :deep(.el-descriptions__label) {
    color: #64748b;
    font-weight: 500;
  }
}

.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;

  .el-button {
    margin: 0;
  }
}

.action-divider {
  display: inline-block;
  width: 1px;
  height: 14px;
  background: #e2e8f0;
  margin: 0 6px;
}

.clean-tip {
  font-size: 13px;
  color: $text-muted;
  margin: 8px 0 0 100px;
}
</style>
