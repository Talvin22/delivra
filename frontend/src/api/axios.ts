import axios from 'axios'
import { useAuthStore } from '@/store/authStore'

export const api = axios.create({ baseURL: '' })

// Attach JWT to every request
api.interceptors.request.use(config => {
  const token = useAuthStore.getState().token
  if (token) config.headers.set('Authorization', `Bearer ${token}`)
  return config
})

// Handle 401 → try refresh → retry once → logout
let refreshing = false
let queue: Array<() => void> = []

api.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true
      if (refreshing) {
        return new Promise(resolve => queue.push(() => resolve(api(original))))
      }
      refreshing = true
      try {
        const refreshToken = useAuthStore.getState().refreshToken
        if (!refreshToken) throw new Error('no refresh token')
        const { data } = await axios.get('/auth/refresh/token', { params: { token: refreshToken } })
        const profile = data.payload
        useAuthStore.getState().setAuth(profile)
        queue.forEach(fn => fn())
        queue = []
        return api(original)
      } catch {
        useAuthStore.getState().logout()
        return Promise.reject(err)
      } finally {
        refreshing = false
      }
    }
    return Promise.reject(err)
  },
)
