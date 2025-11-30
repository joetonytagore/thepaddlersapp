# Run the mobile app (native dev client + EAS)

This guide shows how to run the mobile app locally for native testing. The project uses native Stripe SDK (via `@stripe/stripe-react-native`), so you must use a dev client or an EAS-built binary for features like PaymentSheet, Apple Pay or Google Pay.

Summary / checklist
- Install dependencies and set environment variables
- Restore `app.config.js` from `app.config.js.bak` if missing
- Prebuild native projects (use `npx` if `expo` is not installed globally)
- Install CocoaPods for iOS
- Start Metro in dev-client mode and run the simulator/emulator
- Use the helper script `scripts/run-native.sh` to automate steps and save logs

Prerequisites
- Node.js >= 16
- npm or yarn
- Xcode (macOS) for iOS simulator + CocoaPods (bundler optional)
- Android SDK + emulator for Android (or a physical device)
- eas-cli (optional, for building dev clients / EAS builds): `npm install -g eas-cli`

Important: prefer `npx` over a global `expo` install to avoid version drift. Examples below use `npx` when appropriate.

1) Quick local steps (dev client)

```bash
cd apps/mobile
# 1. install JS deps
npm install

# 2. copy example env and set your publishable key
cp .env.example .env
# edit .env and set STRIPE_PUBLISHABLE_KEY (only publishable key belongs in client)
```

2) Restore `app.config.js` if you intentionally modified it or lost it

A backup (`app.config.js.bak`) exists in the repo. If you need to restore it:

```bash
cd apps/mobile
cp app.config.js.bak app.config.js
# After changing app.config.js re-run the prebuild step below
```

3) Prebuild native projects (applies Expo plugins and generates native Android/iOS projects)

If you have a global `expo` CLI:

```bash
cd apps/mobile
expo prebuild --platform all
```

If you don't have the global CLI (recommended to use `npx`):

```bash
cd apps/mobile
# runs the local or remote expo CLI without requiring a global install
npx expo prebuild --platform all

# alternatively add this script to package.json:
# "scripts": { "prebuild:all": "expo prebuild --platform all" }
# then run:
# npm run prebuild:all
```

4) iOS: install CocoaPods (after prebuild)

```bash
cd apps/mobile/ios
pod install
cd ../..
```

If `pod install` fails, try:

```bash
cd ios
pod repo update
pod install
```

5) Start Metro (dev client) and run

Start Metro (dev-client mode). Use `npx` if `expo` is not installed globally:

```bash
cd apps/mobile
npx expo start --dev-client
```

In another terminal you can run the simulator/emulator (requires prebuilt native projects or a dev-client binary):

```bash
# Run on iOS simulator
cd apps/mobile
npx expo run:ios

# Run on Android emulator
cd apps/mobile
npx expo run:android
```

6) EAS dev client builds (recommended for native SDKs)

The repo contains an `eas.json` scaffold at `apps/mobile/eas.json`. Use EAS secrets to provide the real publishable key when building on EAS:

```bash
# login once
eas login
# set the publishable key as a secret for EAS (recommended)
eas secret:create --name stripe_publishable_key --value "pk_test_..."
# build a development client for iOS
eas build --profile development --platform ios
# build for Android
eas build --profile development --platform android
```

7) Helper: `scripts/run-native.sh`

A helper script is available at `apps/mobile/scripts/run-native.sh`. It automates:
- npm install (if node_modules not present)
- npx expo prebuild --platform all
- cd ios && pod install (if iOS exists)
- run platform-specific runner (npx expo run:ios / npx expo run:android)
- logs saved under `apps/mobile/logs/run-native-*.log`

Usage (from repo root):

```bash
# run the helper (defaults to ios):
./apps/mobile/scripts/run-native.sh ios

# or run android:
./apps/mobile/scripts/run-native.sh android

# run both (prebuild + pod install + run android + run ios):
./apps/mobile/scripts/run-native.sh all

# inspect logs
ls apps/mobile/logs
# tail the run output
tail -f apps/mobile/logs/run-native-*.log
```

8) App identifiers

The app bundle / package IDs were changed from `com.anonymous.thepaddlers-mobile` to `com.thepaddlers.thepaddlers-mobile`. If you need a different bundle id, set the env vars:
- IOS_BUNDLE_IDENTIFIER
- ANDROID_PACKAGE

9) Troubleshooting
- If you see `zsh: command not found: expo` — use `npx expo ...` or install the CLI: `npm install -g expo-cli`.
- If you see `Constants.platform.ios.model has been deprecated` at runtime: it is a deprecation warning from `expo-constants`. It does not prevent the app from running; upgrade `expo-constants` or silence the warning in runtime code if you prefer.
- If your native plugins (like `@stripe/stripe-react-native`) are not available in Expo Go, you must use a dev client or EAS-built binary.
- For Android on macOS prefer using the host Android Studio emulator or a USB device — containerized emulators in docker are not generally supported on macOS.
- If a build or run fails, paste the failing log lines here and I will help debug them.

10) Want me to also run validations or change files?
- I created the helper script and `eas.json`. If you want changes (use yarn instead of npm, change default bundle id, or adjust pod install behavior), tell me and I'll update the scripts.
