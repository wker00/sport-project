<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="content-shell">
      <div class="content-core">
        <div class="code-wrap">
          <span class="code">404</span>
        </div>
        <p class="desc">抱歉，您访问的页面不存在</p>
        <p class="sub">请检查输入的地址是否正确，或点击下方按钮返回首页</p>
        <el-button type="primary" class="back-btn" @click="router.push('/')">
          返回首页
          <span class="btn-arrow"><i class="ph ph-arrow-right"></i></span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/variables" as *;

.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 80px 20px;
}

.content-shell {
  @include solid-card;
  border-radius: 32px;
  padding: 1.5px;
}

.content-core {
  border-radius: calc(32px - 0.375rem);
  padding: 64px 48px;
  background: transparent;
  text-align: center;
  max-width: 480px;
}

.code-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 24px;
}

.code {
  font-size: clamp(100px, 20vw, 160px);
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(135deg, $accent-energy 0%, #ff8c5a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  user-select: none;
  position: relative;
  z-index: 2;
}

.desc {
  font-size: 22px;
  font-weight: 600;
  color: $text-primary;
  margin: 0 0 12px;
}

.sub {
  font-size: 15px;
  color: $text-secondary;
  margin: 0 0 40px;
}

.back-btn {
  width: 100px;
  height: 40px;
  padding: 24px 60px;
  background: $accent-energy;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border: none;
  border-radius: 25px;
  transition: all 0.5s $transition-premium;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.2);

  .btn-arrow {
    @include btn-inner-icon;
    width: 28px; height: 28px; font-size: 13px;
    background: rgba(255, 255, 255, 0.15);
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(255, 107, 53, 0.35);

    .btn-arrow { transform: translateX(2px) scale(1.05); }
  }
}
</style>
