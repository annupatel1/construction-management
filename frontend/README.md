# BuildTrack - Frontend

React + Vite + Tailwind frontend for the Spring Boot construction management API.
Mobile-first, charcoal + teal theme, Hindi / English toggle, amounts in ₹ with lakh/crore grouping.

## Run it

You need **Node 20.19+ or 22+** (check with `node -v`).

```bash
npm install
npm run dev
```

Open http://localhost:5173. The backend must be running on http://localhost:8080
(change it by copying `.env.example` to `.env` and editing `VITE_API_URL`).

Build for production: `npm run build` (output in `dist/`).

## Try the whole flow (about 5 minutes)

OTP codes are printed in the **backend console** (IntelliJ) while `OTP_*_DELIVERY` is `CONSOLE`.

1. Register accounts (use different 10-digit numbers starting 6-9): a **House owner**, a
   **Contractor**, a **Labour**, and optionally an **Engineer**. Copy each OTP from the backend console.
2. **House owner**: Find contractors -> Start project -> pick the contractor, fill details, create.
3. **Contractor**: open the project -> Labour tab -> Assign labour (pick the labourer, daily rate).
4. **Contractor**: open the labourer -> mark attendance, pay wages; add material expenses.
5. **House owner**: open the project -> Payments -> Record payment.
6. **Labour**: Dashboard shows days worked and pay due; My work shows each site.
7. **Engineer**: complete the engineer profile, then open Invitations. The owner/contractor can find the
   engineer in **Find engineers**, open the public profile, choose a project, and send an invitation.
8. **Engineer**: accept the invitation; the project appears under Projects with scoped site access. Financial
   and wage details stay hidden.
9. **Contractor/Engineer**: open a project's **Progress** tab, save a daily report, submit it, then publish it
   (owner, contractor, or admin). Add **Tasks**, **Milestones**, and **Issues** from their tabs.
10. **House owner**: the **Progress** tab shows published reports only. Open Issues to review or resolve
    quality/safety problems, and Tasks/Milestones to track the plan.
11. Switch language with the button at the top (हिन्दी / English).

## What each role can do

| Role | Screens |
|---|---|
| House owner | Post requirements, find contractors, start projects, record payments, see expenses and labour, follow published progress, tasks, milestones and issues |
| Contractor | Browse open requirements (call the owner), manage projects, assign labour, mark attendance, pay wages, add expenses, run site reports/tasks/milestones/issues, see profit |
| Labour | See assigned sites, attendance, wage sheet and balance due |
| Engineer | Maintain a public profile, review invitations, accept/decline, see accepted projects, write daily reports, update assigned tasks, raise/comment on issues |
| Admin | User list (create the admin account directly in the database) |

## How money is calculated (on the screen, from the API data)

- Wages earned = days worked x daily rate + overtime hours x (daily rate / 8)
  (Full day = 1, Half day = 0.5, Overtime = 1 day + overtime hours, Absent = 0)
- Balance due to labourer = wages earned - advance - wage payments made
- Contractor's estimated profit = received from owner - wages earned
  (- material expenses, only for "Labour + Material" contracts)

The overtime divisor (8) is in `src/lib/calc.js` if your sites use a different rule.

## Project layout

```
src/
  lib/          api client, formatting (₹), wage and project calculations, enum labels
  context/      auth (login state), toast messages
  i18n/         language context and hi.js (all Hindi text)
  components/   UI kit (ui.jsx), layout, OTP input, route guards
  pages/        auth/, owner/, shared/, labour/, engineer/, admin/ and Dashboard
                (site execution UI: pages/shared/ProjectSiteTabs.jsx)
```

To add a Hindi translation, add `"English text": "हिन्दी"` to `src/i18n/hi.js`.
To rename the app, edit `src/config.js`.
