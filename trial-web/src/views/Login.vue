<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
    </div>

    <div class="login-container">
      <div class="login-brand">
        <div class="brand-icon">⚔️</div>
        <h1 class="brand-title">试炼坊</h1>
        <p class="brand-subtitle">Trial Workshop</p>
        <p class="brand-desc">属于你的个人练习空间</p>
      </div>

      <div class="login-card">
        <div class="card-header">
          <a-tabs v-model:active-key="activeTab" size="large">
            <a-tab-pane key="login" title="登录" />
            <a-tab-pane key="register" title="注册" />
          </a-tabs>
        </div>

        <div class="card-copy">
          <h2 class="card-title">{{ activeTab === 'login' ? '欢迎回来' : '创建你的账号' }}</h2>
          <p class="card-subtitle">{{ activeTab === 'login' ? '继续你的智能刷题与备考计划' : '开始你的专属练习空间' }}</p>
        </div>

        <!-- 登录表单 -->
        <a-form v-if="activeTab === 'login'" :model="loginForm" @submit="handleLogin" layout="vertical" class="auth-form">
          <a-form-item field="username" label="用户名">
            <a-input v-model="loginForm.username" placeholder="请输入用户名" size="large">
              <template #prefix><icon-user /></template>
            </a-input>
          </a-form-item>
          <a-form-item field="password" label="密码">
            <a-input-password v-model="loginForm.password" placeholder="请输入密码" size="large">
              <template #prefix><icon-lock /></template>
            </a-input-password>
          </a-form-item>
          <a-form-item field="captcha" label="验证码">
            <div class="captcha-row">
              <a-input v-model="loginForm.captcha" placeholder="请输入验证码" size="large" class="captcha-input">
                <template #prefix>🔐</template>
              </a-input>
              <img :src="captchaImage" @click="refreshCaptcha" class="captcha-image" title="点击刷新验证码" />
            </div>
          </a-form-item>
          <a-form-item class="auth-submit-row">
            <a-button type="primary" html-type="submit" long size="large" :loading="loading" class="app-primary-btn auth-submit-btn">
              登 录
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 注册表单 -->
        <a-form v-else :model="registerForm" @submit="handleRegister" layout="vertical" class="auth-form">
          <a-form-item field="username" label="用户名">
            <a-input v-model="registerForm.username" placeholder="3-20个字符" size="large">
              <template #prefix><icon-user /></template>
            </a-input>
          </a-form-item>
          <a-form-item field="password" label="密码">
            <a-input-password v-model="registerForm.password" placeholder="6-30个字符" size="large">
              <template #prefix><icon-lock /></template>
            </a-input-password>
          </a-form-item>
          <a-form-item field="nickname" label="昵称">
            <a-input v-model="registerForm.nickname" placeholder="给自己起个名字吧" size="large" />
          </a-form-item>
          <a-form-item field="captcha" label="验证码">
            <div class="captcha-row">
              <a-input v-model="registerForm.captcha" placeholder="请输入验证码" size="large" class="captcha-input">
                <template #prefix>🔐</template>
              </a-input>
              <img :src="captchaImage" @click="refreshCaptcha" class="captcha-image" title="点击刷新验证码" />
            </div>
          </a-form-item>
          <a-form-item class="auth-submit-row">
            <a-button type="primary" html-type="submit" long size="large" :loading="loading" class="app-primary-btn auth-submit-btn">
              注 册
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '../store/user'
import { authApi } from '../api/request'
import { IconUser, IconLock } from '@arco-design/web-vue/es/icon'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)
const captchaImage = ref('')

const loginForm = reactive({ username: '', password: '', captcha: '' })
const registerForm = reactive({ username: '', password: '', nickname: '', captcha: '' })

