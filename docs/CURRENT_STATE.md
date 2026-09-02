# Current State

## Phase

**Full-stack MVP — implementation and integrated verification complete**

OpsDesk now has a cohesive browser-to-database product slice across identity, workspace membership, tickets, provider profiles, deterministic AI actions, and structured audit events. React/TypeScript is the interactive client and Spring Boot remains the authoritative API/domain boundary. It remains a local proving ground, not a production-ready operations service.

## Implemented

- Java 21, Spring Boot 4.1, Maven, JPA/Hibernate, Flyway, PostgreSQL 17, and Docker Compose
- React 19, TypeScript, Vite, React Router, and TanStack Query
- server-side browser Session/Cookie login with Cookie-to-Header CSRF protection
- JSON 401/403 behavior and explicit Basic compatibility for CLI clients
- optional local bootstrap, disabled by default and requiring `OPSDESK_BOOTSTRAP_PASSWORD`
- workspace-scoped `ADMIN`, `MANAGER`, and `MEMBER` memberships
- cross-tenant/non-member `404` hiding and role-specific `403`
- protection against demoting or deactivating the final active workspace admin
- tenant-owned tickets with `OPEN -> IN_PROGRESS -> DONE`
- provider profiles with validated HTTPS origins, model metadata, and sanitized credential references
- deterministic `MOCK` plus opt-in, server-configured DeepSeek read-only classify/summarize actions
- structured audit records without arbitrary payloads
- Flyway V1 foundation and V2 MVP-domain migrations

## Verification state

Observed MVP evidence:

- Docker Maven clean test run: `10/10` tests passed
- frontend lint, `5/5` Vitest checks, and TypeScript/Vite production build passed
- Flyway V1 and V2 applied successfully in H2 PostgreSQL compatibility mode
- Docker Compose PostgreSQL 17 startup, migration, multi-stage product image, and complete live browser flow passed
- real browser evidence covered login/logout, workspace and ticket creation, provider setup, AI classify/summarize, ticket transition, and structured audit rendering
- credential references and injected ticket instructions were absent from UI audit output and application logs

The integrated suite covers session/CSRF authentication, tenant isolation, ticket workflow, provider sanitization, deterministic AI authority, audit output, and two browser-discovered regressions (nullable correlation IDs and logout cache invalidation). This evidence does not claim production security or exhaustive coverage.

## Current boundaries

- Browser authentication uses an in-memory server-side HTTP session; Basic remains only for CLI compatibility.
- Bootstrap is local-development setup, not user lifecycle management.
- Membership APIs can add only users that already exist.
- The browser cannot submit, read, or receive provider credentials. A live DeepSeek profile can only be imported from server-owned configuration when the explicit feature flag and credential environment variable are present.
- `MOCK` AI is deterministic. Live DeepSeek execution is opt-in, Manager/Admin-only, fixed-origin/model, short-timeout, no-redirect/no-retry/no-tool read-only analysis. A single direct-credential smoke classification passed against the locally configured provider; it left the synthetic ticket `OPEN` and exposed neither credential fields nor ticket text through provider/audit reads.
- Audit is structured and transactional but not exported to an immutable external system.
- The React UI is product-MVP complete but there is no production deployment.

## Next checkpoint

1. Add focused tests for membership management and final-admin concurrency.
2. Define production identity/session deployment: TLS, secure cookies, OIDC/SSO, rate limiting, and distributed revocation requirements.
3. Add a production secret resolver, rate/concurrency controls, a fake/local provider transport, and a budget ledger before broad live-provider access.
4. Add transactional outbox/external audit delivery only if durability requirements justify it.
5. Grow the interaction model with assignment, priority, comments, optimistic concurrency, and server-driven pagination before scaling data volume.

## Agent Lore validation

OpsDesk has now exercised both single-agent work and a depth-1 manager/worker DAG with disjoint frontend, authentication, and observability scopes followed by serial integration/E2E. Agent Lore should continue selecting topology and verification from the current TaskShape, dependencies, trust boundaries, and evidence needs. No phase mandates a fixed agent count, tool, or universal test checklist.

## Context freshness

This document is updated at meaningful integration checkpoints. If source, migrations, tests, or runtime evidence disagree with it, treat those artifacts as authoritative and repair this summary.
