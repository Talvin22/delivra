import { useState, useEffect, useRef, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { MapContainer, TileLayer, Polyline, Marker, useMap } from 'react-leaflet'
import L from 'leaflet'
import { X, Crosshair, MessageSquare, Square, AlertTriangle } from 'lucide-react'
import 'leaflet/dist/leaflet.css'
import { navigationApi } from '@/api/navigation'
import { tasksApi } from '@/api/tasks'
import { useWsStore } from '@/store/wsStore'
import { useGps } from '@/hooks/useGps'
import { useWakeLock } from '@/hooks/useWakeLock'
import { calcRemainingDist, closestWaypointIdx, smoothHeading } from '@/lib/geo'
import { formatDistance, formatDuration } from '@/lib/formatters'
import { ChatPanel } from '@/components/chat/ChatPanel'
import { Button } from '@/components/ui/Button'
import { FullScreenLoader } from '@/components/ui/Spinner'
import type { NavigationEventDTO, RouteDTO, Waypoint } from '@/types/api'

// ─── Driver SVG marker ────────────────────────────────────────────
function makeDriverIcon(heading: number) {
  return L.divIcon({
    className: '',
    html: `<svg class="nav-arrow" style="transform:rotate(${heading}deg)" xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 44 44">
      <circle cx="22" cy="22" r="20" fill="rgba(108,138,255,0.15)"/>
      <circle cx="22" cy="22" r="13" fill="#6c8aff" stroke="white" stroke-width="2.5"/>
      <path d="M22 7 L30 23 L22 19 L14 23 Z" fill="white"/>
    </svg>`,
    iconSize: [44, 44],
    iconAnchor: [22, 22],
  })
}

function makeDestIcon() {
  return L.divIcon({
    className: '',
    html: `<div style="background:#ff4d6d;width:18px;height:18px;border-radius:50%;border:3px solid white;box-shadow:0 2px 8px rgba(255,77,109,0.6)"></div>`,
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  })
}

// ─── Map controller (follow mode + bearing) ───────────────────────
interface MapControllerProps {
  center: [number, number] | null
  follow: boolean
  bearing: number
  onManualMove: () => void
}

function MapController({ center, follow, bearing, onManualMove }: MapControllerProps) {
  const map = useMap()
  const followRef = useRef(follow)
  followRef.current = follow

  useEffect(() => {
    const h = () => { if (followRef.current) onManualMove() }
    map.on('dragstart', h)
    map.on('rotatestart', h)
    return () => { map.off('dragstart', h); map.off('rotatestart', h) }
  }, [map, onManualMove])

  useEffect(() => {
    if (!follow || !center) return
    map.panTo(center, { animate: true, duration: 0.3, easeLinearity: 1 })
  }, [map, center, follow])

  useEffect(() => {
    if (!follow) return
    const m = map as L.Map & { setBearing?: (b: number) => void }
    m.setBearing?.(bearing)
  }, [map, bearing, follow])

  return null
}

// ─── Action icon for turn instructions ────────────────────────────
const ACTION_ICONS: Record<string, string> = {
  depart: '▶', arrive: '⭐', turnLeft: '←', turnRight: '→',
  sharpTurnLeft: '↰', sharpTurnRight: '↱', slightTurnLeft: '↖', slightTurnRight: '↗',
  keepLeft: '↖', keepRight: '↗', uTurn: '↩', continue: '↑', roundaboutExit: '↻',
}

// ─── Main component ───────────────────────────────────────────────
export function NavigationPage() {
  const { id } = useParams<{ id: string }>()
  const taskId = Number(id)
  const navigate = useNavigate()

  const { subscribe, publish, connected } = useWsStore()

  const [sessionId, setSessionId] = useState<number | null>(null)
  const [route, setRoute] = useState<RouteDTO | null>(null)
  const [trimmedWaypoints, setTrimmedWaypoints] = useState<Waypoint[]>([])
  const [driverPos, setDriverPos] = useState<[number, number] | null>(null)
  const [heading, setHeading] = useState(0)
  const headingRef = useRef(0)
  const [followMode, setFollowMode] = useState(true)
  const [onRoute, setOnRoute] = useState(true)
  const [remainingM, setRemainingM] = useState<number | null>(null)
  const [remainingSec, setRemainingSec] = useState<number | null>(null)
  const [instrIdx, setInstrIdx] = useState(0)
  const [chatOpen, setChatOpen] = useState(false)
  const [ended, setEnded] = useState(false)

  useWakeLock(!ended)

  // Load task + active session
  const { data: task } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => tasksApi.getById(taskId).then(r => r.data.payload),
  })

  const { isLoading: sessionLoading, data: sessionData } = useQuery({
    queryKey: ['nav-session', taskId],
    queryFn: () => navigationApi.getActive(taskId).then(r => r.data.payload),
  })

  useEffect(() => {
    if (!sessionData) return
    setSessionId(sessionData.sessionId)
    if (sessionData.route) {
      setRoute(sessionData.route)
      setTrimmedWaypoints(sessionData.route.waypoints ?? [])
    }
  }, [sessionData])

  // Update nav progress (trim route + calc remaining)
  const updateProgress = useCallback((lat: number, lng: number) => {
    if (!route || route.waypoints.length < 2) return
    const wpts = route.waypoints
    const idx = closestWaypointIdx(lat, lng, wpts)
    setTrimmedWaypoints(wpts.slice(Math.max(0, idx)))

    const rem = calcRemainingDist(lat, lng, wpts)
    setRemainingM(rem)
    if (route.distanceInMeters > 0 && route.durationInSeconds > 0) {
      setRemainingSec(Math.round(route.durationInSeconds * (rem / route.distanceInMeters)))
    }

    // Find current instruction
    if (route.instructions?.length > 0) {
      let cum = 0
      let iIdx = 0
      for (let i = 0; i < route.instructions.length; i++) {
        if (cum <= (route.distanceInMeters - rem)) iIdx = i
        cum += route.instructions[i].distanceInMeters
      }
      setInstrIdx(Math.min(iIdx + 1, route.instructions.length - 1))
    }
  }, [route])

  // GPS
  const handleGpsPosition = useCallback((lat: number, lng: number, h: number) => {
    const smoothed = smoothHeading(headingRef.current, h)
    headingRef.current = smoothed
    setHeading(smoothed)
    setDriverPos([lat, lng])
    updateProgress(lat, lng)

    if (sessionId && connected) {
      publish(`/app/navigation/${sessionId}/position`, { latitude: lat, longitude: lng })
    }
  }, [sessionId, connected, publish, updateProgress])

  const gps = useGps(!ended, handleGpsPosition)

  // Subscribe to WS events
  useEffect(() => {
    if (!taskId) return
    const u1 = subscribe(`/topic/navigation/${taskId}/position`, (data) => {
      const event = data as NavigationEventDTO
      setOnRoute(event.onRoute)
      if (event.onRoute && driverPos) updateProgress(driverPos[0], driverPos[1])
    })
    const u2 = subscribe(`/topic/navigation/${taskId}/route`, (data) => {
      const event = data as NavigationEventDTO
      if (event.route) {
        setRoute(event.route)
        setTrimmedWaypoints(event.route.waypoints ?? [])
      }
    })
    return () => { u1(); u2() }
  }, [taskId, subscribe, updateProgress, driverPos])

  const endNav = useMutation({
    mutationFn: () => navigationApi.end(taskId),
    onSuccess: () => { setEnded(true); navigate(`/driver/tasks/${taskId}`) },
  })

  if (sessionLoading) return <FullScreenLoader />

  const dest = route?.waypoints?.at(-1)
  const instruction = route?.instructions?.[instrIdx]

  return (
    <div className="relative w-full h-screen bg-bg-base overflow-hidden">
      {/* Map */}
      <MapContainer
        center={driverPos ?? [50.45, 30.52]}
        zoom={16}
        className="w-full h-full"
        zoomControl={false}
      >
        <TileLayer
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
          attribution="&copy; OpenStreetMap &copy; CARTO"
          maxZoom={19}
        />
        <MapController
          center={driverPos}
          follow={followMode}
          bearing={heading}
          onManualMove={() => setFollowMode(false)}
        />
        {trimmedWaypoints.length > 1 && (
          <Polyline
            positions={trimmedWaypoints.map(w => [w.lat, w.lng])}
            color="#6c8aff"
            weight={5}
            opacity={0.85}
          />
        )}
        {driverPos && (
          <Marker position={driverPos} icon={makeDriverIcon(heading)} zIndexOffset={1000} />
        )}
        {dest && (
          <Marker position={[dest.lat, dest.lng]} icon={makeDestIcon()}>
          </Marker>
        )}
      </MapContainer>

      {/* Instruction bar */}
      {instruction && (
        <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[500] pointer-events-none">
          <div className="flex items-center gap-3 bg-bg-surface/95 border border-bg-border rounded-xl px-4 py-2.5 shadow-xl backdrop-blur-md">
            <span className="text-2xl leading-none">{ACTION_ICONS[instruction.action] ?? '↑'}</span>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-text-primary truncate max-w-[200px]">{instruction.instruction}</p>
              {instruction.distanceInMeters > 0 && (
                <p className="text-xs text-brand mt-0.5">через {formatDistance(instruction.distanceInMeters)}</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Off-route alert */}
      {!onRoute && (
        <div className="absolute top-20 left-1/2 -translate-x-1/2 z-[500] pointer-events-none">
          <div className="flex items-center gap-2 bg-warning/10 border border-warning/40 rounded-lg px-3 py-1.5">
            <AlertTriangle size={14} className="text-warning" />
            <span className="text-xs text-warning font-medium">Перестройка маршрута...</span>
          </div>
        </div>
      )}

      {/* GPS status */}
      {gps.status !== 'ok' && (
        <div className="absolute top-4 left-4 z-[500] pointer-events-none">
          <div className="flex items-center gap-2 bg-bg-surface/90 border border-bg-border rounded-lg px-2.5 py-1.5">
            <span className={`w-2 h-2 rounded-full ${gps.status === 'searching' ? 'bg-warning animate-pulse' : 'bg-danger'}`} />
            <span className="text-xs text-text-secondary">{gps.statusText}</span>
          </div>
        </div>
      )}

      {/* Bottom HUD */}
      <div className="absolute bottom-0 left-0 right-0 z-[500]">
        <div className="mx-3 mb-3 bg-bg-surface/95 border border-bg-border rounded-xl p-3 backdrop-blur-md shadow-xl">
          {/* Stats */}
          {(remainingM !== null || task) && (
            <div className="flex items-center justify-around mb-3 pb-3 border-b border-bg-border">
              {remainingM !== null && (
                <div className="text-center">
                  <p className="text-lg font-bold text-text-primary">{formatDistance(remainingM)}</p>
                  <p className="text-xs text-text-muted">Осталось</p>
                </div>
              )}
              {remainingSec !== null && (
                <div className="text-center">
                  <p className="text-lg font-bold text-text-primary">{formatDuration(remainingSec)}</p>
                  <p className="text-xs text-text-muted">Время</p>
                </div>
              )}
              {task && (
                <div className="text-center max-w-[140px]">
                  <p className="text-xs font-medium text-text-primary truncate">{task.address}</p>
                  <p className="text-xs text-text-muted">Адрес</p>
                </div>
              )}
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-2">
            <Button
              variant="ghost"
              size="md"
              className="flex-1"
              onClick={() => setChatOpen(true)}
            >
              <MessageSquare size={16} />
              Чат
            </Button>

            {!followMode && (
              <Button
                variant="outline"
                size="md"
                className="flex-1"
                onClick={() => setFollowMode(true)}
              >
                <Crosshair size={16} />
                Центр
              </Button>
            )}

            <Button
              variant="danger"
              size="md"
              className="flex-1"
              onClick={() => endNav.mutate()}
              loading={endNav.isPending}
            >
              <Square size={14} />
              Завершить
            </Button>
          </div>
        </div>
      </div>

      {/* Close nav (top-right) */}
      <button
        onClick={() => navigate(-1)}
        className="absolute top-4 right-4 z-[500] w-9 h-9 rounded-full bg-bg-surface/90 border border-bg-border flex items-center justify-center text-text-secondary hover:text-text-primary"
      >
        <X size={16} />
      </button>

      {/* Chat */}
      {chatOpen && <ChatPanel taskId={taskId} onClose={() => setChatOpen(false)} overlay />}
    </div>
  )
}
