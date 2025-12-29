import React from 'react'

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
}

export default function Flag({team, size=28, url=null}){
  const local = `/archive/teams/${team.id}.png`
  const src = url || FLAG_URLS[team.id] || local
  return (
    <span className="flag-wrapper" style={{display:'inline-flex',alignItems:'center',gap:8}}>
      <img
        src={src}
        alt={`${team.name} flag`}
        style={{width: size, height: 'auto', display: src ? 'inline-block' : 'none', border: '1px solid #ddd', borderRadius: 6}}
        onError={(e) => { e.currentTarget.style.display = 'none' }}
      />
    </span>
  )
}
