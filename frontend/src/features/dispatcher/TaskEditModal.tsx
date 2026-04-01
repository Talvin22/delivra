import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { RotateCcw, XCircle, Trash2 } from 'lucide-react'
import { tasksApi } from '@/api/tasks'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { formatDateTime, TASK_STATUS_LABEL } from '@/lib/formatters'
import type { DeliveryTaskDTO, DeliveryTaskStatus, UserSearchDTO } from '@/types/api'

const BADGE_VARIANT: Record<DeliveryTaskStatus, 'brand' | 'warning' | 'success' | 'danger'> = {
  IN_PROGRESS: 'brand', PENDING: 'warning', COMPLETED: 'success', CANCELED: 'danger',
}

interface FormData { address: string; driverId: string }

interface Props {
  task: DeliveryTaskDTO
  drivers: UserSearchDTO[]
  onClose: () => void
  onSuccess: () => void
}

export function TaskEditModal({ task, drivers, onClose, onSuccess }: Props) {
  const qc = useQueryClient()
  const [confirmDelete, setConfirmDelete] = useState(false)

  const { register, handleSubmit, formState: { errors, isDirty } } = useForm<FormData>({
    defaultValues: {
      address: task.address,
      driverId: task.userId ? String(task.userId) : '',
    },
  })

  const update = useMutation({
    mutationFn: (data: FormData) =>
      tasksApi.update(task.id, {
        address: data.address !== task.address ? data.address : undefined,
        driverId: data.driverId ? Number(data.driverId) : undefined,
      }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['dispatcher-tasks'] }); onSuccess(); onClose() },
  })

  const changeStatus = useMutation({
    mutationFn: (status: DeliveryTaskStatus) => tasksApi.update(task.id, { status }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['dispatcher-tasks'] }); onSuccess(); onClose() },
  })

  const deleteTask = useMutation({
    mutationFn: () => tasksApi.delete(task.id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['dispatcher-tasks'] }); onSuccess(); onClose() },
  })

  const anyPending = update.isPending || changeStatus.isPending || deleteTask.isPending

  return (
    <Modal open onClose={onClose} title={`Task #${task.id}`}>
      <div className="flex flex-col gap-4">

        {/* Status + meta */}
        <div className="flex items-center gap-2 flex-wrap">
          <Badge variant={BADGE_VARIANT[task.status]}>{TASK_STATUS_LABEL[task.status]}</Badge>
          <span className="text-xs text-text-muted">{formatDateTime(task.created)}</span>
          {task.createdBy && <span className="text-xs text-text-muted">by {task.createdBy}</span>}
        </div>

        {/* Edit form */}
        <form onSubmit={handleSubmit(d => update.mutate(d))} className="flex flex-col gap-3">
          <Input
            label="Delivery address"
            error={errors.address?.message}
            {...register('address', { required: 'Enter address' })}
          />

          <div className="flex flex-col gap-1">
            <label className="text-xs text-text-secondary">Driver</label>
            <select
              className="w-full rounded-md bg-bg-base border border-bg-border px-3 py-2 text-sm text-text-primary outline-none focus:border-brand transition-colors"
              {...register('driverId')}
            >
              <option value="">No driver</option>
              {drivers.map(d => (
                <option key={d.id} value={d.id}>{d.username} ({d.email})</option>
              ))}
            </select>
          </div>

          {update.isError && <p className="text-sm text-danger">Failed to update task</p>}

          <div className="flex gap-2">
            <Button type="button" variant="ghost" className="flex-1" onClick={onClose} disabled={anyPending}>
              Close
            </Button>
            <Button type="submit" className="flex-1" loading={update.isPending} disabled={!isDirty || anyPending}>
              Save
            </Button>
          </div>
        </form>

        {/* Status actions */}
        {(task.status === 'PENDING' || task.status === 'IN_PROGRESS') && (
          <div className="border-t border-bg-border pt-3">
            <Button
              variant="danger" size="md" className="w-full"
              onClick={() => changeStatus.mutate('CANCELED')}
              loading={changeStatus.isPending} disabled={anyPending}
            >
              <XCircle size={15} /> Cancel task
            </Button>
          </div>
        )}
        {(task.status === 'CANCELED' || task.status === 'COMPLETED') && (
          <div className="border-t border-bg-border pt-3">
            <Button
              variant="outline" size="md" className="w-full"
              onClick={() => changeStatus.mutate('PENDING')}
              loading={changeStatus.isPending} disabled={anyPending}
            >
              <RotateCcw size={15} /> Restore task
            </Button>
          </div>
        )}

        {/* Delete */}
        <div className="border-t border-bg-border pt-3">
          {confirmDelete ? (
            <div className="flex gap-2">
              <Button variant="ghost" size="md" className="flex-1" onClick={() => setConfirmDelete(false)} disabled={anyPending}>
                Cancel
              </Button>
              <Button
                variant="danger" size="md" className="flex-1"
                onClick={() => deleteTask.mutate()}
                loading={deleteTask.isPending} disabled={anyPending}
              >
                <Trash2 size={15} /> Confirm delete
              </Button>
            </div>
          ) : (
            <button
              onClick={() => setConfirmDelete(true)}
              disabled={anyPending}
              className="flex items-center gap-2 text-xs text-text-muted hover:text-danger transition-colors disabled:opacity-40"
            >
              <Trash2 size={13} /> Delete task
            </button>
          )}
        </div>
      </div>
    </Modal>
  )
}
