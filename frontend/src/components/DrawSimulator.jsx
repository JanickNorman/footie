import React, { useState } from 'react'
import { runDraw, getTeams } from '../api'

export default function DrawSimulator(){
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  async function handleSimulate(){
    setLoading(true)
    setError(null)
    try{
      // fetch draw and teams in parallel so we can show flags
      const [draw, teams] = await Promise.all([runDraw(), getTeams()])
      // build name -> flagUrl map
      const flagMap = {}
      if (Array.isArray(teams)) {
        teams.forEach(t => {
          if (t && t.name) flagMap[t.name] = t.flagUrl || t.flag_url || t.flag || (t.code ? `/flags/${t.code}.svg` : null)
        })
      }
      setResult({ draw, flagMap })
    }catch(e){
      setError(e.message || 'Request failed')
    }finally{
      setLoading(false)
    }
  }

  const renderResult = () => {
    if (!result) return null
    const { draw, flagMap } = result
    const groups = Object.entries(draw)
    return (
      <div className="draw-result">
        <div className="groups-grid">
          {groups.map(([group, teams]) => (
            <div key={group} className="group-card">
              <h3>{group}</h3>
              <ul className="team-list">
                {teams.map((teamName, i) => {
                  const src = flagMap[teamName]
                  const imgSrc = src && src.startsWith('/') ? (import.meta.env.VITE_API_BASE || 'http://localhost:8080') + src : src
                  return (
                    <li key={i} className="team-row">
                      {imgSrc ? (
                        <img src={imgSrc} alt={`flag ${teamName}`} />
                      ) : (
                        <span style={{display:'inline-block', width:36}} />
                      )}
                      <span className="team-name">{teamName || '—'}</span>
                    </li>
                  )
                })}
              </ul>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="draw-sim">
      <button onClick={handleSimulate} disabled={loading}>
        {loading ? 'Simulating...' : 'Simulate Draw'}
      </button>
      {error && <div className="error">{error}</div>}
      {renderResult()}
    </div>
  )
}
