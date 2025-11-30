# Security Checklist for ThePaddlersApp

## Secret Handling
- All secrets (API keys, DB passwords, Stripe keys) are stored in environment variables or secret managers.
- Never commit secrets to source control.
- Rotate secrets regularly and after any suspected leak.

## Backup Schedule
- Automated daily backups of the production database.
- Backups are encrypted and stored offsite (e.g., S3).
- Backup restore procedures are documented in `infra/restore_runbook.md`.

## Database Retention
- Production database retention policy: retain data for 7 years unless otherwise required.
- Remove demo/test accounts from production before deployment.

## Deployment Checklist
- All dependencies are up-to-date and scanned for vulnerabilities (Dependabot/Snyk/Qodana).
- Environment variables are set for production (no demo credentials).
- Data seeding only runs in local/dev environments (`SEED=true`).
- All endpoints requiring authentication are protected.
- HTTPS is enforced for all public endpoints.
- Stripe webhook secret is set and signature verification is enabled.
- Admin UI is protected by authentication and role-based access control.

## Incident Response
- Security incidents are logged and reviewed.
- Contact information for security issues is provided in this file.

---
For more details, see `OPS_README.md` and `infra/restore_runbook.md`.
