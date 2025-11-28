import React, {useState} from 'react'
import { authFetch } from './api'

export default function CreateCourt({onCreated}:{onCreated:()=>void}){
  const [name,setName] = useState('')
  const [msg,setMsg] = useState('')
  async function submit(e:React.FormEvent){
    e.preventDefault(); setMsg('')
    try{
      const res = await authFetch('/api/orgs/1/courts', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({name}) })
      if(res.ok){ setMsg('Created'); setName(''); onCreated() }
      else setMsg('Error: '+res.status)
    }catch(e){ setMsg('Network error') }
  }
  return (
    <form onSubmit={submit} className="mt-4 grid gap-2 max-w-sm">
      <label className="block">Court name<input value={name} onChange={e=>setName(e.target.value)} className="mt-1 block w-full border rounded p-2"/></label>
      <button className="bg-green-600 text-white px-3 py-2 rounded">Create Court</button>
      <div className="text-sm text-red-600">{msg}</div>
    </form>
  )
}

