# Footie Draw Simulator — Frontend

This is a minimal React + Vite frontend for the Footie draw simulator.

Quick start:

```bash
cd frontend
npm install
npm run dev
```

- The frontend expects the backend API at `http://localhost:8080` by default.
- To point to a different backend base URL, set `VITE_API_BASE` when running, e.g.:

```bash
VITE_API_BASE=http://localhost:8081 npm run dev
```

API client: `src/api.js` currently posts to `/api/draw`. Update the path if your backend exposes a different route.
