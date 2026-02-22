// Without export {}, this file is a global script and declare module replaces leaflet types.
// With export {}, this becomes a module file and declare module augments existing types.
export {}

declare module 'leaflet-rotate'

declare module 'leaflet' {
  interface MapOptions {
    rotate?: boolean
  }
  interface Map {
    setBearing(bearing: number): void
    getBearing(): number
  }
}