// 获取验证码
async function refreshCaptcha() {
  try {
    const res = await authApi.getCaptcha()
    captchaImage.value = res.data.image
  } catch (error) {
    Message.error('获取验证码失败')
  }
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    Message.warning('请输入用户名和密码')
    return
  }
  if (!loginForm.captcha) {
    Message.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    const res = await authApi.login(loginForm)
    const token = res.data.token
    const userInfo = res.data.user
    userStore.setToken(token)
    userStore.setUserInfo(userInfo)
    Message.success('登录成功，欢迎回到试炼坊！')
    router.push('/dashboard')
  } catch (e: any) {
    // 登录失败后刷新验证码
    refreshCaptcha()
    loginForm.captcha = ''
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.username || !registerForm.password) {
    Message.warning('请填写必填项')
    return
  }
  if (!registerForm.captcha) {
    Message.warning('请输入验证码')
    return
  }
  loading.value = true
  try {
    await authApi.register(registerForm)
    Message.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = registerForm.password
    loginForm.captcha = ''
    refreshCaptcha()
  } catch (e: any) {
    // 注册失败后刷新验证码
    refreshCaptcha()
    registerForm.captcha = ''
  } finally {
    loading.value = false
  }
}

// 页面加载时获取验证码
onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 32px;
}

.login-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(circle at top left, rgba(47, 84, 235, 0.14), transparent 30%),
    radial-gradient(circle at bottom right, rgba(0, 185, 107, 0.12), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.35), transparent 38%);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}
.orb-1 {
  width: 420px; height: 420px;
  background: rgba(102, 126, 234, 0.5);
  top: -120px; left: -100px;
  animation-delay: 0s;
}
.orb-2 {
  width: 340px; height: 340px;
  background: rgba(140, 235, 196, 0.52);
  bottom: -50px; right: -50px;
  animation-delay: 3s;
}
.orb-3 {
  width: 260px; height: 260px;
  background: rgba(181, 163, 255, 0.45);
  top: 48%; left: 58%;
  animation-delay: 5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-30px); }
}

.login-container {
  position: relative;
  z-index: 1;
  width: min(100%, 1080px);
  display: grid;
  grid-template-columns: minmax(280px, 1fr) minmax(360px, 460px);
  align-items: center;
  gap: 40px;
}

.login-brand {
  padding: 28px;
  border-radius: 32px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.46) 0%, rgba(255, 255, 255, 0.1) 100%);
  border: 1px solid rgba(255, 255, 255, 0.48);
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.brand-icon {
  width: 72px;
  height: 72px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  font-size: 36px;
  margin-bottom: 20px;
  background: var(--primary-gradient);
  box-shadow: var(--primary-shadow);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.brand-title {
  font-size: clamp(38px, 5vw, 56px);
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 6px;
}

.brand-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  letter-spacing: 4px;
  text-transform: uppercase;
}

.brand-desc {
  margin-top: 14px;
  color: var(--text-secondary);
  font-size: 16px;
  max-width: 360px;
  line-height: 1.8;
}

.login-card {
  background: linear-gradient(180deg, var(--surface-muted) 0%, var(--bg-card) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--border-light);
  border-radius: 32px;
  padding: 34px;
  width: 100%;
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.12);
}

.card-header {
  margin-bottom: 18px;
}

.card-copy {
  margin-bottom: 24px;
}

.card-title {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
}

.card-subtitle {
  margin-top: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.auth-form {
  margin-top: 4px;
}

.auth-submit-row {
  margin-top: 8px;
  margin-bottom: 0;
}

.auth-submit-btn {
  height: 48px;
  font-size: 16px;
}

:deep(.card-header .arco-tabs-nav-tab-list) {
  width: 100%;
  padding: 6px;
  border-radius: 999px;
  background: var(--surface-muted);
}

:deep(.card-header .arco-tabs-tab) {
  flex: 1;
  justify-content: center;
  margin: 0 !important;
}

:deep(.card-header .arco-tabs-content) {
  padding-top: 0;
}

@media (max-width: 900px) {
  .login-container {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .login-brand {
    text-align: center;
  }

  .brand-desc {
    max-width: none;
    margin-left: auto;
    margin-right: auto;
  }
}

@media (max-width: 576px) {
  .login-page {
    padding: 18px;
  }

  .login-card {
    padding: 24px 20px;
    border-radius: 24px;
  }

  .login-brand {
    padding: 20px;
    border-radius: 24px;
  }
}
</style>
