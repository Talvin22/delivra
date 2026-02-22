import { api } from './axios'
import type { DelivraResponse, PaginationResponse, DeliveryTaskDTO, RouteDTO } from '@/types/api'

export const tasksApi = {
  getAll: (page = 0, limit = 20) =>
    api.get<DelivraResponse<PaginationResponse<DeliveryTaskDTO>>>('/tasks/all', { params: { page, limit } }),

  search: (body: object, page = 0, limit = 20) =>
    api.post<DelivraResponse<PaginationResponse<DeliveryTaskDTO>>>('/tasks/search', body, { params: { page, limit } }),

  getById: (id: number) =>
    api.get<DelivraResponse<DeliveryTaskDTO>>(`/tasks/${id}`),

  create: (body: { driverId?: number; address: string; latitude?: number; longitude?: number }) =>
    api.post<DelivraResponse<DeliveryTaskDTO>>('/tasks/create', body),

  update: (id: number, body: { address?: string; latitude?: number; longitude?: number; status?: string }) =>
    api.put<DelivraResponse<DeliveryTaskDTO>>(`/tasks/update/${id}`, body),

  delete: (id: number) =>
    api.delete(`/tasks/${id}`),

  getRoute: (id: number, body: { originLatitude: number; originLongitude: number }) =>
    api.post<DelivraResponse<RouteDTO>>(`/tasks/${id}/route`, body),
}
