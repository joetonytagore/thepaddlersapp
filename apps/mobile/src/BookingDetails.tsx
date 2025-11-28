import React, {useEffect, useState} from 'react'
import { View, Text, Button, Linking, TouchableOpacity, Clipboard, Alert } from 'react-native'
import { authFetch } from './api'
import { CardField, useStripe } from '@stripe/stripe-react-native'

export default function BookingDetails({booking, onCancel, onNativePay}:{booking:any, onCancel:()=>void, onNativePay?:()=>void}){
  const [msg,setMsg] = useState('')
  const [loading,setLoading] = useState(false)
  const [showCardField, setShowCardField] = useState(false)
  const [cardDetails, setCardDetails] = useState<any>(null)
  const { confirmPayment } = useStripe()

  async function cancel(){
    setMsg('')
    setLoading(true)
    try{
      const res = await authFetch(`/api/bookings/${booking.id}`, { method: 'DELETE' })
      if(res.ok){ setMsg('Cancelled'); onCancel() }
      else {
        let text = ''
        try { text = await res.text() } catch(e) {}
        setMsg(text || 'Failed to cancel')
      }
    }catch(e){ setMsg('Network error') }
    setLoading(false)
  }

  async function copyBookingRef(){
    try{
      await Clipboard.setString(String(booking.id))
      Alert.alert('Copied', 'Booking reference copied to clipboard')
    }catch(e){
      // fallback
      Alert.alert('Copy failed')
    }
  }

  async function addToCalendar(){
    // Minimal 'add to calendar' which opens a shareable url or deep link. For a full integration, use react-native-add-calendar-event.
    const title = `Booking ${booking.court?.name || booking.court?.id}`
    const start = booking.startAt
    const url = `https://www.google.com/calendar/render?action=TEMPLATE&text=${encodeURIComponent(title)}&dates=${encodeURIComponent(start)}/${encodeURIComponent(booking.endAt)}`
    try{ await Linking.openURL(url) } catch(e){ Alert.alert('Unable to open calendar') }
  }

  async function createCheckout(){
    setMsg('')
    setLoading(true)
    try{
      const amount = 1000
      const res = await authFetch('/api/payments/create-checkout-session', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ amount, currency: 'usd', successUrl: 'https://example.com/success', cancelUrl: 'https://example.com/cancel' }) })
      if(res.ok){
        const j = await res.json()
        const url = j.url || (j.session && j.session.url)
        if(url){
          if(onNativePay) {
            onNativePay()
          } else {
            await Linking.openURL(url)
            setMsg('Opened checkout')
          }
        } else {
          setMsg('No checkout URL returned')
        }
      } else {
        let text = ''
        try { text = await res.text() } catch(e){}
        setMsg(text || 'Failed to create checkout session')
      }
    }catch(e){setMsg('Network error')}
    setLoading(false)
  }

  async function charge(){
    setMsg('')
    setLoading(true)
    try{
      const res = await authFetch('/api/payments/charge', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ bookingId: booking.id, amount: 1000 }) })
      if(res.ok){
        const j = await res.json()
        setMsg('Payment success: ' + JSON.stringify(j))
      } else {
        let text = ''
        try { text = await res.text() } catch(e){}
        setMsg(text || 'Payment failed')
      }
    }catch(e){ setMsg('Network error') }
    setLoading(false)
  }

  // New: Create PaymentIntent on the backend and confirm using stripe-native
  async function payWithCard(){
    setMsg('')
    setLoading(true)
    try{
      // Ensure card details present (CardField will collect them)
      if(!cardDetails || !cardDetails.complete){
        setMsg('Please enter card details')
        setLoading(false)
        return
      }

      // Ask backend to create a PaymentIntent and return clientSecret
      const amount = booking.priceCents || 1000
      const res = await authFetch('/api/payments/create-payment-intent', { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ bookingId: booking.id, amount, currency: 'usd' }) })
      if(!res.ok){
        let text = ''
        try{ text = await res.text() }catch(e){}
        setMsg(text || 'Failed to create PaymentIntent')
        setLoading(false)
        return
      }
      const j = await res.json()
      const clientSecret = j.clientSecret || j.client_secret
      if(!clientSecret){ setMsg('No clientSecret returned'); setLoading(false); return }

      // Confirm payment using stripe-native
      if(!confirmPayment){ setMsg('Stripe not initialized'); setLoading(false); return }
      const result = await confirmPayment(clientSecret, { type: 'Card' })
      if(result.error){
        setMsg('Payment failed: ' + result.error.message)
      } else if(result.paymentIntent && result.paymentIntent.status === 'Succeeded' || (result.paymentIntent && result.paymentIntent.status === 'succeeded')){
        setMsg('Payment successful')
      } else {
        // Many Stripe SDKs return paymentIntent with status 'succeeded' or similar
        setMsg('Payment completed: ' + JSON.stringify(result))
      }
    }catch(e:any){
      setMsg('Network or payment error')
    }
    setLoading(false)
  }

  return (
    <View style={{flex:1,padding:16}}>
      <View style={{borderRadius:8, padding:12, backgroundColor:'#f7f7f7', marginBottom:12}}>
        <Text style={{fontWeight:'700', fontSize:16}}>{booking.court?.name || 'Court ' + booking.court?.id}</Text>
        <Text style={{color:'#333', marginTop:4}}>{new Date(booking.startAt).toLocaleString()} - {new Date(booking.endAt).toLocaleString()}</Text>
        <Text style={{marginTop:6}}>Price: ${(booking.priceCents ? (booking.priceCents/100).toFixed(2) : '10.00')}</Text>
        <Text style={{marginTop:6}}>Status: <Text style={{fontWeight:'600'}}>{booking.status}</Text></Text>
        <View style={{height:8}} />
        <TouchableOpacity onPress={copyBookingRef} style={{padding:6}}>
          <Text style={{color:'#0066cc'}}>Copy booking reference</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={addToCalendar} style={{padding:6}}>
          <Text style={{color:'#0066cc'}}>Add to calendar</Text>
        </TouchableOpacity>
      </View>

      <Text style={{color:'red',marginTop:8}}>{msg}</Text>
      <Button title={loading ? 'Working...' : 'Cancel booking'} onPress={cancel} disabled={loading} />
      <View style={{height:8}} />
      <Button title={loading ? 'Working...' : 'Open checkout (Stripe)'} onPress={createCheckout} disabled={loading} />
      <View style={{height:8}} />
      <Button title={loading ? 'Working...' : 'Pay / Fallback charge'} onPress={charge} disabled={loading} />
      <View style={{height:8}} />
      {/* Native card flow using stripe-react-native */}
      <Button title={showCardField ? 'Hide card entry' : 'Pay with card (native)'} onPress={() => setShowCardField(s => !s)} disabled={loading} />
      {showCardField && (
        <View style={{marginTop:12}}>
          <CardField
            postalCodeEnabled={false}
            placeholders={{number: '4242 4242 4242 4242'}}
            cardStyle={{backgroundColor:'#FFFFFF', textColor:'#000000'}}
            style={{height:50, marginVertical:8}}
            onCardChange={(details) => setCardDetails(details)}
          />
          <Button title={loading ? 'Working...' : 'Confirm payment'} onPress={payWithCard} disabled={loading} />
        </View>
      )}
    </View>
  )
}
