#!/usr/bin/env bash
# Orchestrates local e2e: starts DB, runs flyway migrations, starts backend, and starts stripe CLI listener.
set -euo pipefail
ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$ROOT_DIR"

echo "Starting Postgres via docker-compose (only db service)..."
docker-compose -f docker-compose.local.yml up -d db

echo "Running Flyway migrations..."
cd backend
if [ -f gradlew ]; then
  ./gradlew flywayMigrate
else
  echo "gradlew not found; ensure Flyway migrations are applied manually"
fi

echo "Starting backend (bootRun)..."
./gradlew bootRun &
BACKEND_PID=$!
sleep 6

echo "Starting Stripe CLI listen (forward to backend webhook)..."
# Ensure stripe CLI is logged in first
stripe listen --forward-to http://localhost:8080/api/payments/webhook --events payment_intent.succeeded,payment_intent.payment_failed &
STRIPE_PID=$!

echo "Run your e2e tests now (mobile or backend). Kill background processes when done."

echo "Press Ctrl+C to stop and cleanup"
trap "echo stopping; kill $BACKEND_PID $STRIPE_PID; docker-compose -f docker-compose.local.yml down" EXIT
wait

