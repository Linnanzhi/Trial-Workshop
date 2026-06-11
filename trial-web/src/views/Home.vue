<template>
  <div class="landing-page">
    <!-- 顶部导航栏 -->
    <header class="header" :class="{ 'header-scrolled': scrolled }">
      <div class="header-container">
        <div class="logo" @click="scrollToTop">
          <span class="logo-icon">⚔️</span>
          <span class="logo-text">试炼坊</span>
          <span class="logo-sub">Trial Workshop</span>
        </div>
        <nav class="nav-links">
          <a href="#features" @click.prevent="scrollTo('features')">功能特色</a>
          <a href="#showcase" @click.prevent="scrollTo('showcase')">产品展示</a>
          <a href="#tech" @click.prevent="scrollTo('tech')">技术架构</a>
        </nav>
        <div class="nav-actions">
          <a-button v-if="isLoggedIn" type="primary" shape="round" @click="goToDashboard">
            进入控制台 →
          </a-button>
          <template v-else>
            <a-button type="text" @click="goToLogin" class="nav-login-btn">登录</a-button>
            <a-button type="primary" shape="round" @click="goToLogin" class="nav-register-btn">
              免费注册
            </a-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 第一屏：英雄区 -->
    <section class="hero-section">
      <!-- 背景装饰 -->
      <div class="hero-bg">
        <div class="hero-orb orb-1"></div>
        <div class="hero-orb orb-2"></div>
        <div class="hero-orb orb-3"></div>
        <div class="hero-grid"></div>
      </div>

      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          <span>沉浸模拟 · 灵活组题 · AI 解析</span>
        </div>
        <h1 class="hero-title">
          智能刷题 · 高效备考<br />
          <span class="gradient-text">AI 赋能的一站式学习平台</span>
        </h1>
        <p class="hero-subtitle">
          融合智能题库管理、AI 解析、模拟考场、错题本与艾宾浩斯复习于一体，<br class="hidden-xs" />
          让每一次练习都更有针对性，每一次复习都更高效。
        </p>

        <div class="hero-actions">
          <a-button type="primary" size="large" shape="round" class="hero-btn-primary" @click="goToDashboard">
            🚀 立即体验
          </a-button>
          <a-button size="large" shape="round" class="hero-btn-outline" @click="scrollTo('features')">
            了解更多 ↓
          </a-button>
        </div>

        <!-- 数据亮点 -->
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="stat-num">5+</span>
            <span class="stat-label">题型支持</span>
          </div>
          <div class="stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">3</span>
            <span class="stat-label">AI 提供商</span>
          </div>
          <div class="stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">∞</span>
            <span class="stat-label">练习上限</span>
          </div>
          <div class="stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">100%</span>
            <span class="stat-label">开源免费</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 第二屏：核心功能 -->
    <section class="features-section" id="features">
      <div class="section-container">
        <div class="section-header">
          <span class="section-tag">核心功能</span>
          <h2 class="section-title">重塑学习体验的每一环</h2>
          <p class="section-desc">从录入题目到错题复盘，一站式涵盖备考全流程</p>
        </div>

        <div class="features-grid">
          <div v-for="(feature, index) in coreFeatures" :key="index" 
               class="feature-card" :style="{ '--delay': index * 0.1 + 's' }">
            <div class="feature-icon" :style="{ background: feature.gradient }">
              {{ feature.emoji }}
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
            <div class="feature-tag">{{ feature.tag }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 第三屏：产品展示 -->
    <section class="showcase-section" id="showcase">
      <div class="section-container">
        <div class="section-header">
          <span class="section-tag">产品展示</span>
          <h2 class="section-title">全平台无缝衔接，代码极致优雅</h2>
          <p class="section-desc">前后端分离架构，极简代码结构，部署快如闪电</p>
        </div>

        <div class="showcase-layout">
          <!-- 左侧特性列表 -->
          <div class="showcase-features">
            <div v-for="(item, idx) in showcaseItems" :key="idx" class="showcase-item"
                 :class="{ active: activeShowcase === idx }" @click="activeShowcase = idx">
              <div class="showcase-item-icon" :style="{ color: item.color }">{{ item.emoji }}</div>
              <div class="showcase-item-content">
                <h4>{{ item.title }}</h4>
                <p>{{ item.desc }}</p>
              </div>
            </div>
          </div>

          <!-- 右侧模拟窗口 -->
          <div class="showcase-preview">
            <div class="mockup-window">
              <div class="mockup-titlebar">
                <div class="mockup-dots">
                  <i class="dot-red"></i><i class="dot-yellow"></i><i class="dot-green"></i>
                </div>
                <span class="mockup-url">trial-workshop.app</span>
              </div>
              <div class="mockup-body">
                <div class="mockup-sidebar">
                  <div class="mock-logo">⚔️ 试炼坊</div>
                  <div v-for="(menu, mi) in mockMenus" :key="mi" 
                       class="mock-menu-item" :class="{ active: mi === 0 }">
                    {{ menu }}
                  </div>
                </div>
                <div class="mockup-content">
                  <div class="mock-header-bar"></div>
                  <div class="mock-stats-row">
                    <div class="mock-stat-card" v-for="i in 4" :key="i"></div>
                  </div>
                  <div class="mock-charts-row">
                    <div class="mock-chart-card">
                      <div class="mock-chart-line"></div>
                    </div>
                    <div class="mock-chart-card">
                      <div class="mock-chart-radar"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 第四屏：技术架构 -->
    <section class="tech-section" id="tech">
      <div class="section-container">
        <div class="section-header">
          <span class="section-tag">技术架构</span>
          <h2 class="section-title">现代化全栈技术选型</h2>
          <p class="section-desc">严谨的工程实践，确保系统稳定、可扩展</p>
        </div>

        <div class="tech-grid">
          <div v-for="(tech, idx) in techStack" :key="idx" class="tech-card">
            <div class="tech-emoji">{{ tech.emoji }}</div>
            <h4 class="tech-name">{{ tech.name }}</h4>
            <div class="tech-items">
              <span v-for="(item, ii) in tech.items" :key="ii" class="tech-chip">{{ item }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta-section">
      <div class="cta-content">
        <h2>准备好开始了吗？</h2>
        <p>注册即可免费体验全部功能，开启智能备考之旅</p>
        <a-button type="primary" size="large" shape="round" class="cta-btn" @click="goToDashboard">
          🚀 立即开始使用
        </a-button>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-left">
          <div class="footer-logo">⚔️ 试炼坊</div>
          <p class="footer-copyright">© 2026 试炼坊团队 </p>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <h5>产品</h5>
            <a href="#features">功能特色</a>
            <a href="#showcase">产品展示</a>
          </div>
          <div class="footer-col">
            <h5>技术</h5>
            <a href="#tech">技术架构</a>
            <a href="#">API 文档</a>
          </div>
          <div class="footer-col">
            <h5>关于</h5>
            <a href="#">开发团队</a>
            <a href="#">开源协议</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const scrolled = ref(false)
const activeShowcase = ref(0)

const isLoggedIn = computed(() => !!localStorage.getItem('trial_token'))

const goToLogin = () => router.push('/login')
const goToDashboard = () => {
  if (isLoggedIn.value) {
    router.push('/dashboard')
  } else {
    router.push('/login')
  }
}
const scrollToTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })
const scrollTo = (id: string) => {
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' })
}

