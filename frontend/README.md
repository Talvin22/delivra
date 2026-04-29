# Delivra — Frontend

Single-page application for the [Delivra](../README.md) delivery platform. Three role-based UIs (dispatcher, driver, admin) plus a public landing page, all built on React 19 + Vite + Tailwind.

## Stack

- **React 19** + **TypeScript** + **Vite**
- **Tailwind CSS** + **Radix UI** primitives
- **React Router** for routing, **Zustand** for client state, **TanStack Query** for server state
- **React Hook Form** + **Zod** for forms and validation
- **Leaflet** (`react-leaflet`, `leaflet-rotate`) for the map
- **STOMP.js** + **Axios** for WebSocket and REST transport

## Scripts

| Command | What it does |
| --- | --- |
| `pnpm dev` | Start the Vite dev server with HMR (default: <http://localhost:5173>) |
| `pnpm build` | Type-check and produce a production build in `dist/` |
| `pnpm preview` | Serve the production build locally |
| `pnpm lint` | Run ESLint over the project |

## Getting started

```bash
pnpm install
pnpm dev
```

The dev server expects the backend at <http://localhost:8189>. The backend's `WS_ALLOWED_ORIGINS` must include the frontend origin (default `http://localhost:5173`).

## Production build

`pnpm build` is invoked automatically by Maven through the `frontend-maven-plugin`, and the resulting `dist/` is copied into the Spring Boot JAR's static resources. You normally don't need to run it by hand — `./mvnw package` from the repo root builds the whole app into a single artifact.

## Structure

```
src/
├── api/            # axios clients per backend domain (auth, tasks, chat, …)
├── features/
│   ├── landing/    # public landing page
│   ├── auth/       # login, register, password reset, email verification
│   ├── dispatcher/ # task management, driver assignment, chat
│   ├── driver/     # active task, navigation, chat
│   ├── admin/      # company moderation, platform stats
│   └── report/     # Excel exports
├── components/
│   ├── ui/         # generic Radix-based primitives
│   ├── chat/       # message list, composer, file upload
│   └── layout/     # shells, navigation, role guards
├── hooks/          # reusable hooks (auth, websocket, geolocation, …)
├── store/          # Zustand stores
├── lib/            # helpers (formatters, jwt, query client config)
└── types/          # shared TypeScript types
```

## Environment

The dev server's API base URL and WebSocket endpoint are configured in `src/api/axios.ts` and the relevant hooks. If you need to point the SPA at a non-default backend, override the values there or introduce a `.env` with `VITE_*` variables.
