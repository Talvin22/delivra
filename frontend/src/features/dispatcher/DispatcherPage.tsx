import { useState, useEffect, useCallback } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet'
import L from 'leaflet'
import { Plus, MessageSquare, ChevronLeft, RefreshCw } from 'lucide-react'
import 'leaflet/dist/leaflet.css'
import { tasksApi } from '@/api/tasks'
import { usersApi } from '@/api/users'
import { useWsStore } from '@/store/wsStore'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { ChatPanel } from '@/components/chat/ChatPanel'
import { TaskFormModal } from './TaskFormModal'
import { formatDateTime, TASK_STATUS_LABEL } from '@/lib/formatters'
import type { DeliveryTaskDTO, DeliveryTaskStatus, NavigationEventDTO, DriverPosition } from '@/types/api'
import { cn } from '@/lib/utils'

const STATUS_BADGE: Record<DeliveryTaskStatus, 'brand'|'warning'|'success'|'danger'> = {
  IN_PROGRESS: 'brand', PENDING: 'warning', COMPLETED: 'success', CANCELED: 'danger',
}

function makeDriverMapIcon(label: string) {
  return L.divIcon({
    className: '',
    html: `<div style="display:flex;flex-direction:column;align-items:center;gap:2px">
      <div style="width:32px;height:32px;background:#6c8aff;border-radius:50%;border:2px solid white;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:0 2px 8px rgba(108,138,255,0.5)">🚛</div>
      <div style="background:rgba(26,29,46,0.9);color:#e2e8f0;font-size:10px;font-weight:600;padding:1px 5px;border-radius:4px;white-space:nowrap;border:1px solid #2d3148">${label}</div>
    </div>`,
    iconSize: [60, 48],
    iconAnchor: [30, 48],
  })
}

function makeDestIcon() {
  return L.divIcon({
    className: '',
    html: `<div style="background:#ff4d6d;width:14px;height:14px;border-radius:50%;border:2px solid white;box-shadow:0 1px 4px rgba(255,77,109,0.6)"></div>`,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
  })
}

