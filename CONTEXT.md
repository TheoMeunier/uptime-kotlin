# Uptime Kotlin — Project Context

## Purpose

Uptime Kotlin is a lightweight, self-hosted availability monitoring product. It periodically checks network targets, stores observations, shows operational history, and informs operators when availability changes. It is intended to run either as one deployment or as a horizontally scalable set of workers.

This file describes the application as it exists today. Planned concepts are listed separately and must not be treated as implemented behavior.

## Product vocabulary

Use these terms consistently:

- **Probe** — a configured monitor. A Probe defines what target is checked, which protocol is used, how often it runs, retry behavior, whether it is enabled, and which Notification Channels it uses. The code currently uses both “probe” and “monitor”; prefer **Probe** in backend/domain code and **Monitor** only in user-facing text where already established.
- **Probe Check** — one execution attempt against a Probe target.
- **Probe Result** — the outcome of a Probe Check: status, response time, message, and optional TLS certificate information.
- **Monitor Log** — the persisted historical observation produced by a Probe Check. Stored in `probes_monitors_logs`.
- **Probe Status** — the latest operational state of a Probe. Current states are represented by `ProbeMonitorLogStatus` and include success, warning, and failure semantics.
- **Notification Channel** — a reusable destination and its encrypted configuration. Supported channel types are email, Slack, Discord, Microsoft Teams, and generic webhook.
- **Status Page** — a public, explicitly configured projection of selected Probes organized into Status Page Groups. The historical `/status` view remains available until a configured page is selected as the default.
- **Standalone mode** — Probe scheduling and execution occur in the API process using the `db-lock` strategy.
- **Cluster mode** — scheduling, Probe execution, and notifications are distributed through RabbitMQ workers, with Redis used for coordination/leader election.
- **Application User** — an authenticated administrator. The current model supports setup and authentication but has no organization, ownership, or role model.

Avoid using **Incident** for a single failed check. An Incident is a planned product concept representing a bounded outage across multiple checks; it is not implemented yet.

## Current capabilities

- HTTP/HTTPS checks with allowed status codes and TLS-related options.
- TCP connection checks.
- DNS checks with configurable server, port, and record type.
- ICMP ping checks.
- Configurable interval, retry count, retry interval, enabled state, and description.
- Historical response-time and availability views over selected periods.
- Dashboard summaries for enabled Probes, recent checks, latency, and failures.
- Notifications on failure and recovery state transitions.
- Historical public status page plus configurable public Status Pages with explicit Probe selection and grouping.
- First-user setup, JWT login/refresh, profile update, and password update.
- Docker deployment and optional distributed worker topology.

## Repository map

### Backend applications

- `backend/api` — Quarkus REST application. Owns setup, authentication, profile, dashboard, Probe CRUD, status-page data, Notification Channel CRUD/testing, Flyway migrations, and standalone scheduler assembly.
- `backend/worker-monitor` — consumes Probe jobs in cluster mode, executes checks, persists results, and publishes notification work.
- `backend/worker-notification` — consumes notification jobs and dispatches messages to Notification Channels.

### Shared Kotlin modules

- `libs/common` — shared DTOs, enums, exceptions, validation, encryption, bcrypt, and logging utilities. It should not depend on feature modules.
- `libs/databases` — JPA/Panache entities, repositories, mappers, and persistence DTOs for PostgreSQL.
- `libs/scheduler` — Probe protocol implementations, protocol factory, standalone scheduling, retry execution, and result persistence.
- `libs/scheduler-cluster` — RabbitMQ scheduling and Redis-backed cluster coordination/leader election.
- `libs/notifications` — notification dispatch contract, factory, orchestration, and channel-specific adapters.

### Frontends

- `apps/app` — authenticated React/Vite administration application plus the public Status Page. Uses React Router, TanStack Query, Zod, React Hook Form, Tailwind CSS, shadcn-style UI primitives, and i18next.
- `apps/website` — Astro marketing and installation website. It is separate from the operational application.

### Operations

- `compose.yaml` — local infrastructure for PostgreSQL, Redis, RabbitMQ, and Nginx.
- `docker/nginx.conf` — reverse-proxy routing.
- `.github/workflows` — CI and container image build workflows for backend and frontends.

## Runtime flows

### Probe configuration

1. The React application submits a protocol-specific Probe request to `backend/api`.
2. `ResolveMonitorContentService` converts the request into the appropriate `ProbeContent` subtype.
3. The API action asks `ProbeRepository` to persist the Probe and attach selected Notification Channels.
4. Protocol-specific content is stored as JSONB in `probes.content`.

### Standalone Probe execution

1. `ProbeSchedulerTemplateFactory` discovers enabled Probes when `quarkus.scheduler.strategy=db-lock`.
2. `ProbeSchedulerFactory` selects a protocol implementation.
3. The implementation performs the Probe Check and returns a `ProbeResult`.
4. Failed attempts are retried according to the Probe configuration.
5. `SaveProbeMonitor` persists a Monitor Log and updates the Probe Status/last-run timestamp.
6. `NotificationService` sends failure or recovery messages only when the relevant status transition occurs.

