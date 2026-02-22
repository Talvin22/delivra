import { useEffect, useRef } from 'react'

export function useWakeLock(active: boolean) {
  const lockRef = useRef<WakeLockSentinel | null>(null)

  useEffect(() => {
    if (!active) {
      lockRef.current?.release().catch(() => {})
      lockRef.current = null
      return
    }
    if (!('wakeLock' in navigator)) return
    navigator.wakeLock.request('screen').then(lock => { lockRef.current = lock }).catch(() => {})

    const onVisible = () => {
      if (active && !lockRef.current) {
        navigator.wakeLock.request('screen').then(lock => { lockRef.current = lock }).catch(() => {})
      }
    }
    document.addEventListener('visibilitychange', onVisible)
    return () => document.removeEventListener('visibilitychange', onVisible)
  }, [active])
}
