import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

export function RoleRedirect() {
  const token = useAuthStore(s => s.token)
  const primaryRole = useAuthStore(s => s.primaryRole)

  if (!token) return <Navigate to="/login" replace />

  const role = primaryRole()
  if (role === 'DRIVER') return <Navigate to="/driver" replace />
  if (role === 'DISPATCHER') return <Navigate to="/dispatcher" replace />
  return <Navigate to="/admin" replace />
}
