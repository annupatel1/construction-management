#!/usr/bin/env bash
# Create a .env file with strong, independent secrets for Docker Compose.
# Usage: ./scripts/generate-env.sh [--force]
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
EXAMPLE_FILE="$ROOT_DIR/.env.example"

if [[ -f "$ENV_FILE" && "${1:-}" != "--force" ]]; then
  echo "Refusing to overwrite existing $ENV_FILE"
  echo "Run with --force if you really want to replace it."
  exit 1
fi

if ! command -v openssl >/dev/null 2>&1; then
  echo "openssl is required to generate secrets." >&2
  exit 1
fi

JWT_SECRET="$(openssl rand -hex 32)"
OTP_SECRET="$(openssl rand -hex 32)"
DB_ROOT_PASSWORD="$(openssl rand -hex 16)"
DB_PASSWORD="$(openssl rand -hex 16)"

cp "$EXAMPLE_FILE" "$ENV_FILE"

# Portable in-place edits. Secrets are hex so they contain no sed delimiters.
sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$JWT_SECRET|" "$ENV_FILE"
sed -i "s|^OTP_SECRET=.*|OTP_SECRET=$OTP_SECRET|" "$ENV_FILE"
sed -i "s|^DB_ROOT_PASSWORD=.*|DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD|" "$ENV_FILE"
sed -i "s|^DB_PASSWORD=.*|DB_PASSWORD=$DB_PASSWORD|" "$ENV_FILE"

chmod 600 "$ENV_FILE"

echo "Created $ENV_FILE with generated secrets."
echo
echo "Local development (demo mode for AI, OTP codes in logs):"
echo "  docker compose up --build"
echo
echo "For production, also set:"
echo "  SPRING_PROFILES_ACTIVE=prod"
echo "  OTP_EMAIL_DELIVERY=EMAIL (free email OTP; set SPRING_MAIL_* and MAIL_FROM)"
echo "  OTP_PHONE_DELIVERY=DISABLED (optional paid SMS: SMS + MSG91_*)"
echo "  CORS_ORIGINS, AI_ALLOWED_ORIGINS, VITE_API_URL, VITE_AI_URL to your public URLs"
echo "  DOMAIN and ACME_EMAIL (for the optional Caddy HTTPS proxy)"
echo
echo "Validate the production file before starting:"
echo "  make prod-check"
echo
echo "Production with automatic HTTPS:"
echo "  docker compose --profile proxy up -d --build"
