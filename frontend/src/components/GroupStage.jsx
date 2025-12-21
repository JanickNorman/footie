import React, {useState, useMemo} from 'react'

const FLAG_EMOJI = {
  arg: '🇦🇷', bra: '🇧🇷', eng: '🏴', usa: '🇺🇸', fra: '🇫🇷', mex: '🇲🇽', pol: '🇵🇱', den: '🇩🇰', aus: '🇦🇺'
}

const FLAG_URLS = {
  eng: 'https://upload.wikimedia.org/wikipedia/en/b/be/Flag_of_England.svg',
  arg: 'https://upload.wikimedia.org/wikipedia/commons/1/1a/Flag_of_Argentina.svg',
  bra: 'https://upload.wikimedia.org/wikipedia/en/0/05/Flag_of_Brazil.svg',
  usa: 'https://upload.wikimedia.org/wikipedia/en/a/a4/Flag_of_the_United_States.svg',
  fra: 'https://upload.wikimedia.org/wikipedia/en/c/c3/Flag_of_France.svg',
  mex: 'https://upload.wikimedia.org/wikipedia/commons/f/fc/Flag_of_Mexico.svg',
  pol: 'https://upload.wikimedia.org/wikipedia/en/1/12/Flag_of_Poland.svg',
  den: 'https://upload.wikimedia.org/wikipedia/commons/9/9c/Flag_of_Denmark.svg',
  aus: 'https://upload.wikimedia.org/wikipedia/commons/b/b9/Flag_of_Australia.svg',
  fra_alt: 'https://upload.wikimedia.org/wikipedia/commons/c/c3/Flag_of_France.svg'
}

function Flag({team}){
  const emoji = FLAG_EMOJI[team.id] || FLAG_EMOJI[team.country?.toLowerCase?.()] || '🏳️'
  // prefer known remote SVGs, then local archive PNG, then emoji
  const remote = FLAG_URLS[team.id]
  const local = `/archive/teams/${team.id}.png`
  const src = remote || local
  return (
    <span className="flag-wrapper">
      <img src={src} className="team-flag" alt={`${team.name} flag`} onError={(e)=>{
        // if remote/local image fails, hide it and show emoji fallback
        e.target.style.display='none'
        const s = e.target.parentNode.querySelector('.flag-emoji')
        if(s) s.style.display='inline-block'
      }} />
      <span className="flag-emoji" style={{display: remote? 'none':'inline-block'}}>{emoji}</span>
    </span>
  )
}

function computeStats(group){
  const stats = {}
  for(const t of group.teams) stats[t.id] = {team:t, P:0, W:0, D:0, L:0, GF:0, GA:0, Pts:0, GD:0}
  for(const m of (group.matches||[])){
    const h = m.home, a = m.away
    const hs = Number(m.homeScore)
    const as = Number(m.awayScore)
    if(!Number.isFinite(hs) || !Number.isFinite(as)) continue
    if(!stats[h] || !stats[a]) continue
    stats[h].P++; stats[a].P++
    stats[h].GF += hs; stats[h].GA += as
    stats[a].GF += as; stats[a].GA += hs
    if(hs>as){ stats[h].W++; stats[a].L++; stats[h].Pts += 3 }
    else if(hs<as){ stats[a].W++; stats[h].L++; stats[a].Pts += 3 }
    else { stats[h].D++; stats[a].D++; stats[h].Pts += 1; stats[a].Pts += 1 }
  }
  for(const id in stats){ const s = stats[id]; s.GD = s.GF - s.GA }
  return Object.values(stats).sort((a,b)=> b.Pts - a.Pts || b.GD - a.GD || b.GF - a.GF)
}

export default function GroupStage({groups=[]}){
  const [open, setOpen] = useState({})
  return (
    <div className="groups-grid">
      {groups.map(g => {
        const rows = useMemo(()=> computeStats(g), [g])
        return (
          <div key={g.name} className="group-card">
            <h3>Group {g.name}</h3>
            <div className="group-table-compact">
              {rows.slice(0,4).map((r, i) => (
                <div key={r.team.id} className={`compact-row ${i<2? 'top-two':''}`}>
                  <div className="team-col team-cell">
                    <Flag team={r.team} />
                    <div className="team-name">{r.team.name}</div>
                  </div>
                  <div className="pts-col">{r.Pts}</div>
                </div>
              ))}
            </div>

            <div style={{marginTop:8}}>
              <button onClick={()=>setOpen(s=>({...s,[g.name]:!s[g.name]}))}>{open[g.name]? 'Hide' : 'Show'} matches</button>
              {open[g.name] && (
                <div style={{marginTop:8,fontSize:13}}>
                  {(g.matches||[]).length===0 ? <div>No match data</div> : (
                    <ul style={{paddingLeft:16}}>
                      {(g.matches||[]).map((m,i)=>(<li key={i}>{m.home} {m.homeScore || '-'} - {m.awayScore || '-'} {m.away}</li>))}
                    </ul>
                  )}
                </div>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}
