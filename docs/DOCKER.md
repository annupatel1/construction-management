# Docker deployment guide

BuildTrack ships with a complete multi-container setup:

| Container | Image build | Host port | Purpose |
| --- | --- | --- | --- |
| `db` | `mysql:8.4` | internal | MySQL database with a persistent volume |
| `backend` | [`backend/Dockerfile`](../backend/Dockerfile) | `127.0.0.1:8080` | Spring Boot REST API and site-photo storage |
| `ai` | [`ai_platform/Dockerfile`](../ai_platform/Dockerfile) | `127.0.0.1:8000` | FastAPI AI service |
| `frontend` | [`frontend/Dockerfile`](../frontend/Dockerfile) | `127.0.0.1:3000` | React app served by Nginx |
| `caddy` | `caddy:2.8-alpine` | `80`, `443` | Optional automatic HTTPS reverse proxy (`proxy` profile) |

The application ports bind to `127.0.0.1` only. In production, public traffic enters
through Caddy on ports 80/443; the database is never published.

---

## Prerequisites

- **Docker Engine 24+** with **Docker Compose v2** (`docker compose version`)
- **4 GB RAM** recommended (MySQL + JVM + AI service)
- Local ports `3000`, `8080`, and `8000` free on the host (bound to localhost)
- For production HTTPS: ports `80` and `443` open, and a DNS name pointing at the server

