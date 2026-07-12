# Implementation Plan

## Deadline

Target: résumé-ready MVP by August 14, 2026.

## Milestones

### July 13–19 — Foundation and core backend

- Create Spring Boot backend using Java 21 and Gradle.
- Create React and TypeScript frontend using Vite.
- Run PostgreSQL with Docker Compose.
- Add Flyway and the initial database schema.
- Implement organization, service, and incident APIs.

**Finish condition:** backend, frontend, and database run locally; core APIs work against PostgreSQL.

### July 20–26 — Reliable incident workflow

- Add validation, consistent API error responses, and service-layer business rules.
- Add incident updates and public/published state.
- Write unit and integration tests for the core workflow.

**Finish condition:** the service and incident workflow passes API-level tests.

### July 27–August 2 — Secure internal workflow

- Add Spring Security, JWT authentication, and roles.
- Build login and internal dashboard views.
- Enforce authorization in the backend.

**Finish condition:** authorized users can manage incidents through the UI.

### August 3–9 — Public experience

- Build the public status page.
- Publish incident updates and display service health.
- Complete the end-to-end management and public-view flow.

**Finish condition:** a user can manage and publish an incident end to end.

### August 10–14 — Quality and presentation

- Add Dockerfiles and complete Docker Compose setup.
- Add GitHub Actions backend checks.
- Improve tests, API documentation, README, and screenshots.

**Finish condition:** a clean clone runs from the README and automated checks pass.

## Scope protection

Until the MVP is complete, do not add microservices, Kafka, Redis, email notifications, cloud deployment, Kubernetes, monitoring integrations, or AI features.