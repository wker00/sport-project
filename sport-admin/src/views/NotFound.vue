<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="not-found__card">
      <h1 class="not-found__code">404</h1>
      <p class="not-found__title">抱歉，您访问的页面不存在</p>
      <p class="not-found__desc">请检查链接是否正确，或返回首页</p>
      <el-button type="primary" size="large" @click="router.push('/dashboard')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.not-found {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;

  &__card {
    text-align: center;
    padding: 60px 48px;
    background: #fff;
    border-radius: 20px;
    border: 1px solid #f1f5f9;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  }

  &__code {
    font-size: 96px;
    font-weight: 800;
    color: #ff6b35;
    line-height: 1;
    margin: 0 0 16px;
    letter-spacing: -4px;
  }

  &__title {
    font-size: 20px;
    font-weight: 600;
    color: #0f172a;
    margin: 0 0 8px;
  }

  &__desc {
    font-size: 15px;
    color: #94a3b8;
    margin: 0 0 32px;
  }
}
</style>
