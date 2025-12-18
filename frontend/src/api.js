const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export async function runDraw(){
  const res = await fetch(`${BASE}/api/draw`, { method: 'POST' })
  if(!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}

export async function getTeams(){
  const res = await fetch(`${BASE}/api/teams`)
  if(!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}
