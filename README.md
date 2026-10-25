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

## Documentation

- Frontend usage and role walkthrough: [`frontend/README.md`](frontend/README.md)
- Backend architecture and API overview: [`backend/README.md`](backend/README.md)
