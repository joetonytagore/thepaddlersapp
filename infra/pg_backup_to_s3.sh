#!/usr/bin/env bash
set -euo pipefail

# Simple Postgres backup to S3 script (uses pg_dump custom format)
# Requires: AWS CLI configured with permissions to write to S3
# Usage: ./pg_backup_to_s3.sh <s3-bucket> <pg-host> <pg-port> <pg-db> <pg-user>

S3_BUCKET=${1:-}
PG_HOST=${2:-localhost}
PG_PORT=${3:-5432}
PG_DB=${4:-thepaddlers_dev}
PG_USER=${5:-thepaddlers}

if [ -z "$S3_BUCKET" ]; then
  echo "Usage: $0 <s3-bucket> [pg-host] [pg-port] [pg-db] [pg-user]"
  exit 2
fi

TIMESTAMP=$(date -u +%Y%m%dT%H%M%SZ)
FILENAME="thepaddlers-backup-${TIMESTAMP}.dump"
LOCAL_PATH="/tmp/${FILENAME}"

echo "Backing up $PG_DB@$PG_HOST:$PG_PORT to $LOCAL_PATH"
PGPASSWORD=${PG_PASSWORD:-thepaddlers} pg_dump -h "$PG_HOST" -p "$PG_PORT" -U "$PG_USER" -Fc -f "$LOCAL_PATH" "$PG_DB"

echo "Uploading to s3://${S3_BUCKET}/backups/${FILENAME}"
aws s3 cp "$LOCAL_PATH" "s3://${S3_BUCKET}/backups/${FILENAME}"

echo "Backup uploaded. Removing local file"
rm -f "$LOCAL_PATH"

echo "Done: s3://${S3_BUCKET}/backups/${FILENAME}"

