<template>
  <a-layout class="app-layout" :class="{ 'layout-collapsed': collapsed }">
    <!-- 侧边栏 -->
    <a-layout-sider :width="252" :collapsed-width="76" collapsible :collapsed="collapsed" @collapse="handleCollapse"
                    breakpoint="lg" class="app-sider">
      <div class="sider-inner">
        <div class="sider-logo" @click="$router.push('/dashboard')">
          <div class="logo-icon">⚔️</div>
          <div v-if="!collapsed" class="logo-copy">
            <span class="logo-text">试炼坊</span>
            <span class="logo-subtitle">智能题库平台</span>
          </div>
        </div>

        <div class="sider-menu-wrap">
          <a-menu :selected-keys="[currentRoute]" @menu-item-click="handleMenuClick" class="app-menu">
            <a-menu-item key="dashboard">
              <template #icon><icon-dashboard /></template>
              学情仪表盘
            </a-menu-item>
            <a-menu-item key="questions">
              <template #icon><icon-book /></template>
              智能题库
            </a-menu-item>
            <a-menu-item key="categories">
              <template #icon><icon-tags /></template>
              分类与标签
            </a-menu-item>
            <a-menu-item key="exams">
              <template #icon><icon-edit /></template>
              考试工坊
            </a-menu-item>
            <a-menu-item key="error-book">
              <template #icon><icon-bug /></template>
              错题本
            </a-menu-item>
            <a-sub-menu key="ai">
              <template #icon><icon-robot /></template>
              <template #title>AI 智能解析</template>
              <a-menu-item key="ai-config">AI 配置</a-menu-item>
              <a-menu-item key="ai-stats">使用统计</a-menu-item>
            </a-sub-menu>
          </a-menu>
        </div>

        <div v-if="!collapsed" class="sider-footer">
          <span class="sider-footer-label">让学习更专注</span>
          <span class="sider-footer-value">题库 · 组卷 · AI</span>
        </div>
      </div>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout>
      <!-- 顶部导航 -->
      <a-layout-header class="app-header">
        <div class="header-left">
          <a-breadcrumb>
            <a-breadcrumb-item>试炼坊</a-breadcrumb-item>
            <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="header-right">
          <a-tooltip :content="isDarkTheme ? '切换到白天' : '切换到黑夜'">
            <a-button class="theme-toggle-btn" @click="toggleTheme">
              <icon-sun-fill v-if="isDarkTheme" />
              <icon-moon-fill v-else />
            </a-button>
          </a-tooltip>
          <a-dropdown>
            <div class="user-avatar-wrap">
              <a-avatar :size="32" style="background: var(--primary-gradient);">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </div>
            <template #content>
              <a-doption @click="handleLogout">
                <template #icon><icon-export /></template>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 页面内容 -->
      <a-layout-content class="app-content">
        <div class="content-shell">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { useThemeStore } from '../store/theme'
import {
  IconDashboard, IconBook, IconTags, IconEdit, IconBug, IconExport, IconRobot, IconSunFill, IconMoonFill
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const collapsed = ref(false)

const currentRoute = computed(() => {
  const path = route.path.split('/')[1] || 'dashboard'
  return path
})

const currentTitle = computed(() => {
  return (route.meta.title as string) || '首页'
})

const isDarkTheme = computed(() => themeStore.theme === 'dark')

onMounted(() => {
  userStore.fetchUserInfo().catch(() => {})
})

function handleCollapse(value: boolean) {
  collapsed.value = value
}

function handleMenuClick(key: string) {
  router.push(`/${key}`)
}

function toggleTheme() {
  themeStore.toggleTheme()
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-layout.layout-collapsed .app-header,
.app-layout.layout-collapsed .app-content {
  margin-left: 76px;
}

.app-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow: hidden;
  padding: 12px;
  background: transparent !important;
}

.sider-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 8px;
  border-radius: 28px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-card);
  position: relative;
  overflow: hidden;
}

.sider-inner::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 140px;
  background: linear-gradient(180deg, var(--surface-highlight) 0%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
}

