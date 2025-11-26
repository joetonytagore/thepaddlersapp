import React, {useState} from 'react'
import { setToken } from './api'

export default function Login({onLogin}:{onLogin:()=>void}){
  const [email,setEmail] = useState('')
  const [msg,setMsg] = useState('')
  async function submit(e:React.FormEvent){
    e.preventDefault(); setMsg('')
    try{
      const res = await fetch('/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email})})
      if(res.ok){ const j = await res.json(); setToken(j.token); onLogin() }
      else setMsg('Login failed')
    }catch(e){ setMsg('Network error') }
  }
  return (
    <form onSubmit={submit} className="max-w-sm grid gap-2">
      <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="email" className="border p-2" />
      <button className="bg-blue-600 text-white p-2 rounded" type="submit">Login (dev)</button>
      <div className="text-sm text-red-600">{msg}</div>
    </form>
  )
}

