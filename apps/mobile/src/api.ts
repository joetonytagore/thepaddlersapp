import * as SecureStore from 'expo-secure-store'
import { API_BASE } from './config'

const TOKEN_KEY = 'tp_token'

export async function setToken(token:string|null){
  if(token) await SecureStore.setItemAsync(TOKEN_KEY, token)
  else await SecureStore.deleteItemAsync(TOKEN_KEY)
}

export async function getToken(){
  return await SecureStore.getItemAsync(TOKEN_KEY)
}

export async function authFetch(path:string, init:any = {}){
  const headers = new Headers(init.headers || {})
  const token = await getToken()
  if(token) headers.set('Authorization', 'Bearer ' + token)
  const res = await fetch(API_BASE + path, {...init, headers})
  return res
}

export async function login(email:string){
  const res = await fetch(API_BASE + '/api/auth/login', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({email})})
  return res
}

// New helpers
export async function getMe(){
  const res = await authFetch('/api/users/me')
  return res
}

export async function getCourts(){
  const res = await fetch(API_BASE + '/api/courts')
  return res
}

export async function createBooking(courtId:number, userId:number, startIso:string, endIso:string){
  const body = { court: { id: courtId }, user: { id: userId }, startAt: startIso, endAt: endIso }
  const res = await authFetch('/api/bookings', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body)})
  return res
}

// New: fetch bookings for a user (uses query param userId)
export async function getBookings(userId:number){
  const res = await authFetch(`/api/bookings?userId=${userId}`)
  return res
}
