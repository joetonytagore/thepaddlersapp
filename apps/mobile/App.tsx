import React, {useState} from 'react'
import { View, Text, Button } from 'react-native'
import Login from './src/Login'
import { getToken, setToken, authFetch } from './src/api'

export default function App(){
  const [authed,setAuthed] = useState(false)
  const [profile,setProfile] = useState<any>(null)

  React.useEffect(()=>{
    (async ()=>{
      const t = await getToken()
      if(t) setAuthed(true)
    })()
  },[])

  async function loadProfile(){
    try{
      const res = await authFetch('/api/users/me')
      if(res.ok){
        const j = await res.json()
        setProfile(j)
      } else {
        const text = await res.text()
        setProfile({ error: text })
      }
    } catch(e){ setProfile({ error: 'Network error' }) }
  }

  if(!authed) return <Login onLogin={()=>setAuthed(true)} />

  return (
    <View style={{flex:1,justifyContent:'center',alignItems:'center',padding:16}}>
      <Text style={{marginBottom:12}}>Welcome to The Paddlers Mobile</Text>
      <Button title="Load profile (GET /api/users)" onPress={loadProfile} />
      {profile && <Text style={{marginTop:12}}>{JSON.stringify(profile,null,2)}</Text>}
      <Button title="Logout" onPress={async ()=>{ await setToken(null); setAuthed(false) }} />
    </View>
  )
}
