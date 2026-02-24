import { useEffect, useRef, useState, useCallback } from 'react'
import { smoothHeading, calculateBearing } from '@/lib/geo'

export type GpsStatus = 'off' | 'searching' | 'ok' | 'error'

export interface GpsState {
  lat: number | null
  lng: number | null
  heading: number
  speed: number
  status: GpsStatus
  statusText: string
}

export function useGps(active: boolean, onPosition?: (lat: number, lng: number, heading: number, accuracy: number) => void) {
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
        const { latitude: lat, longitude: lng, speed, accuracy } = pos.coords
        let newHeading = headingRef.current

        // Calculate heading from consecutive positions instead of coords.heading.
        // coords.heading comes from the GPS chip and varies in accuracy across devices
        // (often 10–30° off on Android). Bearing from two real positions is the true
        // direction of travel by definition.
        if ((speed ?? 0) > 0.5 && posRef.current) {
          const prev = posRef.current
          const dLat = (lat - prev.lat) * 111_320
          const dLng = (lng - prev.lng) * 111_320 * Math.cos(lat * Math.PI / 180)
          const dist  = Math.sqrt(dLat * dLat + dLng * dLng)
          // Only recalculate when moved ≥ 3 m — shorter baseline = noisy bearing
          if (dist >= 3) {
            const movBearing = calculateBearing(prev.lat, prev.lng, lat, lng)
            newHeading = smoothHeading(headingRef.current, movBearing, 0.45)
            headingRef.current = newHeading
          }
        }

        posRef.current = { lat, lng }
        setGps({ lat, lng, heading: newHeading, speed: speed ?? 0, status: 'ok', statusText: `${lat.toFixed(5)}, ${lng.toFixed(5)}` })
        onPosition?.(lat, lng, newHeading, accuracy ?? 999)
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
