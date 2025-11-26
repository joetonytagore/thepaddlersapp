# ThePaddlers (MVP)

This repo contains a simple MVP layout for The Paddlers: a Kotlin + Spring Boot backend and two small React apps (admin & player).

Quick start (assumes Docker, Node 18+, Java 17):

1) Start local Postgres

```bash
cd infra
docker compose up -d
```

2) Run backend (from repo root)

```bash
./gradlew :backend:clean :backend:bootJar
# run with env (example)
DB_HOST=localhost DB_PORT=5432 DB_NAME=thepaddlers_dev DB_USER=thepaddlers DB_PASSWORD=thepaddlers SERVER_PORT=8081 java -jar services/backend/build/libs/backend-0.1.0.jar
```

3) Run admin UI

```bash
cd apps/web-admin
npm install
npm run dev
# open http://localhost:5173
```

4) Run player UI

```bash
cd apps/web-player
npm install
npm run dev
# open http://localhost:5174
```

Notes
- The frontend apps proxy `/api` to `http://localhost:8081` via Vite config.
- The backend seeds a demo user/court/booking on startup.

---

## Frontend — Run Locally vs Docker

This section explains two supported ways to run the frontends:
- Locally with Vite (fast development with HMR)
- In Docker (containerized, serves built production assets via nginx)

Recommended workflow:
- Use the local Vite dev servers for day-to-day development (hot reload).
- Use the Docker setup when you need an environment that matches production or to avoid installing Node on the host.

### A) Run locally (Vite dev servers)

Requirements
- Node 18+ (we recommend installing via `nvm`)
- npm (bundled with Node)

Install Node (optional, recommended):

```bash
# using nvm (recommended)
curl -fsSL https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
nvm install --lts
nvm use --lts
```

Start each app in its own terminal (hot-reload enabled):

```bash
# admin (defaults to :5173)
cd apps/web-admin
npm install
npm run dev -- --host

# player (we use :5174 to avoid port conflicts)
cd apps/web-player
npm install
npm run dev -- --host --port 5174
```

Open:
- Admin UI: http://localhost:5173
- Player UI: http://localhost:5174

Run both dev servers in one command (single terminal) using `concurrently`:

```bash
# from repo root or any folder
npx concurrently "cd apps/web-admin && npm run dev -- --host" "cd apps/web-player && npm run dev -- --host --port 5174"
```

Notes
- The Vite dev servers proxy API calls to the backend at `http://localhost:8081` (see each app's `vite.config.ts`). Ensure the backend is running when you need real API responses.
- If you see npm/registry errors (eg. ETARGET), verify package versions in `apps/*/package.json` and run `npm cache clean --force` before retrying.

### B) Run in Docker (production/static build served by nginx)

I included a `Dockerfile` in each frontend folder and a `docker-compose.frontends.yml` at the repo root to build and serve the static builds.

Build & run (requires Docker Desktop / Docker Engine):

```bash
# from repo root
docker compose -f docker-compose.frontends.yml up --build -d
```

Open:
- Admin UI (built assets served by nginx): http://localhost:5173
- Player UI (built assets served by nginx): http://localhost:5174

Stop and remove containers:

```bash
docker compose -f docker-compose.frontends.yml down
```

Rebuild after changes (force rebuild images):

```bash
docker compose -f docker-compose.frontends.yml build --no-cache
docker compose -f docker-compose.frontends.yml up -d
```

Notes
- The containers serve files from nginx on container port 80; compose maps host ports 5173/5174 to container port 80.
- Use Docker when you want the production build (no HMR). For development, prefer the Vite dev servers.

### Troubleshooting

- Port already in use: change the port (Vite supports `--port`) or stop the process using the port (`lsof -iTCP -sTCP:LISTEN -P -n | grep 5173` then `kill` the PID).
- Node/npm missing: use `nvm` or Homebrew (`brew install node`) to install Node.
- npm install failures (registry/version issues):
  - Try `npm cache clean --force`
  - Check versions in `apps/*/package.json` (some versions may be incompatible with the registry)
  - If a specific package version is unavailable, try a nearby stable version (example: `tailwindcss@^3.4.0`)

### Summary quick commands

```bash
# Start backend (local Postgres required)
cd infra && docker compose up -d
./gradlew :backend:clean :backend:bootJar
DB_HOST=localhost DB_PORT=5432 DB_NAME=thepaddlers_dev DB_USER=thepaddlers DB_PASSWORD=thepaddlers SERVER_PORT=8081 java -jar services/backend/build/libs/backend-0.1.0.jar

# Dev (Vite)
cd apps/web-admin && npm install && npm run dev -- --host
cd apps/web-player && npm install && npm run dev -- --host --port 5174

# Docker (build & serve static)
docker compose -f docker-compose.frontends.yml up --build -d
```
