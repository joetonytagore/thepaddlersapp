# Run the mobile app (native dev client + EAS)

This guide shows how to run the mobile app locally for native testing. The project uses native Stripe SDK (via `@stripe/stripe-react-native`), so you must use a dev client or an EAS-built binary for features like PaymentSheet, Apple Pay or Google Pay.

Summary / checklist
- Install dependencies and set environment variables
- (Optional) restore `app.config.js` from `app.config.js.bak`
- Prebuild native projects (use `npx` if `expo` is not installed globally)
- Install CocoaPods for iOS
- Start Metro in dev-client mode and run the simulator/emulator
- (Optional) use the helper script `scripts/run-native.sh` to automate these steps and save logs

Prerequisites
- Node.js >= 16
- npm or yarn
- Xcode (macOS) for iOS simulator + CocoaPods (bundler optional)
- Android SDK + emulator for Android
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

The repo already contains an `eas.json` scaffold. Use EAS secrets to provide the real publishable key when building on EAS:

```bash
# login once
eas login
# set the publishable key as a secret for EAS
eas secret:create --name stripe_publishable_key --value "pk_test_..."
# build a development client for iOS
eas build --profile development --platform ios
# build for Android
eas build --profile development --platform android
```

7) New helper: `scripts/run-native.sh`

To simplify local runs I added a helper script at `scripts/run-native.sh` (relative to `apps/mobile/`). It automates:
- npm install
- npx expo prebuild --platform all
- cd ios && pod install (if iOS exists)
- start Metro in dev-client mode (background)
- run the selected platform (ios|android)
- save logs to `apps/mobile/logs/` with timestamps

Usage (from repo root or `apps/mobile`):

```bash
# run the helper (defaults to ios). Run from project root:
./apps/mobile/scripts/run-native.sh ios

# or run android:
./apps/mobile/scripts/run-native.sh android

# run both (prebuild + start metro only):
./apps/mobile/scripts/run-native.sh both

# logs are written to apps/mobile/logs/
ls apps/mobile/logs
# tail the run output
tail -f apps/mobile/logs/prebuild-*.log
```

8) Environment variables and sensitive keys

- Only put publishable Stripe keys in client env. Never store Stripe secret keys in client code or commit them.
- For local testing, put your keys in `.env` (which is in `.gitignore`). For EAS builds use `eas secret:create` to inject secrets.

9) Troubleshooting
- If you see `zsh: command not found: expo` — use `npx expo ...` or install the CLI: `npm install -g expo-cli`.
- If your native plugins (like `@stripe/stripe-react-native`) are not available in Expo Go, you must use a dev client or EAS-built binary.
- If a build or run fails, save and paste the failing log lines here and I will help debug them.

10) Want me to also run validations or change files?
- I created `scripts/run-native.sh` for you. If you want, I can modify it (e.g., use `yarn` instead of `npm`, or add bundler support for CocoaPods). Say what you want changed and I'll update it.
