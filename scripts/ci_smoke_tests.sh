#!/usr/bin/env bash
set -euo pipefail

# Start backend in background and run basic smoke tests (login flows + /api/users/me)
ROOT=$(cd "$(dirname "$0")/.." && pwd)
LOG=/tmp/thepaddlers_backend_ci.log

echo "Starting backend (bootRun)"
nohup ./gradlew :backend:bootRun --no-daemon > "$LOG" 2>&1 &
PID=$!
trap 'echo "Stopping backend..."; kill $PID || true; sleep 1; tail -n 200 "$LOG"' EXIT

# Wait for server
for i in {1..30}; do
  if curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/ | grep -q "200\|404"; then
    echo "backend started"
    break
  fi
  echo "waiting for backend... ($i)"
  sleep 1
done

echo "=== TEST: empty body ==="
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -H "Accept: application/json" -X POST -H "Content-Type: application/json" -d '{}' http://localhost:8081/api/auth/login) || true
if [ "$HTTP" != "400" ]; then
  echo "Expected 400 for empty body, got $HTTP"
  exit 2
fi

echo "=== TEST: paddlers ==="
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -H "Accept: application/json" -X POST -H "Content-Type: application/json" -d '{"email":"paddlers"}' http://localhost:8081/api/auth/login) || true
if [ "$HTTP" != "401" ]; then
  echo "Expected 401 for invalid credentials, got $HTTP"
  exit 3
fi

echo "=== TEST: demo login and /api/users/me ==="
RESP=$(curl -s -H "Content-Type: application/json" -X POST -d '{"email":"demo@paddlers.test"}' http://localhost:8081/api/auth/login)
TOKEN=$(echo "$RESP" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token'))")
if [ -z "$TOKEN" ] || [ "$TOKEN" = "None" ]; then
  echo "Failed to obtain token from login: $RESP"
  exit 4
fi

HTTP=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/users/me) || true
if [ "$HTTP" != "200" ]; then
  echo "Expected 200 for /api/users/me, got $HTTP"
  exit 5
fi

echo "Smoke tests passed"

# success (trap will stop backend)
exit 0

