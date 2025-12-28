import React from 'react';
import Flag from './Flag';

function TeamLine({ team, score, isWinner, isHighlight }) {
  const lineStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: isHighlight ? '12px 16px' : '10px 14px',
    fontSize: isHighlight ? '15px' : '13px',
    fontWeight: isWinner ? '700' : '400',
    backgroundColor: isWinner ? 'rgba(43,108,176,0.08)' : 'transparent',
    borderBottom: '1px solid #e2e8f0',
    transition: 'background-color 0.2s ease',
  };
  
  const teamInfoStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: isHighlight ? '10px' : '8px',
    flex: 1,
    minWidth: 0,
  };
  
  const teamNameStyle = {
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  };
  
  const scoreStyle = {
    fontWeight: '700',
    fontSize: isHighlight ? '16px' : '14px',
    minWidth: '30px',
    textAlign: 'right',
    color: isWinner ? '#2b6cb0' : '#4a5568',
  };
  
  const winnerIcon = {
    marginLeft: '6px',
    color: '#2b6cb0',
    fontSize: '12px',
  };
  
  return (
    <div style={lineStyle}>
      <div style={teamInfoStyle}>
        {team && <Flag team={team} size={isHighlight ? 20 : 18} />}
        <span style={teamNameStyle}>{team?.name || 'TBD'}</span>
      </div>
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {score !== undefined && <span style={scoreStyle}>{score}</span>}
        {isWinner && <span style={winnerIcon}>▸</span>}
      </div>
    </div>
  );
}

export default function MatchBox({ match, isHighlight = false }) {
  if (!match) {
    return <div style={{ height: '68px', opacity: 0 }} />;
  }

  const boxStyle = {
    border: isHighlight ? '3px solid #2b6cb0' : '2px solid #cbd5e0',
    borderRadius: isHighlight ? '10px' : '6px',
    background: '#fff',
    width: isHighlight ? '280px' : '220px',
    overflow: 'hidden',
    boxShadow: isHighlight ? '0 6px 20px rgba(43,108,176,0.2)' : '0 2px 4px rgba(0,0,0,0.05)',
    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
  };

  const matchInfoStyle = {
    padding: isHighlight ? '10px 14px' : '8px 12px',
    background: '#f7fafc',
    borderBottom: '1px solid #e2e8f0',
    fontSize: isHighlight ? '12px' : '11px',
    color: '#718096',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  };

  const venueStyle = {
    fontWeight: '600',
    color: '#4a5568',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    flex: 1,
    marginRight: '8px',
  };

  const timeStyle = {
    fontWeight: '500',
    flexShrink: 0,
  };

  const homeWins = match.homeScore > match.awayScore || match.winner === match.home?.id;
  const awayWins = match.awayScore > match.homeScore || match.winner === match.away?.id;

  return (
    <div style={boxStyle}>
      {(match.venue || match.time) && (
        <div style={matchInfoStyle}>
          {match.venue && <span style={venueStyle}>{match.venue}</span>}
          {match.time && <span style={timeStyle}>{match.time}</span>}
        </div>
      )}
      <TeamLine team={match.home} score={match.homeScore} isWinner={homeWins} isHighlight={isHighlight} />
      <TeamLine team={match.away} score={match.awayScore} isWinner={awayWins} isHighlight={isHighlight} />
    </div>
  );
}
