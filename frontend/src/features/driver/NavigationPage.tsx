import { useState, useEffect, useRef, useCallback, useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { MapContainer, TileLayer, Polyline, Marker, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet-rotate'
import 'leaflet/dist/leaflet.css'
import { X, Crosshair, MessageSquare, CheckCircle } from 'lucide-react'
import { navigationApi } from '@/api/navigation'
import { tasksApi } from '@/api/tasks'
import { useWsStore } from '@/store/wsStore'
import { useGps } from '@/hooks/useGps'
import { useWakeLock } from '@/hooks/useWakeLock'
import { calcRemainingDist, closestWaypointIdx } from '@/lib/geo'
import { formatDistance, formatDuration } from '@/lib/formatters'
import { ChatPanel } from '@/components/chat/ChatPanel'
import { Button } from '@/components/ui/Button'
import { FullScreenLoader } from '@/components/ui/Spinner'
import type { NavigationEventDTO, RouteDTO, Waypoint } from '@/types/api'

// ─── Driver SVG marker ────────────────────────────────────────────
// leaflet-rotate rotates the entire map pane (including marker pane) by the bearing.
// So the SVG must NOT have its own rotation — the map rotation already points it forward.
function makeDriverIcon() {
  return L.divIcon({
    className: '',
    html: `<svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 44 44">
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

// ─── Map controller (follow mode + rotation) ──────────────────────
// Design decisions:
// • blockBearingRef — set SYNCHRONOUSLY in rotatestart (before React re-render)
//   to prevent the race condition where GPS fires setBearing before follow=false lands.
// • Distance threshold for panTo — GPS noise (< 5 m) causes constant micro-pan
//   animations that overlap and produce jitter/flickering.
// • Bearing threshold — skip setBearing for < 3° changes (GPS heading noise).
// • justEnabled — when re-center is pressed, always snap to driver's heading (3rd-person view).

const BEARING_THRESHOLD = 3   // degrees — ignore smaller changes
const PAN_THRESHOLD_M    = 5   // metres  — ignore GPS noise jitter

interface MapControllerProps {
  center: [number, number] | null
  follow: boolean
  bearing: number
  onManualMove: () => void
}

function MapController({ center, follow, bearing, onManualMove }: MapControllerProps) {
  const map = useMap()
  const followRef        = useRef(follow)
  const onMoveRef        = useRef(onManualMove)
  const blockBearingRef  = useRef(false)   // true = manual rotation in flight → block setBearing
  const lastBearingRef   = useRef<number | null>(null)
  const prevFollowPanRef = useRef(follow)
  const prevFollowBrgRef = useRef(follow)
  const lastCenterRef    = useRef<[number, number] | null>(null)

  followRef.current = follow
  onMoveRef.current = onManualMove

  // ── Gesture listeners (stable, no deps on callbacks) ──────────────
  useEffect(() => {
    const onRotateStart = () => {
      // Block SYNCHRONOUSLY — prevents GPS from overriding user rotation
      // before React processes setFollowMode(false)
      blockBearingRef.current = true
      if (followRef.current) onMoveRef.current()
    }
    const onDragStart = () => {
      if (followRef.current) onMoveRef.current()
    }
    map.on('dragstart',   onDragStart)
    map.on('rotatestart', onRotateStart)
    return () => { map.off('dragstart', onDragStart); map.off('rotatestart', onRotateStart) }
  }, [map])  // stable — no changing deps

  // ── Pan to driver (distance-gated to suppress GPS noise) ──────────
  useEffect(() => {
    if (!follow || !center) {
      prevFollowPanRef.current = follow
      return
    }
    const justEnabled = !prevFollowPanRef.current && follow
    prevFollowPanRef.current = follow

    if (!justEnabled && lastCenterRef.current) {
      const [lat, lng] = center
      const [pLat, pLng] = lastCenterRef.current
      const dLat = (lat - pLat) * 111_320
      const dLng = (lng - pLng) * 111_320 * Math.cos(lat * Math.PI / 180)
      if (Math.sqrt(dLat * dLat + dLng * dLng) < PAN_THRESHOLD_M) return
    }

    lastCenterRef.current = center
    // Re-center button: animate back to driver. Continuous follow: no animation
    // (avoids overlapping pan animations from rapid GPS updates causing flicker).
    if (justEnabled) {
      map.panTo(center, { animate: true, duration: 0.4, easeLinearity: 0.8 })
    } else {
      map.setView(center, map.getZoom(), { animate: false })
    }
  }, [map, center, follow])

  // ── Set bearing when following ─────────────────────────────────────
  useEffect(() => {
    if (!follow) {
      prevFollowBrgRef.current = false
      return
    }
    const justEnabled = !prevFollowBrgRef.current && follow
    prevFollowBrgRef.current = follow

    if (justEnabled) {
      // Re-center button pressed → unblock and restore 3rd-person view
      blockBearingRef.current = false
      lastBearingRef.current  = bearing
      map.setBearing(bearing)
      return
    }

    if (blockBearingRef.current) return  // user is rotating manually

    // Threshold — don't jitter on GPS heading noise
    if (lastBearingRef.current !== null) {
      const diff = Math.abs(bearing - lastBearingRef.current)
      if (Math.min(diff, 360 - diff) < BEARING_THRESHOLD) return
    }

    lastBearingRef.current = bearing
    map.setBearing(bearing)
  }, [map, bearing, follow])

  return null
}

// ─── Turn action icons ─────────────────────────────────────────────
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
  const routeFetchedRef  = useRef(false)
  // Last position that was committed to React state — used to filter GPS noise
  const lastCommittedPos = useRef<[number, number] | null>(null)

  useWakeLock(!ended)

  // Memoize icon — recreating on every render causes Leaflet to re-render the marker (jerkiness)
  const driverIcon = useMemo(() => makeDriverIcon(), [])

  // ── Load task + active session ───────────────────────────────────
  const { data: task } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => tasksApi.getById(taskId).then(r => r.data.payload),
  })

  const { isLoading: sessionLoading, data: sessionData, isError: sessionError } = useQuery({
    queryKey: ['nav-session', taskId],
    queryFn: () => navigationApi.getActive(taskId).then(r => r.data.payload),
    retry: false,
  })

  useEffect(() => {
    if (!sessionData) return
    setSessionId(sessionData.sessionId)
    if (sessionData.route?.waypoints?.length) {
      setRoute(sessionData.route)
      setTrimmedWaypoints(sessionData.route.waypoints)
    }
  }, [sessionData])

  // ── Fetch fresh route (with instructions) on first GPS fix ───────
  useEffect(() => {
    if (routeFetchedRef.current || !sessionId || !driverPos) return
    routeFetchedRef.current = true
    tasksApi.getRoute(taskId, { originLatitude: driverPos[0], originLongitude: driverPos[1] })
      .then(r => {
        const fresh = r.data.payload
        if (fresh?.waypoints?.length) {
          setRoute(fresh)
          setTrimmedWaypoints(fresh.waypoints)
        }
      })
      .catch(() => {}) // keep session waypoints if route fetch fails
  }, [driverPos, sessionId, taskId])

  // ── Update nav progress (trim route + calc remaining) ────────────
  const updateProgress = useCallback((lat: number, lng: number) => {
    if (!route || route.waypoints.length < 2) return
    const wpts = route.waypoints
    const idx = closestWaypointIdx(lat, lng, wpts)
    setTrimmedWaypoints(wpts.slice(Math.max(0, idx)))

    const rem = calcRemainingDist(lat, lng, wpts)
    setRemainingM(rem)

    // Estimate remaining time from proportion of total distance
    const totalDist = route.distanceInMeters ?? 0
    const totalSec  = route.durationInSeconds ?? 0
    if (totalDist > 0 && totalSec > 0) {
      setRemainingSec(Math.round(totalSec * (rem / totalDist)))
    }

    // Find current instruction index
    const instructions = route.instructions
    if (instructions?.length > 0 && totalDist > 0) {
      const traveled = totalDist - rem
      let cum = 0
      let iIdx = 0
      for (let i = 0; i < instructions.length; i++) {
        if (cum <= traveled) iIdx = i
        cum += instructions[i].distanceInMeters
      }
      setInstrIdx(Math.min(iIdx + 1, instructions.length - 1))
    }
  }, [route])

  // ── GPS ──────────────────────────────────────────────────────────
  const handleGpsPosition = useCallback((lat: number, lng: number, h: number, accuracy: number) => {
    // h is already smoothed by useGps — no double-smoothing here
    headingRef.current = h
    setHeading(h)

    // Adaptive threshold: when GPS is still stabilising (high accuracy = large error radius),
    // require the device to move more before updating the marker.
    // clamp between 8 m (normal) and 60 m (very poor signal).
    const threshold = Math.min(60, Math.max(8, accuracy * 0.6))

    const prev = lastCommittedPos.current
    const movedEnough = !prev || (() => {
      const dLat = (lat - prev[0]) * 111_320
      const dLng = (lng - prev[1]) * 111_320 * Math.cos(lat * Math.PI / 180)
      return Math.sqrt(dLat * dLat + dLng * dLng) >= threshold
    })()

    if (movedEnough) {
      lastCommittedPos.current = [lat, lng]
      setDriverPos([lat, lng])
      updateProgress(lat, lng)
    }

    if (sessionId && connected) {
      publish(`/app/navigation/${sessionId}/position`, { latitude: lat, longitude: lng })
    }
  }, [sessionId, connected, publish, updateProgress])

  const gps = useGps(!ended, handleGpsPosition)

  // ── WebSocket events ─────────────────────────────────────────────
  useEffect(() => {
    if (!taskId) return
    const u1 = subscribe(`/topic/navigation/${taskId}/position`, (data) => {
      const event = data as NavigationEventDTO
      setOnRoute(event.onRoute)
    })
    const u2 = subscribe(`/topic/navigation/${taskId}/route`, (data) => {
      const event = data as NavigationEventDTO
      if (event.route) {
        setRoute(event.route)
        setTrimmedWaypoints(event.route.waypoints ?? [])
      }
    })
    return () => { u1(); u2() }
  }, [taskId, subscribe])

  // ── End navigation (just stop, don't complete task) ──────────────
  const stopNav = useMutation({
    mutationFn: () => navigationApi.end(taskId).catch(() => {}),
    onSuccess: () => { setEnded(true); navigate(`/driver/tasks/${taskId}`) },
  })

  // ── Complete task (end navigation + mark COMPLETED) ──────────────
  const completeTask = useMutation({
    mutationFn: async () => {
      await navigationApi.end(taskId).catch(() => {}) // ignore if already ended
      await tasksApi.update(taskId, { status: 'COMPLETED' })
    },
    onSuccess: () => { setEnded(true); navigate(`/driver`) },
  })

  if (sessionLoading) return <FullScreenLoader />

  if (sessionError) {
    return (
      <div className="flex flex-col items-center justify-center h-screen gap-4 bg-bg-base px-6">
        <p className="text-text-primary font-semibold text-center">Navigation session not found</p>
        <p className="text-text-muted text-sm text-center">Go back to the task and start navigation again</p>
        <Button onClick={() => navigate(-1)}>Back</Button>
      </div>
    )
  }

  const dest = route?.waypoints?.at(-1)
  const instruction = route?.instructions?.[instrIdx]
  const isPending = stopNav.isPending || completeTask.isPending

  return (
    <div className="relative w-full bg-bg-base overflow-hidden" style={{ height: '100dvh' }}>

      {/* ── Map ───────────────────────────────────────────────────── */}
      <MapContainer
        center={driverPos ?? [50.45, 30.52]}
        zoom={16}
        className="w-full h-full"
        zoomControl={false}
        rotate={true}
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

        {/* Route polyline — remaining portion */}
        {trimmedWaypoints.length > 1 && (
          <Polyline
            positions={trimmedWaypoints.map(w => [w.lat, w.lng])}
            color="#6c8aff"
            weight={6}
            opacity={0.9}
          />
        )}

        {/* Driver marker */}
        {driverPos && (
          <Marker position={driverPos} icon={driverIcon} zIndexOffset={1000} />
        )}

        {/* Destination marker */}
        {dest && (
          <Marker position={[dest.lat, dest.lng]} icon={makeDestIcon()} />
        )}
      </MapContainer>

      {/* ── Turn instruction bar (top, left-aligned, right margin for X button) ── */}
      {instruction && (
        <div className="absolute top-4 left-4 right-14 z-[500] pointer-events-none">
          <div className="flex items-center gap-3 bg-bg-surface/95 border border-bg-border rounded-xl px-4 py-2.5 shadow-xl backdrop-blur-md">
            <span className="text-2xl leading-none flex-shrink-0">
              {ACTION_ICONS[instruction.action] ?? '↑'}
            </span>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-text-primary truncate">{instruction.instruction}</p>
              {instruction.distanceInMeters > 0 && (
                <p className="text-xs text-brand mt-0.5">in {formatDistance(instruction.distanceInMeters)}</p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* ── Off-route alert ──────────────────────────────────────── */}
      {!onRoute && (
        <div className="absolute top-20 left-1/2 -translate-x-1/2 z-[500] pointer-events-none">
          <div className="flex items-center gap-2 bg-warning/10 border border-warning/40 rounded-lg px-3 py-1.5">
            <span className="text-warning text-xs">⚠</span>
            <span className="text-xs text-warning font-medium">Rerouting...</span>
          </div>
        </div>
      )}

      {/* ── GPS status (top left) ─────────────────────────────────── */}
      {gps.status !== 'ok' && (
        <div className="absolute top-4 left-4 z-[500] pointer-events-none">
          <div className="flex items-center gap-2 bg-bg-surface/90 border border-bg-border rounded-lg px-2.5 py-1.5">
            <span className={`w-2 h-2 rounded-full ${gps.status === 'searching' ? 'bg-warning animate-pulse' : 'bg-danger'}`} />
            <span className="text-xs text-text-secondary">{gps.statusText}</span>
          </div>
        </div>
      )}

      {/* ── Close / back button (top right) ──────────────────────── */}
      <button
        onClick={() => navigate(-1)}
        className="absolute top-4 right-4 z-[500] w-9 h-9 rounded-full bg-bg-surface/90 border border-bg-border flex items-center justify-center text-text-secondary hover:text-text-primary"
      >
        <X size={16} />
      </button>

      {/* ── Re-center FAB (right side, above HUD) ────────────────── */}
      <button
        onClick={() => setFollowMode(true)}
        className={`absolute right-4 bottom-44 z-[500] w-11 h-11 rounded-full shadow-lg border flex items-center justify-center transition-all
          ${followMode
            ? 'bg-bg-surface/70 border-bg-border text-text-muted'
            : 'bg-brand border-brand text-white shadow-brand/40'
          }`}
        title="Re-center"
      >
        <Crosshair size={20} />
      </button>

      {/* ── Bottom HUD ───────────────────────────────────────────── */}
      <div className="absolute bottom-0 left-0 right-0 z-[500]">
        <div className="mx-3 mb-3 bg-bg-surface/95 border border-bg-border rounded-xl p-3 backdrop-blur-md shadow-xl">

          {/* Stats row */}
          {(remainingM !== null || task) && (
            <div className="flex items-center justify-around mb-3 pb-3 border-b border-bg-border">
              {remainingM !== null && (
                <div className="text-center">
                  <p className="text-lg font-bold text-text-primary">{formatDistance(remainingM)}</p>
                  <p className="text-xs text-text-muted">Remaining</p>
                </div>
              )}
              {remainingSec !== null && (
                <div className="text-center">
                  <p className="text-lg font-bold text-text-primary">{formatDuration(remainingSec)}</p>
                  <p className="text-xs text-text-muted">ETA</p>
                </div>
              )}
              {task && (
                <div className="text-center max-w-[140px]">
                  <p className="text-xs font-medium text-text-primary truncate">{task.address}</p>
                  <p className="text-xs text-text-muted">Address</p>
                </div>
              )}
            </div>
          )}

          {/* Action buttons */}
          <div className="flex gap-2">
            <Button
              variant="ghost"
              size="md"
              className="flex-1"
              onClick={() => setChatOpen(true)}
            >
              <MessageSquare size={16} />
              Chat
            </Button>

            <Button
              variant="ghost"
              size="md"
              className="flex-1 text-danger hover:text-danger hover:bg-danger/10"
              onClick={() => stopNav.mutate()}
              loading={stopNav.isPending}
              disabled={isPending}
            >
              Abort
            </Button>

            <Button
              variant="success"
              size="md"
              className="flex-1"
              onClick={() => completeTask.mutate()}
              loading={completeTask.isPending}
              disabled={isPending}
            >
              <CheckCircle size={16} />
              Delivered
            </Button>
          </div>
        </div>
      </div>

      {/* ── Chat panel ───────────────────────────────────────────── */}
      {chatOpen && <ChatPanel taskId={taskId} onClose={() => setChatOpen(false)} overlay />}
    </div>
  )
}
