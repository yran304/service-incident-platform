# Architecture and Domain Model

> **Direction change (2026-11):** the project is pivoting from a tenant-style
> platform where organizations post their own incidents (Statuspage-like) to a
> **status aggregator** that automatically pulls service status from major AI
> providers (OpenAI, Anthropic, Gemini, ...) and notifies subscribers.
> Reason: the original model has a cold-start problem (no organization has a
> reason to publish on an empty platform), while an aggregator has data from
> day one and needs no cooperation from providers.
>
> Sections marked **Decided** are settled. Sections marked **Open** still need
> investigation or a decision.

## Architecture

The application uses a modular monolith architecture.

React frontend → Spring Boot REST API → PostgreSQL

Planned backend modules (feature-based):

- `auth` — OAuth 2.0 / OIDC login, session or token handling, authorization
- `organizations` — **providers** (OpenAI, Anthropic, ...); see domain model
- `services` — tracked components of a provider and their current status
- `incidents` — incidents and lifecycle (existing state machine is kept)
- `incidentupdates` — progress messages on an incident
- `ingestion` — **new**: scheduled polling of provider status sources
- `subscriptions` — **new**: which user follows which provider/component
- `notifications` — **new**: email delivery and send log
- `status` — public read-only status data
- `common` — shared error handling, validation, and configuration

Controllers handle HTTP concerns, services contain business rules, and DTOs define API request and response boundaries.

## Domain model

**Decided.** Existing entities are reused with new meaning:

| Existing entity | Meaning now |
| --- | --- |
| Organization | A **provider** (e.g. OpenAI). Created and managed by the system/admin, not by self-signup. |
| TrackedService | A component of a provider (e.g. API, Console). |
| Incident | An incident on a provider's status source. Mirrors the existing lifecycle `INVESTIGATING → IDENTIFIED → MONITORING → RESOLVED`. |
| IncidentUpdate | A progress message on an incident. |

New concepts:

- **User** — a person who signed in via OAuth. Stores the external identity (e.g. GitHub/Google ID) and the verified email from the identity provider.
- **Subscription** — a user following a provider or component.
- **Notification** — a record of a message sent to a user for a given incident update (used to avoid duplicates and to debug).
- **Source** — every incident has a `source`: `POLLED` (fetched from a provider) or `MANUAL` (entered by an admin). `POLLED` incidents are **read-only** for humans, so manual edits never race with the poller or with the existing optimistic lock.

Relationships:

- Provider (Organization) 1 → many Services
- Service 1 → many Incidents
- Incident 1 → many IncidentUpdates
- User 1 → many Subscriptions (each pointing at a provider or service)
- IncidentUpdate 1 → many Notifications (one per subscribed user, at most once)

## Roles and authorization

**Decided.** Three identities; `EDITOR` and `VIEWER` from the original design are dropped.

| Action | Anonymous | USER | ADMIN |
| --- | --- | --- | --- |
| View public status pages, providers, incidents, history | Yes | Yes | Yes |
| Subscribe / unsubscribe; manage own notification email | No | Yes | Yes |
| Create manual incidents / updates (`MANUAL` source) | No | No | Yes |
| Create or edit providers, services, and data sources | No | No | Yes |
| View ingestion health (last poll, failures) | No | No | Yes |

Design choices:

- **Admin = allowlist.** The only admin for now is the project owner. Admin status comes from configuration (e.g. an `ADMIN_EMAILS` environment variable matched against the verified login email), not from a role-management UI. Move to a database role column only if a second admin is ever needed.
- **Notification email = verified email from the OAuth provider** by default, which avoids building our own email-verification flow. Letting users enter a different address would require double opt-in (confirmation email) and is deferred.
- The existing write endpoints (`POST /api/organizations`, `POST /api/.../incidents`, status `PATCH`, etc.) are currently anonymous and must be restricted to `ADMIN` **before any public deployment**.
- Terminology to keep straight: OAuth 2.0 is the authorization framework, OIDC adds the identity layer on top, JWT is only a token format.

## Provider ingestion

**Partly decided.** Source formats were verified on 2026-11-16 (M0 spike, results below); implementation details are still open.

Goal: poll each provider's public status source about every 5 minutes and upsert providers, services, incidents, and updates.

### M0 findings (verified by calling the endpoints on 2026-11-16)

