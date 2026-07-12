# Service Incident & Status Management Platform

A full-stack application that enables organizations to manage service incidents and publish customer-facing status updates from a secure internal dashboard.

## Project status

Planning and foundation setup.

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

## Development roadmap

1. Foundation: backend, frontend, and database start locally.
2. Core service and incident APIs.
3. Authentication and role-based authorization.
4. Internal dashboard and public status page.
5. Tests, Docker Compose, CI, and API documentation.