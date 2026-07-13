# Service Incident & Status Management Platform

A full-stack application that enables organizations to manage service incidents and publish customer-facing status updates from a secure internal dashboard.

## Project status

Foundation complete. The React frontend, Spring Boot backend, PostgreSQL database,
and Flyway migrations run locally. Core product features are in development.

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
- Delivery: Docker Compose and GitHub Actions

## Architecture

React frontend → Spring Boot REST API → PostgreSQL

## Run locally

### Prerequisites

- Java 21
- Node.js 20.19 or later
- Docker Desktop

Start PostgreSQL from the repository root:

```bash
docker compose up -d postgres
```

Start the backend in a second terminal:

```bash
cd backend
./gradlew bootRun
```

The backend health endpoint is available at <http://localhost:8080/actuator/health>.

Start the frontend in another terminal:

```bash
cd frontend
npm run dev
```

Open the URL printed by Vite, normally <http://localhost:5173>.

## Run backend tests

PostgreSQL must be running first. Then run:

```bash
cd backend
./gradlew test
```

## Development roadmap

1. Foundation: backend, frontend, and database start locally.
2. Core service and incident APIs.
3. Authentication and role-based authorization.
4. Internal dashboard and public status page.
5. Tests, Docker Compose, CI, and API documentation.
