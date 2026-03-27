import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { MapPin, Briefcase, TrendingUp, Clock } from 'lucide-react'
import { tasksApi } from '@/api/tasks'
import { Modal } from '@/components/ui/Modal'
import { Button } from '@/components/ui/Button'
import { Spinner } from '@/components/ui/Spinner'
import { cn } from '@/lib/utils'
import type { DriverRecommendationDTO } from '@/types/api'

interface Props {
  taskId: number
  onClose: () => void
  onSuccess: () => void
}

export function DriverRecommendationModal({ taskId, onClose, onSuccess }: Props) {
  const qc = useQueryClient()

  const { data, isLoading, isError } = useQuery({
    queryKey: ['recommendations', taskId],
    queryFn: async () => {
      const res = await tasksApi.getRecommendations(taskId, 5)
      return res.data.payload
    },
  })

  const assign = useMutation({
    mutationFn: (driverId: number) => tasksApi.update(taskId, { driverId }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['dispatcher-tasks'] })
      onSuccess()
      onClose()
    },
  })

  return (
    <Modal
      open
      onClose={onClose}
      title={`Recommended drivers — task #${taskId}`}
      className="max-w-lg"
    >
      {isLoading && (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      )}

      {isError && (
        <p className="text-sm text-danger text-center py-4">Failed to load recommendations</p>
      )}

      {data && data.length === 0 && (
        <p className="text-sm text-text-muted text-center py-4">No available drivers</p>
      )}

      {data && data.length > 0 && (
        <div className="flex flex-col gap-2 max-h-[60vh] overflow-y-auto pr-1">
          {data.map((driver, index) => (
            <DriverCard
              key={driver.driverId}
              driver={driver}
              rank={index + 1}
              onAssign={() => assign.mutate(driver.driverId)}
              isAssigning={assign.isPending && assign.variables === driver.driverId}
            />
          ))}
        </div>
      )}

      <div className="pt-3 border-t border-bg-border mt-3">
        <Button variant="ghost" className="w-full" onClick={onClose}>
          Cancel
        </Button>
      </div>
    </Modal>
  )
}

function ScoreBar({ value, label, icon }: { value: number; label: string; icon: React.ReactNode }) {
  return (
    <div className="flex items-center gap-2">
      <span className="text-text-muted w-3 flex-shrink-0">{icon}</span>
      <div className="flex-1 min-w-0">
        <div className="flex justify-between items-center mb-0.5">
          <span className="text-[10px] text-text-muted">{label}</span>
          <span className="text-[10px] font-medium text-text-secondary">{value.toFixed(1)}%</span>
        </div>
        <div className="h-1 bg-bg-raised rounded-full overflow-hidden">
          <div
            className={cn(
              'h-full rounded-full transition-all duration-300',
              value > 66 ? 'bg-success' : value > 33 ? 'bg-warning' : 'bg-danger',
            )}
            style={{ width: `${Math.min(value, 100)}%` }}
          />
        </div>
      </div>
    </div>
  )
}

function DriverCard({
  driver, rank, onAssign, isAssigning,
}: {
  driver: DriverRecommendationDTO
  rank: number
  onAssign: () => void
  isAssigning: boolean
}) {
  return (
    <div className="bg-bg-raised border border-bg-border rounded-lg p-3 hover:border-brand/40 transition-colors">
      <div className="flex items-start justify-between gap-3 mb-2">
        <div className="flex items-center gap-2 min-w-0">
          <span className="text-xs text-text-muted font-mono w-4 flex-shrink-0">#{rank}</span>
          <div className="min-w-0">
            <p className="text-sm font-medium text-text-primary truncate">{driver.driverUsername}</p>
            <p className="text-xs text-text-muted truncate">{driver.driverEmail}</p>
          </div>
        </div>
        <div className="flex items-center gap-2 flex-shrink-0">
          <div className="text-right">
            <p className="text-lg font-bold text-brand leading-none">{driver.totalScore}</p>
            <p className="text-[10px] text-text-muted">score</p>
          </div>
          <Button size="sm" onClick={onAssign} loading={isAssigning}>
            Assign
          </Button>
        </div>
      </div>

      <div className="flex flex-col gap-1.5">
        <ScoreBar value={driver.proximityScore} label="Proximity" icon={<MapPin size={10} />} />
        <ScoreBar value={driver.workloadScore} label="Workload" icon={<Briefcase size={10} />} />
        <ScoreBar value={driver.successRateScore} label="Success rate" icon={<TrendingUp size={10} />} />
        <ScoreBar value={driver.recencyScore} label="Recency" icon={<Clock size={10} />} />
      </div>

      {driver.distanceMeters != null && (
        <p className="text-[10px] text-text-muted mt-2">
          📍 {(driver.distanceMeters / 1000).toFixed(1)} km from destination
        </p>
      )}
    </div>
  )
}