const handleScroll = () => {
  scrolled.value = window.scrollY > 20
}

onMounted(() => window.addEventListener('scroll', handleScroll))
onUnmounted(() => window.removeEventListener('scroll', handleScroll))

// 功能数据
const coreFeatures = [
  {
    emoji: '📊', title: '学情仪表盘', tag: '数据驱动',
    desc: '实时展示练习进度、成绩趋势与能力雷达图，学习效果一目了然。',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    emoji: '📚', title: '智能题库', tag: '高效管理',
    desc: '支持自定义分类与标签，灵活创建、批量导入与 AI 智能解析。',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  },
  {
    emoji: '🤖', title: 'AI 智能解析', tag: 'AI 赋能',
    desc: '接入通义千问、OpenAI、DeepSeek 等主流大模型，一键解析题目文本。',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  },
  {
    emoji: '📝', title: '组卷考试', tag: '全真模拟',
    desc: '自由组卷、沉浸式考场环境，支持自动计时与智能评分批改。',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  },
  {
    emoji: '📖', title: '智能错题本', tag: '精准提升',
    desc: '自动收录考试错题，基于艾宾浩斯遗忘曲线智能安排复习计划。',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  },
  {
    emoji: '🔗', title: '试卷分享', tag: '协作学习',
    desc: '一键生成分享码，好友通过分享码快速导入试卷，共建题库生态。',
    gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  },
]