export function DispatcherPage() {
  const qc = useQueryClient()
  const { subscribe } = useWsStore()

  const [driverPositions, setDriverPositions] = useState<Map<number, DriverPosition>>(new Map())
  const [chatTaskId, setChatTaskId] = useState<number | null>(null)
  const [taskFormOpen, setTaskFormOpen] = useState(false)
  const [filterStatus, setFilterStatus] = useState<DeliveryTaskStatus | 'ALL'>('ALL')
  const [panelOpen, setPanelOpen] = useState(true)

  const { data: tasks, isLoading, refetch } = useQuery({
    queryKey: ['dispatcher-tasks'],
    queryFn: async () => {
      const res = await tasksApi.getAll(0, 100)
      return res.data.payload.content
    },
    refetchInterval: 30_000,
  })

  const { data: drivers } = useQuery({
    queryKey: ['drivers'],
    queryFn: async () => {
      const res = await usersApi.search({ role: 'DRIVER' }, 0, 100)
      return res.data.payload.content
    },
  })

  // Subscribe to both /position (on-route) and /route (off-route reroute) for all IN_PROGRESS tasks
  useEffect(() => {
    if (!tasks) return
    const inProgress = tasks.filter(t => t.status === 'IN_PROGRESS')

    const handlePosition = (taskId: number) => (data: unknown) => {
      const ev = data as NavigationEventDTO
      setDriverPositions(prev => {
        const next = new Map(prev)
        next.set(taskId, {
          taskId,
          userId: 0,
          username: `#${taskId}`,
          lat: ev.latitude,
          lng: ev.longitude,
          onRoute: ev.onRoute,
          updatedAt: Date.now(),
        })
        return next
      })
    }

    const unsubs = inProgress.flatMap(task => [
      subscribe(`/topic/navigation/${task.id}/position`, handlePosition(task.id)),
      subscribe(`/topic/navigation/${task.id}/route`, handlePosition(task.id)),
    ])
    return () => unsubs.forEach(u => u())
  }, [tasks, subscribe])

  const handleTaskUpdate = useCallback(() => {
    qc.invalidateQueries({ queryKey: ['dispatcher-tasks'] })
  }, [qc])

  const filtered = (tasks ?? []).filter(t => filterStatus === 'ALL' || t.status === filterStatus)
  const STATUSES: (DeliveryTaskStatus | 'ALL')[] = ['ALL', 'IN_PROGRESS', 'PENDING', 'COMPLETED', 'CANCELED']

  return (
    // absolute inset-0: fills the relative main container completely
    <div className="absolute inset-0">

      {/* ── Map layer (always full screen behind panel) ── */}
      <div className="absolute inset-0">
        <MapContainer
          center={[50.45, 30.52]}
          zoom={12}
          className="w-full h-full"
          zoomControl={false}
        >
          <TileLayer
            url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
            attribution="&copy; OpenStreetMap &copy; CARTO"
            maxZoom={19}
          />

          {Array.from(driverPositions.values()).map(dp => (
            <Marker key={dp.taskId} position={[dp.lat, dp.lng]} icon={makeDriverMapIcon(`#${dp.taskId}`)}>
              <Popup>
                <div className="text-xs">
                  <p className="font-semibold">Задача #{dp.taskId}</p>
                  <p className={dp.onRoute ? 'text-green-600' : 'text-red-600'}>
                    {dp.onRoute ? 'На маршруте' : 'Вне маршрута'}
                  </p>
                </div>
              </Popup>
            </Marker>
          ))}

          {(tasks ?? []).filter(t => t.latitude && t.longitude && t.status !== 'COMPLETED').map(t => (
            <Marker key={`dest-${t.id}`} position={[t.latitude!, t.longitude!]} icon={makeDestIcon()}>
              <Popup>
                <div className="text-xs">
                  <p className="font-semibold">#{t.id}</p>
                  <p>{t.address}</p>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>
      </div>

      {/* ── Right panel with tab handle attached to its left edge ── */}
      <div className={cn(
        'absolute top-0 bottom-0 right-0 z-[500]',
        'transition-transform duration-200',
        panelOpen ? 'translate-x-0' : 'translate-x-full',
      )}>
        {/* Tab handle — sticks out to the left of the panel */}
        <button
          onClick={() => setPanelOpen(p => !p)}
          className="absolute left-0 top-20 -translate-x-full bg-bg-surface border border-r-0 border-bg-border rounded-l-lg px-1.5 py-3 flex items-center text-text-secondary hover:text-brand transition-colors shadow-lg"
          title={panelOpen ? 'Скрыть панель' : 'Показать панель'}
        >
          <ChevronLeft size={14} className={cn('transition-transform', panelOpen ? '' : 'rotate-180')} />
        </button>

        {/* Panel content */}
        <div className="w-72 sm:w-80 h-full flex flex-col bg-bg-surface border-l border-bg-border shadow-2xl">
          {/* Header */}
          <div className="flex items-center justify-between p-3 border-b border-bg-border flex-shrink-0">
            <span className="text-sm font-semibold text-text-primary">
              Задачи <span className="text-text-muted font-normal">({filtered.length})</span>
            </span>
            <div className="flex gap-1">
              <Button variant="ghost" size="icon" onClick={() => refetch()}>
                <RefreshCw size={14} />
              </Button>
              <Button size="sm" onClick={() => setTaskFormOpen(true)}>
                <Plus size={14} /> Создать
              </Button>
            </div>
          </div>

          {/* Filter tabs */}
          <div className="flex gap-1 p-2 border-b border-bg-border overflow-x-auto flex-shrink-0">
            {STATUSES.map(s => (
              <button
                key={s}
                onClick={() => setFilterStatus(s)}
                className={cn(
                  'text-xs px-2.5 py-1 rounded-md whitespace-nowrap transition-colors',
                  filterStatus === s
                    ? 'bg-brand/10 text-brand font-medium'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-raised',
                )}
              >
                {s === 'ALL' ? 'Все' : TASK_STATUS_LABEL[s]}
              </button>
            ))}
          </div>

          {/* Task list */}
          <div className="flex-1 overflow-y-auto">
            {isLoading ? (
              <div className="flex items-center justify-center h-24 text-text-muted text-sm">Загрузка...</div>
            ) : filtered.length === 0 ? (
              <div className="flex items-center justify-center h-24 text-text-muted text-sm">Нет задач</div>
            ) : (
              filtered.map(task => (
                <TaskRow
                  key={task.id}
                  task={task}
                  hasDriver={driverPositions.has(task.id)}
                  onChat={() => setChatTaskId(task.id)}
                />
              ))
            )}
          </div>

          {driverPositions.size > 0 && (
            <div className="p-2 border-t border-bg-border flex-shrink-0">
              <p className="text-xs text-text-muted">
                🚛 {driverPositions.size} {driverPositions.size === 1 ? 'водитель' : 'водителей'} онлайн
              </p>
            </div>
          )}
        </div>
      </div>

      {taskFormOpen && (
        <TaskFormModal
          open={taskFormOpen}
          drivers={drivers ?? []}
          onClose={() => setTaskFormOpen(false)}
          onSuccess={handleTaskUpdate}
        />
      )}
      {chatTaskId !== null && (
        <ChatPanel taskId={chatTaskId} onClose={() => setChatTaskId(null)} overlay />
      )}
    </div>
  )
}

function TaskRow({
  task, hasDriver, onChat,
}: {
  task: DeliveryTaskDTO
  hasDriver: boolean
  onChat: () => void
}) {
  return (
    <div className="border-b border-bg-border hover:bg-bg-raised transition-colors">
      <div className="flex items-start gap-2 p-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-1.5 mb-1">
            <span className="text-xs font-mono text-text-muted">#{task.id}</span>
            <Badge variant={STATUS_BADGE[task.status]}>{TASK_STATUS_LABEL[task.status]}</Badge>
            {hasDriver && <span className="text-[10px] text-success">●</span>}
          </div>
          <p className="text-xs text-text-primary truncate">{task.address}</p>
          <p className="text-[10px] text-text-muted mt-0.5">{formatDateTime(task.created)}</p>
        </div>
        <button
          onClick={onChat}
          className="p-1.5 rounded text-text-muted hover:text-brand hover:bg-brand/10 transition-colors flex-shrink-0"
        >
          <MessageSquare size={14} />
        </button>
      </div>
    </div>
  )
}
