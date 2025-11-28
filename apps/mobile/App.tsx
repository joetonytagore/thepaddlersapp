import React, {useState} from 'react'
import { View, Text, Button } from 'react-native'
import Login from './src/Login'
import { getToken, setToken } from './src/api'
import CourtsList from './src/CourtsList'
import BookingsList from './src/BookingsList'
import BookCourt from './src/BookCourt'
import BookingDetails from './src/BookingDetails'
import PaymentScreen from './src/PaymentScreen'
import { StripeProvider } from '@stripe/stripe-react-native'
import { STRIPE_PUBLISHABLE_KEY } from './src/config'
import Constants from 'expo-constants'

export default function App(){
  const [authed,setAuthed] = useState(false)
  const [screen,setScreen] = useState<'courts'|'bookings'|'bookCourt'|'details'|'payment'>('courts')
  const [selectedBooking,setSelectedBooking] = useState<any|null>(null)

  React.useEffect(()=>{
    (async ()=>{
      const t = await getToken()
      if(t) setAuthed(true)
    })()
  },[])

  if(!authed) return <Login onLogin={()=>setAuthed(true)} />

  // Detect if running inside Expo Go (no custom native modules available)
  const appOwnership = (Constants && (Constants as any).appOwnership) || null
  const runningInExpoGo = appOwnership === 'expo' || appOwnership === 'expo-go'

  const AppContents = (
    <View style={{flex:1}}>
      <View style={{flexDirection:'row',justifyContent:'space-around',padding:8}}>
        <Button title="Courts" onPress={()=>setScreen('courts')} />
        <Button title="Bookings" onPress={()=>setScreen('bookings')} />
        <Button title="Book" onPress={()=>setScreen('bookCourt')} />
        <Button title="Logout" onPress={async ()=>{ await setToken(null); setAuthed(false) }} />
      </View>
      {screen === 'courts' && <CourtsList onLogout={async ()=>{ await setToken(null); setAuthed(false) }} />}
      {screen === 'bookings' && <BookingsList onBack={()=>setScreen('courts')} />}
      {screen === 'bookCourt' && <BookCourt onBooked={(b)=>{ setSelectedBooking(b); setScreen('details') }} onCancel={()=>setScreen('courts')} />}
      {screen === 'details' && selectedBooking && <BookingDetails booking={selectedBooking} onCancel={()=>{ setSelectedBooking(null); setScreen('bookings') }} onNativePay={()=>setScreen('payment')} />}
      {screen === 'payment' && selectedBooking && <PaymentScreen booking={selectedBooking} onDone={()=>setScreen('bookings')} />}
    </View>
  )

  // Only initialize StripeProvider when not running under Expo Go (to avoid missing native module crashes)
  if(runningInExpoGo){
    return AppContents
  }

  return (
    <StripeProvider publishableKey={STRIPE_PUBLISHABLE_KEY}>
      {AppContents}
    </StripeProvider>
  )
}
