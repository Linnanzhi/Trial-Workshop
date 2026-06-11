import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '../api/request'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('trial_token') || '')
    const userInfo = ref<any>(null)
    const isLoggedIn = ref(!!token.value)

    async function login(username: string, password: string) {
        const res: any = await authApi.login({ username, password })
        token.value = res.data.token
        userInfo.value = res.data
        isLoggedIn.value = true
        localStorage.setItem('trial_token', res.data.token)
        return res
    }

    async function fetchUserInfo() {
        const res: any = await authApi.getUserInfo()
        userInfo.value = res.data
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        isLoggedIn.value = false
        localStorage.removeItem('trial_token')
    }

    function setToken(newToken: string) {
        token.value = newToken
        isLoggedIn.value = true
        localStorage.setItem('trial_token', newToken)
    }

    function setUserInfo(info: any) {
        userInfo.value = info
    }

    return { token, userInfo, isLoggedIn, login, fetchUserInfo, logout, setToken, setUserInfo }
})
