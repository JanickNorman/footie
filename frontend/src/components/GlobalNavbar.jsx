import React from 'react'

export default function GlobalNavbar(){
  const height = 64
  const style = {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    height,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 20px',
    background: 'linear-gradient(90deg,#063ea7,#0b63d6)',
    color: 'white',
    zIndex: 1100,
    boxShadow: '0 2px 8px rgba(2,6,23,0.08)'
  }
  const navStyle = {display:'flex',gap:18,alignItems:'center'}
  const linkStyle = {color:'rgba(255,255,255,0.9)',textDecoration:'none',fontWeight:600}
  return (
    <header style={style}>
      <div style={{display:'flex',alignItems:'center',gap:12}}>
        <div style={{width:36,height:36,borderRadius:8,background:'#fff',display:'flex',alignItems:'center',justifyContent:'center',color:'#063ea7',fontWeight:800}}>UE</div>
        <div style={{fontWeight:800,letterSpacing:0.3}}>UEFA EURO 28</div>
      </div>

      <nav style={navStyle}>
        <a href="/" style={linkStyle}>Home</a>
        <a href="#" style={linkStyle}>Video</a>
        <a href="#" style={linkStyle}>News</a>
        <a href="#" style={linkStyle}>History</a>
        <a href="/world_cup" style={linkStyle}>World Cup</a>
        <a href="#" style={linkStyle}>About</a>
        <a href="#" style={linkStyle}>Store</a>
      </nav>

      <div style={{display:'flex',alignItems:'center',gap:12}}>
        <button style={{background:'white',color:'#063ea7',border:'none',padding:'8px 12px',borderRadius:8,fontWeight:700}}>Sign in</button>
      </div>
    </header>
  )
}
