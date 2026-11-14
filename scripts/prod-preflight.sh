#!/usr/bin/env bash
# Validate .env before a production deployment. This catches the mistakes that
# otherwise show up only after Caddy, MySQL and the JVM have all started.
#
# Usage (from the repository root):
#   ./scripts/prod-preflight.sh
#   make prod-check
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
failures=0

fail() { echo "ERROR: $*" >&2; failures=$((failures + 1)); }
warn() { echo "WARN: $*" >&2; }

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE. Run ./scripts/generate-env.sh first." >&2
  exit 1
fi

# Read KEY=VALUE lines without executing the file as shell. This keeps a password
# containing $ or backticks from being expanded differently than Docker Compose.
load_env() {
  local file="$1" line key value
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    line="${line#"${line%%[![:space:]]*}"}"
    [[ -z "$line" || "$line" == \#* ]] && continue
    line="${line#export }"
    [[ "$line" != *=* ]] && continue

    key="${line%%=*}"
    key="${key//[[:space:]]/}"
    [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue

    value="${line#*=}"
    value="${value#"${value%%[![:space:]]*}"}"
    if [[ "$value" == \"* || "$value" == \'* ]]; then
      # Quoted value: take the contents up to the matching quote. A trailing
      # comment after the closing quote is ignored, and # inside quotes is kept.
      local quote="${value:0:1}"
      local rest="${value:1}"
      value="${rest%%"$quote"*}"
    else
      value="${value%"${value##*[![:space:]]}"}"
      if [[ "$value" == *[[:space:]]\#* ]]; then
        value="${value%%[[:space:]]\#*}"
        value="${value%"${value##*[![:space:]]}"}"
      fi
    fi
    export "$key=$value"
  done < "$file"
}

load_env "$ENV_FILE"

value() { printf '%s' "${!1:-}"; }

require() {
  local key="$1"
  if [[ -z "$(value "$key")" ]]; then
    fail "$key is empty"
  fi
}

require_not_placeholder() {
  local key="$1" placeholder="$2"
  local current
  current="$(value "$key")"
  if [[ -z "$current" || "$current" == "$placeholder" ]]; then
    fail "$key still uses an example/placeholder value"
  fi
}

# Core production settings.
require DOMAIN
require ACME_EMAIL
require SPRING_PROFILES_ACTIVE
require JWT_SECRET
require OTP_SECRET
require OTP_EMAIL_DELIVERY
require OTP_PHONE_DELIVERY
require CORS_ORIGINS
require VITE_API_URL
require VITE_AI_URL

[[ "$(value SPRING_PROFILES_ACTIVE)" == "prod" ]] \
  || fail "SPRING_PROFILES_ACTIVE must be prod"

# Email OTP is the required free channel.
otp_email="$(value OTP_EMAIL_DELIVERY)"
[[ "${otp_email^^}" == "EMAIL" ]] \
  || fail "OTP_EMAIL_DELIVERY must be EMAIL (email OTP is required)"
otp_phone="$(value OTP_PHONE_DELIVERY)"
case "${otp_phone^^}" in
  DISABLED|SMS) ;;
  *) fail "OTP_PHONE_DELIVERY must be DISABLED or SMS" ;;
esac
if [[ "${otp_phone^^}" == "SMS" ]]; then
  require MSG91_AUTH_KEY
  require MSG91_TEMPLATE_ID
fi

# SMTP is required because email OTP is on.
require MAIL_FROM
require SPRING_MAIL_HOST
require SPRING_MAIL_USERNAME
require SPRING_MAIL_PASSWORD

# Secrets must be independent and strong.
jwt_secret="$(value JWT_SECRET)"
otp_secret="$(value OTP_SECRET)"
[[ ${#jwt_secret} -ge 32 ]] || fail "JWT_SECRET must be at least 32 characters"
[[ ${#otp_secret} -ge 32 ]] || fail "OTP_SECRET must be at least 32 characters"
[[ "$jwt_secret" != "$otp_secret" ]] || fail "OTP_SECRET must differ from JWT_SECRET"

# Public domain and same-origin URLs.
domain="$(value DOMAIN)"
[[ "$domain" == *.* ]] || fail "DOMAIN must be a real DNS name"
[[ "$domain" != *example.com* ]] || fail "DOMAIN still looks like an example domain"
[[ "$(value ACME_EMAIL)" == *@*.* ]] || fail "ACME_EMAIL must be a real email address"
[[ "$(value CORS_ORIGINS)" == *"https://$domain"* ]] \
  || fail "CORS_ORIGINS must include https://$domain"
[[ "$(value VITE_API_URL)" == "https://$domain" ]] \
  || fail "VITE_API_URL must be https://$domain"
[[ "$(value VITE_AI_URL)" == "https://$domain/ai" ]] \
  || fail "VITE_AI_URL must be https://$domain/ai"

# Database credentials must not stay at the example values.
require_not_placeholder DB_PASSWORD "change-me-database-password"
require_not_placeholder DB_ROOT_PASSWORD "change-me-root-password"

# Optional settings: warn, do not block.
[[ -n "$(value GEMINI_API_KEY)" ]] \
  || warn "GEMINI_API_KEY is empty; AI features will run in demo mode"
[[ -n "$(value AI_API_KEY)" ]] \
  || warn "AI_API_KEY is empty; the AI service is reachable without a shared key"
[[ "$(value VITE_SUPPORT_EMAIL)" != *example* ]] \
  || warn "VITE_SUPPORT_EMAIL still looks like an example address"

if (( failures > 0 )); then
  echo "" >&2
  echo "Production preflight failed with $failures error(s). Fix .env and retry." >&2
  exit 1
fi

echo "Production preflight passed."
echo "  Domain: $domain"
echo "  OTP: email (free); phone: $otp_phone"
if [[ -n "$(value GEMINI_API_KEY)" ]]; then
  echo "  AI: live Gemini key set"
else
  echo "  AI: demo mode (no GEMINI_API_KEY)"
fi