| Provider | Endpoint(s) | Format | Notes |
| --- | --- | --- | --- |
| Anthropic | `https://status.claude.com/api/v2/summary.json`, `/incidents.json`, `/components.json` (`status.anthropic.com` redirects here) | Atlassian Statuspage JSON | Full data: components, incidents with `incident_updates`, `impact`. `incidents.json` returned the 50 most recent incidents (~3 months). Sends `ETag`; `Cache-Control: max-age=10`, so 5-minute polling is safe. |
| OpenAI | `https://status.openai.com/api/v2/summary.json`, `/incidents.json`, `/status.json`; also `feed.atom` and `history.rss` | Statuspage-compatible JSON (page is hosted on incident.io) | `summary.json` has components only (no `incidents` key), so incidents must come from `incidents.json` (25 returned, each with `incident_updates`). No `ETag` seen. This is a compatibility layer, not the native Statuspage product, so field drift is a risk. |
| Google Gemini | Google Cloud: `https://status.cloud.google.com/incidents.json` | Custom JSON (not Statuspage) | Only 8 incidents returned; sends `Last-Modified`. Gemini shows up only as the product "Vertex Gemini API" (1 of 8, low severity), so coverage of Gemini specifically is thin. The Google AI Studio status page (`aistudio.google.com/status`) is a JavaScript-rendered page with no JSON endpoint found. |

Pitfall: `https://status.gemini.com` is the **Gemini crypto exchange**, not Google Gemini. It also returns Statuspage JSON, so it would be easy to wire up by mistake.

Consequences for the design:

- **One Statuspage adapter covers Anthropic and OpenAI.** Gemini would need a separate Google Cloud adapter filtered to Vertex AI / Gemini products, with limited coverage; scraping AI Studio's page or reverse-engineering its backend is not recommended (fragile, and possibly against its terms). Decision on Gemini is deferred (see Open questions).
- **Status mapping is not 1:1.** Statuspage incident statuses match our four states, but Statuspage can also report others (e.g. `postmortem`), and its `impact` includes `none`, while our `incidents` table only allows `MINOR`/`MAJOR`/`CRITICAL`. A mapping rule is needed (or a schema change). Component statuses (`operational`, `degraded_performance`, `partial_outage`, `major_outage`, ...) also need mapping onto `current_status`.
- Timestamps arrive in ISO 8601 with differing UTC offsets; `TIMESTAMPTZ` columns handle this.
- Parsers should be tested against **saved real responses** (fixtures), so format drift shows up as a failing test.

### Direction

- A `StatusSource` abstraction (strategy/adapter pattern) with one adapter per source format. Providers that share a format (Atlassian Statuspage-style JSON) share an adapter.
- Providers without a usable API may be dropped or handled via admin `MANUAL` entry.
- Upserts keyed by (provider, external incident id) so repeated polls are idempotent.
- Resilience: timeouts, retries with backoff, conditional requests (ETag / `If-None-Match`) where supported, circuit breaker if justified, and recording of last-success / last-failure per source.
- Scheduling: Spring `@Scheduled` for a single instance; a distributed lock (e.g. ShedLock) only if multiple instances are ever run.
- Respect each provider's terms of use and rate limits; attribute data to its source.

## Notifications

**Open.** Email is the first channel.

- Triggered when ingestion detects a new incident or a new update for something a user subscribes to.
- Must not send duplicates across repeated polls: persist what was sent (candidate: transactional outbox pattern).
- Every email carries an unsubscribe link.
- Requires an email delivery service (candidates: Resend, SES, SendGrid) and a sender domain.
- Webhook delivery is a possible later channel, not part of the first version.

## Deployment

**Open.** Not started. Prerequisites before going public:

1. Write endpoints restricted to `ADMIN`; authentication in place.
2. Hard-coded database credentials in `application.properties` and `compose.yaml` replaced with environment-based secrets.
3. Rate limiting and/or caching on public read endpoints.

Hosting preference: start with a simple, low-cost PaaS; consider AWS later only if there is a concrete reason (see `ROADMAP.md`, stage 3).

## Open questions

- **Decided:** Gemini is **not** integrated for now (thin coverage, would need a second adapter). M1 ships with two providers, Anthropic and OpenAI, which is enough to prove the pipeline end to end. Because both share the Statuspage adapter, adding a provider is a configuration row (name, slug, base URL), not new code. After M1, verify a few more Statuspage-style AI providers to grow the list; introduce a general `StatusSource` interface only when a second source format actually appears.
- **Status/impact mapping:** map Statuspage `impact: none` onto an existing value, or add a new allowed value via a Flyway migration?
- Login provider (GitHub/Google), session vs token, email service, hosting platform: decided in later milestones.

## Suggested implementation order

1. Provider ingestion (source abstraction, scheduler, idempotent upsert, resilience).
2. Public read-only status page in the frontend.
3. OAuth login, `USER`/`ADMIN` authorization, subscriptions.
4. Email notifications.
5. Deployment.

Authentication is deliberately placed after ingestion because its design depends on the product direction, but it must be completed before anything is exposed publicly.

## Deliberate exclusions (for now)

No microservices, Kafka, Redis, Kubernetes, or Webhook notifications. Multi-tenant self-service for organizations is no longer a goal. These can be reconsidered only after the core product works end to end.
