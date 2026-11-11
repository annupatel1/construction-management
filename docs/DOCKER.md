# Docker deployment guide

BuildTrack ships with a complete multi-container setup:

| Container | Image build | Port | Purpose |
| --- | --- | --- | --- |
| `db` | `mysql:8.4` | internal | MySQL database with a persistent volume |
| `backend` | [`backend/Dockerfile`](../backend/Dockerfile) | `8080` | Spring Boot REST API |
| `ai` | [`ai_platform/Dockerfile`](../ai_platform/Dockerfile) | `8000` | FastAPI AI service |
| `frontend` | [`frontend/Dockerfile`](../frontend/Dockerfile) | `3000` | React app served by Nginx |

---

## Prerequisites

- **Docker Engine 24+** with **Docker Compose v2** (`docker compose version`)
- **4 GB RAM** recommended (MySQL + JVM + AI service)
- Ports `3000`, `8080`, and `8000` free on the host

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

Watch the logs:

```bash
docker compose logs -f backend
docker compose logs -f ai
```

Stop everything:

```bash
docker compose down
```

The `db-data` volume survives `down`. To delete the database too:

```bash
docker compose down -v   # destructive
```

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
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/construction_management_db" \
  -e DB_USERNAME=root -e DB_PASSWORD=root \
  -e JWT_SECRET="$(openssl rand -hex 32)" \
  buildtrack-backend
```

Run the AI service alone:

```bash
docker run --rm -p 8000:8000 -e GEMINI_API_KEY=your-key buildtrack-ai
curl -fsS http://localhost:8000/api/health
```

---

## Production configuration

Set these in `.env` before `docker compose up -d --build`:

```dotenv
SPRING_PROFILES_ACTIVE=prod

# Public HTTPS URLs of the deployed services
CORS_ORIGINS=https://app.example.com
VITE_API_URL=https://api.example.com
VITE_AI_URL=https://ai.example.com
AI_ALLOWED_ORIGINS=https://app.example.com

# Email OTP (required in production)
OTP_EMAIL_DELIVERY=EMAIL
SPRING_MAIL_HOST=smtp.your-provider.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...
MAIL_FROM=no-reply@example.com

# SMS OTP (optional; requires an approved MSG91 DLT template)
OTP_PHONE_DELIVERY=SMS
MSG91_AUTH_KEY=...
MSG91_TEMPLATE_ID=...

# AI (optional; leave empty for demo mode)
GEMINI_API_KEY=...
```

The `prod` profile refuses to start if:

- `JWT_SECRET` or `OTP_SECRET` is shorter than 32 characters or still a development default.
- `OTP_SECRET` is the same value as `JWT_SECRET`.
- Either OTP delivery channel is still `CONSOLE`.

Use a reverse proxy (Caddy, Nginx, Traefik, or your cloud load balancer) for HTTPS and to
terminate TLS on ports 3000/8080/8000. Point the public hostnames at the proxy.

### Recommended hardening

- Put the API and AI service behind the same reverse proxy; do not expose `8000` directly to the internet.
- Keep the database off the host network (the Compose file already does this).
- Back up the `db-data` volume regularly.
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

Rootless Podman works because the published ports are above 1024. If your environment blocks
rootless port binding, map `3000`, `8080`, and `8000` to different host ports in
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
- SMTP or MSG91 credentials missing while `OTP_*_DELIVERY` is set to `EMAIL` / `SMS`.
- MySQL still starting — the backend waits for the database health check.

**Port already in use**

```bash
ss -ltnp | grep -E '3000|8080|8000'
```

Change the left-hand side of the port mapping in `docker-compose.yml`.

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
