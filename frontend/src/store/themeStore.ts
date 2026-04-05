import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface ThemeState {
  dark: boolean
  toggle: () => void
}

export const useThemeStore = create<ThemeState>()(
  persist(
    (set) => ({
      dark: true,
      toggle: () => set(s => {
        const next = !s.dark
        document.documentElement.classList.toggle('dark', next)
        return { dark: next }
      }),
    }),
    { name: 'delivra-theme' },
  ),
)

export function initTheme() {
  try {
    const stored = localStorage.getItem('delivra-theme')
    const dark = stored ? (JSON.parse(stored).state?.dark !== false) : true
    document.documentElement.classList.toggle('dark', dark)
  } catch {
    document.documentElement.classList.add('dark')
  }
}