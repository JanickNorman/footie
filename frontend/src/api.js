const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

export async function runDraw({ random } = {}) {
  const res = await fetch(`${BASE}/api/draw`, { 
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ random })
  })
  if(!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}

export async function getTeams(){
  const res = await fetch(`${BASE}/api/teams`)
  if(!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}

export async function getSampleWorldCup(){
  const res = await fetch(`${BASE}/api/worldcup/sample`)
  if(!res.ok) throw new Error(`API error ${res.status}`)
  return res.json()
}
