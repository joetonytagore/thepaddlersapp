# The Paddlers - Mobile (Expo)

This is a minimal Expo scaffold to test login against the backend.

Prerequisites
- Node 18+ and npm or yarn
- Expo CLI: `npm install -g expo-cli` or use `npx expo` commands

Install & run

```bash
cd apps/mobile
npm install
npm start
# use Expo Go (Android/iOS) or run on simulator with `npm run android` / `npm run ios`
```

Notes
- The app uses `expo-secure-store` to store the JWT token.
- Update `src/config.ts` if your backend runs on a different host/port.

