import React from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import TournamentList from './pages/TournamentList'
import TournamentPage from './pages/TournamentPage'
import TeamProfile from './pages/TeamProfile'
import WorldCupPage from './pages/WorldCupPage'

import DrawSimulator from './components/DrawSimulator'

export default function App(){
  const tournaments = []
  return (
    <BrowserRouter>
      <div className="app">
        <header>
          <h1><Link to="/">Footie Archive</Link></h1>
        </header>
        <Routes>
          <Route path="/tourney" element={<TournamentList tournaments={tournaments} />} />
          <Route path="/" element={<DrawSimulator />} />
          <Route path="/worldcup" element={<WorldCupPage />} />
          <Route path="/tourney/:id" element={<RouteWrapper Component={TournamentPage} items={tournaments} />} />
          <Route path="/tourney/team/:id" element={<RouteWrapper Component={TeamProfile} items={tournaments} />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

function RouteWrapper({Component, items}){
  // simple router helper to find item by id param
  const { pathname } = window.location
  const parts = pathname.split('/').filter(Boolean)
  const id = parts[1] || parts[0]
  // find tournament or team
  if(pathname.startsWith('/t/')){
    const t = items.find(x => x.id === id)
    return <Component tournament={t} />
  }
  if(pathname.startsWith('/team/')){
    // find team across tournaments
    let team
    for(const t of items){
      team = t.teams.find(tm => tm.id === id)
      if(team) {
        team.html = fetch(`/archive/teams/${id}.htm`).then(r => r.text()).then(s => team.html = s).catch(()=>{})
        break
      }
    }
    return <Component team={team} />
  }
  return <div>Not found</div>
}
