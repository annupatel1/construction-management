# BuildTrack — Construction Site Management

A full-stack construction marketplace connecting **House Owners**, **Contractors**, and **Labourers**.

This repository contains both applications:

| Folder | Description | Stack |
| --- | --- | --- |
| [`frontend/`](frontend/) | Web app (mobile-first, Hindi/English) | React, Vite, Tailwind CSS |
| [`backend/`](backend/) | REST API with JWT auth and role-based access | Java 21, Spring Boot, Spring Security, JPA, MySQL |

## Features

- House owners post requirements, hire contractors, start projects, and track payments.
- Contractors manage projects, assign labour, mark attendance, log material expenses, and pay wages.
- Labourers see assigned sites, attendance, and wage balances.
- Two contract types: **Labour Only** and **Labour + Material**.
- JWT authentication, OTP verification, and role-based access control.

## Getting started

### Backend

Requirements: Java 21, Maven, MySQL.

```bash
cd backend
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080`. Configure the database and secrets through the
environment variables described in [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties).

### Frontend

Requirements: Node 20.19+ or 22+.

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Copy `.env.example` to `.env` if you need to change `VITE_API_URL`
(defaults to `http://localhost:8080`).

## Database migrations

The backend schema is owned by Flyway (`spring.jpa.hibernate.ddl-auto=none`):

- A fresh database runs `backend/src/main/resources/db/migration/common/V1__baseline.sql` plus the
  vendor migrations for MySQL or H2.
- A database created by the older `ddl-auto=update` setup is baselined at `V1`, then upgraded by the
  MySQL migrations (`V2` gate-0 hardening, `V3` audit events).
- Automated tests run the same migrations against in-memory H2 in MySQL mode. No Docker or MySQL is
  required for `mvn test`.

## Security and data integrity

Gate 0 hardening is in place:

- `BigDecimal` / `DECIMAL` money and quantity columns with shared half-up rounding.
- Per-account token versions; logout, password reset, and suspension revoke old JWTs.
- Account status (`ACTIVE`, `SUSPENDED`, `DELETED`) enforced at login and on every request.
- Password-login throttling, HMAC-hashed single-use OTPs, and OTP rate limits.
- Unique attendance per labour assignment and date.
- PII-minimised public payloads (no email/login data), Aadhaar fields removed.
- Admin-only audit trail for financial and status changes.
- Bounded list endpoints (default 50, maximum 100 per page).
- Production profile fails fast on weak secrets or console OTP delivery.

## Tests and CI

```bash
cd backend && mvn verify
cd frontend && npm ci && npm run build
```

GitHub Actions runs both commands on pushes to `main` and on pull requests
(`.github/workflows/ci.yml`).

## Production

Run the backend with `SPRING_PROFILES_ACTIVE=prod` and set the environment variables listed in
[`backend/README.md`](backend/README.md#production-profile). Console OTP delivery is rejected in the
production profile.

## Documentation

- Frontend usage and role walkthrough: [`frontend/README.md`](frontend/README.md)
- Backend architecture, migrations, testing, and production setup: [`backend/README.md`](backend/README.md)
