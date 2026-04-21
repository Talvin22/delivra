import { api } from './axios'
import type { DelivraResponse, PaginationResponse, TruckProfileDTO, UserDTO, UserSearchDTO } from '@/types/api'

export const usersApi = {
  getAll: (page = 0, limit = 20) =>
    api.get<DelivraResponse<PaginationResponse<UserSearchDTO>>>('/users/all', { params: { page, limit } }),

  search: (body: object, page = 0, limit = 20) =>
    api.post<DelivraResponse<PaginationResponse<UserSearchDTO>>>('/users/search', body, { params: { page, limit } }),

  getById: (id: number) =>
    api.get<DelivraResponse<UserDTO>>(`/users/${id}`),

  create: (body: { username: string; email: string; password: string }) =>
    api.post<DelivraResponse<UserDTO>>('/users/create', body),

  update: (id: number, body: { username?: string; email?: string }) =>
    api.put<DelivraResponse<UserDTO>>(`/users/${id}`, body),

  delete: (id: number) =>
    api.delete(`/users/${id}`),

  updateRoles: (id: number, roles: string[]) =>
    api.put<DelivraResponse<UserDTO>>(`/users/${id}/roles`, { roles }),

  getTruckProfile: () =>
    api.get<DelivraResponse<TruckProfileDTO>>('/users/me/truck-profile'),

  updateTruckProfile: (body: TruckProfileDTO) =>
    api.put<DelivraResponse<TruckProfileDTO>>('/users/me/truck-profile', body),
}
