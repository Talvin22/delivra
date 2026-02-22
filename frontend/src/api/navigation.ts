import { api } from './axios'
import type { DelivraResponse, NavigationSessionDTO } from '@/types/api'

export const navigationApi = {
  start: (taskId: number, body: {
    originLatitude: number
    originLongitude: number
    grossWeight?: number
    height?: number
  }) =>
    api.post<DelivraResponse<NavigationSessionDTO>>(`/tasks/${taskId}/navigation/start`, body),

  end: (taskId: number) =>
    api.post<DelivraResponse<NavigationSessionDTO>>(`/tasks/${taskId}/navigation/end`),

  getActive: (taskId: number) =>
    api.get<DelivraResponse<NavigationSessionDTO>>(`/tasks/${taskId}/navigation/active`),
}
