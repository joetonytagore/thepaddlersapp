# Ops Runbook (ThePaddlers)

This runbook covers operational best practices for thePaddlers app: backups, monitoring, secrets, PCI, GDPR and basic incident steps.

1) Backups
- Use `infra/pg_backup_to_s3.sh` to push daily backups to S3. Schedule via cron or Kubernetes CronJob.
- Test restores monthly using `infra/restore_runbook.md`.

2) Monitoring
- Prometheus: enable Micrometer Prometheus registry (Actuator `/actuator/prometheus`).
- Grafana: import dashboards with JVM and request-latency metrics.
- Sentry: configure DSN via `SENTRY_DSN` env var to capture exceptions.

3) Secrets
- Store secrets in AWS Secrets Manager or HashiCorp Vault. Do not commit secrets in repo.
- In CI, use GitHub Actions secrets or OIDC to fetch short-lived creds.

4) PCI
- Use Stripe Checkout or Elements to avoid handling raw card data.
- Verify webhooks using the Stripe signing secret and store events for idempotency.

5) GDPR
- Use `DELETE /api/users/:id` (or admin flow) to anonymize or remove PII. Keep financial records for retention period.
- Implement `GET /api/users/:id/export` to return user's data.

6) Restore procedure
- See `infra/restore_runbook.md` for step-by-step restore and smoke tests.

7) CI
- GitHub Actions workflow at `.github/workflows/ci.yml` runs backend tests using Postgres service.

8) Incident Response (summary)
- If keys are leaked: rotate Stripe keys, revoke AWS credentials, and update secrets store.
- If data breach: follow legal obligations for notification and contain affected systems.


