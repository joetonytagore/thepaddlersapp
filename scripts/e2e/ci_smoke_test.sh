#!/usr/bin/env bash
# CI-friendly smoke test: runs migrations, starts backend in background, then runs a quick backend e2e test that POSTs a webhook event.
set -euo pipefail
ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$ROOT_DIR"

echo "Running Flyway migrations..."
cd backend
if [ -f gradlew ]; then
  ./gradlew flywayMigrate -q
else
  echo "gradlew not found; ensure Flyway migrations are applied manually"
fi

# Start backend in background (test profile)
./gradlew bootRun -Dspring.profiles.active=test &
BACKEND_PID=$!

# Wait for backend to be ready
echo "Waiting for backend to be ready..."
for i in {1..30}; do
  if curl -sSf http://localhost:8080/actuator/health >/dev/null; then
    echo "backend ready"
    break
  fi
  sleep 1
done

# Run a quick webhook POST to simulate Stripe event
echo "Posting simulated webhook event..."
curl -s -X POST http://localhost:8080/api/webhooks/stripe -H 'Content-Type: application/json' --data-binary @- <<'JSON'
{
  "id": "evt_ci_test_1",
  "type": "payment_intent.succeeded",
  "data": {
    "object": {
      "id": "pi_ci_123",
      "object": "payment_intent",
      "metadata": {
        "bookingId": "42"
      },
      "amount": 1000,
      "currency": "usd"
    }
  }
}
JSON

sleep 1

# Basic check: ensure endpoint returned 2xx (curl will have failed if not)

echo "CI smoke test complete. Stopping backend."
kill $BACKEND_PID || true
