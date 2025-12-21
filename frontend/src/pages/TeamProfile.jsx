import React from 'react'

export default function TeamProfile({team}){
  if(!team) return <div>Team not found</div>
  return (
    <div>
      <h2>{team.name}</h2>
      <p>Country: {team.country}</p>
      <p>Year founded: {team.founded}</p>
      <div dangerouslySetInnerHTML={{__html: team.html || ''}} />
    </div>
  )
}