### Cluster Probe execution

1. Cluster scheduling selects due Probes and publishes Probe jobs through RabbitMQ.
2. `backend/worker-monitor` consumes a job, executes retries, and persists results.
3. Notification work is published when required.
4. `backend/worker-notification` consumes the work and sends through the selected Notification Channel adapter.

Any change to retry, status-transition, or notification semantics must be applied consistently to standalone and cluster flows.

### Read paths

- Authenticated dashboard data is aggregated by `DashboardRepository`.
- Authenticated Probe details combine the Probe, recent Monitor Logs, response-time data, and 24-hour/7-day/30-day uptime.
- The historical public Status Page reads enabled Probes and their recent Monitor Logs without authentication.
- Configured Status Pages read only explicitly published Probes and expose a filtered public DTO without targets or diagnostic messages.

## Persistence model

- `users` — Application Users.
- `refresh_token` — JWT refresh-token records belonging to users.
- `probes` — Probe configuration, latest status, scheduling fields, and protocol-specific JSONB content.
- `probes_monitors_logs` — append-only Probe Check history.
- `notifications_channels` — reusable Notification Channels with JSONB content.
- `probes_notifications_channels` — many-to-many association between Probes and Notification Channels.
- `status_pages` — public Status Page configuration and publication state.
- `status_page_groups` — named groups belonging to a Status Page.
- `status_page_probes` — explicit publication of a Probe in one group on a Status Page.

Important current constraints:

- Timestamps are represented with `LocalDateTime`; code must be explicit and consistent about UTC versus local time.
- Monitor Logs currently have no retention or aggregation mechanism.
- Probe content and Notification Channel content are JSONB polymorphic structures; schema changes require backward-compatible mapping.
- Flyway migrations are the schema source of truth.

## Security model

- Protected API resources use JWT authentication.
- `/api/app/status`, first-user setup, login/refresh, and Status Page data intentionally include unauthenticated paths.
- Notification secrets are encrypted with `EncryptionService` and the configured master key.
- Public Status Page responses must contain only deliberately public data. Internal target URLs, credentials, headers, and diagnostic messages must not be exposed accidentally.
- CORS, JWT keys, database credentials, encryption keys, Redis credentials, and RabbitMQ credentials belong in runtime configuration, never committed production values.

## Architectural invariants

- REST resources translate HTTP concerns and delegate domain work; they should not become persistence or protocol implementations.
- Protocol-specific network behavior belongs behind the scheduler protocol contract and factory.
- Notification-provider behavior belongs behind the notification contract and factory.
- Persistence behavior belongs in `libs/databases`; callers should exchange DTOs rather than depend on query details.
- `libs/common` remains the lowest-level shared module.
- The operational React application consumes validated response schemas where practical.
- Standalone and cluster modes must produce equivalent Probe Results, Monitor Logs, Probe Status transitions, and notifications.
- Status changes must be durable before notifications advertise them.

## Known gaps and planned concepts

These are improvement opportunities, not implemented features:

- **Incident** — outage lifecycle with opening, acknowledgement, updates, resolution, and duration.
- **Maintenance Window** — planned period during which checks or alerts are suppressed without falsifying availability data.
- **Alert Policy** — consecutive-failure/success thresholds, cooldown, reminders, escalation, schedules, and flapping protection.
- Further Status Page capabilities — custom domains, subscriptions, incident history, long-term availability, and richer branding.
- **Data Retention Policy** — configurable raw-log retention with hourly/daily aggregates and latency percentiles.
- **Organization and Role** — multi-user ownership, permissions, invitations, and audit history.
- **Probe Location** — named execution region/agent and optional quorum across locations.
- Advanced HTTP assertions, authentication, request bodies, response-body checks, and multi-step synthetic scenarios.

The recommended product sequence is: Incidents and Alert Policies; retention and aggregation; configurable Status Pages; advanced HTTP checks; multi-user support; geographically distributed Probes.

## Testing and quality baseline

- Backend tests use Quarkus JUnit and RestAssured.
- Kotlin style is enforced with ktlint.
- Frontend lint/build scripts are defined per application.
- Test coverage is currently minimal: domain changes should add focused tests instead of copying the placeholder example tests.
- Highest-risk behavior to test is retry timing, status transitions, duplicate scheduling, notification suppression/recovery, public-data filtering, encryption round trips, and migration compatibility.

## Documentation rules

- Update this file when domain vocabulary, module ownership, runtime topology, security posture, or durable constraints change.
- Record a non-obvious architectural decision in `docs/adr/` rather than expanding this file with historical debate.
- Keep README installation instructions user-facing; keep agent and architectural guidance here.
