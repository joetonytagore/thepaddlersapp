# Mobile: Expo / EAS + Stripe native setup

This document explains how to configure Expo (managed workflow) with EAS prebuild and the `stripe-react-native` SDK so your app can accept payments natively on iOS and Android.

Prerequisites
- Node.js >= 16
- yarn or npm
- expo-cli (optional for local dev): `npm install -g expo-cli`
- eas-cli (for builds): `npm install -g eas-cli`
- stripe CLI (for local webhook testing): https://stripe.com/docs/stripe-cli

Overview
- Use `stripe-react-native` for native Stripe SDK functionality (Apple Pay / Google Pay, PaymentSheet, etc.).
- With Expo managed workflow, run `expo prebuild` (or let EAS prebuild during `eas build`) to generate native iOS/Android projects that will include the Stripe SDK.
- Use EAS secrets or `app.config.js` to inject the publishable key. Never include secret keys in code.

Files you'll create/modify
- apps/mobile/app.config.js (recommended) — inject runtime env into app manifest
- apps/mobile/app.json — keep minimal, include `plugins` entry for stripe plugin
- apps/mobile/README.md (this file)
- apps/mobile/src/* — initialize Stripe in your app startup (see snippet below)

Install Stripe SDK

From the mobile app root:

```
cd apps/mobile
yarn add stripe-react-native
# or: npm install stripe-react-native
```

app.config.js (recommended)

Create `apps/mobile/app.config.js` to expose EAS/ENV secrets into `expo` config at build time:

```js
import 'dotenv/config';

export default ({ config }) => {
  return {
    ...config,
    extra: {
      ...config.extra,
      stripePublishableKey: process.env.STRIPE_PUBLISHABLE_KEY || process.env.EXPO_PUBLIC_STRIPE_PUBLISHABLE_KEY,
    },
    plugins: [
      // This plugin is required to integrate stripe-react-native during prebuild
      'stripe-react-native'
    ]
  };
};
```

If you prefer static `app.json`, add `plugins` and an `extra` object containing `stripePublishableKey`.

app.json (minimal snippet)

```json
{
  "expo": {
    "name": "ThePaddlersApp",
    "slug": "thepaddlersapp",
    "plugins": ["stripe-react-native"],
    "extra": {
      "stripePublishableKey": ""
    }
  }
}
```

Initialize Stripe in JS (App startup)

In `App.tsx` or a central `payments` module, initialize stripe:

```ts
import { initStripe } from '@stripe/stripe-react-native';
import Constants from 'expo-constants';

initStripe({
  publishableKey: Constants.expoConfig?.extra?.stripePublishableKey || process.env.STRIPE_PUBLISHABLE_KEY,
});
```

iOS (Info.plist / Entitlements)

- If you plan to support Apple Pay, configure a merchant id and enable the Apple Pay capability in Xcode after `expo prebuild`.
- The `stripe-react-native` plugin will add necessary pod dependencies during prebuild. You generally don't need to manually edit `Info.plist` unless your app uses camera, photos or other permissions.

Android (AndroidManifest.xml / Gradle)

- `expo prebuild` will create `android/app/src/main/AndroidManifest.xml`. The `stripe-react-native` plugin injects required configuration. Do not hardcode secret keys.
- If you need to add manifest placeholders or permissions, edit `android/app/src/main/AndroidManifest.xml` after prebuild.

EAS build configuration and secrets

Create `eas.json` entries (examples):

```json
{
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal",
      "env": {
        "STRIPE_PUBLISHABLE_KEY": "@stripe_publishable_key"
      }
    },
    "production": {
      "env": {
        "STRIPE_PUBLISHABLE_KEY": "@stripe_publishable_key"
      }
    }
  }
}
```

Set EAS secret (one-time):

```
eas secret:create --name stripe_publishable_key --value pk_test_...
```

Commands (prebuild, build, install pods)

```
cd apps/mobile
yarn install
# Prebuild native projects (optional locally)
expo prebuild --platform all
# iOS pods (if you open Xcode locally)
cd ios && pod install
# Build with EAS
eas build -p ios --profile production
eas build -p android --profile production
```

Local webhook testing with Stripe CLI

Forward Stripe events to your local backend webhook endpoint (example):

```
# Log in once with `stripe login` then:
stripe listen --forward-to http://localhost:8080/api/payments/webhook --events payment_intent.succeeded,payment_intent.payment_failed
```

Then trigger events using the CLI or the dashboard:

```
stripe trigger payment_intent.succeeded
```

Security notes

- Never put your Stripe secret key in the mobile app. Only the publishable key is safe for the client.
- Server-side code must hold the secret key and create/confirm PaymentIntents.

Troubleshooting

- If you see pod or gradle errors after prebuild, remove `ios/` and `android/` and run `expo prebuild` again.
- If Apple Pay needs merchant entitlements, open the generated Xcode project and enable it there.

Useful links
- stripe-react-native: https://github.com/stripe/stripe-react-native
- Stripe docs for mobile: https://stripe.com/docs/payments
- Expo EAS build docs: https://docs.expo.dev/build/introduction/


