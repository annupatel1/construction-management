# Deploying BuildTrack to production

This guide takes a fresh Linux server to a working HTTPS deployment with Docker
Compose. It uses the optional **Caddy** service in `docker-compose.yml` for
automatic TLS certificates. Caddy is not started by `make up`; it is enabled with
the `proxy` profile.

For local development, use `make up`. For staging/pilot sign-off, use
`docs/PILOT_RUNBOOK.md`.

---

## 1. What you need

- A Linux server (Ubuntu 22.04/24.04 or Debian 12 recommended).
- **2 vCPU and 4 GB RAM** minimum. The AI image is large, and MySQL plus the JVM
  need headroom. 2 GB may work for a small pilot but is tight.
- Docker Engine 24+ with Compose v2.
- A domain name with a DNS **A record** (and AAAA if you use IPv6) pointing at the
  server's public IP.
- Ports **22, 80 and 443** open. Do not expose 3000, 8080, 8000 or 3306 publicly.
- Real SMTP credentials and, if you use phone OTP, MSG91 credentials with an
  approved DLT template.

> First-time TLS tip: keep the DNS record **DNS-only** (grey cloud in Cloudflare)
> until Caddy has issued the certificate. A proxied Cloudflare record can block the
> HTTP-01 challenge. After HTTPS works, you can enable the proxy and set Cloudflare
> SSL/TLS to **Full (strict)**.

---

## 2. Prepare the server

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y ca-certificates curl git ufw

# Docker Engine + Compose plugin (official repository)
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl enable --now docker

# Optional: run Docker without sudo for your deploy user
sudo usermod -aG docker "$USER"
newgrp docker

# Firewall
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

Verify:

```bash
docker --version
docker compose version
```

---

## 3. Get the code and create secrets

```bash
sudo mkdir -p /opt/buildtrack
sudo chown "$USER": "$USER" /opt/buildtrack
git clone https://github.com/annupatel1/construction-management.git /opt/buildtrack
cd /opt/buildtrack

make env                 # creates .env with strong JWT, OTP and database secrets
chmod 600 .env
chmod +x deploy/backup-mysql.sh
```

`make env` refuses to overwrite an existing `.env`. Use `./scripts/generate-env.sh
--force` only when you intentionally want to rotate the generated database and JWT
secrets (existing sessions and the database login will break).

---

## 4. Fill in the production values

Edit `.env` and set at least these values:

```dotenv
DOMAIN=buildtrack.your-domain.example
ACME_EMAIL=admin@your-domain.example

SPRING_PROFILES_ACTIVE=prod

CORS_ORIGINS=https://buildtrack.your-domain.example
AI_ALLOWED_ORIGINS=https://buildtrack.your-domain.example

VITE_API_URL=https://buildtrack.your-domain.example
VITE_AI_URL=https://buildtrack.your-domain.example/ai
VITE_SUPPORT_EMAIL=support@your-domain.example

OTP_PHONE_DELIVERY=SMS
OTP_EMAIL_DELIVERY=EMAIL

MSG91_AUTH_KEY=...
MSG91_TEMPLATE_ID=...

SPRING_MAIL_HOST=smtp.your-provider.example
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...
MAIL_FROM=no-reply@your-domain.example

GEMINI_API_KEY=...
```

Notes:

- `DOMAIN` is a hostname only: no `https://`, no trailing slash.
- `CORS_ORIGINS` and `AI_ALLOWED_ORIGINS` are comma-separated exact origins.
  Include the scheme and host, and no trailing slash. Add `http://localhost:3000`
  only if you also test from your laptop against the production API.
- `VITE_*` values are **baked into the frontend at build time**. After changing
  them you must rebuild the frontend image.
- The `prod` Spring profile rejects weak secrets, reused JWT/OTP secrets and
  `CONSOLE` OTP delivery.
- Without `GEMINI_API_KEY`, the AI service runs in clearly labelled demo mode.
- Never commit `.env`. It is ignored by Git.

---

## 5. Start the stack with HTTPS

```bash
docker compose --profile proxy up -d --build
# or, from the repository root:
make prod-up

docker compose ps
# or: make prod-ps
```

First build can take several minutes. Caddy will request a certificate once DNS
points at the server and ports 80/443 are reachable.

Check the public endpoints (the first line reads `DOMAIN` from `.env` for the
commands below):

```bash
DOMAIN="$(grep -m1 '^DOMAIN=' .env | cut -d= -f2-)"
curl -fsS "https://$DOMAIN/api/health"
curl -fsS "https://$DOMAIN/ai/api/health"
curl -fsSI "https://$DOMAIN/" | head
```

Expected:

- Backend health returns `{"status":"UP","database":"UP",...}`.
- AI health returns a healthy JSON response.
- The site returns `200` and shows the public landing page.

Open `https://$DOMAIN`, create a test account, and complete the email OTP flow.
Then test the four roles described in `docs/PILOT_RUNBOOK.md`.

Useful commands:

```bash
docker compose logs -f caddy
docker compose logs -f backend
docker compose ps
```

---

## 6. Backups

The backup script uses the running containers, so the database password is not placed
in your shell history and the photo volume is captured exactly as mounted:

```bash
mkdir -p /var/backups/buildtrack
BACKUP_DIR=/var/backups/buildtrack ./deploy/backup-mysql.sh
# or, with the default ./backups directory:
make backup
```

The script:

- writes a timestamped `.sql.gz` dump **and** a timestamped
  `buildtrack-uploads-*.tar.gz` archive of the site-photo volume;
- verifies both gzip archives and checks that the dump contains `flyway_schema_history`;
- deletes backups older than `RETENTION_DAYS` (default 30).

