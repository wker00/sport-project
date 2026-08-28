<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPointsBalance, getPointsRecords } from '@/api/manager'

const pointsBalance = ref(0)
const pointsRecords = ref([])
const recordsPage = ref(1)
const recordsPageSize = ref(10)

const pointsTypeMap = { 1: '收入', 2: '支出' }
const sourceMap = {
  order: '订单奖励', signin: '签到', exchange: '兑换', refund: '退款返还', review: '评价奖励'
}

const pagedRecords = computed(() => {
  const start = (recordsPage.value - 1) * recordsPageSize.value
  return pointsRecords.value.slice(start, start + recordsPageSize.value)
})

const recordsTotalPages = computed(() => Math.ceil(pointsRecords.value.length / recordsPageSize.value) || 1)

function formatDateTime(d) {
  if (!d) return ''
  const t = new Date(d)
  const pad = (n) => String(n).padStart(2, '0')
  return `${t.getFullYear()}-${pad(t.getMonth() + 1)}-${pad(t.getDate())} ${pad(t.getHours())}:${pad(t.getMinutes())}`
}

onMounted(async () => {
  try {
    const [recRes, balRes] = await Promise.all([
      getPointsRecords(),
      getPointsBalance()
    ])
    if (recRes.data.code === 200) {
      pointsRecords.value = recRes.data.data || []
    }
    if (balRes.data.code === 200) {
      pointsBalance.value = balRes.data.data || 0
    }
  } catch (e) { console.error(e); pointsRecords.value = []; pointsBalance.value = 0 }
})
</script>

<template>
  <div>
    <div class="points-hero-card">
      <div class="points-icon-shell">
        <i class="ph-fill ph-coin"></i>
      </div>
      <div class="points-info">
        <span class="points-label">可用积分</span>
        <span class="points-value">{{ pointsBalance.toLocaleString() }}</span>
      </div>
      <router-link to="/pointsMall" class="btn-trail">
        积分商城
        <span class="btn-inner-icon"><i class="ph ph-arrow-right"></i></span>
      </router-link>
    </div>

    <div class="glass-card">
      <div class="glass-card-header">
        <h3><i class="ph ph-clock-countdown"></i> 积分记录</h3>
      </div>
      <div class="glass-card-body">
        <template v-if="pointsRecords.length === 0">
          <div class="empty-state">
            <i class="ph ph-clock-countdown"></i>
            <p>暂无积分记录</p>
          </div>
        </template>
        <table v-else class="records-table">
          <thead>
            <tr>
              <th>类型</th>
              <th>渠道</th>
              <th>积分变动</th>
              <th>说明</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rec in pagedRecords" :key="rec.id || rec.createTime" class="record-row">
              <td>
                <span :class="['type-tag', rec.type === 1 ? 'earn' : 'spend']">{{ pointsTypeMap[rec.type] || '未知' }}</span>
              </td>
              <td><span class="source-tag">{{ sourceMap[rec.source] || rec.source || '-' }}</span></td>
              <td :class="rec.type === 1 ? 'points-plus' : 'points-minus'">
                {{ rec.type === 1 ? '+' : '-' }}{{ rec.points }}
              </td>
              <td class="rec-desc">{{ rec.description || '-' }}</td>
              <td class="rec-time">{{ formatDateTime(rec.createTime) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="recordsTotalPages > 1" class="pagination-bar">
          <button class="paginate-btn" :disabled="recordsPage <= 1" @click="recordsPage--">
            <i class="ph ph-caret-left"></i>
          </button>
          <span class="paginate-info">{{ recordsPage }} / {{ recordsTotalPages }}</span>
          <button class="paginate-btn" :disabled="recordsPage >= recordsTotalPages" @click="recordsPage++">
            <i class="ph ph-caret-right"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

table{
  cursor: context-menu;
}

.points-hero-card {
  @include solid-card;
  border-radius: $radius-xl;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;

  .points-icon-shell {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: rgba($accent-energy, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: $accent-energy;
    flex-shrink: 0;
    border: 0.5px solid $border-accent;
  }

  .points-info {
    flex: 1;

    .points-label {
      display: block;
      font-size: 13px;
      color: $text-muted;
      margin-bottom: 4px;
    }

    .points-value {
      font-size: 36px;
      font-weight: 800;
      color: $text-primary;
      letter-spacing: -0.02em;
    }
  }

  .btn-trail {
    @include btn-base;
    transition-property: transform, box-shadow, color, border-color, opacity;
    padding: 8px 16px;
    background: $accent-energy;
    border: none;
    border-radius: 12px;
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;

    &:hover {
      box-shadow: 0 8px 32px $accent-energy-glow;
      @include btn-hover-gradient-primary;
      transform: translateY(-2px);
    }

    .btn-inner-icon {
      @include btn-inner-icon;

      i {
        font-size: 14px;
        color: #fff;
      }
    }
  }
}

.glass-card {
  @include solid-card;
  border-radius: $radius-xl;
  margin-bottom: 24px;
  overflow: hidden;

  .glass-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 0.5px solid $border-subtle;

    h3 {
      font-size: 18px;
      font-weight: 700;
      color: $text-primary;
      display: flex;
      align-items: center;
      gap: 8px;

      i { color: $accent-energy; }
    }
  }

  .glass-card-body {
    padding: 20px 24px;
  }
}

.records-table {
  width: 100%;
  border-collapse: collapse;

  th {
    padding: 14px 16px;
    text-align: left;
    font-size: 12px;
    font-weight: 600;
    color: $text-muted;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    border-bottom: 0.5px solid $border-subtle;
  }

  .record-row td {
    padding: 14px 16px;
    font-size: 14px;
    color: $text-secondary;
    border-bottom: 0.5px solid $border-subtle;
    transition: background 0.3s;
  }

  .record-row:hover td {
    background: $bg-subtle;
  }

  .type-tag {
    padding: 3px 10px;
    border-radius: 999px;
    font-size: 12px;
    font-weight: 600;

    &.earn {
      background: rgba($accent-vitality, 0.1);
      color: $accent-vitality;
    }

    &.spend {
      background: $accent-energy-soft;
      color: $accent-energy;
    }
  }

  .source-tag {
    font-size: 12px;
    color: $text-secondary;
  }

  .points-plus {
    color: $accent-vitality;
    font-weight: 700;
  }

  .points-minus {
    color: $accent-energy;
    font-weight: 700;
  }

  .rec-desc {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .rec-time {
    font-size: 13px;
    color: $text-muted;
    white-space: nowrap;
  }
}

.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 0.5px solid $border-subtle;

  .paginate-btn {
    @include btn-base;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: $bg-card;
    border: 0.5px solid $border-subtle;
    color: $text-secondary;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;

    &:hover:not(:disabled) {
      background: $bg-elevated;
      color: $text-primary;
    }
  }

  .paginate-info {
    font-size: 13px;
    color: $text-secondary;
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: $text-muted;

  i {
    display: block;
    margin: 0 auto 16px;
    font-size: 48px;
  }

  p {
    font-size: 14px;
  }
}
</style>
