import { createBrowserRouter, Navigate } from 'react-router-dom'
import { LoginPage } from '@/features/auth/LoginPage'
import { AppLayout } from '@/components/layout/AppLayout'
import { ProtectedRoute } from '@/components/layout/ProtectedRoute'
import { DriverTaskListPage } from '@/features/driver/DriverTaskListPage'
import { TaskDetailPage } from '@/features/driver/TaskDetailPage'
import { NavigationPage } from '@/features/driver/NavigationPage'
import { DispatcherPage } from '@/features/dispatcher/DispatcherPage'
import { AdminLayout } from '@/features/admin/AdminLayout'
import { AdminDashboard } from '@/features/admin/AdminDashboard'
import { UsersPage } from '@/features/admin/UsersPage'
import { TasksAdminPage } from '@/features/admin/TasksAdminPage'
import { RoleRedirect } from '@/features/auth/RoleRedirect'

export const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/', element: <RoleRedirect /> },

  {
    element: (
      <ProtectedRoute roles={['DRIVER']}>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/driver', element: <DriverTaskListPage /> },
      { path: '/driver/tasks/:id', element: <TaskDetailPage /> },
    ],
  },

  // Navigation is fullscreen — no AppLayout
  {
    path: '/driver/tasks/:id/navigate',
    element: (
      <ProtectedRoute roles={['DRIVER']}>
        <NavigationPage />
      </ProtectedRoute>
    ),
  },

  {
    element: (
      <ProtectedRoute roles={['DISPATCHER']}>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/dispatcher', element: <DispatcherPage /> },
    ],
  },

  {
    element: (
      <ProtectedRoute roles={['ADMIN', 'SUPER_ADMIN']}>
        <AdminLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/admin', element: <AdminDashboard /> },
      { path: '/admin/users', element: <UsersPage /> },
      { path: '/admin/tasks', element: <TasksAdminPage /> },
    ],
  },

  { path: '*', element: <Navigate to="/" replace /> },
])
