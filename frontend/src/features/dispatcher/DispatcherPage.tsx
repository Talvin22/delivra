import { useState, useEffect, useCallback, useRef } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import { Plus, MessageSquare, ChevronLeft, RefreshCw, UserPlus, MapPin as MapPinIcon, Pencil } from 'lucide-react'
import 'leaflet/dist/leaflet.css'
import { tasksApi } from '@/api/tasks'
import { usersApi } from '@/api/users'
import { useWsStore } from '@/store/wsStore'
import { useThemeStore } from '@/store/themeStore'
import { Badge } from '@/components/ui/Badge'
import { Button } from '@/components/ui/Button'
import { ChatPanel } from '@/components/chat/ChatPanel'
import { TaskFormModal } from './TaskFormModal'
import { TaskEditModal } from './TaskEditModal'
import { DriverRecommendationModal } from './DriverRecommendationModal'
import { formatDateTime, TASK_STATUS_LABEL } from '@/lib/formatters'
import type { DeliveryTaskDTO, DeliveryTaskStatus, NavigationEventDTO, DriverPosition } from '@/types/api'
import { cn } from '@/lib/utils'

// Flies to position once on first load (initial geolocation)
function FlyToLocation({ center }: { center: [number, number] | null }) {
  const map = useMap()
  const done = useRef(false)
  useEffect(() => {
    if (!center || done.current) return
    done.current = true
    map.flyTo(center, 13, { animate: true, duration: 1.2 })
  }, [map, center])
  return null
}

// Flies to position whenever `target` changes (task focus)
function FlyToTarget({ target }: { target: { pos: [number, number]; zoom: number; seq: number } | null }) {
  const map = useMap()
  const prevSeq = useRef(-1)
  useEffect(() => {
    if (!target || target.seq === prevSeq.current) return
    prevSeq.current = target.seq
    map.flyTo(target.pos, target.zoom, { animate: true, duration: 0.8 })
  }, [map, target])
  return null
}

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
  const dark = useThemeStore(s => s.dark)

  const [userLocation, setUserLocation] = useState<[number, number] | null>(null)
  const [flyTarget, setFlyTarget] = useState<{ pos: [number, number]; zoom: number; seq: number } | null>(null)
  const flySeq = useRef(0)
  const [driverPositions, setDriverPositions] = useState<Map<number, DriverPosition>>(new Map())
  const [chatTaskId, setChatTaskId] = useState<number | null>(null)
  const [taskFormOpen, setTaskFormOpen] = useState(false)
  const [recommendTaskId, setRecommendTaskId] = useState<number | null>(null)
  const [editTask, setEditTask] = useState<DeliveryTaskDTO | null>(null)
  const [filterStatus, setFilterStatus] = useState<DeliveryTaskStatus | 'ALL'>('ALL')
  const [panelOpen, setPanelOpen] = useState(true)

  useEffect(() => {
    if (!navigator.geolocation) return
    navigator.geolocation.getCurrentPosition(
      pos => setUserLocation([pos.coords.latitude, pos.coords.longitude]),
      () => { /* ignore — map stays on default center */ },
      { enableHighAccuracy: false, timeout: 10000 },
    )
  }, [])

  const { data: tasks, isLoading, refetch } = useQuery({
    queryKey: ['dispatcher-tasks'],
    queryFn: async () => {
      const res = await tasksApi.getAll(0, 100)
      return res.data.payload.content
    },
    refetchInterval: 10_000,
  })

  const { data: drivers } = useQuery({
    queryKey: ['drivers'],
    queryFn: async () => {
      const res = await usersApi.search({ role: 'DRIVER' }, 0, 100)
      return res.data.payload.content
    },
  })

  // Subscribe to position/route topics for all non-finished tasks (PENDING + IN_PROGRESS).
  // We include PENDING so we're already subscribed when navigation starts — no 30s poll lag.
  useEffect(() => {
    if (!tasks) return
    const active = tasks.filter(t => t.status === 'IN_PROGRESS' || t.status === 'PENDING')

    const handlePosition = (taskId: number) => (data: unknown) => {
      const ev = data as NavigationEventDTO
      if (ev.latitude == null || ev.longitude == null) return
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

    const unsubs = active.flatMap(task => [
      subscribe(`/topic/navigation/${task.id}/position`, handlePosition(task.id)),
      subscribe(`/topic/navigation/${task.id}/route`, handlePosition(task.id)),
    ])
    return () => unsubs.forEach(u => u())
  }, [tasks, subscribe])

  const handleTaskUpdate = useCallback(() => {
    qc.invalidateQueries({ queryKey: ['dispatcher-tasks'] })
  }, [qc])

  const handleFocusTask = useCallback((task: DeliveryTaskDTO) => {
    const dp = driverPositions.get(task.id)
    const pos: [number, number] | null = dp
      ? [dp.lat, dp.lng]
      : task.latitude && task.longitude
        ? [task.latitude, task.longitude]
        : null
    if (!pos) return
    setFlyTarget({ pos, zoom: dp ? 15 : 13, seq: ++flySeq.current })
    setPanelOpen(false)
  }, [driverPositions])

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
            key={dark ? 'dark' : 'light'}
            url={dark
              ? 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png'
              : 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png'
            }
            attribution="&copy; OpenStreetMap &copy; CARTO"
            maxZoom={19}
          />
          <FlyToLocation center={userLocation} />
          <FlyToTarget target={flyTarget} />

          {Array.from(driverPositions.values()).map(dp => (
            <Marker key={dp.taskId} position={[dp.lat, dp.lng]} icon={makeDriverMapIcon(`#${dp.taskId}`)}>
              <Popup>
                <div className="text-xs">
                  <p className="font-semibold">Task #{dp.taskId}</p>
                  <p className={dp.onRoute ? 'text-green-600' : 'text-red-600'}>
                    {dp.onRoute ? 'On route' : 'Off route'}
                  </p>
                </div>
              </Popup>
            </Marker>
          ))}

          {(tasks ?? []).filter(t => t.latitude && t.longitude && t.status !== 'COMPLETED' && t.status !== 'CANCELED').map(t => (
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
          title={panelOpen ? 'Hide panel' : 'Show panel'}
        >
          <ChevronLeft size={14} className={cn('transition-transform', panelOpen ? '' : 'rotate-180')} />
        </button>

        {/* Panel content */}
        <div className="w-72 sm:w-80 h-full flex flex-col bg-bg-surface border-l border-bg-border shadow-2xl">
          {/* Header */}
          <div className="flex items-center justify-between p-3 border-b border-bg-border flex-shrink-0">
            <span className="text-sm font-semibold text-text-primary">
              Tasks <span className="text-text-muted font-normal">({filtered.length})</span>
            </span>
            <div className="flex gap-1">
              <Button variant="ghost" size="icon" onClick={() => refetch()}>
                <RefreshCw size={14} />
              </Button>
              <Button size="sm" onClick={() => setTaskFormOpen(true)}>
                <Plus size={14} /> Create
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
                {s === 'ALL' ? 'All' : TASK_STATUS_LABEL[s]}
              </button>
            ))}
          </div>

          {/* Task list */}
          <div className="flex-1 overflow-y-auto">
            {isLoading ? (
              <div className="flex items-center justify-center h-24 text-text-muted text-sm">Loading...</div>
            ) : filtered.length === 0 ? (
              <div className="flex items-center justify-center h-24 text-text-muted text-sm">No tasks</div>
            ) : (
              filtered.map(task => (
                <TaskRow
                  key={task.id}
                  task={task}
                  hasDriver={driverPositions.has(task.id)}
                  onEdit={() => setEditTask(task)}
                  onChat={() => setChatTaskId(task.id)}
                  onRecommend={() => setRecommendTaskId(task.id)}
                  onFocus={() => handleFocusTask(task)}
                />
              ))
            )}
          </div>

          {driverPositions.size > 0 && (
            <div className="p-2 border-t border-bg-border flex-shrink-0">
              <p className="text-xs text-text-muted">
                🚛 {driverPositions.size} {driverPositions.size === 1 ? 'driver' : 'drivers'} online
              </p>
            </div>
          )}
        </div>
      </div>

      {editTask && (
        <TaskEditModal
          task={editTask}
          drivers={drivers ?? []}
          onClose={() => setEditTask(null)}
          onSuccess={handleTaskUpdate}
        />
      )}
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
      {recommendTaskId !== null && (
        <DriverRecommendationModal
          taskId={recommendTaskId}
          onClose={() => setRecommendTaskId(null)}
          onSuccess={handleTaskUpdate}
        />
      )}
    </div>
  )
}

