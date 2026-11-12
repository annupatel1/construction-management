# Security policy

BuildTrack handles account, project, labour, payment and site data. Please report
security issues privately and give us a reasonable window to fix them before any
public disclosure.

## Supported versions

Security fixes are applied to the `main` branch and the latest tagged release.
Older deployments should be upgraded to the latest `main`.

## Reporting a vulnerability

**Do not open a public GitHub issue for a security vulnerability.**

Use GitHub's private vulnerability reporting:

<https://github.com/annupatel1/construction-management/security/advisories/new>

If private reporting is unavailable, contact the support address configured in
`VITE_SUPPORT_EMAIL` and use the subject line `Security incident`.

Please include:

- the affected component (backend, frontend, AI service, Docker deployment);
- a short description of the issue and the impact you believe it has;
- steps to reproduce, or a proof of concept;
- the affected version or commit hash;
- the `X-Request-Id` from any error response, if available;
- your preferred contact details.

Never include real passwords, one-time codes, JWT tokens, bank details or
personal data in a report. Use test accounts and redact sensitive values.

## What to expect

| Stage | Target |
| --- | --- |
| Acknowledge the report | within 3 business days |
| Initial triage and severity assessment | within 7 business days |
| Fix for critical issues | as soon as practical, usually within 7 days |
| Fix for high/medium issues | next scheduled release |

We will keep you informed of progress and credit you in the release notes if you
wish. We may ask for more detail or a reproduction environment.

## Scope

In scope:

- authentication, OTP and password-reset flows;
- role-based access control and project data isolation;
- payment, wage, expense, attendance and stock record integrity;
- file and image handling in the AI service and site-photo upload endpoints;
- Docker images, Compose configuration and deployment defaults;
- secrets exposure in the repository, build logs or API responses.

Out of scope:

- social engineering, phishing or physical attacks;
- denial-of-service or volumetric attacks against a live deployment;
- reports from automated scanners without a demonstrated impact;
- issues that require a compromised device, browser extension or root access;
- third-party services (GitHub, SMTP, SMS, Gemini) outside our control.

## Safe harbour

We support good-faith security research. If you make a reasonable effort to avoid
privacy violations, service disruption and data loss, we will not pursue legal
action for your research. Do not test against a production deployment without
written permission; use a local or staging environment.

## Operational guidance

For production deployments:

- terminate TLS at a trusted reverse proxy and redirect HTTP to HTTPS;
- keep the database off the public internet;
- use separate, strong `JWT_SECRET` and `OTP_SECRET` values;
- restrict `CORS_ORIGINS` and `AI_ALLOWED_ORIGINS` to real frontend origins;
- treat uploaded site-photo URLs as sensitive: they are public-by-unguessable-URL so
  they can be rendered in an `<img>` tag, and anyone with the URL can view the image;
- do not upload identity documents, bank details or other confidential material as a
  site photo;
- keep the `uploads-data` volume off public file shares and include it in encrypted
  off-server backups;
- rotate secrets after any suspected exposure;
- run the backup and restore drill in `docs/PILOT_RUNBOOK.md`;
- keep dependencies and base images updated;
- review the audit trail and application logs during incident response.

See `docs/DEPLOYMENT.md` and `docs/PILOT_RUNBOOK.md` for the deployment and
incident-response checklists.
