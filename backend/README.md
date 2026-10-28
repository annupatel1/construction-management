# BuildTrack Backend

Spring Boot REST API for a construction marketplace connecting House Owners, Contractors, Labourers,
and Engineers. Role-based access control, JWT authentication, OTP verification, and a MySQL schema
owned by Flyway migrations.

## Overview

- **House Owners** post requirements, hire contractors, and track payments.
- **Contractors** run sites, assign labour, mark attendance, request and procure materials, track stock,
  and pay wages.
- **Labourers** see assigned sites, attendance, and wage balances.
- **Engineers** publish a professional profile, receive project invitations, and get scoped site access after
  accepting (progress reports, assigned tasks, issues, stock visibility and usage updates); financial,
  supplier, and purchase-order details stay private.
- **Pilot operations** include personal in-app notifications, role-scoped CSV exports, a public health
  probe, request-id correlation, and a runbook for backups, retention, and incidents.
- Two contract types: **Labour Only** (owner supplies material) and **Labour + Material**
  (contractor supplies material).

## Tech stack

- Java 21, Spring Boot 4.1.1
- Spring Security, stateless JWT, BCrypt passwords
- Spring Data JPA / Hibernate with `ddl-auto=none`
- Flyway migrations (MySQL in production, H2 MySQL mode in tests)
- Maven, Lombok

## Run locally

Requirements: Java 21, Maven, MySQL 8.

```bash
mysql -uroot -p -e "CREATE DATABASE construction_management_db CHARACTER SET utf8mb4;"
cd backend
export DB_URL="jdbc:mysql://localhost:3306/construction_management_db"
export DB_USERNAME=root
export DB_PASSWORD=root
./mvnw spring-boot:run
```

Flyway runs automatically on startup. The API listens on `http://localhost:8080`.
In development, OTP codes are printed in the server log (`OTP_*_DELIVERY=CONSOLE`).

## Database migrations

Migrations live in `src/main/resources/db/migration`:

- `common/` — shared baseline, audit table, engineer marketplace, site execution, materials, and
  notifications tables
- `mysql/` — production upgrades
- `h2/` — the same upgrades, written for the H2 test database

