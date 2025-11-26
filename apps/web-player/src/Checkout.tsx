import React, { useState } from 'react'
import { loadStripe } from '@stripe/stripe-js'
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js'

const STRIPE_PUBLISHABLE = (window as any).__TP_STRIPE_PUBLISHABLE__ || 'pk_test_12345'
const stripePromise = loadStripe(STRIPE_PUBLISHABLE)

function PaymentForm({ onSuccess, onError }: { onSuccess: (msg: string) => void, onError: (msg: string) => void }){
  const stripe = useStripe()
  const elements = useElements()
  const [submitting, setSubmitting] = useState(false)

  async function handleConfirm(e: React.FormEvent){
    e.preventDefault()
    if (!stripe || !elements) { onError('Stripe.js not loaded'); return }
    setSubmitting(true)
    try {
      const result = await stripe.confirmPayment({
        elements,
        confirmParams: { return_url: window.location.href }
      })
      if ((result as any).error) {
        onError('Payment failed: ' + (result as any).error.message)
      } else {
        // result may not contain paymentIntent in all flows; show a friendly success message
        onSuccess('Payment submitted — check Stripe dashboard or webhook for status')
      }
    } catch (err: any) {
      onError('Payment confirmation error: ' + err?.message || err)
    }
    setSubmitting(false)
  }

  return (
    <form onSubmit={handleConfirm} className="p-2 border rounded">
      <PaymentElement />
      <button type="submit" disabled={!stripe || submitting} className="mt-3 bg-green-600 text-white px-3 py-1 rounded">Confirm Payment</button>
    </form>
  )
}

function CheckoutInner(){
  const [amount, setAmount] = useState(1000)
  const [currency, setCurrency] = useState('usd')
  const [clientSecret, setClientSecret] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function createIntent(e: React.FormEvent){
    e.preventDefault(); setMessage(null); setClientSecret(null); setLoading(true)
    try{
      const res = await fetch('/api/payments/create-payment-intent', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ amount: amount, currency }) })
      const data = await res.json()
      if (data?.clientSecret) {
        setClientSecret(data.clientSecret)
        setMessage('Client secret obtained — complete payment below')
      } else {
        setMessage('Failed to create intent: ' + JSON.stringify(data))
      }
    }catch(err:any){ setMessage('Network error: '+err?.message || err) }
    setLoading(false)
  }

  return (
    <div className="p-4">
      <h2 className="text-lg font-semibold">Stripe Payment (Elements) Demo</h2>
      <form onSubmit={createIntent} className="mt-3">
        <label className="block">
          Amount (cents)
          <input type="number" value={amount} onChange={e=>setAmount(Number(e.target.value))} className="mt-1 block w-40 border" />
        </label>
        <label className="block mt-2">
          Currency
          <input value={currency} onChange={e=>setCurrency(e.target.value)} className="mt-1 block w-40 border" />
        </label>
        <button type="submit" disabled={loading} className="mt-3 bg-blue-600 text-white px-3 py-1 rounded">Create Intent</button>
      </form>

      {clientSecret ? (
        <div className="mt-4">
          <div className="mb-2 p-2 bg-gray-50 rounded">Client secret obtained — complete payment with the form below.</div>
          {/* Re-mount Elements whenever clientSecret changes by using key */}
          <Elements stripe={stripePromise} options={{ clientSecret }} key={clientSecret}>
            <PaymentForm onSuccess={(m)=>setMessage(m)} onError={(m)=>setMessage(m)} />
          </Elements>
        </div>
      ) : null}

      <div className="mt-4 bg-gray-50 p-3 rounded">
        <pre>{message || (clientSecret ? 'Client secret present' : 'No client secret yet')}</pre>
      </div>

      <div className="mt-3 text-sm text-gray-600">
        <p>Use Stripe test card 4242 4242 4242 4242 (any future expiry, any CVC).</p>
      </div>
    </div>
  )
}

export default function Checkout(){
  return (
    <div>
      <CheckoutInner />
    </div>
  )
}
