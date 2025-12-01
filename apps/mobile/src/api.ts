import * as SecureStore from 'expo-secure-store'
import { API_BASE } from './config'

const TOKEN_KEY = 'tp_token'
const REFRESH_TOKEN_KEY = 'tp_refresh_token'

export async function setTokens(accessToken:string|null, refreshToken:string|null){
  if(accessToken) await SecureStore.setItemAsync(TOKEN_KEY, accessToken)
  else await SecureStore.deleteItemAsync(TOKEN_KEY)
  if(refreshToken) await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken)
  else await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY)
}

export async function getRefreshToken(){
  return await SecureStore.getItemAsync(REFRESH_TOKEN_KEY)
}

export async function setToken(token:string|null){
  if(token) await SecureStore.setItemAsync(TOKEN_KEY, token)
  else await SecureStore.deleteItemAsync(TOKEN_KEY)
}

export async function getToken(){
  return await SecureStore.getItemAsync(TOKEN_KEY)
}

export async function login(email:string, password:string){
  const res = await fetch(API_BASE + '/api/auth/login', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({email, password})
  })
  return res
}

export async function refreshAccessToken(){
  const refreshToken = await getRefreshToken()
  if(!refreshToken) throw new Error('No refresh token')
  const res = await fetch(API_BASE + '/api/auth/refresh', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({refreshToken})
  })
  if(res.ok){
    const j = await res.json()
    await setTokens(j.accessToken, j.refreshToken)
    return j.accessToken
  } else {
    await setTokens(null, null)
    throw new Error('Refresh failed')
  }
}

export async function authFetch(path:string, init:any = {}){
  let token = await getToken()
  let headers = new Headers(init.headers || {})
  if(token) headers.set('Authorization', 'Bearer ' + token)
  let res = await fetch(API_BASE + path, {...init, headers})
  if(res.status === 401 && await getRefreshToken()){
    try {
      token = await refreshAccessToken()
      headers.set('Authorization', 'Bearer ' + token)
      res = await fetch(API_BASE + path, {...init, headers})
    } catch(e) {
      // refresh failed, propagate original 401
    }
  }
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

export async function registerDevice(deviceId:string, platform:string, pushToken:string, appVersion:string, userId:number){
  const res = await fetch(API_BASE + '/api/devices', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ deviceId, platform, pushToken, appVersion, userId })
  })
  return res
}

export async function unregisterDevice(deviceId:string){
  const res = await fetch(API_BASE + `/api/devices/${deviceId}`, { method: 'DELETE' })
  return res
}

export async function syncBookingQueue() {
  const { getBookingQueue, updateBookingQueue } = await import('./bookingQueue')
  let queue = await getBookingQueue()
  let changed = false
  for (const req of queue) {
    if (req.status === 'pending') {
      try {
        const res = await createBooking(req.courtId, req.userId, req.startIso, req.endIso)
        if (res.ok) {
          req.status = 'synced'
          req.error = undefined
          changed = true
        } else {
          const body = await res.json().catch(() => ({}))
          req.status = 'error'
          req.error = body.message || 'Conflict or server error'
          changed = true
        }
      } catch (e) {
        req.status = 'error'
        req.error = 'Network error'
        changed = true
      }
    }
  }
  if (changed) await updateBookingQueue(queue)
  return queue
}
