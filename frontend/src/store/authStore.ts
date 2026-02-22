import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserProfileDTO, UserRole } from '@/types/api'

interface AuthState {
  user: UserProfileDTO | null
  token: string | null
  refreshToken: string | null
  setAuth: (profile: UserProfileDTO) => void
  logout: () => void
  hasRole: (role: UserRole) => boolean
  primaryRole: () => UserRole | null
}

// Role priority: SUPER_ADMIN > ADMIN > DISPATCHER > DRIVER
const ROLE_PRIORITY: UserRole[] = ['SUPER_ADMIN', 'ADMIN', 'DISPATCHER', 'DRIVER']

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      refreshToken: null,

      setAuth: (profile) =>
        set({ user: profile, token: profile.token, refreshToken: profile.refreshToken }),

      logout: () => set({ user: null, token: null, refreshToken: null }),

      hasRole: (role) => {
        const roles = get().user?.roles.map(r => r.name) ?? []
        return roles.includes(role)
      },

      primaryRole: () => {
        const roles = get().user?.roles.map(r => r.name as UserRole) ?? []
        return ROLE_PRIORITY.find(r => roles.includes(r)) ?? null
      },
    }),
    { name: 'delivra-auth' },
  ),
)
