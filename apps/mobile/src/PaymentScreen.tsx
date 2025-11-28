import React, {useState} from 'react'
import { View, Text, Button, ActivityIndicator, Alert } from 'react-native'
import { useStripe } from '@stripe/stripe-react-native'
import { authFetch } from './api'

export default function PaymentScreen({booking, onDone}:{booking:any, onDone:()=>void}){
  const { initPaymentSheet, presentPaymentSheet } = useStripe()
  const [loading,setLoading] = useState(false)
  const [message,setMessage] = useState('')

  async function openPaymentSheet(){
    setLoading(true)
    setMessage('Preparing payment...')
    try{
      // Ask backend to create a PaymentIntent for the booking
      const res = await authFetch('/api/payments/create-payment-intent', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ amount: 1000, currency: 'usd', metadata: { bookingId: booking?.id } }) })
      if(!res.ok){ const text = await res.text().catch(()=>null); setMessage('Failed to create payment intent: ' + (text || res.status)); setLoading(false); return }
      const j = await res.json().catch(()=>null)
      // backend returns clientSecret or intent info
      const clientSecret = j?.clientSecret || (j?.intent && j.intent.client_secret) || null
      if(!clientSecret){ setMessage('No client secret returned'); setLoading(false); return }

      setMessage('Initializing payment sheet...')
      // cast to any to avoid strict type mismatch in this repo setup
      const initParams: any = { paymentIntentClientSecret: clientSecret, merchantDisplayName: 'The Paddlers' }
      const initResult: any = await (initPaymentSheet as any)(initParams)
      const error = initResult?.error
      if(error){ setMessage('Init error: ' + error.message); setLoading(false); return }

      setMessage('Presenting payment sheet...')
      const presentResult: any = await (presentPaymentSheet as any)()
      const presentError = presentResult?.error
      if(presentError){ setMessage('Payment failed: ' + presentError.message); setLoading(false); return }

      setMessage('Payment complete!')
      // friendly native confirmation
      Alert.alert('Payment successful', 'Your booking has been paid.', [{ text: 'OK', onPress: onDone }], { cancelable: false })
    }catch(e){ setMessage('Network or SDK error'); console.error(e); Alert.alert('Payment error', String(e)) }
    setLoading(false)
  }

  return (
    <View style={{flex:1,padding:16}}>
      <Text style={{fontWeight:'bold',fontSize:16,marginBottom:8}}>Pay for booking</Text>
      <Text>Booking: {booking?.id}</Text>
      <View style={{height:12}} />
      {loading ? (
        <View style={{flexDirection:'row',alignItems:'center'}}>
          <ActivityIndicator size="small" />
          <Text style={{marginLeft:8}}>{message}</Text>
        </View>
      ) : (
        <Button title={'Pay now'} onPress={openPaymentSheet} accessibilityLabel="Pay now" />
      )}

      {message && !loading ? <Text style={{color: message.startsWith('Payment') ? 'green' : 'red',marginTop:8}}>{message}</Text> : null}

      {/* quick retry helper when an error occurred */}
      {!loading && message && message.toLowerCase().includes('failed') ? (
        <View style={{marginTop:8}}>
          <Button title="Retry" onPress={openPaymentSheet} />
        </View>
      ) : null}
    </View>
  )
}
