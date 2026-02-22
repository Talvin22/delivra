import { forwardRef } from 'react'
import { cn } from '@/lib/utils'

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, error, id, ...props }, ref) => (
    <div className="flex flex-col gap-1">
      {label && <label htmlFor={id} className="text-xs text-text-secondary">{label}</label>}
      <input
        ref={ref}
        id={id}
        className={cn(
          'w-full rounded-md bg-bg-raised border border-bg-border px-3 py-2 text-sm text-text-primary',
          'placeholder:text-text-muted outline-none transition-colors',
          'focus:border-brand',
          error && 'border-danger',
          className,
        )}
        {...props}
      />
      {error && <span className="text-xs text-danger">{error}</span>}
    </div>
  ),
)
Input.displayName = 'Input'
