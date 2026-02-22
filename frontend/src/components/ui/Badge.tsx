import { cn } from '@/lib/utils'

type BadgeVariant = 'brand' | 'success' | 'warning' | 'danger' | 'muted'

const styles: Record<BadgeVariant, string> = {
  brand:   'bg-brand/10 text-brand border-brand/30',
  success: 'bg-success/10 text-success border-success/30',
  warning: 'bg-warning/10 text-warning border-warning/30',
  danger:  'bg-danger/10 text-danger border-danger/30',
  muted:   'bg-bg-raised text-text-secondary border-bg-border',
}

export function Badge({ children, variant = 'muted', className }: {
  children: React.ReactNode
  variant?: BadgeVariant
  className?: string
}) {
  return (
    <span className={cn('inline-flex items-center px-2 py-0.5 text-xs font-medium rounded border', styles[variant], className)}>
      {children}
    </span>
  )
}
