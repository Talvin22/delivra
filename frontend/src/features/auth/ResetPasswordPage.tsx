import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '@/api/auth'

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    setLoading(true)
    setError(null)
    try {
      await authApi.resetPassword(token, newPassword, confirmPassword)
      setDone(true)
    } catch (err: any) {
      const msg = err?.response?.data?.message
      setError(msg ?? 'Invalid or expired link. Please request a new one.')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="bg-white rounded-2xl shadow p-8 w-full max-w-md text-center">
          <p className="text-red-500 mb-4">Invalid reset link.</p>
          <Link to="/forgot-password" className="text-blue-600 hover:underline text-sm">
            Request a new one
          </Link>
        </div>
      </div>
    )
  }

  if (done) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="bg-white rounded-2xl shadow p-8 w-full max-w-md text-center">
          <div className="text-4xl mb-4">✅</div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">Password updated</h2>
          <p className="text-gray-500 text-sm mb-6">You can now sign in with your new password.</p>
          <Link to="/login" className="text-blue-600 hover:underline text-sm">Go to sign in</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="bg-white rounded-2xl shadow p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-gray-800 mb-1">New password</h1>
        <p className="text-gray-500 text-sm mb-6">Enter your new password below.</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">New password</label>
            <input
              type="password"
              required
              minLength={6}
              value={newPassword}
              onChange={e => setNewPassword(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Min. 6 characters"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Confirm password</label>
            <input
              type="password"
              required
              value={confirmPassword}
              onChange={e => setConfirmPassword(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Repeat password"
            />
          </div>

          {error && <p className="text-red-500 text-sm">{error}</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 rounded-lg text-sm disabled:opacity-50"
          >
            {loading ? 'Saving...' : 'Set new password'}
          </button>
        </form>
      </div>
    </div>
  )
}
