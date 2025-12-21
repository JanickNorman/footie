import React from 'react'
import { Link } from 'react-router-dom'

export default function TournamentList({tournaments}){
  return (
    <div>
      <h2>Tournaments</h2>
      <ul>
        {tournaments.map(t => (
          <li key={t.id}><Link to={`/t/${t.id}`}>{t.name}</Link></li>
        ))}
      </ul>
    </div>
  )
}
