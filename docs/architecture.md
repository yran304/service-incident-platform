# Architecture and Domain Model

## Architecture

The application will use a modular monolith architecture.

React frontend → Spring Boot REST API → PostgreSQL

The backend will be organized by feature:

- `auth` — authentication, JWT, and role-based authorization
- `organizations` — organization setup and membership
- `services` — tracked services and their health
- `incidents` — incidents and lifecycle management
- `status` — public status-page data
- `common` — shared error handling, validation, and configuration

Controllers handle HTTP concerns, services contain business rules, and DTOs define API request and response boundaries.

## MVP domain model

- An **Organization** owns many services and users.
- A **User** belongs to one organization and has one role: `ADMIN`, `EDITOR`, or `VIEWER`.
- A **Service** belongs to one organization and has a current health status.
- An **Incident** belongs to one service and moves through `INVESTIGATING`, `IDENTIFIED`, `MONITORING`, and `RESOLVED`.
- An **IncidentUpdate** belongs to one incident and is optionally published to the public status page.

Relationships:

- Organization 1 → many Users
- Organization 1 → many Services
- Service 1 → many Incidents
- Incident 1 → many IncidentUpdates

## Authorization rules

| Action | Admin | Editor | Viewer |
| --- | --- | --- | --- |
| Manage users and organization | Yes | No | No |
| Create and update services | Yes | Yes | No |
| Create and update incidents | Yes | Yes | No |
| View internal dashboard | Yes | Yes | Yes |
| View public status page | Public | Public | Public |

## Deliberate MVP exclusions

No microservices, Kafka, Redis, email notifications, monitoring integrations, cloud deployment, or Kubernetes. These can be considered only after the core product is complete.