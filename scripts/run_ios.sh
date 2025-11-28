#!/usr/bin/env zsh
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MOBILE_DIR="$REPO_ROOT/apps/mobile"
LOGFILE="$REPO_ROOT/scripts/run_ios.log"

echo "Run iOS helper script" | tee "$LOGFILE"
cd "$MOBILE_DIR"

echo "Project dir: $(pwd)" | tee -a "$LOGFILE"

# Detect expo
if grep -q '"expo"' package.json 2>/dev/null; then
  IS_EXPO=true
else
  IS_EXPO=false
fi

echo "Detected Expo-managed: $IS_EXPO" | tee -a "$LOGFILE"

# Install node deps (lockfile-aware)
if [ -f package-lock.json ]; then
  echo "Running npm ci" | tee -a "$LOGFILE"
  npm ci 2>&1 | tee -a "$LOGFILE"
elif [ -f yarn.lock ]; then
  echo "Running yarn install" | tee -a "$LOGFILE"
  yarn install --frozen-lockfile 2>&1 | tee -a "$LOGFILE" || yarn install 2>&1 | tee -a "$LOGFILE"
else
  echo "Running npm install" | tee -a "$LOGFILE"
  npm install 2>&1 | tee -a "$LOGFILE"
fi

# If ios/ exists, try CocoaPods install with retries and cache cleaning
POD_OK=false
if [ -d ios ]; then
  echo "ios/ exists — attempting CocoaPods install" | tee -a "$LOGFILE"
  cd ios

  tries=0
  max_tries=4
  while [ $tries -lt $max_tries ]; do
    tries=$((tries+1))
    echo "pod install attempt $tries/$max_tries" | tee -a "$LOGFILE"
    if pod install --repo-update 2>&1 | tee -a "$LOGFILE"; then
      POD_OK=true
      break
    fi

    echo "pod install failed — cleaning caches and retrying" | tee -a "$LOGFILE"
    pod cache clean --all 2>&1 | tee -a "$LOGFILE" || true
    rm -rf Pods Podfile.lock ~/Library/Caches/CocoaPods 2>/dev/null || true

    # Try architecture-specific installs for Apple Silicon
    echo "Trying arch -arm64 pod install" | tee -a "$LOGFILE"
    if arch -arm64 pod install --repo-update 2>&1 | tee -a "$LOGFILE"; then
      POD_OK=true
      break
    fi

    echo "Trying arch -x86_64 pod install" | tee -a "$LOGFILE"
    if arch -x86_64 pod install --repo-update 2>&1 | tee -a "$LOGFILE"; then
      POD_OK=true
      break
    fi

    echo "Waiting before next attempt..." | tee -a "$LOGFILE"
    sleep 2
  done

  cd ..
fi

if [ "$POD_OK" = true ]; then
  echo "CocoaPods install succeeded." | tee -a "$LOGFILE"
  echo "Attempting native iOS run via Expo (expo run:ios)" | tee -a "$LOGFILE"
  # Use expo run:ios when Expo-managed but ios exists
  if $IS_EXPO; then
    npx expo run:ios 2>&1 | tee -a "$LOGFILE"
  else
    npx react-native run-ios 2>&1 | tee -a "$LOGFILE"
  fi
  exit 0
fi

# If we reach here, pod install wasn't successful or ios doesn't exist. Fall back to Expo dev server + Expo Go on simulator.

auto_fallback=true
if $auto_fallback; then
  echo "Falling back to Expo start (Expo Go in simulator)" | tee -a "$LOGFILE"
  # Ensure Simulator is open
  open -a Simulator || true
  # Start Expo and open iOS simulator (non-blocking: keep foreground so logs are visible)
  npx expo start --ios 2>&1 | tee -a "$LOGFILE"
  exit 0
fi

echo "Finished script" | tee -a "$LOGFILE"
exit 0

