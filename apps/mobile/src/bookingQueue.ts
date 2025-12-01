import * as SecureStore from 'expo-secure-store'

const QUEUE_KEY = 'tp_booking_queue'

export type BookingRequest = {
  courtId: number
  userId: number
  startIso: string
  endIso: string
  status: 'pending' | 'synced' | 'error'
  error?: string
}

export async function getBookingQueue(): Promise<BookingRequest[]> {
  const raw = await SecureStore.getItemAsync(QUEUE_KEY)
  if (!raw) return []
  try {
    return JSON.parse(raw)
  } catch {
    return []
  }
}

export async function addBookingToQueue(req: BookingRequest) {
  const queue = await getBookingQueue()
  queue.push(req)
  await SecureStore.setItemAsync(QUEUE_KEY, JSON.stringify(queue))
}

export async function updateBookingQueue(queue: BookingRequest[]) {
  await SecureStore.setItemAsync(QUEUE_KEY, JSON.stringify(queue))
}

export async function clearBookingQueue() {
  await SecureStore.deleteItemAsync(QUEUE_KEY)
}

