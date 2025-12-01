#!/bin/bash
echo "Backup complete: $BACKUP_FILE uploaded to $S3_BUCKET"

done
  aws s3 rm "$S3_BUCKET/$oldfile"
aws s3 ls "$S3_BUCKET/" | sort | awk '{print $4}' | grep db-backup- | head -n -$KEEP_N | while read oldfile; do
# Prune old backups

aws s3 cp "$BACKUP_FILE" "$S3_BUCKET/$(basename $BACKUP_FILE)"
# Upload to S3

pg_dump --host=$DB_HOST --port=$DB_PORT --username=$DB_USER --format=custom $DB_NAME | gzip > "$BACKUP_FILE"
# Dump DB

BACKUP_FILE="$BACKUP_DIR/db-backup-$DATE.dump.gz"
DATE=$(date +"%Y%m%d-%H%M%S")
KEEP_N=${KEEP_N:-7}
BACKUP_DIR=${BACKUP_DIR:-/tmp}
S3_BUCKET=${S3_BUCKET:-s3://thepaddlers-backups}
DB_NAME=${DB_NAME:-thepaddlers}
DB_USER=${DB_USER:-postgres}
DB_PORT=${DB_PORT:-5432}
DB_HOST=${DB_HOST:-localhost}
# Configurable variables
set -e

