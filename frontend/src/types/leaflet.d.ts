// Side-effect-only plugin (patches L.Map prototype)
declare module 'leaflet-rotate'

// Augment leaflet types for leaflet-rotate plugin
declare module 'leaflet' {
  interface MapOptions {
    rotate?: boolean
  }
  interface Map {
    setBearing(bearing: number): void
    getBearing(): number
  }
}
