#!/usr/bin/env bash
set -euo pipefail

# Retry helper: retry <max_attempts> <initial_sleep_seconds> -- <cmd> <args...>
# Example: retry 3 5 -- docker compose up -d
retry() {
  if [ "$#" -lt 3 ]; then
    echo "retry: usage: retry <max_attempts> <sleep_seconds> -- <cmd> [args...]" >&2
    return 2
  fi
  local max_attempts="$1"; shift
  local sleep_seconds="$1"; shift
  if [ "$1" != "--" ]; then
    echo "retry: expected -- separator" >&2
    return 2
  fi
  shift
  local attempt=1
  local cmd=("$@")
  while true; do
    "${cmd[@]}" && return 0 || true
    if [ "$attempt" -ge "$max_attempts" ]; then
      echo "Command failed after $attempt attempts: ${cmd[*]}" >&2
      return 1
    fi
    echo "Command failed. Attempt $attempt/$max_attempts. Retrying in $sleep_seconds seconds..."
    sleep "$sleep_seconds"
    attempt=$((attempt+1))
    sleep_seconds=$((sleep_seconds*2))
  done
}

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT_DIR"

echo "1) Start Postgres + Redis via docker compose"
# Retry the initial compose up because network/npm pulls may transiently fail
retry 3 5 -- docker compose up -d

echo "2) Build backend jar"
./gradlew :backend:bootJar --no-daemon --warning-mode=all

JAR=backend/build/libs/backend-0.1.0.jar
if [[ ! -f "$JAR" ]]; then
  echo "Expected jar $JAR not found!" >&2
  exit 1
fi

echo "3) Build Docker image"
# Retry docker build in case of transient registry/network issues
retry 3 5 -- docker build -t thepaddlers-backend:local -f backend/Dockerfile backend

echo "4) Find compose network"
NETWORK=$(docker network ls --filter name=$(basename "$ROOT_DIR")_default --format "{{.Name}}" | head -n1)
if [[ -z "$NETWORK" ]]; then
  NETWORK=$(docker network ls --format "{{.Name}}" | grep default | head -n1 || true)
fi
if [[ -z "$NETWORK" ]]; then
  echo "Could not determine compose network; defaulting to bridge" >&2
  NETWORK=bridge
fi

echo "5) Run backend container on network $NETWORK"
# stop previous if running
docker rm -f thepaddlers-backend-local >/dev/null 2>&1 || true

docker run --rm --name thepaddlers-backend-local \
  --network "$NETWORK" \
  -e DB_HOST=postgres -e DB_PORT=5432 -e DB_NAME=paddlers -e DB_USER=paddlers -e DB_PASSWORD=paddlers \
  -e SERVER_PORT=8081 -p 8081:8081 \
  thepaddlers-backend:local &

CONTAINER_PID=$!
sleep 2

echo "6) Wait for health endpoint"
for i in {1..30}; do
  if curl -sS http://localhost:8081/actuator/health >/dev/null 2>&1; then
    echo "Backend healthy"
    break
  fi
  echo -n '.'
  sleep 1
done

echo

echo "7) Smoke test: GET /api/courts"
curl -sS http://localhost:8081/api/courts | jq || true

echo "Done. If you want to stop the container, run: docker rm -f thepaddlers-backend-local"

# New: optional step to bring up frontends (web-admin, web-player).
# Set BRING_FRONTENDS=false to skip. Default: true.
if [ "${BRING_FRONTENDS:-true}" = "true" ]; then
  if [ -f docker-compose.frontends.yml ]; then
    echo "8) Build & start frontends via docker-compose.frontends.yml"
    # Retry frontend build (npm install might fail transiently) and up
    retry 3 10 -- docker compose -f docker-compose.frontends.yml build --pull
    retry 3 5 -- docker compose -f docker-compose.frontends.yml up -d

    # Wait for frontends to be reachable on their ports
    echo "Waiting for frontends to become available (http://localhost:5173 and http://localhost:5174)"
    # Number of seconds to wait for frontends to become available (default 120)
    FRONTEND_WAIT_SECONDS=${FRONTEND_WAIT_SECONDS:-120}
    for i in $(seq 1 "$FRONTEND_WAIT_SECONDS"); do
      ok_admin=false
      ok_player=false
      if curl -sS http://localhost:5173/ >/dev/null 2>&1; then
        ok_admin=true
      fi
      if curl -sS http://localhost:5174/ >/dev/null 2>&1; then
        ok_player=true
      fi
      if [ "$ok_admin" = true ] && [ "$ok_player" = true ]; then
        echo "Frontends are up: web-admin=5173, web-player=5174"

        # Optionally open the frontends in the default browser (macOS: open, Linux: xdg-open)
        if [ "${OPEN_FRONTENDS_BROWSER:-true}" = "true" ]; then
          echo "Opening frontends in your default browser..."
          if command -v open >/dev/null 2>&1; then
            open http://localhost:5173 || true
            open http://localhost:5174 || true
          elif command -v xdg-open >/dev/null 2>&1; then
            xdg-open http://localhost:5173 || true
            xdg-open http://localhost:5174 || true
          else
            echo "No known command to open browser automatically (tried 'open' and 'xdg-open')." >&2
            echo "Please open http://localhost:5173 and http://localhost:5174 manually."
          fi
        fi

        break
      fi
      echo -n '.'
      sleep 1
    done
    echo
  else
    echo "docker-compose.frontends.yml not found; skipping frontend startup"
  fi
else
  echo "BRING_FRONTENDS set to false; skipping frontend startup"
fi
