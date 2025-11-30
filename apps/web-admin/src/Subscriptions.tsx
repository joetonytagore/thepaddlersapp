import React, { useEffect, useState } from 'react'
import { authFetch } from './api'

export default function Subscriptions(){
  const [subs, setSubs] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(()=>{ fetchSubs() },[])

  async function fetchSubs(){
    setLoading(true); setError(null)
    try{
      const res = await authFetch('/api/subscriptions')
      if(!res.ok) throw new Error('fetch failed')
      const j = await res.json()
      setSubs(j)
    }catch(e){ setError(String(e)) }
    setLoading(false)
  }

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Subscriptions</h2>
      {loading && <div>Loading...</div>}
      {error && <div className="text-red-600">{error}</div>}
      <table className="min-w-full text-left">
        <thead><tr><th>ID</th><th>User</th><th>Status</th><th>Plan</th><th>Started</th><th>Ends</th></tr></thead>
        <tbody>
          {subs.map(sub => (
            <tr key={sub.id} className="border-t">
              <td>{sub.id}</td>
              <td>{sub.user?.email || sub.user?.name || '—'}</td>
              <td>{sub.status}</td>
              <td>{sub.planName || sub.plan_id}</td>
              <td>{sub.startDate}</td>
              <td>{sub.endDate}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

