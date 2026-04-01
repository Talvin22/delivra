// ─── Generic wrappers ─────────────────────────────────────────────
export interface DelivraResponse<T> {
  payload: T
  error: string | null
  success: boolean
  message?: string
}

export interface PaginationResponse<T> {
  content: T[]
  pagination: { page: number; limit: number; totalElements: number; totalPages: number }
}

// ─── Auth ─────────────────────────────────────────────────────────
export type UserRole = 'DRIVER' | 'DISPATCHER' | 'ADMIN' | 'SUPER_ADMIN'

export interface RoleDTO { id: number; name: string }

export interface UserProfileDTO {
  id: number
  username: string
  email: string
  token: string
  refreshToken: string
  roles: RoleDTO[]
  lastLogin: string | null
}

// ─── Users ────────────────────────────────────────────────────────
export type RegistrationStatus = 'ACTIVE' | 'INACTIVE'

export interface UserDTO {
  id: number
  username: string
  email: string
  created: string
  lastLogin: string | null
  registrationStatus: RegistrationStatus
  roles: RoleDTO[]
}

export interface UserSearchDTO {
  id: number
  username: string
  email: string
  deleted: boolean
  roles: RoleDTO[]
}

// ─── Tasks ────────────────────────────────────────────────────────
export type DeliveryTaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELED'

export interface DeliveryTaskDTO {
  id: number
  status: DeliveryTaskStatus
  address: string
  latitude: number | null
  longitude: number | null
  startTime: string | null
  endTime: string | null
  created: string
  updated: string
  createdBy: string
  userId: number | null
}

// ─── Route ────────────────────────────────────────────────────────
export interface Waypoint { lat: number; lng: number }

export interface RouteInstructionDTO {
  action: string
  instruction: string
  durationInSeconds: number
  distanceInMeters: number
}

export interface RouteDTO {
  polyline: string
  durationInSeconds: number
  distanceInMeters: number
  waypoints: Waypoint[]
  instructions: RouteInstructionDTO[]
}

// ─── Navigation ───────────────────────────────────────────────────
export type NavigationSessionStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED'

export interface NavigationSessionDTO {
  sessionId: number
  taskId: number
  status: NavigationSessionStatus
  startedAt: string
  route: RouteDTO | null
}

export type NavigationEventType = 'POSITION' | 'ROUTE_UPDATE'

export interface NavigationEventDTO {
  type: NavigationEventType
  taskId: number
  latitude: number
  longitude: number
  onRoute: boolean
  route: RouteDTO | null
}

// ─── Chat ─────────────────────────────────────────────────────────
export interface ChatMessageDTO {
  id: number
  taskId: number
  senderId: number
  senderUsername: string
  messageText: string
  isRead: boolean
  created: string
}

// ─── Driver recommendation ────────────────────────────────────────
export interface DriverRecommendationDTO {
  driverId: number
  driverUsername: string
  driverEmail: string
  busy: boolean
  totalScore: number
  proximityScore: number
  workloadScore: number
  successRateScore: number
  recencyScore: number
  distanceMeters: number | null
  pendingTasksCount: number
  successRate: number
  hoursSinceLastActivity: number | null
}

// ─── Driver position (local state, from WS) ───────────────────────
export interface DriverPosition {
  taskId: number
  userId: number | null
  username: string
  lat: number
  lng: number
  onRoute: boolean
  updatedAt: number // Date.now()
}
