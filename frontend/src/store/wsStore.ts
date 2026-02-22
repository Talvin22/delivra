import { create } from 'zustand'
import { Client, type StompSubscription } from '@stomp/stompjs'

interface PendingSub {
  topic: string
  cb: (body: unknown) => void
  stomp?: StompSubscription
}

interface WsState {
  client: Client | null
  connected: boolean
  connect: (token: string) => void
  disconnect: () => void
  subscribe: (topic: string, cb: (body: unknown) => void) => () => void
  publish: (destination: string, body: object) => void
}

// Subscription registry — survives reconnects
const registry = new Map<string, PendingSub>()

function resubscribeAll(client: Client) {
  registry.forEach(entry => {
    entry.stomp = client.subscribe(entry.topic, msg => {
      try { entry.cb(JSON.parse(msg.body)) } catch { /* ignore parse errors */ }
    })
  })
}

export const useWsStore = create<WsState>((set, get) => ({
  client: null,
  connected: false,

  connect: (token) => {
    const existing = get().client
    if (existing?.active) return

    const proto = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const client = new Client({
      brokerURL: `${proto}://${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,

      onConnect: () => {
        set({ connected: true })
        resubscribeAll(client)
      },

      onDisconnect: () => set({ connected: false }),
      onStompError: () => set({ connected: false }),
    })

    client.activate()
    set({ client })
  },

  disconnect: () => {
    get().client?.deactivate()
    registry.clear()
    set({ client: null, connected: false })
  },

  subscribe: (topic, cb) => {
    const id = `${topic}::${Math.random().toString(36).slice(2)}`
    const entry: PendingSub = { topic, cb }

    // If already connected — subscribe immediately
    const { client, connected } = get()
    if (client && connected) {
      entry.stomp = client.subscribe(topic, msg => {
        try { entry.cb(JSON.parse(msg.body)) } catch { /* ignore */ }
      })
    }
    // Otherwise it will be established in onConnect → resubscribeAll

    registry.set(id, entry)

    return () => {
      entry.stomp?.unsubscribe()
      registry.delete(id)
    }
  },

  publish: (destination, body) => {
    const { client, connected } = get()
    if (client && connected) {
      client.publish({ destination, body: JSON.stringify(body) })
    }
  },
}))
