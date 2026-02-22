import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Trash2, Search, Filter } from 'lucide-react'
import { tasksApi } from '@/api/tasks'
import { Badge } from '@/components/ui/Badge'
import { FullScreenLoader } from '@/components/ui/Spinner'
import { formatDateTime, TASK_STATUS_LABEL } from '@/lib/formatters'
import type { DeliveryTaskStatus } from '@/types/api'
import { cn } from '@/lib/utils'

const STATUS_BADGE: Record<DeliveryTaskStatus, 'brand'|'warning'|'success'|'danger'> = {
  IN_PROGRESS: 'brand', PENDING: 'warning', COMPLETED: 'success', CANCELED: 'danger',
}

export function TasksAdminPage() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState<DeliveryTaskStatus | 'ALL'>('ALL')

  const { data, isLoading } = useQuery({
    queryKey: ['admin-tasks'],
    queryFn: () => tasksApi.getAll(0, 500).then(r => r.data.payload.content),
    refetchInterval: 30_000,
  })

  const deleteTask = useMutation({
    mutationFn: (id: number) => tasksApi.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-tasks'] }),
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      tasksApi.update(id, { status }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-tasks'] }),
  })

  const filtered = (data ?? []).filter(t => {
    const matchSearch = !search || t.address.toLowerCase().includes(search.toLowerCase()) ||
      String(t.id).includes(search)
    const matchStatus = filterStatus === 'ALL' || t.status === filterStatus
    return matchSearch && matchStatus
  })

  const STATUSES: (DeliveryTaskStatus | 'ALL')[] = ['ALL', 'IN_PROGRESS', 'PENDING', 'COMPLETED', 'CANCELED']

  return (
    <div className="h-full overflow-y-auto p-4 md:p-6 max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold text-text-primary">Задачи</h1>
        <span className="text-sm text-text-muted">{filtered.length} записей</span>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-4">
        <div className="relative">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
          <input
            className="bg-bg-base border border-bg-border rounded-md pl-8 pr-3 py-2 text-sm text-text-primary placeholder:text-text-muted outline-none focus:border-brand transition-colors w-full sm:w-60"
            placeholder="Поиск по адресу, ID..."
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
        <div className="flex gap-1 flex-wrap">
          {STATUSES.map(s => (
            <button
              key={s}
              onClick={() => setFilterStatus(s)}
              className={cn(
                'text-xs px-3 py-1.5 rounded-md transition-colors',
                filterStatus === s
                  ? 'bg-brand/10 text-brand font-medium'
                  : 'text-text-secondary hover:bg-bg-raised',
              )}
            >
              {s === 'ALL' ? 'Все' : TASK_STATUS_LABEL[s]}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? <FullScreenLoader /> : (
        <div className="bg-bg-surface border border-bg-border rounded-lg overflow-hidden">
          <div className="hidden md:grid grid-cols-[50px_1fr_100px_80px_100px_60px] gap-3 px-4 py-2.5 border-b border-bg-border">
            {['ID', 'Адрес', 'Статус', 'Водитель', 'Создано', ''].map(h => (
              <span key={h} className="text-xs text-text-muted font-medium uppercase tracking-wide">{h}</span>
            ))}
          </div>

          {filtered.length === 0 ? (
            <div className="text-center py-12 text-text-muted text-sm">Нет задач</div>
          ) : filtered.map(task => (
            <div
              key={task.id}
              className="grid grid-cols-[50px_1fr] md:grid-cols-[50px_1fr_100px_80px_100px_60px] gap-3 px-4 py-3 border-b border-bg-border hover:bg-bg-raised transition-colors items-center"
            >
              <span className="text-xs font-mono text-text-muted">#{task.id}</span>
              <p className="text-sm text-text-primary truncate">{task.address}</p>
              <div className="hidden md:block">
                <Badge variant={STATUS_BADGE[task.status]}>{TASK_STATUS_LABEL[task.status]}</Badge>
              </div>
              <span className="hidden md:block text-xs text-text-muted">
                {task.userId ? `#${task.userId}` : '—'}
              </span>
              <span className="hidden md:block text-xs text-text-muted">{formatDateTime(task.created)}</span>
              <div className="hidden md:flex justify-end">
                <button
                  onClick={() => deleteTask.mutate(task.id)}
                  className="p-1.5 rounded text-text-muted hover:text-danger hover:bg-danger/10 transition-colors"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
