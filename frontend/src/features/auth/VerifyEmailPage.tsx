import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi } from '@/api/auth'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''

  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setErrorMsg('No token provided.')
      return
    }
    authApi.verifyEmail(token)
      .then(() => setStatus('success'))
      .catch((err: any) => {
        const msg = err?.response?.data?.message
        setErrorMsg(msg ?? 'Invalid or expired verification link.')
        setStatus('error')
      })
  }, [token])

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <p className="text-gray-500">Verifying your email...</p>
      </div>
    )
  }

  if (status === 'success') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="bg-white rounded-2xl shadow p-8 w-full max-w-md text-center">
          <div className="text-4xl mb-4">✅</div>
          <h2 className="text-xl font-semibold text-gray-800 mb-2">Email confirmed!</h2>
          <p className="text-gray-500 text-sm mb-6">Your email address has been successfully verified.</p>
          <Link to="/login" className="text-blue-600 hover:underline text-sm">Go to sign in</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="bg-white rounded-2xl shadow p-8 w-full max-w-md text-center">
        <div className="text-4xl mb-4">❌</div>
        <h2 className="text-xl font-semibold text-gray-800 mb-2">Verification failed</h2>
        <p className="text-gray-500 text-sm mb-6">{errorMsg}</p>
        <Link to="/login" className="text-blue-600 hover:underline text-sm">Back to sign in</Link>
      </div>
    </div>
  )
}
