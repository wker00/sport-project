<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getHotProducts, getCategoryList } from '@/api/manager'
import { useCounterStore } from "@/stores/counter"
import { useRouter } from 'vue-router'
import { toast } from '@/utils/toast'

const router = useRouter()
const store = useCounterStore()
const products = ref([])
const categories = ref([])

function setupReveals() {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible')
          observer.unobserve(entry.target)
        }
      })
    },
    { root: null, rootMargin: '0px', threshold: 0.08 }
  )
  nextTick(() => {
    document.querySelectorAll('.reveal').forEach((el) => observer.observe(el))
  })
}

const toUserPoints = () => {
  const token = store.token
  if (token) {
    router.push({ name: 'points' })
  } else {
    toast('请先登录后查看', 'error')
  }
}

onMounted(async () => {
  try {
    const [catRes, prodRes] = await Promise.all([
      getCategoryList({ limit: 4 }),
      getHotProducts(),
    ])
    if (catRes.data.code === 200) categories.value = catRes.data.data
    if (prodRes.data.code === 200) products.value = prodRes.data.data
  } catch (e) { console.error(e) }
  setupReveals()
})
</script>

<template>
  <div class="home">
    <section class="hero-section">
      <div class="hero-bento reveal">
        <div class="hero-content">
          <span class="eyebrow-tag">
            <span class="eyebrow-dot"></span>
            2026 春夏新品上市
          </span>
          <h1 class="hero-title">
            释放运动热爱<br />
            <span class="gradient-text">打造专属主场</span>
          </h1>
          <p class="hero-desc">
            专注篮球、羽毛球、乒乓球、排球等专业运动装备，精选高品质体育用品，打造专业化的运动购物体验平台。
          </p>
          <div class="hero-actions">
            <router-link to="/category" class="btn-primary">
              立即选购
              <span class="btn-icon-wrap">
                <i class="ph ph-arrow-up-right"></i>
              </span>
            </router-link>
          </div>
        </div>

        <div class="hero-visual">
          <div class="floating-grid">
            <div class="float-item item-1">
              <img src="https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/index/%E5%85%B5%E4%B9%93%E7%90%83.png"
                alt="乒乓球" loading="lazy" />
              <span>乒乓球</span>
            </div>
            <div class="float-item item-2">
              <img src="https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/index/%E7%BE%BD%E6%AF%9B%E7%90%83.png"
                alt="羽毛球" loading="lazy" />
              <span>羽毛球</span>
            </div>
            <div class="float-item item-3">
              <img src="https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/index/%E7%AF%AE%E7%90%83.png" alt="篮球"
                loading="lazy" />
              <span>篮球</span>
            </div>
            <div class="float-item item-4">
              <img src="https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/index/%E8%B6%B3%E7%90%83.png" alt="足球"
                loading="lazy" />
              <span>足球</span>
            </div>
            <div class="float-item item-5">
              <img src="https://sportzone-oss.oss-cn-shenzhen.aliyuncs.com/index/%E6%8E%92%E7%90%83.png" alt="排球"
                loading="lazy" />
              <span>排球</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-header reveal">
        <div>
          <span class="section-eyebrow">CATEGORIES</span>
          <h2 class="section-title">商品分类</h2>
        </div>
        <router-link to="/category" class="section-link">
          查看全部
          <i class="ph ph-arrow-right"></i>
        </router-link>
      </div>
      <div class="bento-grid">
        <router-link v-for="(cat, idx) in categories" :key="cat.id" :to="`/category/${cat.id}`"
          class="bento-card-shell reveal" :class="'reveal-delay-' + (idx + 1)">
          <div class="bento-card-core">
            <div class="bento-img-wrap">
              <img :src="cat.image" :alt="cat.name" loading="lazy" />
              <div class="bento-img-overlay"></div>
            </div>
            <div class="bento-card-content">
              <span class="bento-icon">
                <i class="ph-fill ph-basketball"></i>
              </span>
              <h3>{{ cat.name }}</h3>
              <p>{{ cat.description || '探索 ' + cat.name + ' 装备' }}</p>
              <span class="bento-arrow">
                <i class="ph ph-arrow-up-right"></i>
              </span>
            </div>
          </div>
        </router-link>
      </div>
    </section>

    <section class="section">
      <div class="section-header reveal">
        <div>
          <span class="section-eyebrow">HOT PICKS</span>
          <h2 class="section-title">热门商品推荐</h2>
        </div>
        <router-link to="/category" class="section-link">
          更多商品
          <i class="ph ph-arrow-right"></i>
        </router-link>
      </div>
      <div class="product-scroll">
        <div v-for="(item, index) in products" :key="item.id" class="product-card-shell reveal"
          :class="'reveal-delay-' + (index + 1)">
          <div class="product-card-core">
            <div class="product-img">
              <span v-if="item.badge" class="product-badge">{{ item.badge }}</span>
              <img :src="item.image" :alt="item.name" loading="lazy" />
            </div>
            <div class="product-body">
              <h3>{{ item.name }}</h3>
              <p class="product-meta">{{ item.subtitle || '已售 ' + (item.salesCount || 0) + ' 件' }}</p>
              <div class="product-footer">
                <div class="product-price">
                  <span class="price-current">¥{{ item.price }}</span>
                  <span v-if="item.originalPrice" class="price-original">¥{{ item.originalPrice }}</span>
                </div>
                <router-link :to="`/product/${item.id}`" class="btn-buy">
                  查看
                  <span class="btn-icon-wrap sm">
                    <i class="ph ph-arrow-right"></i>
                  </span>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section section-last">
      <div class="dual-card-grid">
        <div class="dual-card-shell accent reveal reveal-delay-1">
          <div class="dual-card-core accent-core">
            <div class="dual-card-glow"></div>
            <h2>积分商城</h2>
            <p>每次购物、签到均可获得积分，积分可兑换运动装备、优惠券与专属会员权益。</p>
            <button @click="router.push({ name: 'pointsMall' })" class="card-btn">
              立即兑换
              <span class="btn-icon-wrap">
                <i class="ph ph-arrow-right"></i>
              </span>
            </button>
          </div>
        </div>

        <div class="dual-card-shell reveal reveal-delay-2">
          <div class="dual-card-core">
            <h2>会员专属福利</h2>
            <p>会员享受新品优先购、赛事报名优惠、专属折扣与积分加倍特权。</p>
            <button @click="toUserPoints" class="card-btn">
              查看会员
              <span class="btn-icon-wrap">
                <i class="ph ph-arrow-right"></i>
              </span>
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.home {
  padding-top: 100px;
  overflow-x: hidden;
}

