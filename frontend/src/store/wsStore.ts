import { create } from 'zustand'
import { Client } from '@stomp/stompjs'

interface WsState {
  client: Client | null
  connected: boolean
  connect: (token: string) => void
  disconnect: () => void
  subscribe: (topic: string, cb: (body: unknown) => void) => (() => void)
  publish: (destination: string, body: object) => void
}

export const useWsStore = create<WsState>((set, get) => ({
  client: null,
  connected: false,

  connect: (token) => {
    const existing = get().client
    if (existing?.connected) return

    const proto = window.location.protocol === 'https:' ? 'wss' : 'ws'
    const client = new Client({
      brokerURL: `${proto}://${window.location.host}/ws`,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => set({ connected: true }),
      onDisconnect: () => set({ connected: false }),
      onStompError: () => set({ connected: false }),
    })
    client.activate()
    set({ client })
  },

  disconnect: () => {
    get().client?.deactivate()
    set({ client: null, connected: false })
  },

  subscribe: (topic, cb) => {
    const { client } = get()
    if (!client) return () => {}
    const sub = client.subscribe(topic, msg => {
      try { cb(JSON.parse(msg.body)) } catch {}
    })
    return () => sub.unsubscribe()
  },

  publish: (destination, body) => {
    const { client } = get()
    if (client?.connected) {
      client.publish({ destination, body: JSON.stringify(body) })
    }
  },
}))
