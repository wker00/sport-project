<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "@/utils/toast";
import { adminLogin, getAdminInfo } from "@/api/manager";
import { useCounterStore } from "@/stores/counter";
import { encryptPassword } from "@/utils/rsa";

const router = useRouter();
const counterStore = useCounterStore();

const ruleForm = reactive({
  username: "",
  password: "",
});
const remember = ref(false);

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "账号长度为 3~20 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码最少 6 位", trigger: "blur" },
  ],
};

const loading = ref(false);
const formRef = ref(null);

const handleLogin = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  loading.value = true;
  try {
    const res = await adminLogin({
      username: ruleForm.username,
      password: encryptPassword(ruleForm.password),
    });
    counterStore.setToken(res.data.data);
    await fetchAdminInfo();
    router.push("/");
    toast(res.data.message || "登录成功");
  } catch {
    // 业务/网络错误由 axios 拦截器统一提示
  } finally {
    loading.value = false;
  }
};

async function fetchAdminInfo() {
  try {
    const res = await getAdminInfo();
    counterStore.setAdminInfo(res.data.data);
  } catch {
    counterStore.clearToken();
    router.push({ name: "login" });
  }
}
</script>

<template>
  <div class="login">
    <div class="login__card">
      <div class="login__brand">
        <div class="brand-bg">
          <div class="shape shape--gradient" />
          <div class="shape shape--ring" />
          <div class="shape shape--dot" />
          <div class="shape shape--grid" />
        </div>
        <div class="brand-content">
          <div class="brand-logo">
            <span class="brand-logo-icon" />
            <span class="brand-logo-text">SportZone</span>
          </div>
          <h2 class="brand-headline">购物商城<br />管理平台</h2>
          <p class="brand-description">
            全方位管理您的器材库存、订单与会员体系，让运营更高效
          </p>
        </div>
      </div>

      <div class="login__panel">
        <div class="form-container">
          <div class="form-header">
            <h2 class="form-title">管理员登录</h2>
            <p class="form-subtitle">使用您的账户信息登录后台</p>
          </div>

          <el-form ref="formRef" :model="ruleForm" :rules="rules" label-position="top" class="login-form"
            @keyup.enter="handleLogin">
            <el-form-item label="账号" prop="username">
              <el-input v-model="ruleForm.username" placeholder="请输入账号" size="large" clearable>
                <template #prefix>
                  <el-icon>
                    <User />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input v-model="ruleForm.password" type="password" placeholder="请输入密码" size="large" show-password>
                <template #prefix>
                  <el-icon>
                    <Lock />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="remember">记住我</el-checkbox>
                <el-link type="primary" underline="never">忘记密码？</el-link>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" :loading="loading" class="login-btn" @click="handleLogin">
                {{ loading ? "登录中..." : "登 录" }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            <p>Copyright &copy; 2026 SportZone. All rights reserved.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
$orange: #ff6b35;
$dark: #0f172a;
$light: #f8fafc;
$text: #334155;
$text-muted: #94a3b8;

.login {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100dvh;
  background: linear-gradient(135deg, #0f172a 0%, #1a2540 40%, #2a1a30 70%, #8c3b1b 100%);;
  position: relative;
  overflow: hidden;

  &::before,
  &::after {
    content: "";
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    pointer-events: none;
  }

  &::before {
    width: 500px;
    height: 500px;
    background: rgba($orange, 0.15);
    top: -15%;
    right: -8%;
  }

  &::after {
    width: 400px;
    height: 400px;
    background: rgba(#6366f1, 0.12);
    bottom: -10%;
    left: -5%;
  }
}

// ========== Card ==========
.login__card {
  display: flex;
  width: 900px;
  max-width: 95vw;
  height: 520px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

// ========== Brand Panel ==========
.login__brand {
  flex: 0 0 45%;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $dark;
}

.brand-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;

  &--gradient {
    width: min(30vw, 400px);
    height: min(30vw, 400px);
    background: radial-gradient(circle at 30% 30%,
        rgba($orange, 0.18) 0%,
        transparent 65%);
    top: -15%;
    right: -10%;
    animation: float 12s ease-in-out infinite;
  }

  &--ring {
    width: min(22vw, 280px);
    height: min(22vw, 280px);
    border: 1px solid rgba($orange, 0.12);
    bottom: -10%;
    left: -8%;
    animation: float 16s ease-in-out infinite reverse;
  }

  &--dot {
    width: 100px;
    height: 100px;
    background: rgba($orange, 0.06);
    top: 45%;
    left: 25%;
    animation: pulse 8s ease-in-out infinite;
  }

  &--grid {
    inset: 0;
    background-image: linear-gradient(rgba(255, 255, 255, 0.02) 1px,
        transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
    background-size: 60px 60px;
    mask-image: radial-gradient(ellipse at 50% 50%, black 30%, transparent 70%);
    -webkit-mask-image: radial-gradient(ellipse at 50% 50%,
        black 30%,
        transparent 70%);
    animation: none;
  }
}

@keyframes float {

  0%,
  100% {
    transform: translate(0, 0) rotate(0deg);
  }

  33% {
    transform: translate(24px, -16px) rotate(2deg);
  }

  66% {
    transform: translate(-8px, 12px) rotate(-1deg);
  }
}

@keyframes pulse {

  0%,
  100% {
    opacity: 0.5;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.12);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  padding: 48px;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 36px;

  &-icon {
    width: 30px;
    height: 30px;
    background: $orange;
    border-radius: 8px;
    position: relative;
    overflow: hidden;

    &::after {
      content: "";
      position: absolute;
      inset: 3px;
      background: $dark;
      border-radius: 5px;
    }
  }

  &-text {
    font-size: 18px;
    font-weight: 700;
    letter-spacing: -0.3px;
    color: #fff;
  }
}

.brand-headline {
  font-size: clamp(26px, 3vw, 38px);
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -1px;
  color: #fff;
  margin-bottom: 16px;
}

.brand-description {
  font-size: 14px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.5);
  max-width: 32ch;
}

// ========== Form Panel ==========
.login__panel {
  flex: 0 0 55%;
  background: $light;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.form-container {
  width: 100%;
  max-width: 380px;
  padding: 36px;
}

.form-header {
  margin-bottom: 28px;

  .form-title {
    font-size: 24px;
    font-weight: 700;
    color: $dark;
    letter-spacing: -0.5px;
    margin-bottom: 6px;
    margin-top: 0;
  }

  .form-subtitle {
    font-size: 14px;
    color: $text-muted;
    margin: 0;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 500;
    color: $text;
    padding-bottom: 4px;
    line-height: 1.4;
  }

  :deep(.el-input__wrapper) {
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 0 0 1px #e2e8f0;
    padding: 4px 12px;
    transition: box-shadow 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 2px $orange;
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    height: 40px;
  }

  :deep(.el-input__prefix) {
    margin-right: 8px;
  }

  :deep(.el-input__prefix-inner .el-icon) {
    font-size: 16px;
    color: $text-muted;
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: $text;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  background: $orange;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);

  &:hover {
    background: #e55a2b;
    transform: translateY(-1px);
    box-shadow: 0 8px 24px rgba($orange, 0.3);
  }

  &:active {
    transform: scale(0.98);
  }
}

.form-footer {
  margin-top: 32px;
  text-align: center;

  p {
    font-size: 11px;
    color: $text-muted;
    margin: 0;
  }
}

// ========== Responsive ==========
@media (max-width: 768px) {
  .login__card {
    flex-direction: column;
    height: auto;
    min-height: 480px;
  }

  .login__brand {
    flex: 0 0 auto;
    min-height: 120px;
  }

  .brand-content {
    padding: 28px 24px;
    text-align: center;
  }

  .brand-logo {
    justify-content: center;
    margin-bottom: 12px;
  }

  .brand-headline {
    font-size: 22px;
    margin-bottom: 8px;
  }

  .brand-description {
    display: none;
  }

  .login__panel {
    flex: 1;
  }

  .form-container {
    max-width: 100%;
    padding: 28px 24px;
  }

  .form-header {
    margin-bottom: 20px;

    .form-title {
      font-size: 20px;
    }
  }
}
</style>
