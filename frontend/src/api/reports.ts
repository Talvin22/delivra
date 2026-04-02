import { api } from './axios'

export const reportsApi = {
  downloadExcel: () =>
    api.get('/reports/export/excel', { responseType: 'arraybuffer' }),
}