.sider-logo {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  cursor: pointer;
  border: 1px solid var(--border-color);
  border-radius: 22px;
  background: linear-gradient(180deg, var(--bg-card-hover) 0%, var(--bg-card) 100%);
  margin-bottom: 16px;
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.sider-logo:hover {
  transform: translateY(-1px);
  border-color: rgba(47, 84, 235, 0.24);
  box-shadow: var(--shadow-card);
}

.logo-icon {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 24px;
  background: var(--primary-gradient);
  box-shadow: 0 10px 24px rgba(47, 84, 235, 0.22);
  flex-shrink: 0;
}

.logo-copy {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.sider-menu-wrap {
  flex: 1;
  overflow-y: auto;
}

.sider-footer {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 20px;
  border: 1px solid var(--border-color);
  background: linear-gradient(135deg, var(--menu-selected-bg) 0%, var(--bg-card-hover) 100%);
}

.sider-footer-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.sider-footer-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.app-header {
  margin-left: 252px;
  min-height: 74px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  background: var(--header-surface) !important;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 99;
  transition: margin-left 0.25s ease;
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

:deep(.header-left .arco-breadcrumb) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--surface-muted);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

:deep(.header-left .arco-breadcrumb-item),
:deep(.header-left .arco-breadcrumb-item a),
:deep(.header-left .arco-breadcrumb-item:last-child) {
  font-size: 13px;
  color: var(--text-secondary) !important;
}

:deep(.header-left .arco-breadcrumb-item:last-child) {
  color: var(--text-primary) !important;
  font-weight: 700;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.theme-toggle-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 50%;
  border-color: var(--border-color);
  background: var(--bg-card);
  color: var(--text-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.theme-toggle-btn:hover {
  background: var(--bg-card-hover);
  border-color: var(--primary-color);
}

.user-avatar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 8px 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--surface-muted);
  transition: background 0.3s, border-color 0.3s, transform 0.3s;
}

.user-avatar-wrap:hover {
  background: var(--menu-selected-bg);
  border-color: rgba(47, 84, 235, 0.18);
  transform: translateY(-1px);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.app-content {
  margin-left: 252px;
  padding: 28px;
  min-height: calc(100vh - 60px);
  transition: margin-left 0.25s ease;
}

.content-shell {
  width: min(100%, var(--page-max-width));
  margin: 0 auto;
}

.app-layout.layout-collapsed .sider-logo {
  justify-content: center;
  padding: 14px 10px;
}

.app-layout.layout-collapsed .logo-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  font-size: 22px;
}

:deep(.app-menu) {
  background: transparent !important;
  padding: 4px 0;
}

:deep(.app-menu .arco-menu-inner) {
  padding: 0;
}

:deep(.app-menu .arco-menu-item),
:deep(.app-menu .arco-menu-inline-header) {
  min-height: 46px;
  line-height: 46px;
  margin: 6px 0;
  padding-left: 16px !important;
  border-radius: 16px;
  color: var(--text-secondary) !important;
  transition: all 0.25s ease;
}

:deep(.app-menu .arco-menu-item:hover:not(.arco-menu-selected)),
:deep(.app-menu .arco-menu-inline-header:hover) {
  background: var(--menu-hover-bg) !important;
  color: var(--text-primary) !important;
}

:deep(.app-menu .arco-menu-item.arco-menu-selected) {
  background: linear-gradient(135deg, var(--menu-selected-bg) 0%, rgba(47, 84, 235, 0.04) 100%) !important;
  color: var(--accent-blue) !important;
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(47, 84, 235, 0.08), 0 8px 18px rgba(47, 84, 235, 0.08);
  transform: translateX(2px);
}

:deep(.app-menu .arco-menu-icon),
:deep(.app-menu .arco-icon) {
  font-size: 18px;
}

:deep(.app-menu .arco-menu-icon) {
  margin-right: 12px;
}

:deep(.app-menu.arco-menu-collapse .arco-menu-item),
:deep(.app-menu.arco-menu-collapse .arco-menu-inline-header) {
  padding: 0 !important;
  justify-content: center;
}

:deep(.app-menu.arco-menu-collapse .arco-menu-icon) {
  margin-right: 0;
}

:deep(.arco-layout-sider-trigger) {
  left: 18px;
  right: 18px;
  bottom: 18px;
  width: auto;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  background: var(--bg-card-hover);
  color: var(--text-primary);
  box-shadow: var(--shadow-card);
}

:deep(.arco-layout-sider-trigger:hover) {
  border-color: var(--primary-color);
}
</style>
