# Service Incident & Status Management Platform

[![Backend CI](https://github.com/yran304/service-incident-platform/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/yran304/service-incident-platform/actions/workflows/backend-ci.yml)

A full-stack application that enables organizations to manage service incidents and publish customer-facing status updates from a secure internal dashboard.

## Project status

Foundation complete. The backend and PostgreSQL now run via Docker Compose;
the frontend still runs locally. Core product features are in development.

## Planned capabilities

- Role-based access: administrator, editor, and viewer
- Organization and service management
- Incident lifecycle: investigating, identified, monitoring, and resolved
- Internal dashboard for managing incidents
- Public status page for service health and published updates

## Planned technology

- Backend: Java 21 and Spring Boot
- Frontend: React and TypeScript
- Database: PostgreSQL with Flyway migrations
- Security: Spring Security and JWT
- Delivery: Docker Compose for backend + database, GitHub Actions running backend tests on every push/PR to main

## Architecture

React frontend → Spring Boot REST API → PostgreSQL

## Run locally

### Prerequisites

- Java 21
- Node.js 20.19 or later
- Docker Desktop

Start PostgreSQL and the backend from the repository root:

```bash
docker compose up --build
```

The backend health endpoint is available at <http://localhost:8080/actuator/health>.

### Alternative: run the backend outside Docker

Useful when actively developing the backend and you want faster restarts without
rebuilding the image each time.

```bash
docker compose up -d postgres
cd backend
./gradlew bootRun
```

Start the frontend in another terminal:

```bash
cd frontend
npm run dev
```

Open the URL printed by Vite, normally <http://localhost:5173>.

## Run backend tests

Tests use [Testcontainers](https://testcontainers.com/) to start a disposable
PostgreSQL container automatically; Docker must be running, but you do not
need to start `docker compose` first. Each test run gets a fresh database and
Flyway migrations are applied from scratch.

```bash
cd backend
./gradlew test
```

## Development roadmap

1. Foundation: backend, frontend, and database start locally.
2. Core service and incident APIs.
3. Authentication and role-based authorization.
4. Internal dashboard and public status page.
5. Tests, CI, and API documentation. Docker Compose for backend + database is done; frontend containerization is still pending.
