<script setup>
import { ref, reactive, onMounted, onUnmounted } from "vue"
import { useRouter } from "vue-router"
import { toast } from "@/utils/toast"
import { userRegister } from "@/api/manager"
import { encryptPassword } from "@/utils/rsa"

const router = useRouter()

const formRef = ref(null)
const form = reactive({ username: "", password: "", confirmPassword: "" })

const rules = {
  username: [
    { required: true, message: "请输入账号", trigger: "blur" },
    { min: 3, max: 20, message: "账号长度必须在3到20个字符之间", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        const trimmed = value.trim()
        if (trimmed.length < 3) callback(new Error("账号至少3个字符"))
        else if (trimmed.length > 20) callback(new Error("账号最大20个字符"))
        else if (!/^[a-zA-Z0-9]+$/.test(trimmed)) callback(new Error("账号只能包含字母和数字"))
        else callback()
      },
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) callback(new Error("两次密码输入不一致"))
        else callback()
      },
      trigger: "blur",
    },
  ],
}

const isLoading = ref(false)

async function handleRegister() {
  if (isLoading.value) return
  try { await formRef.value.validate() } catch { return }
  isLoading.value = true
  try {
    const res = await userRegister({
      username: form.username.trim(),
      password: encryptPassword(form.password),
      confirmPassword: encryptPassword(form.confirmPassword)
    })
    isLoading.value = false
    if (res.data.code === 200) {
      toast("注册成功")
      router.push({ name: "login" })
    } else {
      toast(res.data.message || "注册失败", "error")
    }
  } catch (err) {
    isLoading.value = false
    toast(err.response?.data?.message || "网络异常，请稍后重试", "error")
  }
}

function goHome() {
  router.push({ name: 'index' })
}

// Canvas particle animation
const canvasRef = ref(null)
let animFrame = null
let particles = []

function initParticles(w, h) {
  const count = 28
  particles = Array.from({ length: count }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    r: Math.random() * 2.5 + 0.8,
    dx: (Math.random() - 0.5) * 0.4,
    dy: (Math.random() - 0.5) * 0.4,
    o: Math.random() * 0.35 + 0.1,
  }))
}

