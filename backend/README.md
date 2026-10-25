# BuildTrack Backend

Spring Boot REST API for a construction marketplace connecting House Owners, Contractors, Labourers,
and Engineers. Role-based access control, JWT authentication, OTP verification, and a MySQL schema
owned by Flyway migrations.

## Overview

- **House Owners** post requirements, hire contractors, and track payments.
- **Contractors** run sites, assign labour, mark attendance, log material expenses, and pay wages.
- **Labourers** see assigned sites, attendance, and wage balances.
- **Engineers** publish a professional profile, receive project invitations, and get scoped site access after
  accepting (progress reports, assigned tasks and issues); financial details stay private.
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

- `common/` — shared baseline, audit table, engineer marketplace, and site execution tables
- `mysql/` — production upgrades
- `h2/` — the same upgrades, written for the H2 test database

```properties
spring.flyway.locations=classpath:db/migration/common,classpath:db/migration/{vendor}
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
```

A fresh database runs `V1` and then the vendor migrations. An existing database created by the old
`ddl-auto=update` setup is baselined at `V1` and then receives the vendor migrations. The current
schema is at `V5` (site execution). Never edit a migration that has already run in an environment;
add a new one instead.

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
