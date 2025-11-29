#!/usr/bin/env bash
# apps/mobile/scripts/run-native.sh
# Helper to automate: npm install -> expo prebuild -> pod install -> start metro -> run platform
# Usage: ./scripts/run-native.sh [ios|android|both]

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

PLATFORM=${1:-ios}
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$LOG_DIR"

# Log files
PREBUILD_LOG="$LOG_DIR/prebuild-$TIMESTAMP.log"
POD_LOG="$LOG_DIR/pod-$TIMESTAMP.log"
METRO_LOG="$LOG_DIR/metro-$TIMESTAMP.log"
RUN_LOG="$LOG_DIR/run-$PLATFORM-$TIMESTAMP.log"

echo "[run-native] Running for platform: $PLATFORM"

echo "[run-native] 1) npm install"
npm install 2>&1 | tee "$LOG_DIR/npm-install-$TIMESTAMP.log"

if command -v npx >/dev/null 2>&1; then
  NPX_CMD="npx"
else
  echo "[run-native] Warning: npx not found. Trying expo command directly."
  NPX_CMD="expo"
fi

# Choose which platform(s) to prebuild for to avoid interactive prompts when config is missing
if [ "$PLATFORM" = "ios" ]; then
  PREBUILD_PLATFORMS="ios"
elif [ "$PLATFORM" = "android" ]; then
  PREBUILD_PLATFORMS="android"
else
  PREBUILD_PLATFORMS="all"
fi

echo "[run-native] 2) prebuild (platforms=$PREBUILD_PLATFORMS, logs -> $PREBUILD_LOG)"
# run prebuild for the selected platforms
$NPX_CMD expo prebuild --platform "$PREBUILD_PLATFORMS" 2>&1 | tee "$PREBUILD_LOG"

# iOS pods
if [ -d "$ROOT_DIR/ios" ] && [ "$PLATFORM" != "android" ]; then
  echo "[run-native] 3) Installing CocoaPods (logs -> $POD_LOG)"
  (cd "$ROOT_DIR/ios" && pod install) 2>&1 | tee "$POD_LOG"
fi

# Start Metro in background
echo "[run-native] 4) Starting Metro (dev-client) in background (logs -> $METRO_LOG)"
# Use npx if available
$NPX_CMD expo start --dev-client > "$METRO_LOG" 2>&1 &
METRO_PID=$!
# Give Metro a moment to start
sleep 4

# Run platform
if [ "$PLATFORM" = "ios" ] || [ "$PLATFORM" = "both" ]; then
  echo "[run-native] 5) Running iOS simulator (logs -> $RUN_LOG)"
  $NPX_CMD expo run:ios 2>&1 | tee -a "$RUN_LOG"
fi

if [ "$PLATFORM" = "android" ] || [ "$PLATFORM" = "both" ]; then
  echo "[run-native] 6) Running Android emulator (logs -> $RUN_LOG)"
  $NPX_CMD expo run:android 2>&1 | tee -a "$RUN_LOG"
fi

# Cleanup: stop Metro
if ps -p $METRO_PID >/dev/null 2>&1; then
  echo "[run-native] Stopping Metro (pid=$METRO_PID)"
  kill $METRO_PID || true
fi

echo "[run-native] Done. Logs saved to: $LOG_DIR"

exit 0
