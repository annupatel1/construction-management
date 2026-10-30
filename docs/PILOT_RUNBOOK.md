# BuildTrack Pilot Runbook

This runbook is the go/no-go checklist for a small BuildTrack pilot. It covers staging setup,
role-based smoke tests, money reconciliation, backups and restore, retention, and incident response.

The in-app policies are published at `/privacy`, `/terms`, and `/support`. Replace the fallback
support address before inviting real users:

```bash
# frontend/.env (or the production frontend environment)
VITE_SUPPORT_EMAIL=support@your-domain.example
```

## 1. Pre-flight configuration

- [ ] `SPRING_PROFILES_ACTIVE=prod` on the backend.
- [ ] `JWT_SECRET` and `OTP_SECRET` are independent, random, 32+ character secrets stored in the
      deployment secret manager.
- [ ] `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` point at the pilot database; the app user is not
      `root`.
- [ ] `CORS_ORIGINS` contains only the real frontend origin(s).
- [ ] SMS/email OTP delivery is configured (`OTP_PHONE_DELIVERY=SMS`, `OTP_EMAIL_DELIVERY=EMAIL`);
      console OTP is rejected by the production profile.
- [ ] `VITE_API_URL` points at the HTTPS API; `VITE_SUPPORT_EMAIL` is a monitored inbox.
- [ ] HTTPS is terminated in front of the API and frontend; HSTS is enabled at the proxy.
- [ ] A database backup has run successfully and a restore has been tested (section 4).
- [ ] `GET /api/health` returns `200` and `{"status":"UP","database":"UP"}`.
- [ ] The frontend production build is the artifact under test: `npm run build`.
- [ ] Flyway reports the current schema version (`V7` at the time of writing) in the startup log.

## 2. Staging smoke test

Use four separate accounts and, where possible, separate browsers or private windows. Record the
`X-Request-Id` from any error so support can find the matching server log line.

| Step | Actor | Expected result |
| --- | --- | --- |
| 1 | Owner | Register, verify OTP, create a project with a contractor. |
| 2 | Contractor | Project appears; assign a labourer and add an expense/material. |
| 3 | Owner | Record a payment; finance tab shows it; contractor receives an in-app notification. |
| 4 | Labour | Only assigned sites are visible; attendance and wage balance are read-only. |
| 5 | Engineer | Complete profile; owner/contractor invite; accept. Only after acceptance can the engineer see site records. |
| 6 | Engineer | Finance, wage, supplier, purchase-order, and stock-cost data are hidden or return 403. |
| 7 | Contractor | Create a material request; approve it; create a purchase order; receive part of it. |
| 8 | Contractor | Stock balance increases, a material expense appears once, and the request moves through the workflow. |
| 9 | Owner | Download finance and stock CSVs. Open them in a spreadsheet; check dates, INR amounts, and totals. |
| 10 | Contractor | Download attendance CSV; verify it contains only this project and only contractor/admin can access it. |
| 11 | Owner/Engineer | Download the site CSV; owner sees published reports only. |
| 12 | All | Notification bell shows unread count; items are personal, links open the right screen, and “mark all read” clears the badge. |
| 13 | Admin | View audit events, suspend a test account, and confirm its existing token stops working. |
| 14 | All | Logout and confirm the old token is rejected. |

Also check on a phone-sized viewport and with throttled 3G:

- The mobile bottom navigation and notification dropdown stay usable.
- Forms show validation errors without losing entered values.
- A slow request shows loading state and does not submit twice.
- The app does not claim an action succeeded when the server is unreachable.

## 3. Financial reconciliation

Do this for every pilot project before the weekly review and before closeout.

1. **Owner payments**: compare the project Payments tab and finance CSV with the owner's bank/cash
   records. The app records payments; it does not move or verify money.
2. **Material expenses**: compare each expense with the supplier invoice or receipt. Goods receipts
   create the expense automatically, so do not add the same purchase again as a manual expense.
3. **Labour wages**: compare attendance and wage payments with the contractor's wage sheet. Wages are
   private to the contractor, labourer, and admin; an owner only sees the contractor payment side.
4. **Stock**: count physical stock and compare with the stock CSV. Movements are append-only; correct a
   mistake with a RETURN/ISSUE/WASTAGE entry and a note rather than editing history.
5. **Totals**: the finance CSV has separate summary rows for owner payments, material expenses, and
   (for contractor/admin) labour payments. Do not add owner payments and expenses into one "cost"
   number; they are different sides of the project.
