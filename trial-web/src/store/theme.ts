import { defineStore } from 'pinia'
import { ref } from 'vue'

export type ThemeMode = 'light' | 'dark'

const THEME_STORAGE_KEY = 'trial_theme'

function getPreferredTheme(): ThemeMode {
    if (typeof window === 'undefined') {
        return 'light'
    }

    const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY)
    if (storedTheme === 'light' || storedTheme === 'dark') {
        return storedTheme
    }

    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

function applyTheme(theme: ThemeMode) {
    if (typeof document === 'undefined') {
        return
    }

    document.documentElement.setAttribute('data-theme', theme)
    document.documentElement.style.colorScheme = theme

    if (document.body) {
        if (theme === 'dark') {
            document.body.setAttribute('arco-theme', 'dark')
        } else {
            document.body.removeAttribute('arco-theme')
        }
    }
}

export const useThemeStore = defineStore('theme', () => {
    const theme = ref<ThemeMode>('light')

    function initializeTheme() {
        theme.value = getPreferredTheme()
        applyTheme(theme.value)
    }

    function setTheme(nextTheme: ThemeMode) {
        theme.value = nextTheme

        if (typeof window !== 'undefined') {
            window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme)
        }

        applyTheme(nextTheme)
    }

    function toggleTheme() {
        setTheme(theme.value === 'dark' ? 'light' : 'dark')
    }

    return { theme, initializeTheme, setTheme, toggleTheme }
})
