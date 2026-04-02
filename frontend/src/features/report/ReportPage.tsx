import { useState } from 'react'
import { FileDown, FileSpreadsheet, CheckCircle, Clock, Users, ListTodo } from 'lucide-react'
import { reportsApi } from '@/api/reports'

export function ReportPage() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleDownload = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await reportsApi.downloadExcel()
      const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `delivra-report-${new Date().toISOString().slice(0, 10)}.xlsx`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(url)
    } catch {
      setError('Failed to download report. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="h-full overflow-y-auto p-4 md:p-6 max-w-2xl mx-auto">
      <h1 className="text-xl font-semibold text-text-primary mb-1">Export Report</h1>
      <p className="text-sm text-text-secondary mb-6">Download task statistics for the last 30 days as an Excel file.</p>

      <div className="bg-bg-surface border border-bg-border rounded-lg mb-6">
        <div className="px-4 py-3 border-b border-bg-border">
          <div className="flex items-center gap-2">
            <FileSpreadsheet size={16} className="text-brand" />
            <span className="text-sm font-semibold text-text-primary">Report contents</span>
          </div>
        </div>
        <div className="divide-y divide-bg-border">
          <SheetRow
            icon={<ListTodo size={16} className="text-brand" />}
            title="Tasks"
            description="Full list of tasks: address, status, driver, created by, timestamps"
          />
          <SheetRow
            icon={<CheckCircle size={16} className="text-success" />}
            title="Summary"
            description="Total count and percentage breakdown by status (Pending, In Progress, Completed, Canceled)"
          />
          <SheetRow
            icon={<Users size={16} className="text-warning" />}
            title="Driver Activity"
            description="Per-driver task counts grouped by status"
          />
        </div>
      </div>

      <div className="flex items-center gap-3 mb-2">
        <div className="flex items-center gap-1.5 text-xs text-text-muted">
          <Clock size={13} />
          Last 30 days
        </div>
        <span className="text-text-muted text-xs">·</span>
        <span className="text-xs text-text-muted">.xlsx format</span>
      </div>

      {error && (
        <p className="text-sm text-danger mb-3">{error}</p>
      )}

      <button
        onClick={handleDownload}
        disabled={loading}
        className="flex items-center gap-2 px-4 py-2.5 bg-brand text-white text-sm font-medium rounded-md hover:bg-brand/90 disabled:opacity-60 disabled:cursor-not-allowed transition-colors"
      >
        <FileDown size={16} />
        {loading ? 'Downloading…' : 'Download Excel'}
      </button>
    </div>
  )
}

function SheetRow({ icon, title, description }: { icon: React.ReactNode; title: string; description: string }) {
  return (
    <div className="flex items-start gap-3 px-4 py-3">
      <div className="mt-0.5 flex-shrink-0">{icon}</div>
      <div>
        <p className="text-sm font-medium text-text-primary">{title}</p>
        <p className="text-xs text-text-secondary mt-0.5">{description}</p>
      </div>
    </div>
  )
}
