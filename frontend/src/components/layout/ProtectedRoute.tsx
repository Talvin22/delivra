import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import type { UserRole } from '@/types/api'

interface Props {
  children: React.ReactNode
  roles?: UserRole[]
}

export function ProtectedRoute({ children, roles }: Props) {
  const token = useAuthStore(s => s.token)
  const hasRole = useAuthStore(s => s.hasRole)

  if (!token) return <Navigate to="/login" replace />
  if (roles && !roles.some(r => hasRole(r))) return <Navigate to="/login" replace />

  return <>{children}</>
}