// === Reveal ===
.reveal {
  opacity: 0;
  transform: translateY(40px) scale(0.98);
  filter: blur(4px);
  transition: all 0.8s $transition-premium;

  &.visible {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

.reveal-delay-1 {
  transition-delay: 80ms;
}

.reveal-delay-2 {
  transition-delay: 160ms;
}

.reveal-delay-3 {
  transition-delay: 240ms;
}

.reveal-delay-4 {
  transition-delay: 320ms;
}

.reveal-delay-5 {
  transition-delay: 400ms;
}

// === Section ===
.section {
  padding: 60px 0;
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;
}

.section-last {
  padding-bottom: 120px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 48px;

  .section-eyebrow {
    display: block;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.2em;
    color: $text-muted;
    margin-bottom: 8px;
  }

  .section-title {
    font-size: clamp(28px, 3.5vw, 44px);
    font-weight: 800;
    color: $price-color;
    letter-spacing: -0.02em;
    margin: 0;
  }

  .section-link {
    color: $text-muted;
    font-weight: 600;
    font-size: 14px;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.4s $transition-premium;
    flex-shrink: 0;

    &:hover {
      color: $accent-energy;
      gap: 14px;
    }
  }
}

// === Hero Bent o===
.hero-section {
  padding: 80px 0 100px;
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;
}

.hero-bento {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 32px;
  align-items: center;
}

.hero-content {
  // text content, no card wrapper
}

.eyebrow-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 16px;
  background: rgba($accent-energy, 0.1);
  border: 0.5px solid rgba($accent-energy, 0.2);
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: $accent-energy;
  margin-bottom: 24px;
}

.eyebrow-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: $accent-energy;
  animation: pulse-dot 2s ease-in-out infinite;
}

