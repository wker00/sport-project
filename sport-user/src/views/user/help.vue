<script setup>
</script>

<template>
  <div class="content-card">
    <div class="content-header">
      <h3>帮助中心</h3>
    </div>
    <div class="content-body">
      <div class="help-list">
        <div class="help-item">
          <h4><i class="ph ph-shopping-bag"></i> 如何下单？</h4>
          <p>浏览商品 → 选择规格 → 加入购物车 → 在购物车中结算 → 填写收货信息 → 提交订单并支付。</p>
        </div>
        <div class="help-item">
          <h4><i class="ph ph-truck"></i> 配送时效</h4>
          <p>下单后 1-3 个工作日内发货，具体送达时间以物流信息为准。</p>
        </div>
        <div class="help-item">
          <h4><i class="ph ph-arrows-counter-clockwise"></i> 退换货政策</h4>
          <p>收到商品 7 天内可申请退货退款，商品需保持完好不影响二次销售。</p>
        </div>
        <div class="help-item">
          <h4><i class="ph ph-coin"></i> 积分规则</h4>
          <p>每消费 ¥1 可获得 10 积分，积分可用于兑换积分商城中的商品。</p>
        </div>
        <div class="help-item">
          <h4><i class="ph ph-headset"></i> 联系客服</h4>
          <p>如遇问题请联系在线客服，工作时间：周一至周五 9:00-18:00。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.content-card {
  background: $bg-card;
  border: 1px solid $border-subtle;
  border-radius: $radius-xl;
  margin-bottom: 24px;

  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid $border-subtle;

    h3 {
      font-size: 18px;
      font-weight: 700;
      color: $text-primary;
    }
  }

  .content-body {
    padding: 20px 24px;
  }
}

.help-list {
  .help-item {
    padding: 20px 0;
    border-bottom: 1px solid $border-subtle;

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    &:first-child {
      padding-top: 0;
    }

    h4 {
      font-size: 15px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 8px;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: $accent-energy;
        font-size: 20px;
      }
    }

    p {
      font-size: 14px;
      color: $text-secondary;
      line-height: 1.8;
      padding-left: 28px;
    }
  }
}
</style>