function TaskRow({
  task, hasDriver, onEdit, onChat, onRecommend, onFocus,
}: {
  task: DeliveryTaskDTO
  hasDriver: boolean
  onEdit: () => void
  onChat: () => void
  onRecommend: () => void
  onFocus: () => void
}) {
  const canFocus = hasDriver || !!(task.latitude && task.longitude)

  return (
    <div
      className="border-b border-bg-border hover:bg-bg-raised transition-colors cursor-pointer"
      onClick={canFocus ? onFocus : onEdit}
    >
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
        <div className="flex gap-0.5 flex-shrink-0" onClick={e => e.stopPropagation()}>
          {task.status === 'PENDING' && (
            <button
              onClick={onRecommend}
              title="Assign driver"
              className="p-1.5 rounded text-text-muted hover:text-success hover:bg-success/10 transition-colors"
            >
              <UserPlus size={14} />
            </button>
          )}
          <button
            onClick={onChat}
            title="Chat"
            className="p-1.5 rounded text-text-muted hover:text-brand hover:bg-brand/10 transition-colors"
          >
            <MessageSquare size={14} />
          </button>
          {canFocus && (
            <button
              onClick={e => { e.stopPropagation(); onFocus() }}
              title="Show on map"
              className="p-1.5 rounded text-text-muted hover:text-brand hover:bg-brand/10 transition-colors"
            >
              <MapPinIcon size={14} />
            </button>
          )}
          <button
            onClick={e => { e.stopPropagation(); onEdit() }}
            title="Edit"
            className="p-1.5 rounded text-text-muted hover:text-text-primary hover:bg-bg-raised transition-colors"
          >
            <Pencil size={14} />
          </button>
        </div>
      </div>
    </div>
  )
}