.hero-title {
  font-size: clamp(36px, 4.5vw, 64px);
  font-weight: 800;
  line-height: 1.05;
  letter-spacing: -0.03em;
  margin-bottom: 20px;
  color: $text-primary;

  .gradient-text {
    background: linear-gradient(135deg, $accent-energy, $accent-gradient-light);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-desc {
  font-size: 16px;
  line-height: 1.8;
  color: $text-muted;
  max-width: 480px;
  margin-bottom: 36px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 16px 32px;
  background: $accent-energy;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  border-radius: 999px;
  transition: all 0.5s $transition-premium;
  box-shadow: 0 4px 20px rgba($accent-energy, 0.25);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 32px rgba($accent-energy, 0.35);
    @include btn-hover-gradient-primary;

    .btn-icon-wrap {
      transform: translateX(2px) translateY(-1px) scale(1.1);
    }
  }
}

.btn-ghost {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  color: $text-secondary;
  font-size: 14px;
  font-weight: 600;
  border-radius: 25px;
  transition: all 0.4s $transition-premium;

  &:hover {
    color: $text-primary;
    background: $bg-card;
  }
}

.btn-icon-wrap {
  @include btn-inner-icon;

  &.sm {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
}

// === Hero Visual ===
.hero-visual {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-grid {
  position: relative;
  width: 100%;
  max-width: 420px;
  aspect-ratio: 1;
}

.float-item {
  position: absolute;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: $bg-card;
  backdrop-filter: blur(16px);
  border: 0.5px solid $border-glass;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  transition: all 0.5s $transition-premium;

  img {
    width: 55%;
    height: 55%;
    object-fit: cover;
    border-radius: 50%;
  }

  span {
    font-size: 11px;
    font-weight: 600;
    color: $text-secondary;
  }

  &:hover {
    transform: translateY(-6px) scale(1.03);
    border-color: rgba($accent-energy, 0.3);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
    z-index: 10;
  }
}

.item-1 {
  width: 150px;
  height: 150px;
  top: 12%;
  left: 5%;
  animation: float-a 6s ease-in-out infinite;
}

.item-2 {
  width: 130px;
  height: 130px;
  top: 8%;
  right: 10%;
  animation: float-b 7s ease-in-out infinite;
}

.item-3 {
  width: 170px;
  height: 170px;
  top: 32%;
  right: -2%;
  animation: float-c 8s ease-in-out infinite;
}

.item-4 {
  width: 120px;
  height: 120px;
  bottom: 10%;
  left: 2%;
  animation: float-d 6.5s ease-in-out infinite;
}

.item-5 {
  width: 140px;
  height: 140px;
  bottom: 8%;
  right: 8%;
  animation: float-e 7.5s ease-in-out infinite;
}

@keyframes float-a {

  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(8px, -12px);
  }
}

@keyframes float-b {

  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-10px, 8px);
  }
}

@keyframes float-c {

  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-6px, -10px);
  }
}

@keyframes float-d {

  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(12px, 6px);
  }
}

@keyframes float-e {

  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-8px, 10px);
  }
}

@keyframes pulse-dot {

  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }

  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

// === Bento Grid ===
.bento-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;

  .bento-card-shell {
    @include solid-card;
    border-radius: $radius-squircle;
    padding: 1.5px;
    display: block;
  }

  .bento-card-core {
    border-radius: calc($radius-squircle - 0.375rem);
    overflow: hidden;
    background: transparent;
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
    transition: all 0.5s $transition-premium;

    &:hover {

      .bento-img-wrap img {
        transform: scale(1.05);
      }

      .bento-arrow {
        background: $accent-energy;
        color: #fff;
        transform: rotate(-45deg);
      }
    }
  }

  .bento-img-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    height: 180px;
    overflow: hidden;
    flex-shrink: 0;

    img {
      height: 100%;
      object-fit: cover;
      transition: transform 0.6s $transition-premium;
    }

    .bento-img-overlay {
      position: absolute;
      inset: 0;
    }
  }

  .bento-card-content {
    padding: 20px 24px 24px;
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: rgba(255, 255, 255, 0.1);
  }

  .bento-icon {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    background: linear-gradient(135deg, $accent-energy, $accent-gradient-light);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 22px;
    margin-bottom: 14px;
    box-shadow: 0 6px 16px rgba($accent-energy, 0.25);
  }

  h3 {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 6px;
    color: $text-primary;
  }

  p {
    font-size: 13px;
    color: $text-muted;
    line-height: 1.6;
    margin: 0;
    flex: 1;
  }

  .bento-arrow {
    position: absolute;
    bottom: 20px;
    right: 20px;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: #ff6b351f;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 15px;
    color: $accent-energy;
    transition: all 0.4s $transition-premium;
  }
}

