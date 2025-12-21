import React from 'react'
import { Link } from 'react-router-dom'

export default function TournamentPage({tournament}){
  if(!tournament) return <div>Not found</div>
  return (
    <div>
      <h2>{tournament.name}</h2>
      <p>{tournament.year}</p>
      <h3>Teams</h3>
      <ul>
        {tournament.teams.map(team => (
          <li key={team.id}><Link to={`/team/${team.id}`}>{team.name}</Link></li>
        ))}
      </ul>
    </div>
  )
}