```properties
spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/{vendor}
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

A fresh database runs `V1` and then the vendor migrations. An existing database created by the old
`ddl-auto=update` setup is baselined at `V1` and then receives the vendor migrations. The current
schema is at `V7` (notifications). Never edit a migration that has already run in an environment; add
a new one instead.

## Tests

```bash
./mvnw test     # unit + H2 integration tests through the real Flyway migrations
./mvnw verify   # tests plus the packaged jar
```

The test profile (`src/test/resources/application-test.properties`) uses an in-memory H2 database in
MySQL mode, so no MySQL server or Docker is needed for CI.

## Production profile

Start with `SPRING_PROFILES_ACTIVE=prod` and provide the environment variables read by
`src/main/resources/application-prod.properties`:

| Variable | Purpose |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `JWT_SECRET` | 32+ character HMAC key |
| `OTP_SECRET` | 32+ character OTP hashing key |
| `OTP_PHONE_DELIVERY`, `OTP_EMAIL_DELIVERY` | `SMS` / `EMAIL`, never `CONSOLE` |
| `MSG91_AUTH_KEY`, `MSG91_TEMPLATE_ID` | SMS delivery |
| `SPRING_MAIL_*`, `MAIL_FROM` | SMTP delivery |
| `CORS_ORIGINS` | Comma-separated frontend origins |

`ProductionConfigCheck` fails startup if secrets are weak or console OTP delivery is enabled.

## Security and data integrity baseline

- Money and quantities use `BigDecimal` and `DECIMAL` columns; rounding is centralised in
  `util/Money`.
- Passwords are BCrypt-hashed; registration cannot create an `ADMIN`.
- JWT tokens carry a per-account token version. Logout, password reset, and suspension invalidate
  old tokens; suspended/deleted accounts cannot authenticate.
- Password login is throttled per phone/email; OTPs are HMAC-hashed, single-use, expiring, and rate
  limited.
- Attendance is unique per labour assignment and date at the database level.
- Public requirement, project, and directory payloads use `DirectoryUserResponse` (no email or login
  data). Aadhaar fields were removed.
- Financial and status changes are written to the admin-only audit table (`/api/admin/audit`).
- List endpoints are bounded (default 50, maximum 100 per page). Site execution list endpoints
  accept `page`/`size`; tasks, milestones, and issues default to 100.

## Engineer marketplace

- `ENGINEER` role and `engineer_profile` (skills, specialty, experience, area, languages, availability,
  portfolio, credential state).
- Directory search by text, city, specialty, minimum experience, and availability.
- Public engineer responses never contain phone or email; accepted project membership unlocks contact
  details for the project team.
- Invitations carry scope, dates, optional fee terms, requested permissions, and an optional deadline.
- Pending invitations grant no project access. Acceptance creates `project_membership` transactionally and
  is audited. Declines, cancellations, and removals are audited too.
- Accepted engineers get scoped project access. They can write progress reports, update tasks assigned
  to them, and raise or comment on site issues; finance, wage, attendance-management, and
  team-management endpoints remain owner/contractor/admin only.

## Site execution

- `progress_report` + `progress_photo`: daily planned/completed work, blockers, next-day plan, dated
  photo links, and DRAFT/SUBMITTED/PUBLISHED status. Owners only see published reports. Published
  reports are immutable; edit a draft/submitted report to send it back to draft before resubmitting.
- `milestone`: target date, status, completed date, and task completion counters.
- `project_task`: assignee, due date, status, delay reason, and completed timestamp. Contractors/admins
  manage tasks; an assignee can update status.
- `project_issue` + `issue_photo` + `issue_comment`: quality/safety/delay issues, photo evidence,
  comments, and resolution evidence.
- All site routes require the owner, contractor, admin, or an active engineer membership. Labour is
  denied even when assigned to the project. Every create/update/status change is written to the audit
  trail.

## Materials and procurement

- `material_item`: project catalog with category, unit, minimum-stock threshold, and active flag.
- `supplier`: project supplier directory (owner/contractor/admin only).
- `material_request`: requested quantity, needed-by date, requester, and
  REQUESTED → APPROVED/REJECTED → ORDERED → FULFILLED lifecycle. Requests can be cancelled while
  requested or approved; cancelling an unreceived purchase order returns the request to approved.
- `purchase_order`: supplier, ordered/received quantities, unit price, total, status, and expected date.
  Partial receipts are supported; unreceived orders can be cancelled and re-created.
- `goods_receipt`: partial or full receipt; each receipt creates a `RECEIPT` stock movement and a
  material `Expense` so procurement cost appears in project finance.
- `stock_movement`: traceable RECEIPT, ISSUE, RETURN, and WASTAGE entries. Balances are calculated from
  the ledger, never edited directly. Issue/wastage is rejected when it would make stock negative.
- Routes under `/api/projects/{projectId}`: `stock-ledger`, `materials`, `suppliers`,
  `material-requests`, `purchase-orders`, and `stock-movements`.
- Owners see procurement read-only. Contractors and admins manage materials, suppliers, approvals,
  purchase orders, and receipts. Engineers can read stock (with receipt unit costs redacted), raise
  material requests, and record issue/return/wastage movements, but cannot approve requests, manage
  suppliers, create purchase orders, or receive goods. Labour is denied.
- Lists are bounded; the stock ledger returns the latest 100 movements by default.

## Pilot hardening

### In-app notifications

- `notification` stores a recipient, optional project, type, title/body, deep link, and `read_at`.
  The actor who caused an event is not notified about their own action.
- Routes are personal; a user can only list, count, or mark their own notifications.
  `GET /api/notifications?page=0&size=20`, `GET /api/notifications/unread-count`,
  `POST /api/notifications/{id}/read`, and `POST /api/notifications/read-all`.
- Current triggers: the engineer invitation lifecycle, owner payment recorded, task assigned, issue
  assigned/commented, material request raised/reviewed, and goods received. The frontend polls the
  unread count every 60 seconds.

### CSV exports

Routes under `/api/projects/{projectId}/exports/`:

| Route | Allowed roles | Contents |
| --- | --- | --- |
| `finance.csv` | owner, contractor, admin | Owner payments, material expenses, and (contractor/admin only) labour payments with summary totals |
| `attendance.csv` | contractor, admin | Per-labourer attendance, days, overtime, earned wages, and totals |
| `stock.csv` | owner, contractor, admin | Material catalog, current balance, low-stock flag, and movement history with balances |
| `site.csv` | owner, contractor, engineer, admin | Progress reports, tasks, milestones, and issues; owners see published reports only |

Exports are generated on demand, never stored by the API, and return
`Content-Disposition: attachment`. Files use a UTF-8 BOM for Excel, quote text cells, and prefix
spreadsheet formula characters to prevent CSV injection. Engineers cannot download finance,
attendance, or stock exports; supplier prices and wage data remain closed to them.

### Health and request correlation

- `GET /api/health` is public and returns `200` with `{"status":"UP","database":"UP"}` when the
  database is reachable, or `503` with `DOWN`. Use it for staging checks and uptime monitoring.
- Every response gets `X-Request-Id`. A client-supplied id is reused only when it matches
  `[A-Za-z0-9._-]{8,64}`; otherwise the server generates a UUID.
- API error bodies include `requestId`, and the console log pattern includes `%X{requestId}` so a
  support report can be matched to the server log line.

### Pilot runbook

Backups, restore drills, reconciliation, retention cleanup, incident response, rollback, and the
go/no-go checklist are in [`../docs/PILOT_RUNBOOK.md`](../docs/PILOT_RUNBOOK.md).
