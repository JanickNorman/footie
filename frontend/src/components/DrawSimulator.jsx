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
      const [draw] = await Promise.all([runDraw()])
      console.log('draw result', draw);
      setResult({ draw })
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
                {teams.map((team, i) => {
                  const imgSrc = team.flagUrl
                  return (
                    <li key={i} className="team-row">
                      {imgSrc ? (
                        <img src={imgSrc} alt={`flag ${team.code}`} />
                      ) : (
                        <span style={{display:'inline-block', width:36}} />
                      )}
                      <span className="team-name">{team.name || '—'}</span>
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
