# Restore Runbook (Postgres backup restore)

This document describes how to restore a backup created with `infra/pg_backup_to_s3.sh` into a test Postgres instance.

Prerequisites
- AWS CLI configured with access to the S3 bucket where backups are stored.
- Access to a Postgres instance (host, port, user, password) to restore into.

Steps
1) Download backup from S3

```bash
BUCKET=my-backups
KEY=$(aws s3 ls s3://$BUCKET/backups/ --recursive | sort | tail -n 1 | awk '{print $4}')
aws s3 cp s3://$BUCKET/$KEY /tmp/restore.dump
```

2) Create target database (example localhost)

```bash
export PGPASSWORD=thepaddlers
createdb -h localhost -U thepaddlers test_restore
```

3) Restore using pg_restore

```bash
pg_restore -h localhost -U thepaddlers -d test_restore /tmp/restore.dump
```

4) Run smoke tests against `test_restore` (example)

```bash
# set env for app to use this test DB and run smoke tests
export DB_HOST=localhost DB_PORT=5432 DB_NAME=test_restore DB_USER=thepaddlers DB_PASSWORD=thepaddlers
./gradlew :backend:test --tests org.thepaddlers.SmokeTest
```

5) Cleanup

```bash
rm -f /tmp/restore.dump
```

Notes
- For PITR (WAL restore) use `pg_basebackup` + WAL archive; this script is for full-base restore from pg_dump in custom format.
- Test restores at least monthly to verify backups are valid.

