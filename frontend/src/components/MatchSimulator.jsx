import React, { useState, useEffect, useRef } from 'react';
import './MatchSimulator.css';

export default function MatchSimulator() {
  const [homeTeam, setHomeTeam] = useState('Barcelona');
  const [awayTeam, setAwayTeam] = useState('Real Madrid');
  const [matchState, setMatchState] = useState(null);
  const [events, setEvents] = useState([]);
  const [isSimulating, setIsSimulating] = useState(false);
  const [error, setError] = useState(null);
  const eventSourceRef = useRef(null);

  const startMatch = async () => {
    setError(null);
    setEvents([]);
    setIsSimulating(true);

    try {
      const response = await fetch('http://localhost:8080/api/match/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ homeTeam, awayTeam })
      });

      if (!response.ok) throw new Error('Failed to start match');
      
      const match = await response.json();
      setMatchState(match);
      
      // Start simulation
      simulateMatch(match.matchId);
    } catch (err) {
      setError(err.message);
      setIsSimulating(false);
    }
  };

  const simulateMatch = (matchId) => {
    // Close existing connection if any
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
    }

    // Create new EventSource for Server-Sent Events
    const eventSource = new EventSource(
      `http://localhost:8080/api/match/${matchId}/simulate-realtime?intervalMillis=500`
    );

    eventSource.onmessage = (event) => {
      const matchEvent = JSON.parse(event.data);
      
      setEvents(prev => [...prev, matchEvent]);
      
      // Update match state
      setMatchState(prevState => ({
        ...prevState,
        currentMinute: matchEvent.minute,
        homeScore: matchEvent.homeScore,
        awayScore: matchEvent.awayScore
      }));

      // Close connection on full-time
      if (matchEvent.eventType === 'FULL_TIME') {
        eventSource.close();
        setIsSimulating(false);
      }
    };

    eventSource.onerror = (err) => {
      console.error('EventSource error:', err);
      eventSource.close();
      setIsSimulating(false);
      setError('Connection lost to server');
    };

    eventSourceRef.current = eventSource;
  };

  const stopSimulation = () => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    setIsSimulating(false);
  };

  useEffect(() => {
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, []);

  const getEventIcon = (eventType) => {
    const icons = {
      GOAL: '⚽',
      YELLOW_CARD: '🟨',
      RED_CARD: '🟥',
      KICKOFF: '⚡',
      HALF_TIME: '⏸️',
      FULL_TIME: '⏹️',
      SHOT_ON_TARGET: '🎯',
      SHOT_OFF_TARGET: '❌',
      FOUL: '🚫',
      CORNER: '📐',
      SAVE: '🧤',
      POSSESSION: '📊'
    };
    return icons[eventType] || '⚪';
  };

  return (
    <div className="match-simulator">
      <h2>⚽ Live Match Simulator with Kafka</h2>

      {error && <div className="error">{error}</div>}

      {!matchState ? (
        <div className="setup">
          <div className="team-input">
            <label>Home Team:</label>
            <input 
              type="text" 
              value={homeTeam} 
              onChange={(e) => setHomeTeam(e.target.value)}
              disabled={isSimulating}
            />
          </div>
          <div className="team-input">
            <label>Away Team:</label>
            <input 
              type="text" 
              value={awayTeam} 
              onChange={(e) => setAwayTeam(e.target.value)}
              disabled={isSimulating}
            />
          </div>
          <button onClick={startMatch} disabled={isSimulating}>
            Start Match
          </button>
        </div>
      ) : (
        <div className="match-view">
          <div className="scoreboard">
            <div className="team home-team">
              <h3>{matchState.homeTeam}</h3>
              <div className="score">{matchState.homeScore || 0}</div>
            </div>
            <div className="match-info">
              <div className="minute">
                {matchState.currentMinute}'
              </div>
              <div className="status">
                {matchState.status?.replace('_', ' ')}
              </div>
            </div>
            <div className="team away-team">
              <h3>{matchState.awayTeam}</h3>
              <div className="score">{matchState.awayScore || 0}</div>
            </div>
          </div>

          <div className="controls">
            {isSimulating ? (
              <button onClick={stopSimulation} className="stop-btn">
                Stop Simulation
              </button>
            ) : (
              <button onClick={() => {
                setMatchState(null);
                setEvents([]);
              }}>
                New Match
              </button>
            )}
          </div>

          <div className="events-feed">
            <h3>Match Events</h3>
            <div className="events-list">
              {events.slice().reverse().map((event, idx) => (
                <div 
                  key={idx} 
                  className={`event ${event.eventType.toLowerCase()}`}
                >
                  <span className="event-icon">
                    {getEventIcon(event.eventType)}
                  </span>
                  <span className="event-minute">{event.minute}'</span>
                  <span className="event-description">
                    {event.description}
                  </span>
                  {event.eventType === 'GOAL' && (
                    <span className="event-score">
                      ({event.homeScore} - {event.awayScore})
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      <div className="info">
        <p>📡 Events are streamed in real-time via Kafka</p>
        <p>⏱️ Each second represents one minute of match time</p>
      </div>
    </div>
  );
}