const showcaseItems = [
  { emoji: '🎯', title: '沉浸式模拟考场', desc: '全真模拟答题环境，自动计时交卷', color: '#f5576c' },
  { emoji: '📈', title: '数据驱动大屏', desc: '细颗粒度数据分析，学情一目了然', color: '#667eea' },
  { emoji: '🧠', title: 'AI 拓展接口', desc: '内置多家 AI 服务，拥抱智能化浪潮', color: '#4facfe' },
  { emoji: '🔄', title: '艾宾浩斯复习', desc: '科学间隔复习，精准攻克薄弱知识', color: '#43e97b' },
]

const mockMenus = ['📊 学情仪表盘', '📚 智能题库', '🏷️ 分类与标签', '📝 考试工坊', '📖 错题本', '🤖 AI 智能解析']

const techStack = [
  { emoji: '🎨', name: '前端', items: ['Vue 3', 'TypeScript', 'Arco Design', 'ECharts', 'Vite'] },
  { emoji: '⚙️', name: '后端', items: ['Spring Boot', 'MyBatis-Plus', 'JWT', 'Knife4j'] },
  { emoji: '🗄️', name: '数据库', items: ['MySQL 8', 'UTF8MB4', 'InnoDB'] },
  { emoji: '🤖', name: 'AI 集成', items: ['通义千问', 'OpenAI', 'DeepSeek', 'HTTP Client'] },
]
</script>

<style scoped>
/* ============================
   全局基础
   ============================ */
.landing-page {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: var(--text-primary);
  background: transparent;
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
}

.hidden-xs { display: inline; }
@media (max-width: 768px) { .hidden-xs { display: none; } }

.section-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ============================
   顶部导航
   ============================ */
.header {
  position: fixed;
  top: 0; left: 0; right: 0;
  height: 68px;
  z-index: 1000;
  transition: all 0.3s ease;
  background: transparent;
}

.header-scrolled {
  background: var(--header-surface);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-card);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 22px;
  background: var(--primary-gradient);
  box-shadow: var(--primary-shadow);
}

.logo-text {
  font-size: 22px;
  font-weight: 800;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo-sub {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 400;
  margin-left: 4px;
  opacity: 0.7;
}

.nav-links {
  display: flex;
  gap: 32px;
}

.nav-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s;
  position: relative;
}

.nav-links a:hover { color: var(--primary-color); }

.nav-links a::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--primary-gradient);
  transition: width 0.3s;
  border-radius: 2px;
}

.nav-links a:hover::after { width: 100%; }

.nav-actions { display: flex; align-items: center; gap: 8px; }

.nav-login-btn { color: var(--text-secondary); font-weight: 500; }

.nav-register-btn {
  background: var(--primary-gradient) !important;
  border: none !important;
  font-weight: 600;
  box-shadow: var(--primary-shadow);
}

/* ============================
   英雄区
   ============================ */
.hero-section {
  position: relative;
  padding: 160px 24px 100px;
  text-align: center;
  overflow: hidden;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: orbFloat 8s ease-in-out infinite;
}

.orb-1 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, #667eea 0%, transparent 70%);
  top: -10%; right: -5%;
}

.orb-2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #00b96b 0%, transparent 70%);
  bottom: -15%; left: -10%;
  animation-delay: -3s;
}

.orb-3 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #f5576c 0%, transparent 70%);
  top: 30%; left: 50%;
  animation-delay: -5s;
  opacity: 0.2;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(47, 84, 235, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(47, 84, 235, 0.035) 1px, transparent 1px);
  background-size: 60px 60px;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.05); }
  66% { transform: translate(-20px, 15px) scale(0.95); }
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  animation: fadeInUp 0.8s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: var(--surface-muted);
  border: 1px solid rgba(47, 84, 235, 0.12);
  border-radius: 50px;
  font-size: 13px;
  font-weight: 500;
  color: var(--primary-color);
  margin-bottom: 32px;
  box-shadow: var(--shadow-card);
}

.badge-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #00b96b;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.5); }
}

