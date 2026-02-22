import { useEffect, useRef, useState, useCallback } from 'react'
import { smoothHeading } from '@/lib/geo'

export type GpsStatus = 'off' | 'searching' | 'ok' | 'error'

export interface GpsState {
  lat: number | null
  lng: number | null
  heading: number
  speed: number
  status: GpsStatus
  statusText: string
}

export function useGps(active: boolean, onPosition?: (lat: number, lng: number, heading: number) => void) {
  const [gps, setGps] = useState<GpsState>({
    lat: null, lng: null, heading: 0, speed: 0,
    status: 'off', statusText: 'GPS не определён',
  })

  const watchId = useRef<number | null>(null)
  const headingRef = useRef(0)
  const posRef = useRef<{ lat: number; lng: number } | null>(null)

  const stop = useCallback(() => {
    if (watchId.current !== null) { navigator.geolocation.clearWatch(watchId.current); watchId.current = null }
    setGps(g => ({ ...g, status: 'off', statusText: 'GPS не определён' }))
  }, [])

  useEffect(() => {
    if (!active) { stop(); return }
    if (!navigator.geolocation) {
      setGps(g => ({ ...g, status: 'error', statusText: 'GPS не поддерживается' }))
      return
    }
    setGps(g => ({ ...g, status: 'searching', statusText: 'Определяю позицию...' }))

    watchId.current = navigator.geolocation.watchPosition(
      pos => {
        const { latitude: lat, longitude: lng, speed, heading: rawHeading } = pos.coords
        let newHeading = headingRef.current
        if ((speed ?? 0) > 1 && rawHeading !== null && !isNaN(rawHeading)) {
          newHeading = smoothHeading(headingRef.current, rawHeading)
          headingRef.current = newHeading
        }
        posRef.current = { lat, lng }
        setGps({ lat, lng, heading: newHeading, speed: speed ?? 0, status: 'ok', statusText: `${lat.toFixed(5)}, ${lng.toFixed(5)}` })
        onPosition?.(lat, lng, newHeading)
      },
      err => {
        if (err.code === 1) setGps(g => ({ ...g, status: 'error', statusText: 'Доступ к GPS запрещён' }))
        else if (err.code === 2) setGps(g => ({ ...g, status: 'error', statusText: 'GPS недоступен' }))
        // code 3 = timeout, ignore — watchPosition will retry
      },
      { enableHighAccuracy: true, maximumAge: 0, timeout: 30000 },
    )
    return stop
  }, [active, stop, onPosition])

  return gps
}
