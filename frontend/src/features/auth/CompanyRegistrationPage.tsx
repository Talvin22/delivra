import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { companiesApi } from '@/api/companies'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'

interface FormData {
  companyName: string
  adminUsername: string
  adminEmail: string
  adminPassword: string
  confirmPassword: string
}

export function CompanyRegistrationPage() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const { register, handleSubmit, watch, formState: { isSubmitting, errors } } = useForm<FormData>()
  const password = watch('adminPassword')

  const onSubmit = async (data: FormData) => {
    setError('')
    try {
      await companiesApi.register(data)
      setSuccess(true)
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'Registration failed. Please try again.')
    }
  }

  if (success) {
    return (
      <div className="min-h-screen bg-bg-base flex items-center justify-center p-4">
        <div className="w-full max-w-sm bg-bg-surface border-2 border-success/30 rounded-xl p-8 shadow-2xl shadow-success/10 text-center">
          <div className="text-4xl mb-4">✓</div>
          <h2 className="text-xl font-bold text-text-primary mb-2">Registration successful!</h2>
          <p className="text-text-secondary text-sm mb-6">
            Your company account has been created with a 14-day free trial.
            Sign in with the admin credentials you provided.
          </p>
          <Button onClick={() => navigate('/login')} size="lg" className="w-full">
            Go to sign in
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-bg-base flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-bg-surface border-2 border-brand/30 rounded-xl p-8 shadow-2xl shadow-brand/10">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-brand tracking-wide">⬡ Delivra</h1>
          <p className="text-text-secondary text-sm mt-1">Register your company — 14 days free</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <Input
            id="companyName"
            label="Company name"
            placeholder="Acme Logistics"
            {...register('companyName', { required: 'Required' })}
            error={errors.companyName?.message}
          />

          <div className="border-t border-bg-border pt-4">
            <p className="text-xs text-text-muted mb-3 uppercase tracking-wide font-semibold">Admin account</p>
            <div className="flex flex-col gap-4">
              <Input
                id="adminUsername"
                label="Username"
                placeholder="johndoe"
                {...register('adminUsername', { required: 'Required' })}
                error={errors.adminUsername?.message}
              />
              <Input
                id="adminEmail"
                label="Email"
                type="email"
                placeholder="admin@company.com"
                autoComplete="email"
                {...register('adminEmail', { required: 'Required' })}
                error={errors.adminEmail?.message}
              />
              <Input
                id="adminPassword"
                label="Password"
                type="password"
                placeholder="••••••••"
                autoComplete="new-password"
                {...register('adminPassword', { required: 'Required', minLength: { value: 6, message: 'Min 6 characters' } })}
                error={errors.adminPassword?.message}
              />
              <Input
                id="confirmPassword"
                label="Confirm password"
                type="password"
                placeholder="••••••••"
                autoComplete="new-password"
                {...register('confirmPassword', {
                  required: 'Required',
                  validate: v => v === password || 'Passwords do not match',
                })}
                error={errors.confirmPassword?.message}
              />
            </div>
          </div>

          {error && (
            <p className="text-sm text-danger bg-danger/10 border border-danger/30 rounded-md px-3 py-2">
              {error}
            </p>
          )}

          <Button type="submit" size="lg" loading={isSubmitting} className="mt-2">
            Create account
          </Button>
        </form>

        <p className="text-center text-sm text-text-muted mt-6">
          Already have an account?{' '}
          <Link to="/login" className="text-brand hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