.hero-title {
  font-size: clamp(36px, 5vw, 54px);
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.02em;
  margin-bottom: 24px;
  color: var(--text-primary);
}

.gradient-text {
  background: linear-gradient(135deg, #2f54eb 0%, #00b96b 50%, #667eea 100%);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientShift 4s ease-in-out infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% center; }
  50% { background-position: 100% center; }
}

.hero-subtitle {
  font-size: clamp(15px, 2vw, 18px);
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 64px;
}

.hero-btn-primary {
  background: var(--primary-gradient) !important;
  border: none !important;
  font-weight: 700;
  font-size: 16px !important;
  padding: 0 40px !important;
  height: 48px !important;
  box-shadow: var(--primary-shadow);
  transition: all 0.3s ease;
}

.hero-btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(47, 84, 235, 0.4);
}

.hero-btn-outline {
  font-weight: 600;
  font-size: 16px !important;
  padding: 0 32px !important;
  height: 48px !important;
  border-color: var(--border-color) !important;
  background: var(--surface-muted) !important;
  color: var(--text-secondary) !important;
}

.hero-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  padding: 24px 40px;
  background: var(--surface-muted);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
}

.hero-stat { text-align: center; }

.stat-num {
  display: block;
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #2f54eb, #00b96b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(180deg, transparent, #e5e6eb, transparent);
}

/* ============================
   通用 Section 头部
   ============================ */
.section-header { text-align: center; margin-bottom: 64px; }

.section-tag {
  display: inline-block;
  padding: 6px 16px;
  background: linear-gradient(135deg, rgba(47, 84, 235, 0.08), rgba(0, 185, 107, 0.08));
  border-radius: 50px;
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 16px;
}

