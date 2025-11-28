import React, {useEffect, useState} from 'react'
import { View, Text, FlatList, Button } from 'react-native'
import { getBookings, getMe } from './api'

export default function BookingsList({onBack}:{onBack:()=>void}){
  const [bookings,setBookings] = useState<any[]>([])
  const [msg,setMsg] = useState('')

  useEffect(()=>{
    (async ()=>{
      try{
        const mr = await getMe()
        if(!mr.ok){ setMsg('Not authenticated'); return }
        const me = await mr.json()
        const res = await getBookings(me.id)
        if(res.ok){ setBookings(await res.json()) }
        else { setMsg('Failed to load bookings') }
      }catch(e){ setMsg('Network error') }
    })()
  },[])

  return (
    <View style={{flex:1,padding:16}}>
      <Text style={{fontWeight:'bold',marginBottom:8}}>Your bookings</Text>
      <FlatList data={bookings} keyExtractor={b=>String(b.id)} renderItem={({item})=> (
        <View style={{padding:8,borderBottomWidth:1,borderColor:'#eee'}}>
          <Text>Court: {item.court?.name || item.court?.id}</Text>
          <Text>From: {item.startAt}</Text>
          <Text>To: {item.endAt}</Text>
          <Text>Status: {item.status}</Text>
        </View>
      )} />
      <Text style={{color:'red',marginTop:8}}>{msg}</Text>
      <Button title="Back" onPress={onBack} />
    </View>
  )
}

