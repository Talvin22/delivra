import { forwardRef } from 'react'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 rounded-md font-medium transition-all focus-visible:outline-none disabled:pointer-events-none disabled:opacity-40 cursor-pointer select-none',
  {
    variants: {
      variant: {
        primary:  'bg-brand text-white hover:bg-brand-hover active:bg-brand-hover',
        danger:   'bg-danger/10 text-danger border border-danger/30 hover:bg-danger/20',
        ghost:    'text-text-secondary hover:text-text-primary hover:bg-bg-raised',
        outline:  'border border-bg-border text-text-primary hover:bg-bg-raised',
        success:  'bg-success/10 text-success border border-success/30 hover:bg-success/20',
      },
      size: {
        sm:   'text-xs px-3 py-1.5',
        md:   'text-sm px-4 py-2',
        lg:   'text-base px-5 py-3 rounded-lg',
        icon: 'w-9 h-9',
      },
    },
    defaultVariants: { variant: 'primary', size: 'md' },
  },
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  loading?: boolean
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, loading, children, disabled, ...props }, ref) => (
    <button
      ref={ref}
      className={cn(buttonVariants({ variant, size }), className)}
      disabled={disabled || loading}
      {...props}
    >
      {loading && (
        <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
        </svg>
      )}
      {children}
    </button>
  ),
)
Button.displayName = 'Button'
