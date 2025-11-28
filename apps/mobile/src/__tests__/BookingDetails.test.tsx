import React from 'react'
import { render, fireEvent, waitFor } from '@testing-library/react-native'
import BookingDetails from '../BookingDetails'

// Mock authFetch to intercept network calls
jest.mock('../api', () => ({
  authFetch: jest.fn()
}))

const confirmMock = jest.fn()

// Mock stripe-native: CardField will call onCardChange with complete details to simulate entry
jest.mock('@stripe/stripe-react-native', () => ({
  CardField: (props) => {
    // Call the callback synchronously to simulate that card details are entered.
    if (props && typeof props.onCardChange === 'function') {
      props.onCardChange({ complete: true })
    }
    return null
  },
  useStripe: () => ({
    confirmPayment: confirmMock
  })
}))

const api = require('../api')
import { useStripe } from '@stripe/stripe-react-native'

describe('BookingDetails native pay flow', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('creates PaymentIntent and confirms payment via stripe-native', async () => {
    const booking = {
      id: 123,
      court: { name: 'Court A', id: 1 },
      startAt: new Date().toISOString(),
      endAt: new Date(Date.now() + 3600 * 1000).toISOString(),
      status: 'CONFIRMED',
      priceCents: 1500
    }

    // Mock backend create-payment-intent
    api.authFetch.mockImplementation(function(path, opts) {
      if(path === '/api/payments/create-payment-intent'){
        return Promise.resolve({ ok: true, json: async () => ({ clientSecret: 'pi_123_secret_abc' }) })
      }
      return Promise.resolve({ ok: true, json: async () => ({}) })
    })

    confirmMock.mockResolvedValue({ paymentIntent: { status: 'succeeded' } })

    const { getByText } = render(<BookingDetails booking={booking} onCancel={() => {}} />)

    // Show card field
    const showBtn = getByText('Pay with card (native)')
    fireEvent.press(showBtn)

    const confirmBtn = await waitFor(() => getByText('Confirm payment'))
    fireEvent.press(confirmBtn)

    await waitFor(() => {
      expect(api.authFetch).toHaveBeenCalledWith('/api/payments/create-payment-intent', expect.anything())
      expect(confirmMock).toHaveBeenCalledWith('pi_123_secret_abc', { type: 'Card' })
    })
  })

  it('shows backend error when create-payment-intent fails', async () => {
    const booking = {
      id: 124,
      court: { name: 'Court B', id: 2 },
      startAt: new Date().toISOString(),
      endAt: new Date(Date.now() + 3600 * 1000).toISOString(),
      status: 'CONFIRMED',
      priceCents: 1200
    }

    // Backend returns non-ok with a textual error
    api.authFetch.mockImplementation(function(path, opts) {
      if(path === '/api/payments/create-payment-intent'){
        return Promise.resolve({ ok: false, text: async () => 'backend error: insufficient_funds' })
      }
      return Promise.resolve({ ok: true, json: async () => ({}) })
    })

    const { getByText, findByText } = render(<BookingDetails booking={booking} onCancel={() => {}} />)
    fireEvent.press(getByText('Pay with card (native)'))
    fireEvent.press(await waitFor(() => getByText('Confirm payment')))

    // Expect the backend error message to be displayed in the UI
    await findByText('backend error: insufficient_funds')
  })

  it('shows stripe error when confirmPayment returns an error', async () => {
    const booking = {
      id: 125,
      court: { name: 'Court C', id: 3 },
      startAt: new Date().toISOString(),
      endAt: new Date(Date.now() + 3600 * 1000).toISOString(),
      status: 'CONFIRMED',
      priceCents: 2000
    }

    api.authFetch.mockImplementation(function(path, opts) {
      if(path === '/api/payments/create-payment-intent'){
        return Promise.resolve({ ok: true, json: async () => ({ clientSecret: 'pi_456_secret_def' }) })
      }
      return Promise.resolve({ ok: true, json: async () => ({}) })
    })

    // Simulate Stripe returning an error
    confirmMock.mockResolvedValue({ error: { message: 'Card declined' } })

    const { getByText, findByText } = render(<BookingDetails booking={booking} onCancel={() => {}} />)
    fireEvent.press(getByText('Pay with card (native)'))
    fireEvent.press(await waitFor(() => getByText('Confirm payment')))

    await findByText('Payment failed: Card declined')
  })
})
