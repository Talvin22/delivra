import { api } from './axios'
import type { DelivraResponse, PaginationResponse, ChatMessageDTO } from '@/types/api'

export const chatApi = {
  getHistory: (taskId: number, page = 0, limit = 50) =>
    api.get<DelivraResponse<PaginationResponse<ChatMessageDTO>>>(`/tasks/${taskId}/chat`, {
      params: { page, limit },
    }),

  markRead: (taskId: number) =>
    api.patch(`/tasks/${taskId}/chat/read`),
}
