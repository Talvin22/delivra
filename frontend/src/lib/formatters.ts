import { format, formatDistanceToNow } from 'date-fns'
import { ru } from 'date-fns/locale'

export function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return h > 0 ? `${h}ч ${m}мин` : `${m}мин`
}

export function formatDistance(meters: number): string {
  return meters >= 1000 ? `${(meters / 1000).toFixed(1)} км` : `${Math.round(meters)} м`
}

export function formatDateTime(iso: string): string {
  try { return format(new Date(iso), 'dd.MM.yyyy HH:mm', { locale: ru }) }
  catch { return iso }
}

export function formatTime(iso: string): string {
  try { return format(new Date(iso), 'HH:mm', { locale: ru }) }
  catch { return '' }
}

export function formatRelative(iso: string): string {
  try { return formatDistanceToNow(new Date(iso), { addSuffix: true, locale: ru }) }
  catch { return '' }
}

import type { DeliveryTaskStatus } from '@/types/api'

export const TASK_STATUS_LABEL: Record<DeliveryTaskStatus, string> = {
  PENDING:     'Ожидает',
  IN_PROGRESS: 'В пути',
  COMPLETED:   'Выполнено',
  CANCELED:    'Отменено',
}

export const TASK_STATUS_COLOR: Record<DeliveryTaskStatus, string> = {
  PENDING:     'text-warning bg-warning/10 border-warning/30',
  IN_PROGRESS: 'text-brand bg-brand/10 border-brand/30',
  COMPLETED:   'text-success bg-success/10 border-success/30',
  CANCELED:    'text-danger bg-danger/10 border-danger/30',
}
