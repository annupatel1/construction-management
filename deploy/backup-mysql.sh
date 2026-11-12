#!/usr/bin/env bash
# Back up the Compose MySQL database and the uploaded site photos.
#
# Usage (from the repository root):
#   ./deploy/backup-mysql.sh
#
# Outputs:
#   backups/buildtrack-<timestamp>.sql.gz          MySQL dump
#   backups/buildtrack-uploads-<timestamp>.tar.gz  uploaded site photos
#
# Options:
#   BACKUP_DIR=/var/backups/buildtrack   destination directory (default: ./backups)
#   RETENTION_DAYS=30                    delete backups older than this (default: 30)
#   COMPOSE="podman compose"             use Podman instead of Docker Compose
#
# The script reads database credentials from the running `db` container, so the
# host shell does not need the database password. Run it from a trusted host.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
STAMP="$(date +%F-%H%M%S)"

if [[ -n "${COMPOSE:-}" ]]; then
  read -r -a COMPOSE_CMD <<< "$COMPOSE"
else
  COMPOSE_CMD=(docker compose)
fi

mkdir -p "$BACKUP_DIR"
FILE="$BACKUP_DIR/buildtrack-$STAMP.sql.gz"
UPLOADS_FILE="$BACKUP_DIR/buildtrack-uploads-$STAMP.tar.gz"

echo "Backing up MySQL to $FILE ..."

"${COMPOSE_CMD[@]}" exec -T db sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -uroot --single-transaction --routines --triggers "$MYSQL_DATABASE"' \
  | gzip > "$FILE"

if [[ ! -s "$FILE" ]]; then
  echo "Backup failed: the output file is empty." >&2
  exit 1
fi

if ! gzip -t "$FILE"; then
  echo "Backup failed: the gzip archive is corrupt." >&2
  exit 1
fi

if ! zgrep -q "flyway_schema_history" "$FILE"; then
  echo "Backup failed: flyway_schema_history is missing from the dump." >&2
  exit 1
fi

# Uploaded photos live in the uploads-data volume mounted at /app/uploads.
# Archive them through the running backend so the exact volume is captured.
if "${COMPOSE_CMD[@]}" ps --status running --services 2>/dev/null | grep -qx backend; then
  echo "Backing up uploaded photos to $UPLOADS_FILE ..."
  "${COMPOSE_CMD[@]}" exec -T backend sh -c 'tar -czf - -C /app/uploads .' > "$UPLOADS_FILE"
  if [[ ! -s "$UPLOADS_FILE" ]]; then
    echo "Uploads backup failed: the output file is empty." >&2
    exit 1
  fi
  if ! gzip -t "$UPLOADS_FILE"; then
    echo "Uploads backup failed: the gzip archive is corrupt." >&2
    exit 1
  fi
else
  echo "Backend is not running; skipping the uploads archive." >&2
fi

find "$BACKUP_DIR" -type f -name 'buildtrack-*.sql.gz' -mtime +"$RETENTION_DAYS" -delete
find "$BACKUP_DIR" -type f -name 'buildtrack-uploads-*.tar.gz' -mtime +"$RETENTION_DAYS" -delete

echo "Backup OK: $FILE"
if [[ -s "$UPLOADS_FILE" ]]; then
  echo "Uploads OK: $UPLOADS_FILE"
fi
echo "Restore drill: see docs/PILOT_RUNBOOK.md section 4."
