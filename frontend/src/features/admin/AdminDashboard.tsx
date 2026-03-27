import { useQuery } from '@tanstack/react-query'
import { Users, ListTodo, Navigation, TrendingUp } from 'lucide-react'
import { tasksApi } from '@/api/tasks'
import { usersApi } from '@/api/users'
import { FullScreenLoader } from '@/components/ui/Spinner'

export function AdminDashboard() {
  const { data: tasks } = useQuery({
    queryKey: ['admin-tasks-all'],
    queryFn: () => tasksApi.getAll(0, 1000).then(r => r.data.payload),
  })
  const { data: users } = useQuery({
    queryKey: ['admin-users-all'],
    queryFn: () => usersApi.getAll(0, 1000).then(r => r.data.payload),
  })

  const taskCounts = {
    total:      tasks?.content.length ?? 0,
    inProgress: tasks?.content.filter(t => t.status === 'IN_PROGRESS').length ?? 0,
    completed:  tasks?.content.filter(t => t.status === 'COMPLETED').length ?? 0,
    pending:    tasks?.content.filter(t => t.status === 'PENDING').length ?? 0,
  }

  const userCounts = {
    total:    users?.content.length ?? 0,
    drivers:  users?.content.filter(u => u.roles.some(r => r.name === 'DRIVER')).length ?? 0,
    dispatchers: users?.content.filter(u => u.roles.some(r => r.name === 'DISPATCHER')).length ?? 0,
  }

  return (
    <div className="h-full overflow-y-auto p-4 md:p-6 max-w-5xl mx-auto">
      <h1 className="text-xl font-semibold text-text-primary mb-6">Dashboard</h1>

      {/* Stats grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-8">
        <StatCard icon={<Users size={20} className="text-brand" />} label="Users" value={userCounts.total} sub={`${userCounts.drivers} drivers`} />
        <StatCard icon={<ListTodo size={20} className="text-warning" />} label="Total tasks" value={taskCounts.total} sub={`${taskCounts.pending} pending`} />
        <StatCard icon={<Navigation size={20} className="text-success" />} label="In progress" value={taskCounts.inProgress} sub="active routes" />
        <StatCard icon={<TrendingUp size={20} className="text-brand" />} label="Completed" value={taskCounts.completed} sub={taskCounts.total > 0 ? `${Math.round(taskCounts.completed / taskCounts.total * 100)}%` : '0%'} />
      </div>

      {/* Recent tasks */}
      <div className="bg-bg-surface border border-bg-border rounded-lg">
        <div className="px-4 py-3 border-b border-bg-border">
          <h2 className="text-sm font-semibold text-text-primary">Recent tasks</h2>
        </div>
        <div className="divide-y divide-bg-border">
          {(tasks?.content.slice(0, 10) ?? []).map(t => (
            <div key={t.id} className="flex items-center gap-3 px-4 py-3">
              <span className="text-xs font-mono text-text-muted w-8">#{t.id}</span>
              <span className={`w-2 h-2 rounded-full flex-shrink-0 ${
                t.status === 'IN_PROGRESS' ? 'bg-brand' :
                t.status === 'COMPLETED'   ? 'bg-success' :
                t.status === 'CANCELED'    ? 'bg-danger' : 'bg-warning'
              }`} />
              <span className="text-sm text-text-primary flex-1 truncate">{t.address}</span>
              <span className="text-xs text-text-muted">{t.createdBy}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
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
