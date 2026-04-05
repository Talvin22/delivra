import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { useWsStore } from '@/store/wsStore'
import { Input } from '@/components/ui/Input'
import { Button } from '@/components/ui/Button'

interface FormData { email: string; password: string }

export function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore(s => s.setAuth)
  const primaryRole = useAuthStore(s => s.primaryRole)
  const connect = useWsStore(s => s.connect)
  const [error, setError] = useState('')

  const { register, handleSubmit, formState: { isSubmitting } } = useForm<FormData>()

  const onSubmit = async (data: FormData) => {
    setError('')
    try {
      const res = await authApi.login(data.email, data.password)
      const profile = res.data.payload
      setAuth(profile)
      connect(profile.token)

      const role = primaryRole()
      if (role === 'DRIVER') navigate('/driver')
      else if (role === 'DISPATCHER') navigate('/dispatcher')
      else navigate('/admin')
    } catch (e: unknown) {
      const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message
      setError(msg ?? 'Invalid email or password')
    }
  }

  return (
    <div className="min-h-screen bg-bg-base flex items-center justify-center p-4">
      <div className="w-full max-w-sm bg-bg-surface border-2 border-brand/30 rounded-xl p-8 shadow-2xl shadow-brand/10">
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-brand tracking-wide">⬡ Delivra</h1>
          <p className="text-text-secondary text-sm mt-1">Sign in to your account</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
          <Input
            id="email"
            label="Email"
            type="email"
            placeholder="driver@example.com"
            autoComplete="email"
            {...register('email', { required: true })}
          />
          <Input
            id="password"
            label="Password"
            type="password"
            placeholder="••••••••"
            autoComplete="current-password"
            {...register('password', { required: true })}
          />

          {error && <p className="text-sm text-danger bg-danger/10 border border-danger/30 rounded-md px-3 py-2">{error}</p>}

          <Button type="submit" size="lg" loading={isSubmitting} className="mt-2">
            Sign in
          </Button>
        </form>

        <p className="text-center text-sm text-text-muted mt-6">
          No account?{' '}
          <Link to="/register" className="text-brand hover:underline">
            Register your company
          </Link>
        </p>
      </div>
    </div>
  )
}