function drawParticles(ctx, w, h) {
  ctx.clearRect(0, 0, w, h)
  for (const p of particles) {
    p.x += p.dx; p.y += p.dy
    if (p.x < 0) p.x = w; if (p.x > w) p.x = 0
    if (p.y < 0) p.y = h; if (p.y > h) p.y = 0
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255,107,53,${p.o})`
    ctx.fill()
  }
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = (dx * dx + dy * dy) ** 0.5
      if (dist < 100) {
        ctx.beginPath()
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.strokeStyle = `rgba(255,107,53,${(1 - dist / 100) * 0.2})`
        ctx.lineWidth = 0.6
        ctx.stroke()
      }
    }
  }
  animFrame = requestAnimationFrame(() => drawParticles(ctx, w, h))
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return
  const parent = canvas.parentElement
  canvas.width = parent.clientWidth
  canvas.height = parent.clientHeight
  initParticles(canvas.width, canvas.height)
  const ctx = canvas.getContext('2d')
  drawParticles(ctx, canvas.width, canvas.height)
  const resize = () => {
    canvas.width = parent.clientWidth
    canvas.height = parent.clientHeight
    initParticles(canvas.width, canvas.height)
  }
  window.addEventListener('resize', resize)
  canvas._cleanup = () => {
    window.removeEventListener('resize', resize)
    cancelAnimationFrame(animFrame)
  }
})

onUnmounted(() => {
  canvasRef.value?._cleanup?.()
})
</script>

<template>
  <div class="auth-page">
    <div class="auth-glass">
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-icon" @click="goHome" style="cursor:pointer">
            <svg viewBox="0 0 40 40" width="48" height="48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="20" cy="20" r="20" fill="#ff6b35" />
              <path d="M22.5 8L13 22h6l-2.5 10L27 18h-6l2.5-10z" fill="#fff" stroke="#fff" stroke-width="0.5" stroke-linejoin="round" />
            </svg>
          </div>
          <h2>加入我们</h2>
          <p>创建您的账户，开启运动购物之旅</p>
          <div class="brand-stats">
            <div class="stat-item">
              <span class="stat-value">10万+</span>
              <span class="stat-label">运动爱好者</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">24h</span>
              <span class="stat-label">极速发货</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">98%</span>
              <span class="stat-label">用户好评</span>
            </div>
          </div>
        </div>
        <canvas ref="canvasRef" class="brand-canvas"></canvas>
      </div>

      <div class="auth-form-panel">
        <div class="auth-form-inner">
          <div class="auth-form-header">
            <h1>用户注册</h1>
            <p>已有账号？<router-link to="/user/login" class="auth-link">立即登录</router-link></p>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" autocomplete="off" @keyup.enter="handleRegister">
            <el-form-item prop="username" label="账号">
              <div class="input-shell">
                <div class="input-core">
                  <i class="ph ph-user input-icon"></i>
                  <el-input v-model="form.username" placeholder="请输入账号" autocomplete="off" required />
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="password" label="密码">
              <div class="input-shell">
                <div class="input-core">
                  <i class="ph ph-lock input-icon"></i>
                  <el-input v-model="form.password" placeholder="请输入密码" type="password" show-password autocomplete="off" required />
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword" label="确认密码">
              <div class="input-shell">
                <div class="input-core">
                  <i class="ph ph-lock input-icon"></i>
                  <el-input v-model="form.confirmPassword" placeholder="确认密码" type="password" show-password autocomplete="off" required />
                </div>
              </div>
            </el-form-item>

            <el-button type="primary" @click="handleRegister" class="auth-submit" :loading="isLoading">
              <span v-if="!isLoading">注册</span>
              <i class="ph ph-arrow-right" v-if="!isLoading"></i>
            </el-button>

            <div class="auth-divider">
              <span>注册即表示同意</span>
            </div>

            <div class="terms-text">
              注册即表示您已阅读并同意 <a class="auth-link" style="cursor: pointer;">服务条款</a> 和<a class="auth-link" style="cursor: pointer;">隐私政策</a>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/assets/variables" as *;

.auth-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: $bg-primary;
}

.auth-glass {
  display: flex;
  width: 100%;
  max-width: 960px;
  min-height: 560px;
  background: var(--auth-bg);
  border-radius: $radius-2xl;
  box-shadow: 
    var(--shadow-glass),
    0 0 0 1px rgba(255,255,255,0.05),
    inset 0 1px 0 rgba(255,255,255,0.1);
  border: 1px solid var(--border-glass);
  transition: all 0.5s $transition-premium;
  overflow: hidden;
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px);

  &:hover {
    box-shadow: 
      var(--shadow-glass-hover),
      0 0 0 1px rgba(255,255,255,0.08),
      inset 0 1px 0 rgba(255,255,255,0.12);
  }
}

.auth-brand {
  flex: 1;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $accent-energy-soft, $auth-brand-bg-end);
  border-radius: calc($radius-2xl - 0.375rem) 0 0 calc($radius-2xl - 0.375rem);

  &::after {
    content: '';
    position: absolute;
    right: 0;
    top: 10%;
    height: 80%;
    width: 0.5px;
    background: var(--border-medium);
  }
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  padding: 48px 40px;
}

.brand-icon {
  margin-bottom: 24px;
  position: relative;

  svg { 
    display: inline-block;
    filter: drop-shadow(0 0 20px rgba(255,107,53,0.4));
    animation: icon-glow 3s ease-in-out infinite;
  }
}

@keyframes icon-glow {
  0%, 100% { filter: drop-shadow(0 0 20px rgba(255,107,53,0.4)); }
  50% { filter: drop-shadow(0 0 30px rgba(255,107,53,0.6)); }
}

.brand-content h2 {
  font-size: 32px;
  font-weight: 800;
  color: $text-primary;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.brand-content p {
  font-size: 15px;
  color: $text-muted;
  margin-bottom: 32px;
  line-height: 1.6;
}

.brand-stats {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  cursor: context-menu;
}

.stat-item {
  text-align: center;
  padding: 14px 10px;
  background: rgba(255,255,255,0.6);
  border-radius: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.8);
  transition: all 0.4s $transition-premium;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
    transition: left 0.6s ease;
  }

  &:hover::before {
    left: 100%;
  }

  .stat-value {
    display: block;
    font-size: 22px;
    font-weight: 800;
    color: #ff6b35;
    line-height: 1.2;
    margin-bottom: 4px;
    text-shadow: 0 2px 4px rgba(255,107,53,0.2);
  }

  .stat-label {
    display: block;
    font-size: 11px;
    font-weight: 500;
    color: var(--text-muted);
    letter-spacing: 0.5px;
  }

  &:hover {
    background: rgba(255,255,255,0.9);
    transform: translateY(-4px) scale(1.02);
    box-shadow: 0 8px 20px rgba(255,107,53,0.15);
  }
}

.brand-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

// 响应式优化
@media (max-width: 768px) {
  .auth-glass {
    flex-direction: column;
    max-width: 480px;
    min-height: auto;
  }

  .auth-brand {
    border-radius: $radius-2xl $radius-2xl 0 0;
    min-height: 200px;
    padding: 40px 20px;
    
    &::after {
      right: 10%;
      top: auto;
      bottom: 0;
      height: 0.5px;
      width: 80%;
    }
  }

  .brand-content {
    padding: 20px;
    
    h2 {
      font-size: 26px;
    }
    
    p {
      font-size: 14px;
      margin-bottom: 20px;
    }
  }

  .brand-stats {
    gap: 6px;
    
    .stat-item {
      padding: 10px 6px;
      
      .stat-value {
        font-size: 18px;
      }
      
      .stat-label {
        font-size: 10px;
      }
    }
  }

  .auth-form-panel {
    border-radius: 0 0 $radius-2xl $radius-2xl;
  }

  .auth-form-inner {
    padding: 32px 24px;
  }

  .auth-form-header {
    margin-bottom: 28px;
    
    h1 {
      font-size: 24px;
    }
  }
}

@media (max-width: 480px) {
  .auth-page {
    padding: 10px;
  }
  
  .auth-glass {
    max-width: 100%;
  }
  
  .brand-stats {
    flex-wrap: wrap;
    
    .stat-item {
      min-width: calc(50% - 4px);
    }
  }
  
  .terms-text {
    font-size: 11px;
    padding: 0;
  }
}

.auth-form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-secondary;
  backdrop-filter: blur(24px);
  border-radius: 0 calc($radius-2xl - 0.375rem) calc($radius-2xl - 0.375rem) 0;
}

.auth-form-inner {
  width: 100%;
  max-width: 360px;
  padding: 48px 40px;
}

.auth-form-header {
  margin-bottom: 36px;
  text-align: center;

  h1 {
    font-size: 28px;
    font-weight: 800;
    color: $text-primary;
    margin-bottom: 10px;
    letter-spacing: -0.5px;
    position: relative;
    display: inline-block;

    &::after {
      content: '';
      position: absolute;
      bottom: -8px;
      left: 50%;
      transform: translateX(-50%);
      width: 40px;
      height: 3px;
      background: linear-gradient(90deg, $accent-energy, $accent-vitality);
      border-radius: 2px;
    }
  }

  p {
    font-size: 14px;
    color: $text-muted;
    margin-top: 16px;
  }
}

.auth-link {
  color: $accent-energy;
  font-weight: 600;
  transition: all 0.3s $transition-premium;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 0;
    width: 0;
    height: 2px;
    background: $accent-energy;
    transition: width 0.3s ease;
    border-radius: 1px;
  }

  &:hover {
    color: darken($accent-energy, 10%);
    
    &::after {
      width: 100%;
    }
  }
}

:deep(.el-form) {
  .el-form-item { margin-bottom: 22px; }
  .el-form-item__label {
    display: block;
    color: $text-secondary;
    font-size: 13px;
    font-weight: 500;
    margin-bottom: 6px;
    line-height: normal;
    padding: 0;
    text-align: left;
  }
  .el-form-item__label::after { display: none; }
  .el-form-item.is-error .input-icon { color: var(--el-color-danger); }
}

.input-shell {
  width: 100%;
  border-radius: 14px;
  background: $auth-input-shell-bg;
  box-shadow: 
    var(--shadow-glass),
    inset 0 1px 0 rgba(255,255,255,0.1);
  border: 1px solid var(--border-glass);
  transition: all 0.4s $transition-premium;
  position: relative;

  &::after {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(255,107,53,0.3), rgba(0,212,170,0.3));
    opacity: 0;
    z-index: -1;
    transition: opacity 0.4s ease;
  }

  &:focus-within {
    border-color: $accent-energy;
    box-shadow: 
      0 0 0 3px rgba(255,107,53,0.15),
      var(--shadow-glass),
      inset 0 1px 0 rgba(255,255,255,0.15);
    transform: translateY(-1px);

    &::after {
      opacity: 1;
    }
  }
}

.input-core {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  border-radius: calc(14px - 1px);
  background: $auth-input-core-bg;
  transition: all 0.4s $transition-premium;
  position: relative;
  
  .input-icon {
    font-size: 18px;
    color: $text-muted;
    flex-shrink: 0;
    transition: all 0.3s $transition-premium;
    position: relative;
    z-index: 1;
  }

  &:focus-within .input-icon {
    color: $accent-energy;
    transform: scale(1.1);
    filter: drop-shadow(0 0 4px rgba(255,107,53,0.4));
  }

  :deep(.el-input) {
    --el-input-border-color: transparent;
    --el-input-hover-border-color: transparent;
    --el-input-focus-border-color: transparent;
    --el-input-border-radius: 0;
    --el-input-height: 48px;
    --el-font-size-base: 15px;
    --el-input-bg-color: transparent;
    --el-input-text-color: $text-primary;
    --el-input-placeholder-color: $text-muted;
    --el-input-hover-bg-color: transparent;

    .el-input__wrapper {
      padding: 0;
      box-shadow: none !important;
      background: transparent;
    }

    .el-input__inner { 
      font-family: inherit;
      transition: all 0.3s ease;
      
      &:focus {
        transform: translateX(2px);
      }
    }
  }
}

:deep(.auth-submit) {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, $accent-energy, #e85d26);
  border: none;
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.4s $transition-premium;
  box-shadow: 
    0 4px 16px rgba(255, 107, 53, 0.25),
    inset 0 1px 0 rgba(255,255,255,0.2);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: left 0.5s ease;
  }

  &:hover:not(.is-loading) {
    transform: translateY(-3px);
    box-shadow: 
      0 8px 28px rgba(255, 107, 53, 0.4),
      inset 0 1px 0 rgba(255,255,255,0.25);

    &::before {
      left: 100%;
    }
  }

  &:active:not(.is-loading) {
    transform: translateY(-1px) scale(0.98);
    box-shadow: 
      0 4px 12px rgba(255, 107, 53, 0.3),
      inset 0 1px 0 rgba(255,255,255,0.2);
  }

  &.is-loading {
    opacity: 0.8;
    transform: none;
  }
}

.auth-divider {
  display: flex;
  align-items: center;
  margin: 24px 0 20px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--border-glass), transparent);
  }

  span {
    padding: 0 16px;
    font-size: 12px;
    color: var(--text-muted);
    white-space: nowrap;
    letter-spacing: 0.5px;
  }
}

.terms-text {
  text-align: center;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.8;
  padding: 0 10px;
  cursor: context-menu;

  .auth-link {
    font-size: 12px;
    font-weight: 600;
  }
}
</style>