import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  MapPin, ListTodo, Users, BarChart3, LogOut,
  Navigation, Menu, X, MessageSquare, FileDown, Sun, Moon,
} from 'lucide-react'
import { useAuthStore } from '@/store/authStore'
import { useWsStore } from '@/store/wsStore'
import { useThemeStore } from '@/store/themeStore'
import { cn } from '@/lib/utils'

interface NavItem { to: string; icon: React.ReactNode; label: string }

function useNavItems(): NavItem[] {
  const primaryRole = useAuthStore(s => s.primaryRole)
  const role = primaryRole()

  if (role === 'DRIVER') return [
    { to: '/driver', icon: <ListTodo size={20} />, label: 'My Tasks' },
  ]
  if (role === 'DISPATCHER') return [
    { to: '/dispatcher', icon: <MapPin size={20} />, label: 'Map' },
    { to: '/dispatcher/report', icon: <FileDown size={20} />, label: 'Report' },
  ]
  return [
    { to: '/admin', icon: <BarChart3 size={20} />, label: 'Dashboard' },
    { to: '/admin/users', icon: <Users size={20} />, label: 'Users' },
    { to: '/admin/tasks', icon: <ListTodo size={20} />, label: 'Tasks' },
    { to: '/admin/report', icon: <FileDown size={20} />, label: 'Report' },
  ]
}

export function AppLayout() {
  const navigate = useNavigate()
  const logout = useAuthStore(s => s.logout)
  const disconnect = useWsStore(s => s.disconnect)
  const user = useAuthStore(s => s.user)
  const connected = useWsStore(s => s.connected)
  const navItems = useNavItems()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const { dark, toggle: toggleTheme } = useThemeStore()

  const handleLogout = () => {
    disconnect()
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen bg-bg-base overflow-hidden">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-[1000] bg-black/50 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={cn(
        'fixed lg:static inset-y-0 left-0 z-[1001] w-60 bg-bg-surface border-r border-bg-border',
        'flex flex-col transition-transform duration-200',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0',
      )}>
        {/* Logo */}
        <div className="flex items-center justify-between h-14 px-4 border-b border-bg-border flex-shrink-0">
          <div className="flex items-center gap-2">
            <Navigation size={18} className="text-brand" />
            <span className="font-bold text-brand tracking-wide">DELIVRA</span>
          </div>
          <button className="lg:hidden text-text-muted hover:text-text-primary" onClick={() => setSidebarOpen(false)}>
            <X size={18} />
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-3 overflow-y-auto">
          {navItems.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end
              onClick={() => setSidebarOpen(false)}
              className={({ isActive }) => cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-md text-sm transition-colors mb-0.5',
                isActive
                  ? 'bg-brand/10 text-brand font-medium'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-raised',
              )}
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* User / status */}
        <div className="p-3 border-t border-bg-border flex-shrink-0">
          <div className="flex items-center gap-2 px-2 py-1.5 mb-1">
            <span className={cn('w-2 h-2 rounded-full flex-shrink-0', connected ? 'bg-success' : 'bg-bg-muted')} />
            <span className="text-xs text-text-secondary truncate">{user?.username ?? user?.email}</span>
          </div>
          <div className="flex gap-1 mb-1">
            <button
              onClick={toggleTheme}
              className="flex items-center gap-2 flex-1 px-3 py-2 text-sm text-text-secondary hover:text-text-primary hover:bg-bg-raised rounded-md transition-colors"
              title={dark ? 'Switch to light theme' : 'Switch to dark theme'}
            >
              {dark ? <Sun size={16} /> : <Moon size={16} />}
              {dark ? 'Light mode' : 'Dark mode'}
            </button>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 w-full px-3 py-2 text-sm text-text-secondary hover:text-danger hover:bg-danger/10 rounded-md transition-colors"
          >
            <LogOut size={16} />
            Sign out
          </button>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Mobile header */}
        <header className="lg:hidden h-14 flex items-center gap-3 px-4 bg-bg-surface border-b border-bg-border flex-shrink-0">
          <button className="text-text-secondary hover:text-text-primary" onClick={() => setSidebarOpen(true)}>
            <Menu size={22} />
          </button>
          <div className="flex items-center gap-2">
            <MessageSquare size={16} className="text-brand" />
            <span className="font-bold text-brand text-sm tracking-wide">DELIVRA</span>
          </div>
        </header>

        <main className="flex-1 overflow-hidden relative">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
