import React, {useEffect, useState} from 'react'
import { authFetch } from './api'

type Audit = { id:number, actionType:string, entityType:string, entityId:string, details:any, userAgent?:string, ipAddress?:string, createdAt?:string, userId?:number }

export default function AuditViewer({orgId=1}:{orgId?:number}){
  const [logs,setLogs] = useState<Audit[]>([])
  const [loading,setLoading] = useState(false)
  const [page,setPage] = useState(0)
  const [size,setSize] = useState(20)
  const [total,setTotal] = useState(0)
  const [action,setAction] = useState('')
  const [entity,setEntity] = useState('')
  const [userId,setUserId] = useState('')

  useEffect(()=>{ fetchLogs() },[page,size])

  async function fetchLogs(){
    setLoading(true)
    try{
      const q = new URLSearchParams()
      q.set('page', String(page))
      q.set('size', String(size))
      if(action) q.set('action', action)
      if(entity) q.set('entity', entity)
      if(userId) q.set('userId', userId)
      const res = await authFetch('/api/orgs/'+orgId+'/admin/audit?'+q.toString())
      if(res.ok){ const j = await res.json(); setTotal(j.total||0); setLogs(j.items||[]) }
      else console.error('fetch failed',res.status)
    }catch(e){console.error(e)}
    setLoading(false)
  }

  function downloadCsv(){
    const q = new URLSearchParams()
    if(action) q.set('action', action)
    if(entity) q.set('entity', entity)
    if(userId) q.set('userId', userId)
    const url = '/api/orgs/'+orgId+'/admin/audit.csv?'+q.toString()
    window.location.href = url
  }

  return (
    <div className="mt-6">
      <h2 className="text-lg font-semibold">Audit Logs</h2>

      <div className="mt-2 flex gap-2">
        <input placeholder="action" value={action} onChange={e=>setAction(e.target.value)} className="border p-1" />
        <input placeholder="entity" value={entity} onChange={e=>setEntity(e.target.value)} className="border p-1" />
        <input placeholder="userId" value={userId} onChange={e=>setUserId(e.target.value)} className="border p-1" />
        <button onClick={()=>{ setPage(0); fetchLogs() }} className="bg-blue-600 text-white px-3 py-1 rounded">Filter</button>
        <button onClick={downloadCsv} className="bg-gray-700 text-white px-3 py-1 rounded">Export CSV</button>
      </div>

      {loading ? <div>Loading...</div> : (
        <div className="mt-4">
          <table className="min-w-full border">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-2 py-1">ID</th>
                <th className="px-2 py-1">Action</th>
                <th className="px-2 py-1">Entity</th>
                <th className="px-2 py-1">User</th>
                <th className="px-2 py-1">IP</th>
                <th className="px-2 py-1">When</th>
                <th className="px-2 py-1">Details</th>
              </tr>
            </thead>
            <tbody>
              {logs.map(l=> (
                <tr key={l.id} className="border-t">
                  <td className="px-2 py-1">{l.id}</td>
                  <td className="px-2 py-1">{l.actionType}</td>
                  <td className="px-2 py-1">{l.entityType}/{l.entityId}</td>
                  <td className="px-2 py-1">{l.userId || ''}</td>
                  <td className="px-2 py-1">{l.ipAddress || ''}</td>
                  <td className="px-2 py-1">{l.createdAt || ''}</td>
                  <td className="px-2 py-1"><pre className="text-xs">{JSON.stringify(l.details,null,2)}</pre></td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="mt-2 flex items-center gap-2">
            <button onClick={()=>{ if(page>0){ setPage(page-1); fetchLogs() } }} className="px-2 py-1 border rounded">Prev</button>
            <span>Page {page+1} — {total} items</span>
            <button onClick={()=>{ setPage(page+1); fetchLogs() }} className="px-2 py-1 border rounded">Next</button>
            <select value={size} onChange={e=>{ setSize(Number(e.target.value)); setPage(0); fetchLogs() }} className="ml-4 border p-1">
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      )}
    </div>
  )
}
