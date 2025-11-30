import React, { useEffect, useState } from 'react'
import { authFetch } from './api'

export default function Invoices(){
  const [invoices, setInvoices] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(()=>{ fetchInvoices() }, [])

  async function fetchInvoices(){
    setLoading(true); setError(null)
    try{
      const res = await authFetch('/api/invoices')
      if(!res.ok) throw new Error('fetch failed')
      const j = await res.json()
      setInvoices(j)
    }catch(e){ setError(String(e)) }
    setLoading(false)
  }

  async function refund(invoiceId){
    const amount = prompt('Refund amount in cents (leave empty to refund full)')
    const reason = prompt('Reason (optional)', 'requested_by_customer')
    const body = { stripePaymentIntentId: null, amount_cents: amount ? Number(amount) : null, reason }
    // try to call admin refund endpoint (orgId=1 placeholder)
    const res = await authFetch('/api/admin/orgs/1/invoices/' + invoiceId + '/refund', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
    if(!res.ok){ alert('refund failed: ' + res.status); return }
    alert('refund requested')
    fetchInvoices()
  }

  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Invoices</h2>
      {loading && <div>Loading...</div>}
      {error && <div className="text-red-600">{error}</div>}
      <table className="min-w-full text-left">
        <thead><tr><th>ID</th><th>User</th><th>Total</th><th>Status</th><th>Created</th><th></th></tr></thead>
        <tbody>
          {invoices.map(inv => (
            <tr key={inv.id} className="border-t">
              <td>{inv.id}</td>
              <td>{inv.user?.email || inv.user?.name || '—'}</td>
              <td>{inv.total}</td>
              <td>{inv.status}</td>
              <td>{inv.createdAt}</td>
              <td><button className="px-2 py-1 bg-red-600 text-white rounded" onClick={()=>refund(inv.id)}>Refund</button></td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

