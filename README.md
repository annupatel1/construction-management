# BuildTrack — Construction Site Management

A full-stack construction marketplace connecting **House Owners**, **Contractors**, **Labourers**, and **Engineers**.

This repository contains both applications:

| Folder | Description | Stack |
| --- | --- | --- |
| [`frontend/`](frontend/) | Web app (mobile-first, Hindi/English) | React, Vite, Tailwind CSS |
| [`backend/`](backend/) | REST API with JWT auth and role-based access | Java 21, Spring Boot, Spring Security, JPA, MySQL |

## Features

- House owners post requirements, hire contractors, start projects, and track payments.
- Contractors manage projects, assign labour, mark attendance, request and procure materials, track stock,
  and pay wages.
- Labourers see assigned sites, attendance, and wage balances.
- Engineers publish a professional profile, get discovered by owners/contractors, and join projects through invitations with scoped site access.
- Material requests, purchase orders, goods receipts, a traceable stock ledger, and low-stock warnings.
- In-app notifications for invitations, payments, tasks, site issues, material requests, and receipts.
- Role-scoped CSV exports for finance, attendance, stock, and site records.
- Public privacy, terms, and support pages with a pilot runbook for backups, retention, and incidents.
- Health/readiness probe (`GET /api/health`) and request-id correlation on every API response.
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
(defaults to `http://localhost:8080`) or set the public `VITE_SUPPORT_EMAIL` shown on the legal pages.

## Database migrations

The backend schema is owned by Flyway (`spring.jpa.hibernate.ddl-auto=none`):

- A fresh database runs `backend/src/main/resources/db/migration/common/V1__baseline.sql` plus the
  vendor migrations for MySQL or H2.
- A database created by the older `ddl-auto=update` setup is baselined at `V1`, then upgraded by the
  vendor migrations (`V2` gate-0 hardening, `V3` audit events, `V4` engineer marketplace,
  `V5` site execution, `V6` materials and procurement, `V7` notifications).
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

Gate 2 engineer marketplace is in place:

- Engineer profiles with skills, specialty, experience, service area, languages, availability, portfolio,
  and credential-review state.
- Searchable engineer directory with public contact details kept private.
- Owner/contractor invitations with scope, dates, fee terms, and a response deadline.
- Accept/decline/cancel workflow; only acceptance creates a project membership.
- Accepted engineers get scoped project access; finance, wage, and team-management endpoints stay closed.
- Invitation and membership changes are audited.

Gate 3 site execution is in place:

- Daily progress reports with planned/completed work, blockers, next-day plan, dated photo links, and a
  draft → submitted → published workflow. Owners see published reports only.
- Tasks with assignees, due dates, milestones, delay reasons, and overdue tracking.
- Milestones with target/completed dates and task completion progress.
- Quality/safety/delay issues with photo evidence, assignee, due date, comments, and resolution evidence.
- Site records are scoped to the owner, contractor, admin, and accepted engineer members. Labour cannot
  read site execution records, and financial data stays hidden from engineers.
- Progress report lists are paginated; task, milestone, and issue lists are bounded and support
  `page`/`size`.

Gate 4 materials and procurement is in place:

- Project material catalog with category, unit, minimum-stock threshold, and a stock balance derived
  from movements rather than an editable number.
- Material requests with requester, needed-by date, contractor/admin approval or rejection, and
  cancellation by the requester or contractor.
- Suppliers and purchase orders; approved requests can be converted into orders without re-typing.
- Partial or full goods receipts. Each receipt posts a stock-in movement and creates a material expense,
  so project finance and stock stay aligned.
- Manual issue, return, and wastage movements with negative-stock protection. Stock, requests,
  movements, and low-stock counts are visible to the project team; purchase prices and supplier details
  stay owner/contractor/admin-only.

Gate 5 pilot hardening is in place:

- Personal in-app notifications with an unread badge, mark-one-read, and mark-all-read. Invitations,
  contractor payments, assigned tasks, site issues/comments, material requests/reviews, and goods
  receipts notify the right person without notifying the actor who triggered them.
- CSV exports for finance (owner/contractor/admin), attendance (contractor/admin), stock
  (owner/contractor/admin), and site operations (owner/contractor/engineer/admin), with project metadata,
  totals, UTF-8 BOM for Excel, and formula-injection protection. Engineer finance/wage/supplier data
  remains hidden.
- Public `/privacy`, `/terms`, and `/support` pages, linked from the app footer and auth screen.
- `GET /api/health` is a public liveness/readiness probe with a database check.
- Every response carries `X-Request-Id`; API errors include the same `requestId`, and it appears in the
  server log pattern so support can trace a user report.
- The pilot runbook covers staging smoke tests, money reconciliation, backup/restore, retention,
  incident response, rollback, and go/no-go review: [`docs/PILOT_RUNBOOK.md`](docs/PILOT_RUNBOOK.md).

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
- Pilot operations, backups, retention, and incident response: [`docs/PILOT_RUNBOOK.md`](docs/PILOT_RUNBOOK.md)
