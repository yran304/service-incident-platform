# Service Incident & Status Management Platform

A full-stack application that enables organizations to manage service incidents and publish customer-facing status updates from a secure internal dashboard.

## Project status

Planning and foundation setup.

## Planned capabilities

- Role-based user access: administrator, editor, and viewer
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

```text
React frontend → Spring Boot REST API → PostgreSQL