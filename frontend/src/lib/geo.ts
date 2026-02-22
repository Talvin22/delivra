import type { Waypoint } from '@/types/api'

export function haversineM(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371000
  const φ1 = (lat1 * Math.PI) / 180
  const φ2 = (lat2 * Math.PI) / 180
  const Δφ = ((lat2 - lat1) * Math.PI) / 180
  const Δλ = ((lng2 - lng1) * Math.PI) / 180
  const a = Math.sin(Δφ / 2) ** 2 + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

export function calculateBearing(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const dL = ((lng2 - lng1) * Math.PI) / 180
  const r1 = (lat1 * Math.PI) / 180
  const r2 = (lat2 * Math.PI) / 180
  return (
    ((Math.atan2(
      Math.sin(dL) * Math.cos(r2),
      Math.cos(r1) * Math.sin(r2) - Math.sin(r1) * Math.cos(r2) * Math.cos(dL),
    ) * 180) /
      Math.PI +
      360) %
    360
  )
}

export function closestWaypointIdx(lat: number, lng: number, waypoints: Waypoint[]): number {
  let minDist = Infinity
  let minIdx = 0
  for (let i = 0; i < waypoints.length; i++) {
    const d = haversineM(lat, lng, waypoints[i].lat, waypoints[i].lng)
    if (d < minDist) { minDist = d; minIdx = i }
  }
  return minIdx
}

export function calcRemainingDist(lat: number, lng: number, waypoints: Waypoint[]): number {
  if (waypoints.length < 2) return 0
  const idx = closestWaypointIdx(lat, lng, waypoints)
  let dist = haversineM(lat, lng, waypoints[idx].lat, waypoints[idx].lng)
  for (let i = idx; i < waypoints.length - 1; i++) {
    dist += haversineM(waypoints[i].lat, waypoints[i].lng, waypoints[i + 1].lat, waypoints[i + 1].lng)
  }
  return dist
}

export function smoothHeading(current: number, next: number, alpha = 0.25): number {
  let diff = next - current
  if (diff > 180) diff -= 360
  if (diff < -180) diff += 360
  return (current + diff * alpha + 360) % 360
}
