import { useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { useWsStore } from '@/store/wsStore'
import type { UserRole } from '@/types/api'

interface Props {
  children: React.ReactNode
  roles?: UserRole[]
}

export function ProtectedRoute({ children, roles }: Props) {
  const token = useAuthStore(s => s.token)
  const hasRole = useAuthStore(s => s.hasRole)
  const connect = useWsStore(s => s.connect)
  const connected = useWsStore(s => s.connected)

  // Reconnect WS on page refresh or direct navigation (token exists but WS not active)
  useEffect(() => {
    if (token && !connected) {
      connect(token)
    }
  }, [token, connected, connect])

  if (!token) return <Navigate to="/login" replace />
  if (roles && !roles.some(r => hasRole(r))) return <Navigate to="/login" replace />

  return <>{children}</>
}
