import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { MapPin, Clock, ChevronRight, RefreshCw } from 'lucide-react'
import { tasksApi } from '@/api/tasks'
import { useAuthStore } from '@/store/authStore'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { FullScreenLoader } from '@/components/ui/Spinner'
import { formatDateTime, TASK_STATUS_LABEL } from '@/lib/formatters'
import type { DeliveryTaskDTO, DeliveryTaskStatus } from '@/types/api'
import { cn } from '@/lib/utils'

const STATUS_ORDER: DeliveryTaskStatus[] = ['IN_PROGRESS', 'PENDING', 'COMPLETED', 'CANCELED']

const BADGE_VARIANT: Record<DeliveryTaskStatus, 'brand' | 'warning' | 'success' | 'danger'> = {
  IN_PROGRESS: 'brand',
  PENDING:     'warning',
  COMPLETED:   'success',
  CANCELED:    'danger',
}

export function DriverTaskListPage() {
  const navigate = useNavigate()
  const user = useAuthStore(s => s.user)

  const { data, isLoading, refetch, isRefetching } = useQuery({
    queryKey: ['driver-tasks', user?.id],
    queryFn: async () => {
      const res = await tasksApi.search({ userId: user?.id }, 0, 100)
      return res.data.payload.content
    },
    refetchInterval: 30_000,
    enabled: !!user?.id,
  })

  const sorted = [...(data ?? [])].sort(
    (a, b) => STATUS_ORDER.indexOf(a.status) - STATUS_ORDER.indexOf(b.status),
  )

  if (isLoading) return <FullScreenLoader />

  return (
    <div className="p-4 md:p-6 max-w-2xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-text-primary">Мои задачи</h1>
          <p className="text-sm text-text-secondary mt-0.5">Привет, {user?.username} 👋</p>
        </div>
        <Button variant="ghost" size="icon" onClick={() => refetch()} loading={isRefetching}>
          <RefreshCw size={16} />
        </Button>
      </div>

      {sorted.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 text-text-muted">
          <MapPin size={40} className="mb-3 opacity-40" />
          <p className="text-sm">Задач пока нет</p>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {sorted.map(task => (
            <TaskCard key={task.id} task={task} onClick={() => navigate(`/driver/tasks/${task.id}`)} />
          ))}
        </div>
      )}
    </div>
  )
}

function TaskCard({ task, onClick }: { task: DeliveryTaskDTO; onClick: () => void }) {
  const isActive = task.status === 'IN_PROGRESS'
  return (
    <button
      onClick={onClick}
      className={cn(
        'w-full text-left bg-bg-surface border rounded-lg p-4 transition-all hover:border-brand/50 active:scale-[0.99]',
        isActive ? 'border-brand/40 shadow-lg shadow-brand/5' : 'border-bg-border',
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-xs font-mono text-text-muted">#{task.id}</span>
            <Badge variant={BADGE_VARIANT[task.status]}>{TASK_STATUS_LABEL[task.status]}</Badge>
          </div>
          <p className="text-sm font-medium text-text-primary truncate">{task.address}</p>
          <div className="flex items-center gap-1 mt-1.5 text-xs text-text-muted">
            <Clock size={11} />
            <span>{formatDateTime(task.created)}</span>
          </div>
        </div>
        <ChevronRight size={16} className="text-text-muted mt-1 flex-shrink-0" />
      </div>
      {isActive && (
        <div className="mt-2 pt-2 border-t border-brand/20">
          <span className="text-xs text-brand font-medium animate-pulse2">● Маршрут активен</span>
        </div>
      )}
    </button>
  )
}
