# Operations Runbook

## Deployment Steps
1. Clone repo and set up environment variables (.env)
2. Build backend: `./gradlew :backend:build`
3. Build frontend: `npm ci && npm run build` in each frontend app
4. Build Docker images: `docker build -t thepaddlersapp-backend ./services/backend`
5. Push images to registry: `docker push <registry>/thepaddlersapp-backend`
6. Deploy using Kubernetes manifests or docker-compose
7. Verify `/actuator/health` and `/actuator/metrics` endpoints

## Backup/Restore
- Use `infra/scripts/backup_db.sh` to backup Postgres DB to S3
- Use `infra/scripts/restore_db.sh <backup_file>` to restore
- S3 lifecycle rules auto-expire old backups (see infra/terraform/s3_backup.tf)
- RTO: 1 hour, RPO: 24 hours

## Scaling
- Use horizontal pod autoscaling for backend (Kubernetes HPA)
- Use managed DB (AWS RDS/Aurora) for scaling
- Use S3 for media storage

## Monitoring
- Prometheus metrics at `/actuator/prometheus`
- Grafana dashboards (see docs/grafana-metrics.md)
- Sentry for error tracking (set SENTRY_DSN)

## Incident Contact
- Email: ops@thepaddlers.club
- Slack: #ops-support
- PagerDuty escalation for critical incidents

