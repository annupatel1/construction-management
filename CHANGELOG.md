# Changelog

All notable changes to BuildTrack are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Public marketing landing page at `/welcome` with features, roles, AI overview,
  pilot pricing, FAQ, legal links and support contact.
- Phone-friendly site-photo uploads for daily progress reports and issue evidence,
  with project-scoped authorization, JPEG/PNG/WebP validation, a 10 MB limit and a
  persistent `uploads-data` Docker volume.
- Production deployment kit: Caddy reverse proxy profile, automatic HTTPS
  configuration, MySQL **and site-photo** backups, and a step-by-step deployment guide.
- Frontend unit tests for wage, overtime, project-finance and INR formatting
  helpers, wired into CI and `make test`.
- `LICENSE`, `SECURITY.md`, `CHANGELOG.md` and `robots.txt`.
- Localhost-only host port bindings for the backend, AI and frontend; production
  traffic is expected to arrive through the TLS reverse proxy.

### Changed

- Unauthenticated visitors now land on the public page instead of the login form.
  Login and registration still preserve the originally requested route.
- The API now returns the specific user-facing validation message for bad requests
  (for example "End date cannot be before start date") instead of a generic error.
- `make test` now runs frontend tests before the production build.
- CI runs frontend tests before building the frontend image.

### Security

- Uploaded photos are stored under random UUID names, validated by file signature,
  and served with `X-Content-Type-Options: nosniff` and immutable caching. Photo URLs
  are public but unguessable; the deployment and security docs explain the trade-off.

## [1.0.0] - 2026-11-12

### Added

- Full Docker Compose stack for MySQL, Spring Boot backend, React frontend and
  Python AI service, with healthchecks, non-root containers and generated secrets.
- Email OTP registration, email/phone verification, password and OTP login,
  password reset, account suspension and token revocation.
- Role-based access for house owners, contractors, engineers, labour and admins.
- Projects, requirements, invitations, engineer profiles, attendance, wages,
  payments, expenses, materials, purchase orders, stock, progress reports, tasks,
  milestones, issues, notifications, audit events and CSV exports.
- AI Intelligence service for PPE checks, crack analysis, INR project valuation,
  weather advisories and a deterministic demo fallback without a Gemini key.
- GitHub Actions CI for backend tests, frontend build, AI tests and all three
  Docker images.
- Docker, pilot-runbook and production-readiness documentation.

### Security

- Production profile rejects weak secrets, reused OTP/JWT secrets and console
  OTP delivery.
- Verified-account takeover protection during registration.
- CORS allow-list, login throttling and request-id correlation.
