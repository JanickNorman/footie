import React from 'react'

const Scoreboard = React.forwardRef(function Scoreboard({ collapsed = false }, ref) {
  // Sample match data - you can replace this with API data later
  const matches = [
    { homeTeam: 'ENG', awayTeam: 'FRA', homeScore: 2, awayScore: 1, status: 'FT', competition: 'UEFA' },
    { homeTeam: 'ESP', awayTeam: 'GER', homeScore: 3, awayScore: 2, status: 'FT', competition: 'UEFA' },
    { homeTeam: 'ITA', awayTeam: 'NED', homeScore: 1, awayScore: 1, status: '87\'', competition: 'UEFA' },
    { homeTeam: 'POR', awayTeam: 'BEL', homeScore: 0, awayScore: 0, status: '45\'', competition: 'UEFA' },
  ]

  const height = 40
  const containerStyle = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    height: `${height}px`,
    backgroundColor: '#1a1a1a',
    borderBottom: '1px solid #2a2a2a',
    zIndex: 1200,
    display: 'flex',
    alignItems: 'center',
    overflowX: 'auto',
    overflowY: 'hidden',
    padding: '0 8px',
    gap: '12px',
    transform: collapsed ? `translateY(-${height}px)` : 'translateY(0)',
    transition: 'transform 200ms linear',
    // only animate transform for best perf and avoid repaints
    willChange: 'transform',
    pointerEvents: collapsed ? 'none' : 'auto'
  }

  const matchCardStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    padding: '4px 12px',
    backgroundColor: '#2a2a2a',
    borderRadius: '4px',
    minWidth: '180px',
    height: '32px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
    flexShrink: 0
  }

  const matchCardHoverStyle = {
    ...matchCardStyle,
    backgroundColor: '#333333'
  }

  const competitionStyle = {
    fontSize: '10px',
    color: '#999',
    fontWeight: '600',
    marginRight: '4px',
    letterSpacing: '0.5px'
  }

  const teamStyle = {
    fontSize: '12px',
    fontWeight: '600',
    color: '#ffffff',
    minWidth: '35px',
    textAlign: 'center'
  }

  const scoreStyle = {
    fontSize: '13px',
    fontWeight: '700',
    color: '#ffffff',
    minWidth: '12px',
    textAlign: 'center'
  }

  const statusStyle = {
    fontSize: '10px',
    color: '#ffb800',
    fontWeight: '600',
    marginLeft: '4px'
  }

  const separatorStyle = {
    width: '1px',
    height: '20px',
    backgroundColor: '#444',
    margin: '0 4px'
  }

  return (
    <div style={containerStyle} ref={ref}>
      <div style={{maxWidth:1200,margin:'0 auto',width:'100%',display:'flex',alignItems:'center',gap:'12px',padding:'0 12px'}}>
        <div style={{ fontSize: '11px', color: '#999', fontWeight: '600', marginRight: '8px', flexShrink: 0 }}>
          TOP EVENTS
        </div>
        {matches.map((match, index) => (
          <MatchCard key={index} match={match} />
        ))}
      </div>
    </div>
  )
})

export default Scoreboard

function MatchCard({ match }) {
  const [isHovered, setIsHovered] = React.useState(false)

  const matchCardStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    padding: '4px 12px',
    backgroundColor: isHovered ? '#333333' : '#2a2a2a',
    borderRadius: '4px',
    minWidth: '180px',
    height: '32px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
    flexShrink: 0
  }

  const competitionStyle = {
    fontSize: '10px',
    color: '#999',
    fontWeight: '600',
    marginRight: '2px',
    letterSpacing: '0.5px'
  }

  const teamStyle = {
    fontSize: '12px',
    fontWeight: '600',
    color: '#ffffff',
    minWidth: '30px',
    textAlign: 'left'
  }

  const scoreStyle = {
    fontSize: '13px',
    fontWeight: '700',
    color: '#ffffff',
    minWidth: '12px',
    textAlign: 'center'
  }

  const statusStyle = {
    fontSize: '10px',
    color: match.status === 'FT' ? '#999' : '#ffb800',
    fontWeight: '600',
    marginLeft: '6px',
    minWidth: '28px'
  }

  const separatorStyle = {
    color: '#666',
    fontSize: '11px',
    margin: '0 2px'
  }

  return (
    <div 
      style={matchCardStyle}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <span style={competitionStyle}>{match.competition}</span>
      <span style={teamStyle}>{match.homeTeam}</span>
      <span style={scoreStyle}>{match.homeScore}</span>
      <span style={separatorStyle}>-</span>
      <span style={scoreStyle}>{match.awayScore}</span>
      <span style={teamStyle}>{match.awayTeam}</span>
      <span style={statusStyle}>{match.status}</span>
    </div>
  )
}
