import { useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { tasksApi } from '@/api/tasks'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'
import type { UserSearchDTO } from '@/types/api'

interface FormData {
  address: string
  driverId: string
}

interface Props {
  open: boolean
  drivers: UserSearchDTO[]
  onClose: () => void
  onSuccess: () => void
}

export function TaskFormModal({ open, drivers, onClose, onSuccess }: Props) {
  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>()

  const create = useMutation({
    mutationFn: (data: FormData) =>
      tasksApi.create({
        address: data.address,
        driverId: data.driverId ? Number(data.driverId) : undefined,
      }),
    onSuccess: () => { reset(); onSuccess(); onClose() },
  })

  return (
    <Modal open={open} onClose={onClose} title="Новая задача">
      <form onSubmit={handleSubmit(d => create.mutate(d))} className="flex flex-col gap-4">
        <Input
          label="Адрес доставки *"
          placeholder="ул. Крещатик, 1, Киев"
          error={errors.address?.message}
          {...register('address', { required: 'Введите адрес' })}
        />

        <div className="flex flex-col gap-1">
          <label className="text-xs text-text-secondary">Назначить водителя</label>
          <select
            className="w-full rounded-md bg-bg-base border border-bg-border px-3 py-2 text-sm text-text-primary outline-none focus:border-brand transition-colors"
            {...register('driverId')}
          >
            <option value="">Без водителя</option>
            {drivers.map(d => (
              <option key={d.id} value={d.id}>{d.username} ({d.email})</option>
            ))}
          </select>
        </div>

        {create.isError && (
          <p className="text-sm text-danger">Ошибка создания задачи</p>
        )}

        <div className="flex gap-2 pt-2">
          <Button type="button" variant="ghost" className="flex-1" onClick={onClose}>Отмена</Button>
          <Button type="submit" className="flex-1" loading={create.isPending}>Создать</Button>
        </div>
      </form>
    </Modal>
  )
}
