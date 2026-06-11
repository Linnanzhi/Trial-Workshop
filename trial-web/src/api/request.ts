import axios from 'axios'
import { Message } from '@arco-design/web-vue'
import router from '../router'

const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
    withCredentials: true, // 允许携带 Cookie/Session
})

// 请求拦截器 - 自动附加 Token
request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('trial_token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器 - 统一处理错误
request.interceptors.response.use(
    (response) => {
        const res = response.data
        if (res.code !== 200) {
            Message.error(res.message || '请求失败')
            if (res.code === 401) {
                localStorage.removeItem('trial_token')
                router.push('/login')
            }
            return Promise.reject(new Error(res.message))
        }
        return res
    },
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('trial_token')
            router.push('/login')
            Message.error('登录已过期，请重新登录')
        } else {
            Message.error(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request

// ======= API 接口定义 =======

// 认证
export const authApi = {
    login: (data: any) => request.post('/auth/login', data),
    register: (data: any) => request.post('/auth/register', data),
    getUserInfo: () => request.get('/auth/userinfo'),
    getCaptcha: () => request.get('/captcha'),
}

// 题库
export const questionApi = {
    list: (params: any) => request.get('/questions', { params }),
    detail: (id: number) => request.get(`/questions/${id}`),
    add: (data: any) => request.post('/questions', data),
    update: (data: any) => request.put('/questions', data),
    remove: (id: number) => request.delete(`/questions/${id}`),
    batchRemove: (ids: number[]) => request.delete('/questions/batch', { data: ids }),
    batchUpdateCategory: (ids: number[], categoryId: number | null) => 
        request.put('/questions/batch-category', { ids, categoryId }),
    batchUpdateTags: (ids: number[], tagIds: number[]) => 
        request.put('/questions/batch-tags', { ids, tagIds }),
    batchImport: (data: any) => request.post('/questions/batch-import', data),
    stats: () => request.get('/questions/stats'),
    uploadFile: (file: File) => {
        const formData = new FormData()
        formData.append('file', file)
        return request.post('/questions/upload-file', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
            timeout: 60000,
        })
    },
    parseText: (text: string, config?: any) => request.post('/questions/parse-text', { text, ...config }),
}

// 分类与标签
export const categoryTagApi = {
    listCategories: () => request.get('/category-tag/categories'),
    addCategory: (data: any) => request.post('/category-tag/categories', data),
    updateCategory: (data: any) => request.put('/category-tag/categories', data),
    deleteCategory: (id: number) => request.delete(`/category-tag/categories/${id}`),
    listTags: () => request.get('/category-tag/tags'),
    addTag: (data: any) => request.post('/category-tag/tags', data),
    updateTag: (data: any) => request.put('/category-tag/tags', data),
    deleteTag: (id: number) => request.delete(`/category-tag/tags/${id}`),
}

// 考试
export const examApi = {
    create: (data: any) => request.post('/exams', data),
    list: (params: any) => request.get('/exams', { params }),
    detail: (id: number) => request.get(`/exams/${id}`),
    start: (id: number) => request.post(`/exams/${id}/start`),
    saveProgress: (recordId: number, data: any) => request.post(`/exams/records/${recordId}/save`, data),
    submit: (data: any) => request.post('/exams/submit', data),
    listRecords: (params: any) => request.get('/exams/records', { params }),
    recordDetail: (id: number) => request.get(`/exams/records/${id}`),
    remove: (id: number) => request.delete(`/exams/${id}`),
    share: (id: number) => request.post(`/exams/${id}/share`),
    importShared: (shareCode: string) => request.post('/exams/import', null, { params: { shareCode } }),
}

// 学情分析
export const analysisApi = {
    dashboard: () => request.get('/analysis/dashboard'),
    errorBook: (params: any) => request.get('/analysis/error-book', { params }),
    todayReview: () => request.get('/analysis/today-review'),
    reviewFeedback: (errorBookId: number, mastered: boolean) =>
        request.post('/analysis/review-feedback', null, { params: { errorBookId, mastered } }),
    radar: () => request.get('/analysis/radar'),
}

// AI 配置和解析（从 ai.ts 导入）
export { aiConfigApi, aiParseApi } from './ai'
