import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { Users, ListTodo, Navigation, TrendingUp, Building2, Clock, Plus } from 'lucide-react'
import { companiesApi, type CompanyStatus, type CompanyRegistrationRequest } from '@/api/companies'
import { useAuthStore } from '@/store/authStore'
import { FullScreenLoader } from '@/components/ui/Spinner'
import { Modal } from '@/components/ui/Modal'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'

export function AdminDashboard() {
  const isSuperAdmin = useAuthStore(s => s.hasRole('SUPER_ADMIN'))
  return isSuperAdmin ? <SuperAdminDashboard /> : <CompanyDashboard />
}

// ── Company (ADMIN / DISPATCHER) view ────────────────────────────────────────

function CompanyDashboard() {
  const { data, isLoading } = useQuery({
    queryKey: ['company-stats'],
    queryFn: () => companiesApi.getMyStats().then(r => r.data.payload),
  })

  if (isLoading || !data) return <FullScreenLoader />
  const s = data

  return (
    <div className="h-full overflow-y-auto p-4 md:p-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-semibold text-text-primary">{s.companyName}</h1>
          <p className="text-xs text-text-muted mt-0.5">
            {s.status === 'TRIAL' && s.trialEndsAt
              ? `Trial — expires ${new Date(s.trialEndsAt).toLocaleDateString()}`
              : s.status}
          </p>
        </div>
        <StatusBadge status={s.status} />
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-8">
        <StatCard icon={<Users size={20} className="text-brand" />} label="Drivers" value={s.totalDrivers} sub={`${s.totalDispatchers} dispatchers`} />
        <StatCard icon={<ListTodo size={20} className="text-warning" />} label="Total tasks" value={s.totalTasks} sub={`${s.pendingTasks} pending`} />
        <StatCard icon={<Navigation size={20} className="text-success" />} label="In progress" value={s.inProgressTasks} sub="active routes" />
        <StatCard icon={<TrendingUp size={20} className="text-brand" />} label="Completed" value={s.completedTasks} sub={s.totalTasks > 0 ? `${Math.round(s.completedTasks / s.totalTasks * 100)}%` : '0%'} />
      </div>

      <div className="bg-bg-surface border border-bg-border rounded-lg p-4">
        <div className="flex items-center gap-2 text-text-secondary mb-1">
          <Navigation size={16} />
          <span className="text-sm font-medium">Navigation sessions</span>
        </div>
        <p className="text-3xl font-bold text-text-primary">{s.totalNavigations}</p>
        <p className="text-xs text-text-muted mt-1">total route navigations started</p>
      </div>
    </div>
  )
}

// ── SUPER_ADMIN view ──────────────────────────────────────────────────────────

function SuperAdminDashboard() {
  const queryClient = useQueryClient()
  const [createOpen, setCreateOpen] = useState(false)

  const { data: global, isLoading: loadingGlobal, error: globalError } = useQuery({
    queryKey: ['global-stats'],
    queryFn: () => companiesApi.getGlobalStats().then(r => r.data.payload),
  })

  const { data: companies, isLoading: loadingCompanies } = useQuery({
    queryKey: ['all-companies'],
    queryFn: () => companiesApi.getAllCompanies().then(r => r.data.payload),
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: CompanyStatus }) =>
      companiesApi.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['all-companies'] })
      queryClient.invalidateQueries({ queryKey: ['global-stats'] })
    },
  })

  if (loadingGlobal || loadingCompanies) return <FullScreenLoader />
  if (globalError || !global) {
    const msg = (globalError as { response?: { data?: { message?: string } } })?.response?.data?.message
    return (
      <div className="h-full flex items-center justify-center p-6">
        <p className="text-sm text-danger">{msg ?? 'Failed to load stats. Check backend logs.'}</p>
      </div>
    )
  }
  const g = global

  return (
    <div className="h-full overflow-y-auto p-4 md:p-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-xl font-semibold text-text-primary">Global Overview</h1>
        <Button size="sm" onClick={() => setCreateOpen(true)}>
          <Plus size={14} className="mr-1" /> New company
        </Button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-8">
        <StatCard icon={<Building2 size={20} className="text-brand" />} label="Companies" value={g.totalCompanies} sub={`${g.activeCompanies} active`} />
        <StatCard icon={<Clock size={20} className="text-warning" />} label="On trial" value={g.trialCompanies} sub={`${g.suspendedCompanies} suspended`} />
        <StatCard icon={<Users size={20} className="text-success" />} label="Users" value={g.totalUsers} sub="across all companies" />
        <StatCard icon={<ListTodo size={20} className="text-brand" />} label="Tasks" value={g.totalTasks} sub={`${g.completedTasks} completed`} />
      </div>

      <div className="bg-bg-surface border border-bg-border rounded-lg">
        <div className="px-4 py-3 border-b border-bg-border">
          <h2 className="text-sm font-semibold text-text-primary">Companies</h2>
        </div>
        <div className="divide-y divide-bg-border">
          {(companies ?? []).map(c => (
            <div key={c.id} className="flex items-center gap-3 px-4 py-3">
              <Building2 size={16} className="text-text-muted flex-shrink-0" />
              <div className="flex-1 min-w-0">
                <p className="text-sm text-text-primary truncate">{c.name}</p>
                <p className="text-xs text-text-muted truncate">{c.email}</p>
              </div>
              {c.status === 'TRIAL' && c.trialEndsAt && (
                <span className="text-xs text-text-muted hidden sm:block">
                  until {new Date(c.trialEndsAt).toLocaleDateString()}
                </span>
              )}
              <StatusBadge status={c.status} />
              <select
                value={c.status}
                onChange={e => updateStatus.mutate({ id: c.id, status: e.target.value as CompanyStatus })}
                className="text-xs bg-bg-raised border border-bg-border rounded px-2 py-1 text-text-primary outline-none focus:border-brand"
              >
                <option value="TRIAL">Trial</option>
                <option value="ACTIVE">Active</option>
                <option value="SUSPENDED">Suspended</option>
              </select>
            </div>
          ))}
          {(companies ?? []).length === 0 && (
            <div className="px-4 py-6 text-center text-sm text-text-muted">No companies yet</div>
          )}
        </div>
      </div>

      <CreateCompanyModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={() => {
          setCreateOpen(false)
          queryClient.invalidateQueries({ queryKey: ['all-companies'] })
          queryClient.invalidateQueries({ queryKey: ['global-stats'] })
        }}
      />
    </div>
  )
}

