import { api } from './axios'
import type { DelivraResponse, UserProfileDTO } from '@/types/api'

export const authApi = {
  login: (email: string, password: string) =>
    api.post<DelivraResponse<UserProfileDTO>>('/auth/login', { email, password }),

  refresh: (token: string) =>
    api.get<DelivraResponse<UserProfileDTO>>('/auth/refresh/token', { params: { token } }),

  forgotPassword: (email: string) =>
    api.post<DelivraResponse<string>>('/auth/password/forgot', { email }),

  resetPassword: (token: string, newPassword: string, confirmPassword: string) =>
    api.post<DelivraResponse<string>>('/auth/password/reset', { token, newPassword, confirmPassword }),

  verifyEmail: (token: string) =>
    api.post<DelivraResponse<string>>('/auth/verify-email', null, { params: { token } }),
}
