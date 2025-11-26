import React, {useState} from 'react'
import { setToken } from './api'

export default function Login({onLogin}:{onLogin:()=>void}){
  // Prefill with the seeded demo user for local/dev convenience
  const [email,setEmail] = useState('demo@paddlers.test')
  const [msg,setMsg] = useState('')
  async function submit(e:React.FormEvent){
    e.preventDefault(); setMsg('')
    try{
      const res = await fetch('/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email})})
      if(res.ok){ const j = await res.json(); setToken(j.token); onLogin() }
      else {
        // Try to parse an error message from the backend (JSON or text)
        let backendMsg: string | null = null
        let backendCode: string | null = null
        try {
          const ct = res.headers.get('content-type') || ''
          if (ct.includes('application/json')) {
            const body = await res.json()
            if (typeof body === 'string') backendMsg = body
            else if (body) {
              if (body.code) backendCode = body.code
              if (body.message) backendMsg = body.message
              else if (body.error) backendMsg = body.error
              else backendMsg = JSON.stringify(body)
            }
          } else {
            const text = await res.text()
            if (text) backendMsg = text
          }
        } catch (parseErr) {
          // ignore parse errors and keep backendMsg null
          console.debug('Failed to parse error response', parseErr)
        }

        // Prefer mapping based on backend code when present
        const codeMap:{[k:string]:string} = {
          'AUTH_BAD_REQUEST': 'Please provide an email address',
          'AUTH_INVALID_CREDENTIALS': 'That email is not registered or credentials are invalid',
        }
        // Map common HTTP status codes to friendlier messages
        const statusMap:{[k:number]:string} = {
          400: 'Please provide an email address',
          401: 'Invalid email or user not found',
          403: 'Access denied',
          404: 'Not found',
          422: 'Invalid input',
          500: 'Server error — please try again later'
        }

        const fallback = statusMap[res.status] || `Login failed (${res.status})`
        const finalMsg = (backendCode && codeMap[backendCode]) ? codeMap[backendCode] : (backendMsg && backendMsg.trim() ? backendMsg : fallback)
        setMsg(finalMsg)
      }
    }catch(e){ setMsg('Network error') }
  }
  return (
    <form onSubmit={submit} className="max-w-sm grid gap-2">
      <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="email" className="border p-2" />
      <button className="bg-blue-600 text-white p-2 rounded" type="submit">Login (dev)</button>
      <div className="text-sm text-gray-500">Dev: try <strong>demo@paddlers.test</strong> or <strong>admin@paddlers.test</strong></div>
      <div className="text-sm text-red-600">{msg}</div>
    </form>
  )
}
