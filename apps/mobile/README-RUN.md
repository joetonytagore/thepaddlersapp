# Run the mobile app (native dev client + EAS)

This guide shows how to run the mobile app locally for native testing (Stripe native SDK requires a dev client / EAS build).

Prerequisites
- Node >= 16
- npm or yarn
- Xcode (for iOS simulator) + CocoaPods
- Android SDK and emulator (for Android)
- eas-cli (for building dev clients): `npm install -g eas-cli`

Quick local steps (dev client)

```bash
cd apps/mobile
# 1. install dependencies
npm install

# 2. copy example env and set your publishable key
cp .env.example .env
# edit .env and set STRIPE_PUBLISHABLE_KEY

# 3. Prebuild native projects (optional: EAS will prebuild for you)
expo prebuild --platform all

# 4a. iOS: install pods
cd ios && pod install && cd ..

# 4b. Start the Metro server in dev-client mode
expo start --dev-client

# 5a. Run iOS simulator (must be prebuilt or built via EAS dev client)
expo run:ios

# 5b. Run Android emulator
expo run:android
```

Use EAS for dev client builds (recommended)

```bash
# login once
eas login
# create secret (one-time) to set your publishable key in EAS
eas secret:create --name stripe_publishable_key --value "pk_test_..."
# create a dev client build for iOS
eas build --profile development --platform ios
# or Android
eas build --profile development --platform android
```

Notes
- Expo Go does not include native modules required by `@stripe/stripe-react-native`. Use a dev client or EAS-built binary to test PaymentSheet / Apple Pay / Google Pay.
- Do not put your Stripe secret key in client code. Only publishable keys belong in the client.
- If you enable Apple Pay you must add merchant entitlements in Xcode after prebuild and provide a valid Apple Merchant ID.

Troubleshooting
- If you change `app.config.js`, re-run `expo prebuild` so plugins are applied to the native projects.
- If pod install fails, `cd ios && pod repo update && pod install` may help.
- If Android builds fail, ensure `ANDROID_HOME` and related SDK env variables are set.