// === Product Scroll ===
.product-scroll {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card-shell {
  @include solid-card;
  padding: 1.5px;

  p,
  h3 {}

  &:hover {
    @include card-hover;
  }
}

.product-card-core {
  border-radius: calc($radius-xl - 0.375rem);
  overflow: hidden;
  background: transparent;
  transition: all 0.5s $transition-premium;

  &:hover {
    .product-img img {
      transform: scale(1.05);
    }
  }
}

.product-img {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: rgba($accent-energy, 0.03);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s $transition-premium;
  }
}

.product-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 5px 12px;
  background: $accent-energy;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  z-index: 2;
}

.product-body {
  background-color: rgba(255, 255, 255, 0.1);
  padding: 20px;

  h3 {
    font-size: 16px;
    font-weight: 700;
    margin-bottom: 6px;
    color: $text-primary;
  }
}

.product-meta {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 18px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-current {
  font-size: 20px;
  font-weight: 800;
  color: $price-color;
}

.price-original {
  font-size: 12px;
  color: $price-muted;
  text-decoration: line-through;
}

.btn-buy {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: $accent-energy;
  border: none;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.4s $transition-premium;

  .btn-icon-wrap.sm {
    background: rgba(255, 255, 255, 0.2);
  }

  &:hover {
    background: #e55a2b;
    box-shadow: 0 4px 16px rgba($accent-energy, 0.25);

    .btn-icon-wrap.sm {
      transform: translateX(2px);
    }
  }
}

// === Dual Card ===
.dual-card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.dual-card-shell {
  @include solid-card;
  border-radius: $radius-squircle;
  padding: 1.5px;

  &.accent {
    background: linear-gradient(135deg, $accent-energy, #ff8c5a);
  }
}

.dual-card-core {
  border-radius: calc($radius-squircle - 0.375rem);
  padding: 48px;
  position: relative;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.5);

  h2 {
    font-size: 26px;
    font-weight: 800;
    margin-bottom: 16px;
    color: $text-primary;
    position: relative;
    z-index: 2;
  }

  p {
    font-size: 14px;
    line-height: 1.8;
    color: $text-secondary;
    margin-bottom: 28px;
    position: relative;
    z-index: 2;
    max-width: 400px;
  }
}

.accent-core {
  background: transparent;

  h2 {
    color: #fff;
  }

  p {
    color: rgba(255, 255, 255, 0.9);
  }
}

.dual-card-glow {
  position: absolute;
  width: 250px;
  height: 250px;
  border-radius: 50%;
  right: -60px;
  top: -60px;
  background: rgba($accent-energy, 0.1);
  filter: blur(60px);
  pointer-events: none;
}

.card-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  background: #fff;
  border: none;
  border-radius: 999px;
  color: $accent-energy;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.5s $transition-premium;
  position: relative;
  z-index: 2;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

  .btn-icon-wrap {
    background: rgba($accent-energy, 0.15);
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);

    .btn-icon-wrap {
      background: $accent-energy;
      color: #fff;
      transform: translateX(2px) translateY(-1px);
    }
  }
}

// === Responsive ===
@include respond(lg) {
  .hero-bento {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    display: none;
  }

  .bento-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .product-scroll {
    grid-template-columns: repeat(2, 1fr);
  }

  .dual-card-grid {
    grid-template-columns: 1fr;
  }
}

@include respond(md) {
  .hero-section {
    padding-top: 20px;
  }

  .hero-title {
    font-size: clamp(28px, 8vw, 36px);
  }

  .section {
    padding: 60px 0;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .bento-card-content {
    padding: 16px 20px 20px;
  }

  .bento-img-wrap {
    height: 140px;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .product-scroll {
    grid-template-columns: 1fr;
  }

  .product-img {
    height: 180px;
  }

  .dual-card-core {
    padding: 32px 24px;
  }
}

@include respond(sm) {
  .hero-section {
    padding: 40px 0 60px;
  }

  .hero-title {
    font-size: clamp(24px, 10vw, 28px);
  }

  .hero-desc {
    font-size: 14px;
  }

  .btn-primary {
    padding: 14px 24px;
    font-size: 13px;
  }

  .section {
    padding: 40px 0;
  }

  .section-title {
    font-size: 24px;
  }

  .dual-card-shell {
    border-radius: $radius-xl;
  }

  .dual-card-core {
    padding: 24px 20px;

    h2 {
      font-size: 22px;
    }

    p {
      font-size: 13px;
    }
  }

  .product-body {
    padding: 16px;
  }
}
</style>
