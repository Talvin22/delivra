import { api } from './axios'

export const reportsApi = {
  downloadExcel: (companyId?: number) =>
    api.get('/reports/export/excel', {
      responseType: 'arraybuffer',
      params: companyId != null ? { companyId } : undefined,
    }),
}
