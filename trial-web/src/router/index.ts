import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        // 🌟 1. 新增：将官网首页配置为默认的根路径 '/'
        {
            path: '/',
            name: 'Home',
            component: () => import('../views/Home.vue'),
            meta: { requiresAuth: false } // 明确标识此页面不需要登录拦截
        },
        {
            path: '/login',
            name: 'Login',
            component: () => import('../views/Login.vue'),
            meta: { requiresAuth: false }
        },
        // 🌟 2. 修改：保留 Layout，但去掉 `redirect: '/dashboard'` 
        // 这样一来，访问 '/' 时会被上面的 Home.vue 捕获；
        // 而访问 '/dashboard' 等路径时，依然会顺利进入 Layout 组件的子路由，不会改变您原有的 URL 结构。
        {
            path: '/',
            component: () => import('../views/Layout.vue'),
            children: [
                {
                    path: 'dashboard',
                    name: 'Dashboard',
                    component: () => import('../views/Dashboard.vue'),
                    meta: { title: '学情仪表盘' } // 默认不写 requiresAuth: false 的都会被后面的守卫拦截
                },
                {
                    path: 'questions',
                    name: 'Questions',
                    component: () => import('../views/QuestionBank.vue'),
                    meta: { title: '智能题库' }
                },
                {
                    path: 'categories',
                    name: 'Categories',
                    component: () => import('../views/CategoryTag.vue'),
                    meta: { title: '分类与标签' }
                },
                {
                    path: 'exams',
                    name: 'Exams',
                    component: () => import('../views/ExamList.vue'),
                    meta: { title: '考试工坊' }
                },
                {
                    path: 'exams/create',
                    name: 'ExamCreate',
                    component: () => import('../views/ExamCreate.vue'),
                    meta: { title: '组卷中心' }
                },
                {
                    path: 'error-book',
                    name: 'ErrorBook',
                    component: () => import('../views/ErrorBook.vue'),
                    meta: { title: '错题本' }
                },
                {
                    path: 'ai-config',
                    name: 'AIConfig',
                    component: () => import('../views/AIConfig.vue'),
                    meta: { title: 'AI 配置' }
                },
                {
                    path: 'ai-stats',
                    name: 'AIUsageStats',
                    component: () => import('../views/AIUsageStats.vue'),
                    meta: { title: 'AI 使用统计' }
                },
            ]
        },
        {
            path: '/exam-room/:examId',
            name: 'ExamRoom',
            component: () => import('../views/ExamRoom.vue'),
            meta: { title: '模拟考场', requiresAuth: true }
        },
    ]
})

// 🌟 3. 路由守卫优化
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('trial_token')

    // 情况 A：如果要去的页面需要权限，且当前没有 token，并且不是去登录页
    if (to.meta.requiresAuth !== false && !token && to.path !== '/login') {
        next('/login')
    }
    // 情况 B (优化体验)：如果用户已经登录了，但又手动访问了 /login 页面，直接把他送回控制台
    else if (token && to.path === '/login') {
        next('/dashboard')
    }
    // 情况 C：已登录用户访问首页时，直接跳转到控制台
    else if (token && to.path === '/' && to.name === 'Home') {
        next('/dashboard')
    }
    // 情况 D：正常放行（包括未登录时访问首页 '/'）
    else {
        next()
    }
})

export default router