Podman works too — see the [Podman section](#podman).

---

## One-command quick start

```bash
cd construction-management

# 1. Generate a .env with strong, independent secrets
chmod +x scripts/generate-env.sh
./scripts/generate-env.sh

# 2. Build and start the stack
docker compose up -d --build

# 3. Check health
docker compose ps
make health
```

Or let the Makefile create `.env` for you on first run:

```bash
make up
```

Open:

| Service | URL |
| --- | --- |
| Web app | http://localhost:3000 |
| REST API | http://localhost:8080 |
| API health | http://localhost:8080/api/health |
| AI docs | http://localhost:8000/docs |

In this default setup:

- OTP codes are printed in the backend logs (`OTP_*_DELIVERY=CONSOLE`).
- AI runs in deterministic demo mode until `GEMINI_API_KEY` is set.
- The database is reachable only inside the Compose network.
- Site photos are written to the `uploads-data` volume, not inside the image.

Watch the logs:

```bash
docker compose logs -f backend
docker compose logs -f ai
```

Stop everything:

```bash
docker compose down
```

The `db-data` and `uploads-data` volumes survive `down`. To delete the database and
uploaded photos too:

```bash
docker compose down -v   # destructive
```

---

## Site photo uploads

Progress reports and site issues accept photos from a phone camera or gallery. The
browser uploads the file to `POST /api/projects/{projectId}/files`; the backend
checks the project membership and stores the image under the `uploads-data` volume.

- Accepted formats: JPEG, PNG and WebP. Other formats, including SVG, are rejected.
- Default limit: 10 MB per photo (`UPLOAD_MAX_BYTES` and `UPLOAD_MAX_FILE_SIZE` in `.env`).
- Generated filenames are random UUIDs; the original client filename is never used.
- Photos are served from `/api/files/{filename}` with `X-Content-Type-Options: nosniff`
  and a one-year immutable cache header.
- Photo URLs are public but unguessable so they can be rendered in an `<img>` tag.
  Treat them as sensitive: anyone with the URL can view the image. Do not upload
  identity documents, financial records or other confidential material.

Back up the `uploads-data` volume together with the database. `make backup` runs
[`deploy/backup-mysql.sh`](../deploy/backup-mysql.sh), which writes both a `.sql.gz`
dump and a `buildtrack-uploads-*.tar.gz` archive.

```bash
make backup
ls -lh backups/
```

When the backend is not running, the script still backs up MySQL and warns that the
uploads archive was skipped.

---

## Make targets

```bash
make env        # create .env with generated secrets
make build      # build all images
make up         # start the stack
make down       # stop containers (keeps the database volume)
make restart    # restart services
make logs       # follow logs
make ps         # show service status
make health     # call the backend and AI health endpoints
make test       # run backend, frontend and AI tests locally
make backup     # archive MySQL and uploaded site photos
make clean      # remove local build output
```

---

## Build images individually

```bash
docker build -t buildtrack-backend ./backend
docker build -t buildtrack-ai ./ai_platform
docker build -t buildtrack-frontend ./frontend
```

Run the API alone (it still needs a database):

```bash
docker run --rm -p 127.0.0.1:8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/construction_management_db" \
  -e DB_USERNAME=root -e DB_PASSWORD=root \
  -e JWT_SECRET="$(openssl rand -hex 32)" \
  buildtrack-backend
```

Run the AI service alone:

```bash
docker run --rm -p 127.0.0.1:8000:8000 -e GEMINI_API_KEY=your-key buildtrack-ai
curl -fsS http://localhost:8000/api/health
```

---

## Production configuration

The built-in Caddy profile handles HTTPS and same-domain routing. Set these in
`.env`, then start the stack with the `proxy` profile:

```dotenv
DOMAIN=buildtrack.example.com
ACME_EMAIL=admin@example.com

SPRING_PROFILES_ACTIVE=prod

# One public origin for the app, API and AI service
CORS_ORIGINS=https://buildtrack.example.com
AI_ALLOWED_ORIGINS=https://buildtrack.example.com
VITE_API_URL=https://buildtrack.example.com
VITE_AI_URL=https://buildtrack.example.com/ai

# Email OTP (free and required in production)
OTP_EMAIL_DELIVERY=EMAIL
SPRING_MAIL_HOST=smtp.your-provider.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...
MAIL_FROM=no-reply@example.com

# Phone/SMS OTP (optional and paid; requires an approved MSG91 DLT template)
OTP_PHONE_DELIVERY=DISABLED
# OTP_PHONE_DELIVERY=SMS
# MSG91_AUTH_KEY=...
# MSG91_TEMPLATE_ID=...

# AI (optional; leave empty for demo mode)
GEMINI_API_KEY=...
```

```bash
docker compose --profile proxy up -d --build
# or: make prod-up
docker compose logs -f caddy
# or: make prod-logs
DOMAIN="$(grep -m1 '^DOMAIN=' .env | cut -d= -f2-)"
curl -fsS "https://$DOMAIN/api/health"
curl -fsS "https://$DOMAIN/ai/api/health"
# or: make prod-health
```

The `prod` profile refuses to start if:

- `JWT_SECRET` or `OTP_SECRET` is shorter than 32 characters or still a development default.
- `OTP_SECRET` is the same value as `JWT_SECRET`.
- Email OTP is not enabled (`OTP_EMAIL_DELIVERY=EMAIL`) or SMTP settings are missing.
- Phone OTP is set to `CONSOLE` (use `DISABLED` or `SMS`).
- `OTP_PHONE_DELIVERY=SMS` but the MSG91 key/template is missing.

Email OTP is free: a Gmail App Password or a free transactional provider works.
Phone/SMS OTP stays optional and paid.

If you use an external reverse proxy instead of the built-in Caddy profile, point it
at `127.0.0.1:3000` (frontend), `127.0.0.1:8080` (API) and `127.0.0.1:8000` (AI),
and set the public `VITE_*` URLs before building the frontend.

See the full [production deployment guide](DEPLOYMENT.md) for server setup, DNS,
firewall rules, backups, updates and rollback.

### Recommended hardening

- Only ports 22, 80 and 443 should be reachable from the internet.
- Keep the database off the host network (the Compose file already does this).
- Use the built-in [backup script](../deploy/backup-mysql.sh) and copy both the SQL
  and uploads archives off-server.
- If you set `AI_API_KEY`, either build the frontend with `VITE_AI_API_KEY` (the key is visible in the browser bundle) or, preferably, add the header in your reverse proxy.
- Rotate `JWT_SECRET` / `OTP_SECRET` through your secret manager, not by editing the image.

---

## Podman

Podman is a drop-in alternative on Linux:

```bash
# With podman-compose (install it first: pipx install podman-compose)
podman-compose up -d --build
podman-compose ps
podman-compose logs -f

# Or via the Makefile (auto-detects podman-compose / podman compose)
make podman-up
make podman-down
```

Or use the Compose provider:

```bash
make up COMPOSE="podman compose"
```

Rootless Podman works for the local stack because the published application ports are
above 1024. Caddy needs ports 80/443, which rootless Podman cannot bind by default; use
rootful Podman for the production proxy, or map Caddy to high ports such as
`8080:80` and `8443:443` and put another TLS terminator in front. If your environment
blocks rootless port binding, map `3000`, `8080`, and `8000` to different host ports in
`docker-compose.yml`.

> Podman's default OCI image format drops `HEALTHCHECK` directives. The Compose-level
> healthchecks still work. For standalone `podman run` healthchecks, build with
> `podman build --format docker -t buildtrack-backend ./backend`.

---

## Troubleshooting

**A container exits immediately**

```bash
docker compose logs backend
docker compose logs ai
docker compose logs db
```

Common causes:

- `JWT_SECRET` / `OTP_SECRET` missing in `prod`.
- SMTP credentials missing while `OTP_EMAIL_DELIVERY=EMAIL`; or MSG91 credentials
  missing while `OTP_PHONE_DELIVERY=SMS`.
- MySQL still starting — the backend waits for the database health check.

**Port already in use**

```bash
ss -ltnp | grep -E '3000|8080|8000'
```

Change the host side of the port mapping in `docker-compose.yml`. Keep the
`127.0.0.1:` prefix unless you intentionally want to expose that service on a
public interface.

**Reset the database**

```bash
docker compose down -v
docker compose up -d --build
```

**Rebuild after a dependency change**

```bash
docker compose build --no-cache backend frontend ai
docker compose up -d
```

---

## CI image builds

`.github/workflows/ci.yml` validates the Compose file and builds all three images on every push
and pull request, so Dockerfile regressions are caught before release.