If the backend container is not running, the photo archive is skipped with a warning.
Start the stack and rerun the backup before relying on it.

Run it daily with cron:

```cron
0 2 * * * cd /opt/buildtrack && BACKUP_DIR=/var/backups/buildtrack ./deploy/backup-mysql.sh >> /var/log/buildtrack-backup.log 2>&1
```

Copy **both** the SQL and uploads archives off the server (object storage, another
host, or an encrypted backup service). A backup that only exists on the same disk is
not a backup.

### Restore drill

Test a restore on a disposable server or Compose project before every pilot
milestone. The steps below replace the live database and photo volume, so stop public
traffic first and take a fresh backup of the current state.

```bash
BACKUP_DIR=/var/backups/buildtrack
SQL=$(ls -t "$BACKUP_DIR"/buildtrack-*.sql.gz | head -1)
UPLOADS=$(ls -t "$BACKUP_DIR"/buildtrack-uploads-*.tar.gz | head -1)

# 1. Stop public traffic.
docker compose --profile proxy stop caddy frontend

# 2. Restore the database.
gunzip -c "$SQL" | docker compose exec -T db sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot "$MYSQL_DATABASE"'

# 3. Restore photos into the running backend's uploads volume.
docker compose exec -T backend sh -c 'rm -rf /app/uploads/* && tar -xzf - -C /app/uploads' \
  < "$UPLOADS"

# 4. Restart the frontend and proxy.
docker compose --profile proxy up -d
```

Then run the smoke test in `docs/PILOT_RUNBOOK.md` section 4. Never restore over the
live database during working hours.

With Podman, pass the Compose command explicitly:

```bash
COMPOSE="podman compose" ./deploy/backup-mysql.sh
```

---

## 7. Update the deployment

```bash
cd /opt/buildtrack
git pull --ff-only
docker compose --profile proxy up -d --build
docker image prune -f
DOMAIN="$(grep -m1 '^DOMAIN=' .env | cut -d= -f2-)"
curl -fsS "https://$DOMAIN/api/health"
```

Flyway applies new migrations automatically on backend startup. Never edit an
applied migration or the `flyway_schema_history` table. If a schema change must be
reversed, write a new corrective migration.

Before updating, take a backup. After updating, run the smoke test in
`docs/PILOT_RUNBOOK.md`.

---

## 8. Rollback

```bash
cd /opt/buildtrack
git log --oneline -5
git checkout <previous-good-commit>
docker compose --profile proxy up -d --build
```

- The frontend is static and can be rolled back independently by rebuilding it.
- Flyway migrations are forward-only. If the bad release changed the schema,
  roll back the application only if the old code is compatible; otherwise write a
  corrective migration.
- Restore a database backup only for data loss or corruption, and take a fresh
  backup of the current state first so the incident can be investigated.

---

## 9. Troubleshooting

**Caddy cannot get a certificate**

- Confirm DNS resolves to this server: `dig +short "$DOMAIN"`.
- Confirm ports 80 and 443 are open in the firewall and at the hosting provider.
- If Cloudflare is proxying the record, set it to DNS-only until the certificate is
  issued.
- Check `docker compose logs caddy` for the ACME error.

**502 Bad Gateway**

- `docker compose ps` should show backend, frontend and AI as healthy.
- Check `docker compose logs backend` and `docker compose logs frontend`.
- The frontend depends on backend health only; a failing AI container does not block
  the core app, but AI pages will show errors until the AI service is healthy.

**The frontend still calls localhost:8080**

`VITE_API_URL` is a build-time value. Set it in `.env`, then rebuild:

```bash
docker compose --profile proxy build --no-cache frontend
docker compose --profile proxy up -d frontend caddy
```

**CORS errors in the browser**

`CORS_ORIGINS` must exactly match the browser origin, including `https://` and no
trailing slash. Restart the backend after changing it:

```bash
docker compose up -d backend
```

**OTP email or SMS is not delivered**

- `SPRING_PROFILES_ACTIVE=prod` rejects console delivery. Set
  `OTP_EMAIL_DELIVERY=EMAIL` or `OTP_PHONE_DELIVERY=SMS`.
- Check `SPRING_MAIL_*`, `MAIL_FROM`, `MSG91_AUTH_KEY` and `MSG91_TEMPLATE_ID`.
- Check the provider dashboard and the backend logs for a rejected request.
- Confirm SPF, DKIM and DMARC for the sending domain.

**AI always returns demo results**

`GEMINI_API_KEY` is unset or invalid. Set it, restart the AI service, and check
`docker compose logs ai`. Demo mode is intentional when no key is configured.

**Database problems**

```bash
docker compose logs db
docker compose exec db mysqladmin ping -uroot -p
```

Do not publish port 3306 to the internet. Use an SSH tunnel or a one-off container
if you need a SQL client.

---

## 10. Production security checklist

- [ ] Only 22, 80 and 443 are reachable from the internet.
- [ ] MySQL has no host port; backend, AI and frontend bind to `127.0.0.1` only.
- [ ] `SPRING_PROFILES_ACTIVE=prod` and strong, distinct secrets.
- [ ] `CORS_ORIGINS` and `AI_ALLOWED_ORIGINS` contain only real frontend origins.
- [ ] Real SMTP/SMS delivery is tested end to end.
- [ ] HTTPS works and HSTS is present; HTTP redirects to HTTPS.
- [ ] Daily off-server backups and a tested restore.
- [ ] Uptime and error monitoring with an alert destination.
- [ ] `VITE_SUPPORT_EMAIL` reaches a monitored inbox.
- [ ] Privacy, terms and support pages are reviewed for your entity and jurisdiction.
- [ ] `SECURITY.md` reporting channel is monitored.
- [ ] Dependencies and base images are updated regularly.
