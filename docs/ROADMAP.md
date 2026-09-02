# Roadmap

OpsDesk grows through useful product slices. Each slice may generate Agent Lore evidence, but product clarity and current repository evidence outrank benchmark complexity.

## Phase 0 — Runnable foundation

**Status: implemented**

- Spring Boot application and health endpoint
- Maven build and Java 21 runtime
- PostgreSQL Compose service
- Flyway migration path
- migration-aware context test

## Phase 1 — Identity, workspace, ticket

**Status: implemented and integrated verification complete**

- database authentication through browser Session/CSRF plus explicit Basic CLI compatibility
- local opt-in bootstrap with externally supplied password
- workspace membership and `ADMIN / MANAGER / MEMBER`
- tenant hiding and role guards
- final active admin protection
- ticket create/list/view
- `OPEN -> IN_PROGRESS -> DONE`

Remaining evidence:

- membership role/deactivation and final-admin concurrency
- production authentication and provider-boundary evidence as those later slices are introduced

## Phase 2 — Provider boundary

**Status: local MVP implemented; real-provider work deferred**

- tenant-owned provider profiles
- validated HTTPS trusted origin
- `env:NAME` and `secret://...` reference syntax
- response sanitization to `credentialConfigured`
- admin writes and manager/admin reads

Next slice:

- define a credential resolver interface backed first by synthetic/fake secrets
- prove origin binding, redirects, retries, fallback, logging, and error sanitization
- do not use a real production credential for adversarial validation

## Phase 3 — Read-only AI

**Status: deterministic MOCK MVP implemented**

- classify and summarize a tenant-scoped ticket
- deterministic result with no network dependency
- structured audit action
- no write authority derived from ticket text

Next slice:

- fake/local HTTP provider transport
- explicit timeout and failure behavior
- prompt/content adversarial cases without granting product write tools

## Phase 4 — Interactive product MVP

**Status: implemented and browser-verified**

- React 19 + TypeScript + Vite product shell
- authenticated workspace selection and creation
- operational overview, ticket queue/search/create/detail/status
- member and workspace-role management
- sanitized Provider configuration
- deterministic AI classification and summarization
- structured audit timeline
- responsive layout, accessible interaction states, and application error fallback
- one production-style Docker image serving SPA + API against PostgreSQL

Next interaction slices, driven by actual product needs:

- assignment, priority, comments, and richer ticket lifecycle
- pagination/filter contracts and optimistic concurrency
- notification/inbox model and SLA/escalation views
- replace hash routing when public URL/deployment requirements justify an edge fallback

## Phase 5 — Production hardening before a real provider

Possible work, driven by deployment requirements:

- move from local sessions to the production identity/session design (likely OIDC/SSO)
- TLS/reverse proxy and secure operational configuration
- account lifecycle and rate limiting
- secret-manager integration and credential rotation
- transactional outbox or external immutable audit sink
- PostgreSQL-focused integration/concurrency evidence
- observability, backup, restore, and deployment procedures

## Phase 6 — Bounded AI action

Only after read-only behavior and real authentication/provider boundaries are stable:

- propose an assignment or priority change
- require normal workspace authorization
- prefer proposal/confirmation before direct mutation
- test prompt injection against permission and approval boundaries

## Later expansion

- richer workflows and approvals
- notifications, attachments, SLA/escalation, scheduling, and webhooks
- richer interactive workflows only when they add product value
- service decomposition only when modular-monolith evidence justifies it

## Evaluation principle

Do not prescribe an expected agent topology or verification tool for a phase. Use the smallest useful execution shape and the evidence appropriate to the current claim and risk. Parallel agents, E2E tests, Testcontainers, Compose, mutation, and adversarial checks remain available methods rather than mandatory rituals.
