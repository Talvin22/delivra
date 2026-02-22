import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Trash2, Search, Shield } from 'lucide-react'
import { usersApi } from '@/api/users'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Badge } from '@/components/ui/Badge'
import { Modal } from '@/components/ui/Modal'
import { FullScreenLoader } from '@/components/ui/Spinner'
import { useForm } from 'react-hook-form'
import { formatDateTime } from '@/lib/formatters'
import type { UserSearchDTO } from '@/types/api'
import { cn } from '@/lib/utils'

interface CreateForm { username: string; email: string; password: string }

const ALL_ROLES = ['DRIVER', 'DISPATCHER', 'ADMIN', 'SUPER_ADMIN'] as const
type RoleName = typeof ALL_ROLES[number]

const ROLE_BADGE_VARIANT: Record<string, 'brand'|'warning'|'success'|'danger'> = {
  DRIVER: 'brand', DISPATCHER: 'warning', ADMIN: 'success', SUPER_ADMIN: 'danger',
}

export function UsersPage() {
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [rolesUser, setRolesUser] = useState<UserSearchDTO | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => usersApi.getAll(0, 200).then(r => r.data.payload.content),
  })

  const deleteUser = useMutation({
    mutationFn: (id: number) => usersApi.delete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-users'] }),
  })

  const createUser = useMutation({
    mutationFn: (form: CreateForm) => usersApi.create(form),
    onSuccess: () => { setCreateOpen(false); qc.invalidateQueries({ queryKey: ['admin-users'] }) },
  })

  const updateRoles = useMutation({
    mutationFn: ({ id, roles }: { id: number; roles: string[] }) => usersApi.updateRoles(id, roles),
    onSuccess: () => { setRolesUser(null); qc.invalidateQueries({ queryKey: ['admin-users'] }) },
  })

  const filtered = (data ?? []).filter(u =>
    !search || u.username.toLowerCase().includes(search.toLowerCase()) ||
    u.email.toLowerCase().includes(search.toLowerCase()),
  )

  return (
    <div className="h-full overflow-y-auto p-4 md:p-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-xl font-semibold text-text-primary">Пользователи</h1>
        <Button size="sm" onClick={() => setCreateOpen(true)}>
          <Plus size={14} /> Добавить
        </Button>
      </div>

      <div className="relative mb-4">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
        <input
          className="w-full max-w-xs bg-bg-base border border-bg-border rounded-md pl-8 pr-3 py-2 text-sm text-text-primary placeholder:text-text-muted outline-none focus:border-brand transition-colors"
          placeholder="Поиск по имени, email..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
      </div>

      {isLoading ? <FullScreenLoader /> : (
        <div className="bg-bg-surface border border-bg-border rounded-lg overflow-hidden">
          <div className="hidden md:grid grid-cols-[50px_1fr_1fr_1fr_100px] gap-3 px-4 py-2.5 border-b border-bg-border">
            {['ID', 'Имя', 'Email', 'Роли', ''].map(h => (
              <span key={h} className="text-xs text-text-muted font-medium uppercase tracking-wide">{h}</span>
            ))}
          </div>

          {filtered.length === 0 ? (
            <div className="text-center py-12 text-text-muted text-sm">Нет пользователей</div>
          ) : filtered.map(user => (
            <UserRow
              key={user.id}
              user={user}
              onDelete={() => deleteUser.mutate(user.id)}
              onEditRoles={() => setRolesUser(user)}
            />
          ))}
        </div>
      )}

      {/* Create modal */}
      <Modal open={createOpen} onClose={() => setCreateOpen(false)} title="Новый пользователь">
        <CreateUserForm
          onSubmit={data => createUser.mutate(data)}
          loading={createUser.isPending}
          error={createUser.isError ? 'Ошибка создания' : undefined}
        />
      </Modal>

      {/* Edit roles modal */}
      <Modal open={!!rolesUser} onClose={() => setRolesUser(null)} title={`Роли: ${rolesUser?.username}`}>
        {rolesUser && (
          <EditRolesForm
            currentRoles={rolesUser.roles.map(r => r.name)}
            loading={updateRoles.isPending}
            error={updateRoles.isError ? 'Ошибка обновления ролей' : undefined}
            onSubmit={roles => updateRoles.mutate({ id: rolesUser.id, roles })}
          />
        )}
      </Modal>
    </div>
  )
}

