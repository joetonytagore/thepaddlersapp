export function getToken(){ return localStorage.getItem('tp_token') }
export function setToken(token){ if(token) localStorage.setItem('tp_token', token); else localStorage.removeItem('tp_token') }

export async function authFetch(input, init = {}){
  const headers = new Headers(init.headers || {})
  const token = getToken()
  if(token) headers.set('Authorization', 'Bearer ' + token)
  const res = await fetch(input, {...init, headers})
  return res
}
