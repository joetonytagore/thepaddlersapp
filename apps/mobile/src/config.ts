import Constants from 'expo-constants'

export const API_BASE = 'http://localhost:8080'

const extra = (Constants.expoConfig && (Constants.expoConfig.extra || {})) || (Constants.manifest && (Constants.manifest.extra || {})) || {}
export const STRIPE_PUBLISHABLE_KEY = extra.stripePublishableKey || process.env.STRIPE_PUBLISHABLE_KEY || ''
