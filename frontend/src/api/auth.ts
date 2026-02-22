import { api } from './axios'
import type { DelivraResponse, UserProfileDTO } from '@/types/api'

export const authApi = {
  login: (email: string, password: string) =>
    api.post<DelivraResponse<UserProfileDTO>>('/auth/login', { email, password }),

  refresh: (token: string) =>
    api.get<DelivraResponse<UserProfileDTO>>('/auth/refresh/token', { params: { token } }),
}
