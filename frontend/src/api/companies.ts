import { api } from './axios'
import type { DelivraResponse, UserProfileDTO } from '@/types/api'

export type CompanyStatus = 'TRIAL' | 'ACTIVE' | 'SUSPENDED'

export interface CompanyDTO {
  id: number
  name: string
  email: string
  status: CompanyStatus
  trialEndsAt: string | null
  created: string
}

export interface CompanyStatsDTO {
  companyId: number
  companyName: string
  status: CompanyStatus
  trialEndsAt: string | null
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  pendingTasks: number
  canceledTasks: number
  totalDrivers: number
  totalDispatchers: number
  totalNavigations: number
}

export interface GlobalStatsDTO {
  totalCompanies: number
  activeCompanies: number
  trialCompanies: number
  suspendedCompanies: number
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  totalUsers: number
  totalNavigations: number
}

export interface CompanyRegistrationRequest {
  companyName: string
  adminUsername: string
  adminEmail: string
  adminPassword: string
  confirmPassword: string
}

export const companiesApi = {
  register: (body: CompanyRegistrationRequest) =>
    api.post<DelivraResponse<UserProfileDTO>>('/companies/register', body),

  getMyCompany: () =>
    api.get<DelivraResponse<CompanyDTO>>('/companies/my'),

  getMyStats: () =>
    api.get<DelivraResponse<CompanyStatsDTO>>('/companies/my/stats'),

  getAllCompanies: () =>
    api.get<DelivraResponse<CompanyDTO[]>>('/admin/companies'),

  getCompanyStats: (id: number) =>
    api.get<DelivraResponse<CompanyStatsDTO>>(`/admin/companies/${id}/stats`),

  getGlobalStats: () =>
    api.get<DelivraResponse<GlobalStatsDTO>>('/admin/stats/overview'),

  updateStatus: (id: number, status: CompanyStatus) =>
    api.put<DelivraResponse<CompanyDTO>>(`/admin/companies/${id}/status?status=${status}`),
}
