#!/bin/bash
set -e
# Usage: restore_db.sh <backup_file>
BACKUP_FILE="$1"
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_USER=${DB_USER:-postgres}
DB_NAME=${DB_NAME:-thepaddlers}

if [ -z "$BACKUP_FILE" ]; then
  echo "Usage: $0 <backup_file>"
  exit 1
fi

gunzip -c "$BACKUP_FILE" | pg_restore --host=$DB_HOST --port=$DB_PORT --username=$DB_USER --dbname=$DB_NAME --clean

echo "Restore complete from $BACKUP_FILE"