// ── Create Company Modal ──────────────────────────────────────────────────────

function CreateCompanyModal({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: () => void }) {
  const [error, setError] = useState('')
  const { register, handleSubmit, watch, reset, formState: { isSubmitting, errors } } = useForm<CompanyRegistrationRequest>()
  const password = watch('adminPassword')

  const onSubmit = async (data: CompanyRegistrationRequest) => {
    setError('')
    try {
      await companiesApi.register(data)
      reset()
      onCreated()
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'Failed to create company')
    }
  }

  return (
    <Modal open={open} onClose={onClose} title="Create company">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <Input
          id="companyName"
          label="Company name"
          placeholder="Acme Logistics"
          {...register('companyName', { required: 'Required' })}
          error={errors.companyName?.message}
        />
        <div className="border-t border-bg-border pt-3">
          <p className="text-xs text-text-muted mb-3 uppercase tracking-wide font-semibold">Admin account</p>
          <div className="flex flex-col gap-3">
            <Input
              id="create-username"
              label="Username"
              placeholder="johndoe"
              {...register('adminUsername', { required: 'Required' })}
              error={errors.adminUsername?.message}
            />
            <Input
              id="create-email"
              label="Email"
              type="email"
              placeholder="admin@company.com"
              {...register('adminEmail', { required: 'Required' })}
              error={errors.adminEmail?.message}
            />
            <Input
              id="create-password"
              label="Password"
              type="password"
              placeholder="••••••••"
              {...register('adminPassword', { required: 'Required', minLength: { value: 6, message: 'Min 6 characters' } })}
              error={errors.adminPassword?.message}
            />
            <Input
              id="create-confirm"
              label="Confirm password"
              type="password"
              placeholder="••••••••"
              {...register('confirmPassword', {
                required: 'Required',
                validate: v => v === password || 'Passwords do not match',
              })}
              error={errors.confirmPassword?.message}
            />
          </div>
        </div>

        {error && (
          <p className="text-sm text-danger bg-danger/10 border border-danger/30 rounded-md px-3 py-2">{error}</p>
        )}

        <div className="flex gap-2 pt-1">
          <Button type="button" variant="outline" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button type="submit" className="flex-1" loading={isSubmitting}>Create</Button>
        </div>
      </form>
    </Modal>
  )
}

// ── Shared components ─────────────────────────────────────────────────────────

function StatusBadge({ status }: { status: CompanyStatus }) {
  const cls =
    status === 'ACTIVE'  ? 'bg-success/15 text-success border-success/30' :
    status === 'TRIAL'   ? 'bg-warning/15 text-warning border-warning/30' :
                           'bg-danger/15 text-danger border-danger/30'
  return (
    <span className={`text-xs px-2 py-0.5 rounded-full border font-medium flex-shrink-0 ${cls}`}>
      {status}
    </span>
  )
}

function StatCard({ icon, label, value, sub }: { icon: React.ReactNode; label: string; value: number; sub: string }) {
  return (
    <div className="bg-bg-surface border border-bg-border rounded-lg p-4">
      <div className="flex items-center gap-2 mb-3">{icon}<span className="text-xs text-text-secondary">{label}</span></div>
      <p className="text-2xl font-bold text-text-primary">{value}</p>
      <p className="text-xs text-text-muted mt-1">{sub}</p>
    </div>
  )
}
