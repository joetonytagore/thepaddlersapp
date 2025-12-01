import { useEffect } from 'react'
import * as Notifications from 'expo-notifications'
import Constants from 'expo-constants'
import { registerDevice } from './api'
import { Platform } from 'react-native'

export async function getPushToken() {
  const { status } = await Notifications.getPermissionsAsync()
  let finalStatus = status
  if (status !== 'granted') {
    const { status: askStatus } = await Notifications.requestPermissionsAsync()
    finalStatus = askStatus
  }
  if (finalStatus !== 'granted') return null
  const tokenData = await Notifications.getExpoPushTokenAsync()
  return tokenData.data
}

export function PushRegister({ userId }: { userId: number }) {
  useEffect(() => {
    async function register() {
      const pushToken = await getPushToken()
      if (!pushToken) return
      const deviceId = Constants.deviceId || Constants.installationId || 'unknown'
      const platform = Platform.OS
      const appVersion = Constants.expoConfig?.version || 'unknown'
      await registerDevice(deviceId, platform, pushToken, appVersion, userId)
    }
    register()
  }, [userId])
  return null
}

