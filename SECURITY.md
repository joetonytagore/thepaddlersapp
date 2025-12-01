# Security Guide
- Audit all access quarterly
- Stripe keys: rotate annually or after staff changes
- API keys, DB passwords, and JWT secrets: rotate every 90 days
## Rotation Schedule

- Enforce MFA for privileged actions
- Admins must enable MFA (TOTP or SMS)
## MFA Guidance

- No password reuse allowed
- Passwords hashed with BCrypt
- Minimum 12 characters, at least 1 number and symbol
## Password Policies

- Rotate secrets quarterly and after any incident
- Use environment variables for runtime secrets
- Never commit secrets to git
- Store secrets in AWS Secrets Manager or Vault
## Secret Handling

- We respond within 48h and coordinate fixes
- Email security@thepaddlers.club for vulnerabilities
## Responsible Disclosure


