<template>
  <footer class="footer-glass">
    <div class="footer-inner">
      <div class="footer-grid">
        <div class="footer-brand">
          <div class="brand-logo">
            <svg viewBox="0 0 40 40" width="32" height="32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="20" cy="20" r="20" fill="#ff6b35" />
              <path d="M22.5 8L13 22h6l-2.5 10L27 18h-6l2.5-10z" fill="#fff" stroke="#fff" stroke-width="0.5" stroke-linejoin="round" />
            </svg>
          </div>
          <p class="brand-desc">专注体育用品与运动装备精选，打造专业化的运动购物体验平台。</p>
          <div class="social-row">
            <a class="social-icon"><i class="ph-fill ph-wechat-logo"></i></a>
            <a class="social-icon"><i class="ph-fill ph-twitter-logo"></i></a>
            <a class="social-icon"><i class="ph-fill ph-video"></i></a>
            <a class="social-icon"><i class="ph-fill ph-book-open"></i></a>
          </div>
        </div>
        <div class="footer-col">
          <h4>商城服务</h4>
          <router-link to="/category">商品分类</router-link>
          <router-link to="/pointsMall">积分商城</router-link>
          <router-link to="/cart">购物车</router-link>
        </div>
        <div class="footer-col">
          <h4>用户服务</h4>
          <router-link to="/user">个人中心</router-link>
          <router-link to="/user/orders">我的订单</router-link>
          <router-link to="/user/points">积分中心</router-link>
        </div>
        <div class="footer-col">
          <h4>帮助中心</h4>
          <a>联系客服</a>
          <a>配送说明</a>
          <a>售后服务</a>
        </div>
      </div>
      <div class="footer-bottom">
        <div class="bottom-line"></div>
        <p>&copy; 2026</p>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
@use '@/assets/variables' as *;

.footer-glass {
  border-top: 0.5px solid $border-subtle;
  padding: 60px 0 32px;
  margin-top: 80px;
  background: var(--footer-bg);
  backdrop-filter: var(--footer-blur);
}

.footer-inner {
  width: 92%;
  max-width: 1400px;
  margin: 0 auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr repeat(3, 1fr);
  gap: 48px;
  margin-bottom: 40px;
}

.footer-brand {
  .brand-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;

    svg { display: block; }
    span { font-size: 18px; font-weight: 700; color: $text-primary; }
  }

  .brand-desc {
    color: $text-muted;
    font-size: 14px;
    line-height: 1.8;
    max-width: 300px;
  }

  .social-row {
    display: flex;
    gap: 10px;
    margin-top: 24px;
  }

  .social-icon {
    width: 38px;
    height: 38px;
    border-radius: 50%;
    background: $bg-card;
    border: 0.5px solid $border-subtle;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: $text-muted;
    cursor: pointer;
    transition: all 0.4s $transition-premium;

    &:hover {
      background: rgba(255, 107, 53, 0.15);
      border-color: rgba(255, 107, 53, 0.3);
      color: $accent-energy;
    }
  }
}

.footer-col {
  h4 {
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    color: $text-muted;
    margin-bottom: 20px;
  }

  a {
    display: block;
    color: $text-muted;
    font-size: 14px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s $transition-premium;

    &:hover {
      color: $accent-energy;
    }
  }
}

.footer-bottom {
  .bottom-line {
    height: 0.5px;
    background: $border-subtle;
    margin-bottom: 20px;
  }

  p {
    text-align: center;
    color: $text-muted;
    font-size: 13px;
  }
}

@include respond(lg) {
  .footer-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 32px;
  }

  .footer-brand {
    grid-column: 1 / -1;
  }
}

@include respond(md) {
  .footer-glass {
    padding: 40px 0 24px;
    margin-top: 48px;
  }

  .footer-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 28px;
  }

  .footer-brand {
    grid-column: auto;
  }

  .brand-desc {
    max-width: none;
  }

  .footer-col h4 {
    margin-bottom: 16px;
  }

  .footer-col a {
    font-size: 13px;
    margin-bottom: 10px;
  }
}

@include respond(sm) {
  .footer-grid {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .footer-brand {
    text-align: center;
  }

  .brand-desc {
    max-width: none;
    margin: 0 auto;
  }

  .social-row {
    justify-content: center;
  }

  .footer-col {
    text-align: center;
  }
}
</style>
