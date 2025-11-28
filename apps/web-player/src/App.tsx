import React, { useEffect, useState } from 'react'
import './styles.css'
import Login from './Login'
import { getToken } from './api'
import Checkout from './Checkout'

type Court = { id:number, name:string }

export default function App(){
  const [courts,setCourts] = useState<Court[]>([])
  const [authed,setAuthed] = useState(!!getToken())
  const [showCheckout, setShowCheckout] = useState(window.location.hash === '#checkout')

  useEffect(()=>{ if(authed) fetch('/api/courts').then(r=>r.json()).then(setCourts).catch(()=>[]) },[authed])

  useEffect(()=>{
    const handler = ()=> setShowCheckout(window.location.hash === '#checkout')
    window.addEventListener('hashchange', handler)
    return ()=> window.removeEventListener('hashchange', handler)
  },[])

  return (
    <div className="p-6 font-sans">
      <h1 className="text-2xl font-bold">The Paddlers — Player</h1>
      <p className="text-sm text-gray-600">Browse courts and book a slot.</p>

      {!authed ? (
        <div className="mt-4"><Login onLogin={()=>setAuthed(true)} /></div>
      ) : showCheckout ? (
        <div className="mt-4"><Checkout /></div>
      ) : (
        <div>
          <div className="mt-4">
            <a href="#checkout" className="text-blue-600">Try Payment Demo</a>
          </div>
          <ul className="mt-4 list-disc pl-6">
            {courts.map(c=> <li key={c.id}>{c.name} — <a className="text-blue-600" href={'/admin?court='+c.id}>Book</a></li>)}
          </ul>
        </div>
      )}
    </div>
  )
}
