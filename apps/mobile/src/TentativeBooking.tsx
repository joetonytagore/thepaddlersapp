import { useState, useEffect } from 'react'
import NetInfo from '@react-native-community/netinfo'
import { addBookingToQueue, BookingRequest } from './bookingQueue'
import { createBooking } from './api'
import * as Notifications from 'expo-notifications'

export async function tentativeBooking(req: BookingRequest) {
  const net = await NetInfo.fetch()
  if (!net.isConnected) {
    await addBookingToQueue({ ...req, status: 'pending' })
    return { status: 'queued', message: 'Booking queued for sync when online.' }
  } else {
    const res = await createBooking(req.courtId, req.userId, req.startIso, req.endIso)
    if (res.ok) {
      return { status: 'confirmed', message: 'Booking confirmed.' }
    } else {
      const body = await res.json().catch(() => ({}))
      return { status: 'error', message: body.message || 'Conflict or server error' }
    }
  }
}

// Real-time and background update hooks
// Example: subscribe to WebSocket for booking/waitlist updates
export function useBookingUpdates(onUpdate: (data: any) => void) {
  useEffect(() => {
    const ws = new WebSocket('wss://your-server.example.com/ws/bookings')
    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        onUpdate(data)
      } catch {}
    }
    return () => ws.close()
  }, [onUpdate])
}

// Example: handle background/silent push notifications
Notifications.setNotificationHandler({
  handleNotification: async () => ({ shouldShowAlert: false }),
  handleSuccess: () => {},
  handleError: () => {}
})

Notifications.addNotificationReceivedListener(notification => {
  // Handle background/silent push for reminders, waitlist promotions
  if (notification.request.content.data?.type === 'waitlist_promotion') {
    // e.g., refresh bookings/waitlist
  }
  if (notification.request.content.data?.type === 'booking_reminder') {
    // e.g., show local reminder or update UI
  }
})
