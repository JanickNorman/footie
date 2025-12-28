import React, {useState, useMemo} from 'react'
import Flag from './Flag'
import MatchCard from './MatchCard'

function computeStats(group){
  const stats = {}
  for(const t of group.teams) stats[t.id] = {team:t, P:0, W:0, D:0, L:0, GF:0, GA:0, Pts:0, GD:0, form:[]}
  // iterate matches in chronological order to build stats and form
  for(const m of (group.matches||[])){
    const h = m.home, a = m.away
    const hs = Number(m.homeScore)
    const as = Number(m.awayScore)
    // skip if incomplete score or teams missing
    if(!Number.isFinite(hs) || !Number.isFinite(as)) continue
    if(!stats[h.id] || !stats[a.id]) continue
    const sh = stats[h.id]
    const sa = stats[a.id]
    sh.P++; sa.P++
    sh.GF += hs; sh.GA += as
    sa.GF += as; sa.GA += hs
    if(hs>as){ sh.W++; sa.L++; sh.Pts += 3; sh.form.push('W'); sa.form.push('L') }
    else if(hs<as){ sa.W++; sh.L++; sa.Pts += 3; sa.form.push('W'); sh.form.push('L') }
    else { sh.D++; sa.D++; sh.Pts += 1; sa.Pts += 1; sh.form.push('D'); sa.form.push('D') }
    // cap form length to last 5
    if(sh.form.length>5) sh.form = sh.form.slice(-5)
    if(sa.form.length>5) sa.form = sa.form.slice(-5)
  }
  for(const id in stats){ const s = stats[id]; s.GD = s.GF - s.GA }
  return Object.values(stats).sort((a,b)=> b.Pts - a.Pts || b.GD - a.GD || b.GF - a.GF)
}

export default function GroupStage({groups=[]}){
  const [open, setOpen] = useState({})
  return (
    <div className="grid-12" style={{marginTop:16}}>
      {groups.map((g, idx) => {
        const rows = useMemo(()=> computeStats(g), [g])
        const spanClass = 'col-12'
        return (
          <div id={`group-${g.name}`} key={g.name} className={`group-card ${spanClass}`}>
            <h3 id={`group-${g.name}-title`}>Group {g.name}</h3>

            <div className="group-table">
              <div className="group-table-header">
                <div>Team</div>
                <div style={{textAlign:'center'}}>P</div>
                <div style={{textAlign:'center'}}>W</div>
                <div style={{textAlign:'center'}}>D</div>
                <div style={{textAlign:'center'}}>L</div>
                <div style={{textAlign:'center'}}>For</div>
                <div style={{textAlign:'center'}}>Against</div>
                <div style={{textAlign:'center'}}>GD</div>
                <div style={{textAlign:'center'}}>Pts</div>
                <div style={{textAlign:'center'}}>Form</div>
              </div>
              <div className="group-table-body">
                {rows.map((r,i)=>(
                  <div key={r.team.id} className={`group-row ${i<2? 'top-two':''}`}>
                    <div className="team-cell">
                      <Flag team={r.team} />
                      <div className="team-name">{r.team.name}</div>
                    </div>
                    <div style={{textAlign:'center'}}>{r.P}</div>
                    <div style={{textAlign:'center'}}>{r.W}</div>
                    <div style={{textAlign:'center'}}>{r.D}</div>
                    <div style={{textAlign:'center'}}>{r.L}</div>
                    <div style={{textAlign:'center'}}>{r.GF}</div>
                    <div style={{textAlign:'center'}}>{r.GA}</div>
                    <div style={{textAlign:'center'}}>{r.GD}</div>
                    <div style={{textAlign:'center',fontWeight:700}}>{r.Pts}</div>
                    <div style={{display:'flex',gap:6,justifyContent:'center',alignItems:'center'}}>
                      {(r.form||[]).slice(-5).map((f,ii)=> (
                        <span key={ii} className="form-dot" title={f} style={{background: f==='W'? '#2ecc71' : f==='D'? '#9aa0a6' : '#ff6b6b'}} />
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div style={{marginTop:8}}>
              <button onClick={()=>setOpen(s=>({...s,[g.name]:!s[g.name]}))}>{open[g.name]? 'Hide' : 'Show'} matches</button>
              {open[g.name] && (
                <div style={{marginTop:8,fontSize:13,display:'flex',flexDirection:'column',gap:12}}>
                  {(g.matches||[]).length===0 ? <div>No match data</div> : (
                    (g.matches||[]).map((m,i)=>(
                      <MatchCard key={i} match={m} />
                    ))
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
