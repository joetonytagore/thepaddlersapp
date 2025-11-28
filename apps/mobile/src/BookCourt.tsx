import React, {useEffect, useState} from 'react'
import { View, Text, Button, FlatList, TouchableOpacity } from 'react-native'
import { getCourts, getMe, createBooking } from './api'

export default function BookCourt({onBooked, onCancel}:{onBooked:(b:any)=>void, onCancel:()=>void}){
  const [courts,setCourts] = useState<any[]>([])
  const [selectedCourt,setSelectedCourt] = useState<any | null>(null)
  const [slots,setSlots] = useState<any[]>([])
  const [msg,setMsg] = useState('')
  const [me,setMe] = useState<any>(null)

  useEffect(()=>{
    (async ()=>{
      try{
        const r = await getCourts()
        if(r.ok){ setCourts(await r.json()) }
        const mr = await getMe()
        if(mr.ok) setMe(await mr.json())
      }catch(e){ setMsg('Network error') }
    })()
  },[])

  useEffect(()=>{
    // build next 8 hourly slots from next hour when court selected
    if(!selectedCourt) return
    const now = new Date()
    const start = new Date(now)
    start.setMinutes(0,0,0)
    start.setHours(start.getHours()+1)
    const s = []
    for(let i=0;i<8;i++){
      const st = new Date(start.getTime() + i*60*60*1000)
      const et = new Date(st.getTime() + 60*60*1000)
      s.push({start: st, end: et})
    }
    setSlots(s)
  },[selectedCourt])

  async function confirm(slot:any){
    if(!me){ setMsg('Not authenticated'); return }
    setMsg('')
    const res = await createBooking(selectedCourt.id, me.id, slot.start.toISOString(), slot.end.toISOString())
    if(res.ok){ const j = await res.json(); onBooked(j) }
    else { const t = await res.text(); setMsg(t || 'Failed to book') }
  }

  return (
    <View style={{flex:1,padding:16}}>
      <Text style={{fontWeight:'bold',marginBottom:8}}>Select court</Text>
      <FlatList data={courts} keyExtractor={c=>String(c.id)} renderItem={({item})=> (
        <TouchableOpacity onPress={()=>setSelectedCourt(item)} style={{padding:8,borderBottomWidth:1,borderColor:'#eee'}}>
          <Text style={{fontSize:16}}>{item.name}</Text>
        </TouchableOpacity>
      )} />

      {selectedCourt && (
        <View style={{marginTop:12}}>
          <Text style={{fontWeight:'bold'}}>Available next hours for {selectedCourt.name}</Text>
          <FlatList data={slots} keyExtractor={s=>s.start.toISOString()} renderItem={({item})=> (
            <View style={{padding:8,borderBottomWidth:1,borderColor:'#eee'}}>
              <Text>{item.start.toLocaleString()} - {item.end.toLocaleString()}</Text>
              <Button title="Book this slot" onPress={()=>confirm(item)} />
            </View>
          )} />
        </View>
      )}

      <Text style={{color:'red',marginTop:8}}>{msg}</Text>
      <Button title="Cancel" onPress={onCancel} />
    </View>
  )
}

