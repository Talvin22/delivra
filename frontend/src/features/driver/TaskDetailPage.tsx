import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { MapPin, Navigation, ArrowLeft, MessageSquare, RotateCcw, XCircle } from 'lucide-react'
import { tasksApi } from '@/api/tasks'
import { navigationApi } from '@/api/navigation'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { FullScreenLoader } from '@/components/ui/Spinner'
import { ChatPanel } from '@/components/chat/ChatPanel'
import { formatDateTime, TASK_STATUS_LABEL } from '@/lib/formatters'
import type { DeliveryTaskStatus } from '@/types/api'

const BADGE_VARIANT: Record<DeliveryTaskStatus, 'brand'|'warning'|'success'|'danger'> = {
  IN_PROGRESS: 'brand', PENDING: 'warning', COMPLETED: 'success', CANCELED: 'danger',
}

export function TaskDetailPage() {
  const { id } = useParams<{ id: string }>()
  const taskId = Number(id)
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [chatOpen, setChatOpen] = useState(false)

  const { data: task, isLoading } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => tasksApi.getById(taskId).then(r => r.data.payload),
  })

  const startNav = useMutation({
    mutationFn: async () => {
      const pos = await new Promise<GeolocationPosition>((res, rej) =>
        navigator.geolocation.getCurrentPosition(res, rej, { enableHighAccuracy: true, timeout: 15000 }),
      )
      return navigationApi.start(taskId, {
        originLatitude: pos.coords.latitude,
        originLongitude: pos.coords.longitude,
      })
    },
    onSuccess: () => navigate(`/driver/tasks/${taskId}/navigate`),
    onError: (err: { response?: { status?: number; data?: { message?: string } } }) => {
      if (err?.response?.status === 409) {
        const msg = err?.response?.data?.message ?? ''
        if (msg.includes('active navigation session')) {
          navigate(`/driver/tasks/${taskId}/navigate`)
        }
      }
    },
  })

  const changeStatus = useMutation({
    mutationFn: (status: DeliveryTaskStatus) =>
      tasksApi.update(taskId, { status }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task', taskId] })
      queryClient.invalidateQueries({ queryKey: ['tasks'] })
    },
  })

  if (isLoading || !task) return <FullScreenLoader />

  const canStart = task.status === 'PENDING' || task.status === 'IN_PROGRESS'
  const isActive = task.status === 'IN_PROGRESS'
  const anyPending = startNav.isPending || changeStatus.isPending

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center gap-3 p-4 border-b border-bg-border bg-bg-surface">
        <button onClick={() => navigate(-1)} className="text-text-secondary hover:text-text-primary p-1 rounded">
          <ArrowLeft size={20} />
        </button>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <span className="text-xs font-mono text-text-muted">Task #{task.id}</span>
            <Badge variant={BADGE_VARIANT[task.status]}>
              {TASK_STATUS_LABEL[task.status]}
            </Badge>
          </div>
        </div>
        <button onClick={() => setChatOpen(true)} className="text-text-secondary hover:text-brand p-2 rounded-lg hover:bg-brand/10 transition-colors">
          <MessageSquare size={20} />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-4 md:p-6 max-w-2xl mx-auto w-full">
        {/* Address */}
        <div className="bg-bg-surface border border-bg-border rounded-lg p-4 mb-4">
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-full bg-danger/10 flex items-center justify-center flex-shrink-0 mt-0.5">
              <MapPin size={16} className="text-danger" />
            </div>
            <div>
              <p className="text-xs text-text-muted mb-1">Delivery address</p>
              <p className="text-text-primary font-medium">{task.address}</p>
              {task.latitude && task.longitude && (
                <p className="text-xs text-text-muted mt-1 font-mono">
                  {task.latitude.toFixed(5)}, {task.longitude.toFixed(5)}
                </p>
              )}
            </div>
          </div>
        </div>

        {/* Info */}
        <div className="bg-bg-surface border border-bg-border rounded-lg divide-y divide-bg-border mb-6">
          <InfoRow label="Created" value={formatDateTime(task.created)} />
          <InfoRow label="Created by" value={task.createdBy} />
          {task.startTime && <InfoRow label="Started" value={formatDateTime(task.startTime)} />}
          {task.endTime && <InfoRow label="Finished" value={formatDateTime(task.endTime)} />}
        </div>

        {/* Navigation button */}
        {canStart && (
          <Button
            size="lg"
            className="w-full mb-3"
            onClick={() => startNav.mutate()}
            loading={startNav.isPending}
            disabled={anyPending}
          >
            <Navigation size={18} />
            {isActive ? 'Continue navigation' : 'Start navigation'}
          </Button>
        )}

        {startNav.isError && (
          <p className="text-sm text-danger mb-3 text-center">
            {(() => {
              const err = startNav.error as { response?: { status?: number; data?: { message?: string } }; message?: string }
              if (err?.response?.status === 409) return null
              if (err?.response?.status === 401) return 'Session expired — please sign in again'
              if (err?.response?.status === 403) return 'No access to this task'
              if (err?.response?.status === 404) return 'Task not found'
              return err?.response?.data?.message ?? err?.message ?? 'Failed to start navigation'
            })()}
          </p>
        )}

        {/* Status management */}
        <div className="bg-bg-surface border border-bg-border rounded-lg p-4">
          <p className="text-xs text-text-muted mb-3">Status management</p>
          <div className="flex flex-col gap-2">

            {/* Revert accidental completion */}
            {task.status === 'COMPLETED' && (
              <Button
                variant="outline"
                size="md"
                className="w-full"
                onClick={() => changeStatus.mutate('IN_PROGRESS')}
                loading={changeStatus.isPending}
                disabled={anyPending}
              >
                <RotateCcw size={15} />
                Reopen task
              </Button>
            )}

            {/* Restore canceled task */}
            {task.status === 'CANCELED' && (
              <Button
                variant="outline"
                size="md"
                className="w-full"
                onClick={() => changeStatus.mutate('PENDING')}
                loading={changeStatus.isPending}
                disabled={anyPending}
              >
                <RotateCcw size={15} />
                Restore task
              </Button>
            )}

            {/* Cancel active/pending task */}
            {(task.status === 'PENDING' || task.status === 'IN_PROGRESS') && (
              <Button
                variant="danger"
                size="md"
                className="w-full"
                onClick={() => changeStatus.mutate('CANCELED')}
                loading={changeStatus.isPending}
                disabled={anyPending}
              >
                <XCircle size={15} />
                Cancel task
              </Button>
            )}

          </div>

          {changeStatus.isError && (
            <p className="text-xs text-danger mt-2 text-center">
              Failed to update status
            </p>
          )}
        </div>
      </div>

      {/* Chat drawer */}
      {chatOpen && <ChatPanel taskId={taskId} onClose={() => setChatOpen(false)} />}
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between items-center px-4 py-3">
      <span className="text-sm text-text-secondary">{label}</span>
      <span className="text-sm text-text-primary font-medium">{value}</span>
    </div>
  )
}
