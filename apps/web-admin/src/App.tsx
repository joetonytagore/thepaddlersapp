import React, {useEffect, useState} from 'react'
import './styles.css'
import Login from './Login'
import { getToken, setToken, authFetch } from './api'
import CreateOrg from './CreateOrg'
import CreateCourt from './CreateCourt'
import AuditViewer from './AuditViewer'
import Invoices from './Invoices'
import BookingRuleManager from './BookingRuleManager'
import ConcurrentBookingTest from './ConcurrentBookingTest';
import MembershipManager from './MembershipManager';
import POSManager from './POSManager';
import InstructorScheduleManager from './InstructorScheduleManager';
import EventManager from './EventManager';
import Subscriptions from './Subscriptions';

type Court = { id: number, name: string }

export default function App(){
  const [courts, setCourts] = useState<Court[]>([])
  const [loading, setLoading] = useState(false)
  const [authed, setAuthed] = useState(!!getToken())
  const [showInvoices, setShowInvoices] = useState(false)

  useEffect(()=>{ if(authed) fetchCourts() },[authed])

  async function fetchCourts(){
    setLoading(true)
    try{
      const res = await authFetch('/api/courts')
      if(res.ok){ const data = await res.json(); setCourts(data) }
    }catch(e){console.error(e)}
    setLoading(false)
  }

  function onLogin(){ setAuthed(true); fetchCourts() }
  function logout(){ setToken(null); setAuthed(false); setCourts([]) }

  return (
    <div className="p-6 font-sans">
      <header className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">The Paddlers — Admin</h1>
          <p className="text-sm text-gray-600">Simple admin UI (dev)</p>
        </div>
        <div>
          {authed ? <button onClick={logout} className="text-sm text-red-600">Logout</button> : null}
        </div>
      </header>

      {!authed ? (
        <div className="mt-6"><Login onLogin={onLogin} /></div>
      ) : (
        <main>
          <section className="mt-4">
            <h2 className="text-lg font-semibold">Courts</h2>
            {loading ? <div>Loading...</div> : (
              <ul className="list-disc pl-6">
                {courts.map(c=> <li key={c.id}>{c.name} (#{c.id})</li>)}
              </ul>
            )}
          </section>

          <section className="mt-6">
            <h2 className="text-lg font-semibold">Create reservation (quick)</h2>
            <ReservationForm courts={courts} onSuccess={()=>fetchCourts()} />
          </section>

          <section className="mt-6">
            <h2 className="text-lg font-semibold">Admin Actions</h2>
            <CreateOrg onCreated={()=>fetchCourts()} />
            <CreateCourt onCreated={()=>fetchCourts()} />
            <div className="mt-4">
              <AuditViewer orgId={1} />
            </div>
            <div className="mt-4">
              <button onClick={()=>setShowInvoices(s=>!s)} className="px-3 py-1 bg-gray-200 rounded">Toggle Invoices</button>
            </div>
            {showInvoices ? <div className="mt-4"><Invoices /></div> : null}
            <div className="mt-4">
              <Subscriptions />
            </div>
            <div className="mt-4">
              <BookingRuleManager />
            </div>
            <ConcurrentBookingTest />
            <MembershipManager />
            <POSManager />
            <InstructorScheduleManager />
            <EventManager />
          </section>

        </main>
      )}
    </div>
  )
}

function ReservationForm({courts,onSuccess}:{courts: any[], onSuccess: ()=>void}){
  const [courtId, setCourtId] = useState<number>(courts?.[0]?.id || 1)
  const [userId, setUserId] = useState<number>(1)
  const [start, setStart] = useState('')
  const [end, setEnd] = useState('')
  const [msg, setMsg] = useState('')

  useEffect(()=>{ if(courts?.length) setCourtId(courts[0].id) },[courts])

  async function submit(e: React.FormEvent){
    e.preventDefault(); setMsg('')
    const payload = { court:{id: Number(courtId) }, user:{ id: Number(userId) }, startAt: start, endAt: end }
    try{
      const res = await fetch('/api/bookings', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload) })
      if(res.status===201 || res.ok){ setMsg('Created'); onSuccess() }
      else { const t = await res.text(); setMsg('Error: '+res.status+' '+t) }
    }catch(err){ setMsg('Network error') }
  }

  return (
    <form onSubmit={submit} className="grid gap-4 max-w-md">
      <label className="block">
        Court
        <select value={courtId} onChange={e=>setCourtId(Number(e.target.value))} className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring focus:ring-opacity-50">
          {courts.map(c=> <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </label>
      <label className="block">
        User ID
        <input value={userId} onChange={e=>setUserId(Number(e.target.value))} className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring focus:ring-opacity-50" />
      </label>
      <label className="block">
        Start (ISO)
        <input value={start} onChange={e=>setStart(e.target.value)} placeholder="2025-11-26T15:00:00Z" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring focus:ring-opacity-50" />
      </label>
      <label className="block">
        End (ISO)
        <input value={end} onChange={e=>setEnd(e.target.value)} placeholder="2025-11-26T16:00:00Z" className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring focus:ring-opacity-50" />
      </label>
      <button type="submit" className="mt-2 bg-blue-600 text-white rounded-md px-4 py-2 shadow-sm hover:bg-blue-500 transition-colors">Create</button>
      <div className="text-sm text-red-600">{msg}</div>
    </form>
  )
}
