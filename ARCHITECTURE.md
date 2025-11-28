High-level architecture (recommended) for The Paddlers

Frontend
- Player + Admin web: React (TypeScript) with Next.js for marketing/SSR and Tailwind CSS for styling.
- Mobile: React Native or Flutter (prefer React Native to reuse JS/TS code).

Backend
- Kotlin + Spring Boot or Java + Spring Boot (current project uses Java + Spring Boot). Modular services:
  - API service (REST + optional GraphQL)
  - Auth service (OAuth2/JWT)
  - Scheduler/Booking service (booking rules, conflict resolution)
  - Billing/Payments service (Stripe integration)
  - Notifications service (background jobs for email/SMS/push)

Persistence & Cache
- PostgreSQL: primary relational DB for users, bookings, invoices
- Redis: cache, rate-limiting, distributed locks
- S3-compatible object store for media and receipts

Realtime & Background
- WebSockets (Spring WebSocket) or Socket.IO for realtime updates
- Background workers: RabbitMQ / Redis Queue / Kafka for async jobs

Third-party integrations
- Payments: Stripe (Payment Intents, Checkout, Connect)
- SMS: Twilio
- Email: SendGrid / Amazon SES
- Door/access: Kisi / SALTO APIs

Infra & DevOps
- Containerize services with Docker, deploy on Kubernetes (EKS/GKE)
- RDS for Postgres, ElastiCache for Redis
- CI/CD: GitHub Actions — build, test, image push, deploy
- Monitoring: Prometheus + Grafana; Sentry for error tracking

Auth & Security
- OAuth2 / JWT for APIs, role-based access for admin
- PCI: use tokenized payment providers (Stripe) — never store raw card data
- GDPR: data export/delete endpoints, retention policies

Notes for this repository
- `backend/` contains a Java + Spring Boot example API with:
  - Entities: User, Court, Booking
  - Repositories and simple booking endpoint with conflict checks
  - Local `docker-compose.yml` for Postgres + Redis
- Next steps to follow this architecture:
  - Split backend into services if scale requires it (billing, scheduler)
  - Add async workers and message broker for notifications/invoicing
  - Add Redis-based distributed locks when booking concurrency is high
  - Integrate Stripe webhooks and secure endpoints
  - Add React admin/mobile clients in `web/` and `mobile/` folders

This document captures the recommended stack and where to focus next while building parity with platforms like CourtReserve.

