import React from 'react'
import Flag from './Flag'

function ScorerList({scorers, side}){
  if(!scorers || scorers.length===0) return <div style={{opacity:0.6,fontSize:12}}>—</div>
  return (
    <div style={{display:'flex',flexDirection:'column',gap:4,fontSize:13}}>
      {scorers.map((s,i)=> (
        <div key={i} style={{display:'flex',gap:8,alignItems:'center'}}>
          <div style={{color:'#666'}}>{s.minute}'</div>
          <div style={{whiteSpace:'nowrap'}}>{s.player}</div>
        </div>
      ))}
    </div>
  )
}

export default function MatchCard({match}){
  const home = match.home, away = match.away
  const hs = match.homeScore == null ? '-' : String(match.homeScore)
  const as_ = match.awayScore == null ? '-' : String(match.awayScore)
  const containerStyle = {border:'1px solid #e6e6e6',borderRadius:8,padding:16,display:'flex',gap:16,alignItems:'center',background:'#fff'}
  const teamCol = {flex:1,display:'flex',flexDirection:'column',gap:8}
  const scoreBox = {width:96,display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center'}

  return (
    <div style={containerStyle}>
      <div style={teamCol}>
        <div style={{display:'flex',alignItems:'center',gap:12}}>
          <Flag team={home} size={22} />
          <div style={{fontWeight:600}}>{home.name}</div>
        </div>
        <div style={{marginTop:8}}>
          <ScorerList scorers={(match.scorers||[]).filter(s=>s.team==='home')} />
        </div>
      </div>

      <div style={scoreBox}>
        <div style={{fontSize:28,fontWeight:700}}>{hs} <span style={{margin:'0 8px'}}>—</span> {as_}</div>
        <div style={{color:'#888',fontSize:12,marginTop:8}}>{match.venue || ''} {match.time? '· '+match.time: ''}</div>
      </div>

      <div style={teamCol}>
        <div style={{display:'flex',alignItems:'center',gap:12,justifyContent:'flex-end'}}>
          <div style={{fontWeight:600}}>{away.name}</div>
          <Flag team={away} size={22} />
        </div>
        <div style={{marginTop:8,display:'flex',justifyContent:'flex-end'}}>
          <ScorerList scorers={(match.scorers||[]).filter(s=>s.team==='away')} />
        </div>
      </div>
    </div>
  )
}
