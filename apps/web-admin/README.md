# web-admin

Quick dev:

```bash
cd apps/web-admin
npm install
npm run dev -- --host
# open http://localhost:5173
```

Docker (from repo root):

```bash
docker compose -f ../../docker-compose.frontends.yml up --build -d
# open http://localhost:5173
```

