import React from 'react';
import MatchBox from './MatchBox';

export default function Bracket({ data }) {
  const containerStyle = {
    display: 'flex',
    justifyContent: 'center',
    padding: '40px 20px',
    overflowX: 'auto',
    background: 'linear-gradient(to bottom, #f7fafc 0%, #edf2f7 100%)',
  };

  const bracketStyle = {
    display: 'flex',
    gap: '60px',
    alignItems: 'flex-start',
    position: 'relative',
  };

  const roundContainerStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  };

  const roundLabelStyle = {
    fontSize: '14px',
    fontWeight: '700',
    color: '#2d3748',
    marginBottom: '20px',
    textAlign: 'center',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  };

  const roundColumnStyle = {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-around',
    gap: '80px',
  };

  const matchWrapperStyle = {
    position: 'relative',
  };

  const finalStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '30px',
    padding: '20px',
  };

  const finalHeaderStyle = {
    background: 'linear-gradient(135deg, #2b6cb0 0%, #3182ce 100%)',
    color: '#fff',
    padding: '16px 32px',
    borderRadius: '8px',
    fontSize: '24px',
    fontWeight: '800',
    textTransform: 'uppercase',
    letterSpacing: '1.5px',
    boxShadow: '0 4px 12px rgba(43,108,176,0.3)',
    textAlign: 'center',
  };

  const thirdPlaceHeaderStyle = {
    background: '#718096',
    color: '#fff',
    padding: '10px 24px',
    borderRadius: '6px',
    fontSize: '14px',
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    marginBottom: '12px',
  };

  const connectorStyle = (height, side = 'right') => ({
    position: 'absolute',
    [side]: '-30px',
    top: '50%',
    width: '30px',
    height: `${height}px`,
    transform: 'translateY(-50%)',
    borderTop: '2px solid #cbd5e0',
    borderBottom: '2px solid #cbd5e0',
    [side === 'right' ? 'borderRight' : 'borderLeft']: '2px solid #cbd5e0',
    pointerEvents: 'none',
  });

  const renderMatchWithConnector = (match, index, showConnector = true, side = 'right') => (
    <div key={index} style={matchWrapperStyle}>
      <MatchBox match={match} />
      {showConnector && (
        <div style={connectorStyle(index % 2 === 0 ? 100 : -100, side)} />
      )}
    </div>
  );

  const renderRound = (matches, label, showConnectors = true, side = 'right') => (
    <div style={roundContainerStyle}>
      {label && <div style={roundLabelStyle}>{label}</div>}
      <div style={roundColumnStyle}>
        {matches.map((match, index) => 
          renderMatchWithConnector(match, index, showConnectors, side)
        )}
      </div>
    </div>
  );

  // Split rounds for left and right
  const leftR16 = data.roundOf16?.slice(0, 4) || [];
  const rightR16 = data.roundOf16?.slice(4, 8) || [];
  const leftQF = data.quarterfinals?.slice(0, 2) || [];
  const rightQF = data.quarterfinals?.slice(2, 4) || [];
  const leftSF = data.semifinals?.slice(0, 1) || [];
  const rightSF = data.semifinals?.slice(1, 2) || [];

  return (
    <div style={containerStyle}>
      <div style={bracketStyle}>
        {/* LEFT SIDE - Round of 16 */}
        {leftR16.length > 0 && renderRound(leftR16, 'Round of 16', true, 'right')}
        
        {/* LEFT SIDE - Quarterfinals */}
        {leftQF.length > 0 && renderRound(leftQF, 'Quarter-finals', true, 'right')}
        
        {/* LEFT SIDE - Semifinals */}
        {leftSF.length > 0 && renderRound(leftSF, 'Semi-finals', true, 'right')}

        {/* CENTER - FINAL */}
        <div style={finalStyle}>
          <div style={finalHeaderStyle}>Final</div>
          <MatchBox match={data.final} isHighlight={true} />
          {data.thirdPlace && (
            <div style={{ marginTop: '40px', textAlign: 'center' }}>
              <div style={thirdPlaceHeaderStyle}>Third Place</div>
              <MatchBox match={data.thirdPlace} />
            </div>
          )}
        </div>

        {/* RIGHT SIDE - Semifinals */}
        {rightSF.length > 0 && renderRound(rightSF, 'Semi-finals', true, 'left')}
        
        {/* RIGHT SIDE - Quarterfinals */}
        {rightQF.length > 0 && renderRound(rightQF, 'Quarter-finals', true, 'left')}
        
        {/* RIGHT SIDE - Round of 16 */}
        {rightR16.length > 0 && renderRound(rightR16, 'Round of 16', true, 'left')}
      </div>
    </div>
  );
}
