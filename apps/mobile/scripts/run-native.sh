#!/usr/bin/env bash
# apps/mobile/scripts/run-native.sh
# Helper to automate native prebuild, CocoaPods install and run for iOS/Android.
set -euo pipefail

# Usage: ./run-native.sh [ios|android|all]
TARGET=${1:-ios}
# Resolve MOBILE_DIR relative to the script location to avoid duplicate path segments
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MOBILE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ROOT_DIR="$(cd "$MOBILE_DIR/.." && pwd)"
LOG_DIR="$MOBILE_DIR/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
LOGFILE="$LOG_DIR/run-native-$TIMESTAMP.log"

echo "[run-native] Root: $ROOT_DIR"
echo "[run-native] Mobile dir: $MOBILE_DIR"
echo "[run-native] Log: $LOGFILE"

cd "$MOBILE_DIR"

# Install JS deps if node_modules missing or package-lock changed
if [ ! -d node_modules ]; then
  echo "[run-native] Installing JS dependencies..." | tee -a "$LOGFILE"
  npm install 2>&1 | tee -a "$LOGFILE"
else
  echo "[run-native] Using existing node_modules" | tee -a "$LOGFILE"
fi

# Expo prebuild (generates ios/ and android/ if needed)
echo "[run-native] Running expo prebuild (platform: all)..." | tee -a "$LOGFILE"
# Use npx so global expo cli is not required
npx expo prebuild --platform all 2>&1 | tee -a "$LOGFILE"

# CocoaPods install (iOS native deps)
if [ "$TARGET" = "ios" ] || [ "$TARGET" = "all" ]; then
  if [ -d ios ]; then
    echo "[run-native] Installing CocoaPods (ios/)" | tee -a "$LOGFILE"
    (cd ios && pod install --repo-update) 2>&1 | tee -a "$LOGFILE"
  else
    echo "[run-native] Warning: ios/ directory not found after prebuild" | tee -a "$LOGFILE"
  fi
fi

# Run iOS app
if [ "$TARGET" = "ios" ]; then
  echo "[run-native] Starting iOS (expo run:ios)..." | tee -a "$LOGFILE"
  # Run via expo which will call the native runner; this requires Xcode / simulator available on host
  npx expo run:ios 2>&1 | tee -a "$LOGFILE"
fi

# Run Android app
if [ "$TARGET" = "android" ] || [ "$TARGET" = "all" ]; then
  echo "[run-native] Starting Android (expo run:android)..." | tee -a "$LOGFILE"
  # Ensure an Android emulator is running or a device is connected
  npx expo run:android 2>&1 | tee -a "$LOGFILE"
fi

echo "[run-native] Completed. Logs: $LOGFILE" | tee -a "$LOGFILE"