.section-title {
  font-size: clamp(28px, 3vw, 36px);
  font-weight: 800;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.section-desc {
  font-size: 16px;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
  line-height: 1.6;
}

/* ============================
   功能特色
   ============================ */
.features-section {
  padding: 100px 24px;
  background: transparent;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.feature-card {
  padding: 32px;
  background: linear-gradient(180deg, var(--surface-muted) 0%, var(--bg-card) 100%);
  border-radius: 20px;
  border: 1px solid var(--border-light);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(14px);
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--gradient, linear-gradient(135deg, #667eea, #764ba2));
  opacity: 0;
  transition: opacity 0.3s;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
  border-color: rgba(47, 84, 235, 0.12);
}

.feature-card:hover::before { opacity: 1; }

.feature-icon {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.feature-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 16px;
}

.feature-tag {
  display: inline-block;
  padding: 4px 12px;
  background: var(--bg-card-hover);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* ============================
   产品展示
   ============================ */
.showcase-section {
  padding: 100px 24px;
  background: transparent;
}

.showcase-layout {
  display: flex;
  gap: 48px;
  align-items: center;
}

.showcase-features {
  flex: 0 0 320px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.showcase-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.showcase-item:hover,
.showcase-item.active {
  background: var(--surface-muted);
  border-color: var(--border-color);
  box-shadow: var(--shadow-card);
}

.showcase-item-icon { font-size: 28px; flex-shrink: 0; }

.showcase-item-content h4 {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 4px;
  color: var(--text-primary);
}

.showcase-item-content p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.showcase-preview {
  flex: 1;
  perspective: 1000px;
}

/* 模拟窗口 */
.mockup-window {
  background: var(--bg-card);
  border-radius: 16px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.1), 0 4px 16px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  border: 1px solid var(--border-color);
  transform: rotateY(-2deg) rotateX(1deg);
  transition: transform 0.5s;
}

.mockup-window:hover {
  transform: rotateY(0) rotateX(0);
}

.mockup-titlebar {
  height: 44px;
  background: var(--bg-card-hover);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 12px;
}

.mockup-dots {
  display: flex;
  gap: 6px;
}

.mockup-dots i {
  width: 10px; height: 10px; border-radius: 50%;
}

.dot-red { background: #ff5f56; }
.dot-yellow { background: #ffbd2e; }
.dot-green { background: #27c93f; }

.mockup-url {
  font-size: 12px;
  color: var(--text-secondary);
  background: var(--surface-muted);
  padding: 4px 16px;
  border-radius: 6px;
  flex: 1;
  text-align: center;
}

.mockup-body {
  display: flex;
  height: 340px;
}

.mockup-sidebar {
  width: 160px;
  background: var(--bg-card-hover);
  border-right: 1px solid var(--border-color);
  padding: 16px 8px;
}

.mock-logo {
  font-size: 14px;
  font-weight: 700;
  padding: 8px 12px;
  margin-bottom: 16px;
  color: var(--primary-color);
}

.mock-menu-item {
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  margin-bottom: 2px;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.mock-menu-item.active {
  background: var(--menu-selected-bg);
  color: var(--primary-color);
  font-weight: 600;
}

.mockup-content {
  flex: 1;
  background: var(--bg-color);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mock-header-bar {
  height: 12px;
  width: 120px;
  background: var(--border-color);
  border-radius: 4px;
}

.mock-stats-row {
  display: flex; gap: 8px;
}

.mock-stat-card {
  flex: 1;
  height: 52px;
  background: var(--bg-card);
  border-radius: 10px;
  box-shadow: var(--shadow-card);
}

.mock-charts-row {
  display: flex;
  gap: 8px;
  flex: 1;
}

.mock-chart-card {
  flex: 1;
  background: var(--bg-card);
  border-radius: 10px;
  box-shadow: var(--shadow-card);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.mock-chart-line {
  width: 80%;
  height: 60%;
  background: linear-gradient(180deg, rgba(47, 84, 235, 0.15) 0%, rgba(47, 84, 235, 0) 100%);
  border-radius: 4px;
  position: relative;
}

.mock-chart-line::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, #2f54eb, #00b96b);
  border-radius: 2px;
}

.mock-chart-radar {
  width: 64px; height: 64px;
  border: 2px solid rgba(47, 84, 235, 0.2);
  clip-path: polygon(50% 0%, 100% 38%, 82% 100%, 18% 100%, 0% 38%);
  background: rgba(47, 84, 235, 0.06);
}

/* ============================
   技术架构
   ============================ */
.tech-section {
  padding: 100px 24px;
  background: transparent;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.tech-card {
  text-align: center;
  padding: 32px 24px;
  background: linear-gradient(180deg, var(--surface-muted) 0%, var(--bg-card) 100%);
  border-radius: 20px;
  border: 1px solid var(--border-light);
  transition: all 0.3s;
}

.tech-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.tech-emoji { font-size: 40px; margin-bottom: 16px; }

.tech-name {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.tech-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}

.tech-chip {
  padding: 4px 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* ============================
   CTA
   ============================ */
.cta-section {
  padding: 100px 24px;
  background: linear-gradient(135deg, #15213f 0%, #2f54eb 52%, #00b96b 100%);
  text-align: center;
}

.cta-content h2 {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 12px;
}

.cta-content p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 32px;
}

.cta-btn {
  background: #fff !important;
  color: #2f54eb !important;
  border: none !important;
  font-weight: 700;
  font-size: 16px !important;
  padding: 0 40px !important;
  height: 48px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}

/* ============================
   页脚
   ============================ */
.footer {
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.98) 0%, rgba(10, 15, 26, 1) 100%);
  padding: 60px 24px 30px;
  color: rgba(255, 255, 255, 0.6);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.footer-logo {
  font-size: 20px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 8px;
}

.footer-copyright { font-size: 13px; }

.footer-links {
  display: flex;
  gap: 64px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-col h5 {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.footer-col a {
  color: rgba(255, 255, 255, 0.5);
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s;
}

.footer-col a:hover { color: #fff; }

/* ============================
   响应式
   ============================ */
@media (max-width: 992px) {
  .nav-links { display: none; }
  .features-grid { grid-template-columns: repeat(2, 1fr); }
  .tech-grid { grid-template-columns: repeat(2, 1fr); }
  .showcase-layout { flex-direction: column; }
  .showcase-features { flex: none; width: 100%; flex-direction: row; overflow-x: auto; gap: 8px; }
  .showcase-item { min-width: 220px; flex-shrink: 0; }
}

@media (max-width: 576px) {
  .features-grid { grid-template-columns: 1fr; }
  .tech-grid { grid-template-columns: 1fr 1fr; }
  .hero-stats { flex-wrap: wrap; gap: 16px; padding: 16px; }
  .stat-divider { display: none; }
  .footer-container { flex-direction: column; gap: 32px; }
  .footer-links { gap: 32px; }
}
</style>