6. **Disputes**: record the evidence (CSV, invoice, payment reference) outside the app and escalate to
   the pilot owner. BuildTrack does not arbitrate disputes.

## 4. Backup and restore drill

Run from a host that can reach MySQL and has `mysqldump`. Replace the names and credentials with the
pilot values; do not put the password directly in shell history.

```bash
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=buildtrack_pilot
export DB_USER=buildtrack_app
export BACKUP=/secure/backups/buildtrack-$(date +%F-%H%M).sql

mysqldump --single-transaction --routines --triggers \
  -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p "$DB_NAME" > "$BACKUP"

# Verify the dump is non-empty and contains the Flyway history table.
test -s "$BACKUP"
grep -q "flyway_schema_history" "$BACKUP"
```

Restore into a **separate** database first:

```bash
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p -e \
  "CREATE DATABASE ${DB_NAME}_restore CHARACTER SET utf8mb4;"
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p "${DB_NAME}_restore" < "$BACKUP"
```

Then point a test backend at `${DB_NAME}_restore`, start it, and confirm:

- [ ] Startup validates all Flyway migrations without checksum errors.
- [ ] `GET /api/health` returns `UP`.
- [ ] Counts for users, projects, payments, expenses, attendance, and stock movements are at least
      as high as the source.
- [ ] One owner, one contractor, one labourer, and one engineer can log in (test accounts only).
- [ ] A finance CSV export from the restored copy matches the source CSV.

Store backups encrypted, keep at most 30 daily copies, and test a restore before every pilot
milestone. Never restore over the live database during working hours.

## 5. Retention

The published privacy policy is the source of truth for users. Operationally:

| Data | Retention |
| --- | --- |
| Account and project records | While the account/project is active, then only as needed for tax, dispute, or legal obligations |
| OTP codes | 5 minutes; expired records may be purged |
| Audit events | Up to 24 months |
| Read in-app notifications | Up to 90 days (manual cleanup during the pilot) |
| Backups | Up to 30 days, encrypted |
| Exported CSV files | Not stored by BuildTrack; the user downloads them directly |

Manual pilot cleanup (run in a maintenance window after exporting any needed audit data):

```sql
DELETE FROM notification WHERE read_at IS NOT NULL AND read_at < (NOW() - INTERVAL 90 DAY);
DELETE FROM audit_event WHERE created_at < (NOW() - INTERVAL 24 MONTH);
```

Record the date, operator, and row counts in the pilot operations log.

## 6. Incident response

Severity:

- **S1** — suspected data breach, exposed secrets, widespread outage, or wrong-user data exposure.
- **S2** — one project's finance/attendance is wrong, an OTP/SMS outage, or a role check failure.
- **S3** — cosmetic issue or single-user support request.

For S1/S2:

1. **Contain**: revoke affected tokens/credentials, disable the affected account, or take the
   affected surface offline. Do not delete logs.
2. **Preserve evidence**: copy the `X-Request-Id`, timestamp, user id, project id, audit rows, and
   relevant server logs. Do not paste passwords, OTPs, or full payment details into tickets.
3. **Assess**: determine which users/projects and which data classes were affected.
4. **Notify**: inform the pilot owner and, where required by law or contract, affected users and
   authorities. The support inbox is the published contact.
5. **Recover**: deploy the fix or restore a verified backup; rotate secrets if exposure is possible.
6. **Review**: write a short post-incident note with cause, impact, fix, and follow-up owner/date.

Support users should email the published support address with the subject `Security incident` and
include the request id from the error. BuildTrack support is not an emergency service; for site
danger, call local emergency services first.

## 7. Rollback

- Frontend and backend artifacts are versioned; redeploy the previous known-good pair.
- Flyway migrations are forward-only. Do not edit `flyway_schema_history` or an applied migration.
  If a schema change must be reversed, write a new corrective migration.
- Restore a backup only for data loss/corruption, and only after taking a fresh backup of the current
  state so the incident can be investigated.

## 8. Go/no-go

Proceed only when:

- [ ] The full backend test suite and frontend production build pass in CI.
- [ ] The staging smoke test passes for all five roles.
- [ ] The restore drill passes.
- [ ] Finance reconciliation matches for at least one end-to-end project.
- [ ] Privacy, terms, and support links are reachable and show the configured support address.
- [ ] Every S1/S2 issue from the pilot readiness review has an owner and a target date.
