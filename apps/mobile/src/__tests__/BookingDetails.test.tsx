import React from 'react'
import { render } from '@testing-library/react-native'
import BookingDetails from '../BookingDetails'

describe('BookingDetails', () => {
  it('renders booking summary correctly', () => {
    const booking = {
      id: 123,
      court: { name: 'Court A', id: 1 },
      startAt: new Date().toISOString(),
      endAt: new Date(Date.now() + 3600 * 1000).toISOString(),
      status: 'CONFIRMED',
      priceCents: 1500
    }

    const { getByText } = render(<BookingDetails booking={booking} onCancel={() => {}} />)
    expect(getByText('Booking details') || getByText('Court A')).toBeTruthy()
    expect(getByText(/Price:/)).toBeTruthy()
    expect(getByText(/Status:/)).toBeTruthy()
  })
})