function UserRow({ user, onDelete, onEditRoles }: { user: UserSearchDTO; onDelete: () => void; onEditRoles: () => void }) {
  const roleNames = user.roles.map(r => r.name)
  return (
    <div className="grid grid-cols-[50px_1fr_80px] md:grid-cols-[50px_1fr_1fr_1fr_100px] gap-3 px-4 py-3 border-b border-bg-border hover:bg-bg-raised transition-colors items-center">
      <span className="text-xs font-mono text-text-muted">#{user.id}</span>
      <div>
        <p className="text-sm text-text-primary font-medium">{user.username}</p>
        <p className="text-xs text-text-muted md:hidden">{user.email}</p>
      </div>
      <p className="hidden md:block text-sm text-text-secondary truncate">{user.email}</p>
      <div className="hidden md:flex flex-wrap gap-1">
        {roleNames.map(r => (
          <Badge key={r} variant={ROLE_BADGE_VARIANT[r] ?? 'brand'}>{r}</Badge>
        ))}
      </div>
      <div className="flex justify-end gap-1 md:col-auto">
        <button
          onClick={onEditRoles}
          className="p-1.5 rounded text-text-muted hover:text-brand hover:bg-brand/10 transition-colors"
          title="Изменить роли"
        >
          <Shield size={14} />
        </button>
        <button
          onClick={onDelete}
          className="p-1.5 rounded text-text-muted hover:text-danger hover:bg-danger/10 transition-colors"
          title="Удалить"
        >
          <Trash2 size={14} />
        </button>
      </div>
    </div>
  )
}

function EditRolesForm({ currentRoles, loading, error, onSubmit }: {
  currentRoles: string[]
  loading: boolean
  error?: string
  onSubmit: (roles: string[]) => void
}) {
  const [selected, setSelected] = useState<Set<string>>(new Set(currentRoles))

  const toggle = (role: string) =>
    setSelected(prev => {
      const next = new Set(prev)
      if (next.has(role)) next.delete(role)
      else next.add(role)
      return next
    })

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        {ALL_ROLES.map(role => (
          <button
            key={role}
            type="button"
            onClick={() => toggle(role)}
            className={cn(
              'flex items-center gap-3 p-3 rounded-lg border transition-colors text-left',
              selected.has(role)
                ? 'border-brand bg-brand/5 text-text-primary'
                : 'border-bg-border bg-bg-base text-text-secondary hover:border-brand/40',
            )}
          >
            <div className={cn(
              'w-4 h-4 rounded border-2 flex items-center justify-center flex-shrink-0',
              selected.has(role) ? 'border-brand bg-brand' : 'border-bg-muted',
            )}>
              {selected.has(role) && <span className="text-white text-[10px] leading-none">✓</span>}
            </div>
            <Badge variant={ROLE_BADGE_VARIANT[role] ?? 'brand'}>{role}</Badge>
          </button>
        ))}
      </div>
      {error && <p className="text-sm text-danger">{error}</p>}
      <Button
        onClick={() => onSubmit(Array.from(selected))}
        loading={loading}
        disabled={selected.size === 0}
      >
        Сохранить
      </Button>
    </div>
  )
}

function CreateUserForm({ onSubmit, loading, error }: {
  onSubmit: (d: CreateForm) => void
  loading: boolean
  error?: string
}) {
  const { register, handleSubmit, formState: { errors } } = useForm<CreateForm>()
  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
      <Input label="Имя пользователя *" {...register('username', { required: 'Обязательно' })} error={errors.username?.message} />
      <Input label="Email *" type="email" {...register('email', { required: 'Обязательно' })} error={errors.email?.message} />
      <Input label="Пароль *" type="password" {...register('password', { required: 'Обязательно', minLength: { value: 6, message: 'Минимум 6 символов' } })} error={errors.password?.message} />
      {error && <p className="text-sm text-danger">{error}</p>}
      <Button type="submit" loading={loading} className="mt-1">Создать</Button>
    </form>
  )
}
