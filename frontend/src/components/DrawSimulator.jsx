import React, { useState } from 'react'
import { runDraw } from '../api'

export default function DrawSimulator(){
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  async function handleSimulate(){
    setLoading(true)
    setError(null)
    try{
      const data = await runDraw()
      setResult(data)
    }catch(e){
      setError(e.message || 'Request failed')
    }finally{
      setLoading(false)
    }
  }

  return (
    <div className="draw-sim">
      <button onClick={handleSimulate} disabled={loading}>
        {loading ? 'Simulating...' : 'Simulate Draw'}
      </button>
      {error && <div className="error">{error}</div>}
      {result && (
        <pre className="result">{JSON.stringify(result, null, 2)}</pre>
      )}
    </div>
  )
}
