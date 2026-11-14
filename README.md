# BuildTrack — Construction Management Platform

A production-oriented, full-stack construction management platform for **House Owners, Contractors, Labour, and Engineers**. It combines day-to-day site operations (labour, attendance, wages, materials, procurement, progress, issues) with a multimodal **AI Intelligence** service for safety audits, crack diagnostics, INR project valuation, and weather-aware advisories.

---

## What is inside

| Module | Description | Stack |
| --- | --- | --- |
| [`backend/`](backend/) | REST API, JWT auth, role-based access, business rules | Java 21 · Spring Boot 4.1 · Spring Security · JPA · Flyway · MySQL |
| [`frontend/`](frontend/) | Mobile-first web app (Hindi / English) | React 19 · Vite 8 · Tailwind CSS 4 |
| [`ai_platform/`](ai_platform/) | Multimodal AI service + Streamlit console | Python 3.12 · FastAPI · Google Gemini |

---

## Feature highlights

**House owners**
- Post requirements, hire contractors, create and track projects
- Record payments and export finance reports

**Contractors**
- Manage sites, assign labour, mark attendance, and pay wages
- Raise material requests, create purchase orders, receive goods, and track stock
- Publish daily progress reports, tasks, milestones, and site issues

**Labour**
- See assigned sites, attendance, and wage balance (read-only finance)

**Engineers**
- Publish a professional profile and get discovered
- Accept scoped project invitations with site access, without financial visibility

**Platform**
- Public landing page with pilot onboarding, feature overview and legal links
- Phone-friendly site-photo uploads for daily progress reports and issue evidence
- JWT authentication, OTP verification, account suspension, and token revocation
- Audit trail for financial and status changes
- In-app notifications, CSV exports, health probe, and request-id correlation
- Deterministic demo fallback for every AI feature when no Gemini key is configured

---

## Architecture

```
Browser (React + Vite)
   │  JWT
   ▼
Spring Boot API :8080 ──────► MySQL 8 (Flyway migrations)
   │
   │  optional AI calls
   ▼
FastAPI AI service :8000 ───► Google Gemini (or deterministic demo mode)
```

- The backend owns all authorization and money math (`BigDecimal` / `DECIMAL`).
- The AI service is stateless and never stores project data.
- The frontend is a static SPA; API base URLs are configured at build time.

---

## Quick start with Docker

```bash
cd construction-management
chmod +x scripts/generate-env.sh
./scripts/generate-env.sh   # writes .env with strong secrets
docker compose up -d --build
```

| Service | URL |
| --- | --- |
| Web app | http://localhost:3000 |
| REST API | http://localhost:8080 |
| API health | http://localhost:8080/api/health |
| AI service docs | http://localhost:8000/docs |

> Compose uses `GEMINI_API_KEY` for live AI. Without it, every AI feature returns a clearly labelled demonstration result, so the UI remains fully usable. Uploaded site photos are stored in the `uploads-data` Docker volume. See the full [Docker guide](docs/DOCKER.md) for production, Podman, backups, and troubleshooting.

---

## Production deployment

The repository includes a production kit with an optional Caddy reverse proxy for
automatic HTTPS, localhost-only service ports, and backups for MySQL plus uploaded
site photos.

```bash
# On the server, after setting DOMAIN, ACME_EMAIL, free email/SMTP settings
# and the public VITE_* URLs in .env:
make prod-check
make prod-up
# equivalent to: docker compose --profile proxy up -d --build

make prod-health
```

Email OTP is the free default. Phone/SMS OTP is optional and stays disabled with
`OTP_PHONE_DELIVERY=DISABLED` unless you configure MSG91.

The public traffic path is:

```text
Internet :80/:443
   └── Caddy (automatic TLS)
        ├── /api/*  -> backend:8080
        ├── /ai/*   -> ai:8000      (the /ai prefix is stripped)
        └── /*      -> frontend:80
```

See the full [production deployment guide](docs/DEPLOYMENT.md) for DNS, firewall,
secrets, backups, updates, rollback and troubleshooting.

---

## Manual development setup

### 1. Backend

Requirements: **Java 21**, Maven, MySQL 8.

```bash
cd backend
export DB_URL="jdbc:mysql://localhost:3306/construction_management_db"
export DB_USERNAME=root
export DB_PASSWORD=root
./mvnw spring-boot:run
```

Flyway creates or upgrades the schema automatically. Email OTP is the default and is free. In development, email codes are printed in the backend log (`OTP_EMAIL_DELIVERY=CONSOLE`); phone/SMS OTP is disabled unless you configure MSG91.

