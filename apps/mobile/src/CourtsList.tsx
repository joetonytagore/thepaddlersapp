import React, {useState, useEffect} from 'react'
import { View, Text, Button, FlatList, TouchableOpacity } from 'react-native'
import { getCourts, createBooking, getMe } from './api'

export default function CourtsList({onLogout}:{onLogout:()=>void}){
  const [courts,setCourts] = useState<any[]>([])
  const [msg,setMsg] = useState('')
  const [me,setMe] = useState<any>(null)

  useEffect(()=>{
    (async ()=>{
      try{
        const r = await getCourts()
        if(r.ok){ const j = await r.json(); setCourts(j) }
        const mr = await getMe()
        if(mr.ok){ setMe(await mr.json()) }
      }catch(e){ setMsg('Network error') }
    })()
  },[])

  async function bookNext(court:any){
    setMsg('')
    if(!me){ setMsg('No user info'); return }
    // book 1 hour from now
    const start = new Date(); start.setHours(start.getHours()+1,0,0,0)
    const end = new Date(start.getTime() + 60*60*1000)
    const res = await createBooking(court.id, me.id, start.toISOString(), end.toISOString())
    if(res.ok){ setMsg('Booking created') }
    else { const t = await res.text(); setMsg(t || 'Failed to book') }
  }

  return (
    <View style={{flex:1,padding:16}}>
      <Text style={{fontWeight:'bold',marginBottom:8}}>Courts</Text>
      <FlatList data={courts} keyExtractor={c=>String(c.id)} renderItem={({item})=> (
        <View style={{padding:8,borderBottomWidth:1,borderColor:'#eee'}}>
          <Text style={{fontSize:16}}>{item.name}</Text>
          <Button title="Book next hour" onPress={()=>bookNext(item)} />
        </View>
      )} />
      <Text style={{color:'red',marginTop:8}}>{msg}</Text>
      <Button title="Logout" onPress={onLogout} />
    </View>
  )
}

