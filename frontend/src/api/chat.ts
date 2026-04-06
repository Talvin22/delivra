import { api } from './axios'
import type { DelivraResponse, PaginationResponse, ChatMessageDTO } from '@/types/api'

export const chatApi = {
  getHistory: (taskId: number, page = 0, limit = 50) =>
    api.get<DelivraResponse<PaginationResponse<ChatMessageDTO>>>(`/tasks/${taskId}/chat`, {
      params: { page, limit },
    }),

  markRead: (taskId: number) =>
    api.patch(`/tasks/${taskId}/chat/read`),

  uploadFile: (taskId: number, file: File) => {
    const form = new FormData()
    form.append('file', file)
    return api.post<ChatMessageDTO>(`/tasks/${taskId}/chat/files`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