### 2. Frontend

Requirements: **Node 20.19+ or 22+**.

```bash
cd frontend
cp .env.example .env
npm ci
npm run dev
```

Open http://localhost:5173.

### 3. AI platform

```bash
cd ai_platform
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn backend_api:app --host 0.0.0.0 --port 8000 --reload
```

Optional Streamlit console:

```bash
streamlit run app.py --server.port 8501
```

---

## Configuration

Copy [`.env.example`](.env.example) and set real values before deploying.

### Backend

| Variable | Required | Purpose |
| --- | --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | yes | MySQL connection |
| `JWT_SECRET` | yes | 32+ random characters; signs access tokens |
| `OTP_SECRET` | yes | HMAC key for OTP hashes (independent of JWT) |
| `CORS_ORIGINS` | yes | Comma-separated frontend origins |
| `OTP_EMAIL_DELIVERY` | yes | `EMAIL` in production; free email OTP is the required channel |
| `OTP_PHONE_DELIVERY` | no | `DISABLED` by default; `SMS` is optional and paid |
| `SPRING_MAIL_*`, `MAIL_FROM` | yes | SMTP credentials for free email OTP |
| `MSG91_AUTH_KEY`, `MSG91_TEMPLATE_ID` | for SMS | Optional MSG91 DLT-approved template |
| `PORT` | no | Defaults to `8080` |

Run production with `SPRING_PROFILES_ACTIVE=prod`. The app refuses to start without email/SMTP settings, with console OTP delivery, or with short/reused secrets. A free Gmail App Password or a free transactional provider is enough for email OTP; SMS is optional.

### Frontend

| Variable | Purpose |
| --- | --- |
| `VITE_API_URL` | Spring Boot API base URL |
| `VITE_AI_URL` | FastAPI AI base URL |
| `VITE_AI_API_KEY` | Optional shared key for the AI service |
| `VITE_SUPPORT_EMAIL` | Support inbox shown on legal pages |

### AI service

| Variable | Purpose |
| --- | --- |
| `GEMINI_API_KEY` | Google AI key; omit to run in demo mode |
| `GEMINI_MODEL` | Model name (default `gemini-3.6-flash`) |
| `AI_ALLOWED_ORIGINS` | Comma-separated browser origins |
| `AI_API_KEY` | Optional shared key required on AI routes |

---

## Testing

```bash
# Backend: 28 integration/unit tests on H2 with real Flyway migrations
cd backend && mvn verify

# Frontend: 9 unit tests + production build
cd frontend && npm ci && npm test && npm run build

# AI platform: unittest + FastAPI TestClient
cd ai_platform && python test_suite.py
```

CI runs all three on every push and pull request (`.github/workflows/ci.yml`).
The frontend suite covers wage, overtime, project-finance and INR formatting
helpers; backend integration tests cover the REST and money flows; the AI suite
covers the FastAPI routes and demo fallbacks.

---

## Database migrations

The schema is owned by Flyway (`spring.jpa.hibernate.ddl-auto=none`):

- `db/migration/common/` — baseline, audit, engineer marketplace, site execution, materials, notifications
- `db/migration/mysql/` — production upgrades
- `db/migration/h2/` — the same upgrades for the in-memory test database

Fresh databases run `V1` plus vendor migrations. Existing databases are baselined at `V1` and upgraded from `V2`.

---

## Security notes

- Passwords are BCrypt-hashed; access tokens are stateless JWTs with per-account token versions.
- Logout, password reset, and suspension revoke all existing tokens.
- OTPs are single-use, HMAC-hashed, rate-limited, and never stored in plain text.
- `AccessGuard` enforces project ownership and role scope on every service call.
- CSV exports neutralise spreadsheet formula injection.
- The production profile fails fast on weak secrets or console OTP delivery.
- Production Compose binds the backend, AI and frontend to `127.0.0.1` only; the
  database is never published. Public traffic enters through the TLS proxy.
- Report vulnerabilities privately using [SECURITY.md](SECURITY.md).

---

## Documentation

- [Backend guide](backend/README.md)
- [Frontend guide](frontend/README.md)
- [AI platform guide](ai_platform/README.md)
- [Docker deployment guide](docs/DOCKER.md)
- [Production deployment guide](docs/DEPLOYMENT.md)
- [Pilot runbook](docs/PILOT_RUNBOOK.md)
- [Security policy](SECURITY.md)
- [Changelog](CHANGELOG.md)

---

## License

Proprietary. All rights reserved. See [LICENSE](LICENSE).
