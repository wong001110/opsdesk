# Architecture

## Current shape

OpsDesk is a modular monolith: one Spring Boot application and one PostgreSQL database.

```text
Browser
  |
  v
React / TypeScript SPA
  | same-origin JSON + Session Cookie + CSRF header
  v
Spring Security -> identity -> workspace/RBAC
                              |-> ticket
                              |-> provider -> MOCK or controlled DeepSeek read-only AI
                              `-> structured audit
  |
  v
PostgreSQL / Flyway

Explicit HTTP Basic -> same API (CLI compatibility only)
```

The backend uses Java 21, Spring Boot 4.1, Spring MVC, Spring Security, JPA/Hibernate, Flyway, PostgreSQL 17, and Maven. The frontend uses React 19, TypeScript, Vite, React Router, TanStack Query, and a small project-owned design system. H2 PostgreSQL compatibility mode supports fast tests; real PostgreSQL remains the migration/runtime reference.

## Modules

### identity and security

Database users authenticate in the browser through a JSON login endpoint. Spring Security verifies encoded passwords, rotates the session identifier, stores an erased-credential principal server-side, and protects mutations with a Cookie-to-Header CSRF token. `OpsDeskPrincipal` carries the stable user UUID; it does not carry workspace roles as global authorities.

Explicit HTTP Basic remains a CLI compatibility adapter and bypasses CSRF only when the request actually carries a Basic Authorization header. A future OIDC/SSO adapter should continue producing the same authenticated user identity and workspace-local authority model.

### web

The SPA owns interaction state, navigation, query caching, loading/error states, and responsive presentation. It never stores passwords in local/session storage. It sees only sanitized provider views and fixed audit metadata. Hash-based routing keeps static production serving simple while the product is a modular monolith; the route boundary can move behind an edge/router later without changing domain APIs.

TanStack Query keys are workspace-scoped. Logout explicitly clears tenant-scoped client cache and sets the current-user query to signed out. A top-level error boundary prevents one unexpected page payload from blanking the entire application.

### workspace

`WorkspaceAccess` is the shared authorization boundary. It resolves an active membership for `(workspaceId, userId)`, returns `404` for non-members, and applies workspace-local role checks. Workspace creation and its initial admin membership are one transaction. Membership changes lock active memberships before enforcing the final-admin invariant.

### ticket

Tickets carry explicit `workspace_id`. Single-object repository lookups and status locks use both workspace and ticket IDs. Members read/create; managers/admins move status one step at a time.

### provider

Profiles separate provider type, normalized trusted HTTPS origin, model, and credential reference. Responses replace the reference with a boolean configuration signal.

`MOCK` must use `https://mock.invalid`. A `DEEPSEEK` profile is never created from browser-submitted origin/model/reference fields: an admin imports one opaque option assembled from server configuration, only when the explicit live flag and dedicated environment credential are available. This prevents the UI from turning arbitrary environment variables into outbound authorization headers.

### ai

Classify/summarize reads a tenant-scoped ticket and tenant-scoped provider profile, returns derived text, and records an audit action. `MOCK` is deterministic local code. The opt-in DeepSeek client is constrained to its configured HTTPS origin plus `/chat/completions`, one short request with no redirect/retry/tool capability, capped input/output, and allowlisted classification output. Live execution requires Manager/Admin; all provider types are limited to `READ_ONLY_ANALYSIS` by a central policy, so future sensitive capabilities need an explicit server-side policy extension.

### audit

Audit records have fixed workspace, actor, action, target, outcome, correlation, and timestamp columns. Business services join audit writes to their transaction. Workspace activities are handled at `BEFORE_COMMIT`, so the domain change and its audit record commit or roll back together.

This is atomic in-process auditing, not durable event delivery. Introduce an outbox only when external delivery or crash recovery is required.

## Data and tenant constraints

Application-generated UUIDs make API/integration fixtures deterministic. Workspace-owned tables contain explicit `workspace_id`; ticket participants and provider creators use composite membership foreign keys. Database constraints supplement but do not replace server-side authorization.

Important separations remain:

```text
identity != membership != workspace role
provider != trusted origin != credential reference
AI read capability != product write authority
audit metadata != arbitrary payload
```

## Runtime configuration

- database: `OPSDESK_DATABASE_URL`, `OPSDESK_DATABASE_USERNAME`, `OPSDESK_DATABASE_PASSWORD`
- optional bootstrap: `OPSDESK_BOOTSTRAP_ENABLED`, `OPSDESK_BOOTSTRAP_EMAIL`, `OPSDESK_BOOTSTRAP_DISPLAY_NAME`, `OPSDESK_BOOTSTRAP_PASSWORD`
- opt-in live DeepSeek: `OPSDESK_AI_LIVE_ENABLED`, `OPSDESK_DEEPSEEK_API_KEY`, `OPSDESK_DEEPSEEK_BASE_URL`, `OPSDESK_DEEPSEEK_MODEL`

Bootstrap defaults off. Compose enables it for local use and refuses startup unless its password is supplied.

The Dockerfile builds the Vite application in a Node stage, copies its immutable assets into Spring Boot static resources, then produces one runtime image. Development uses the Vite same-origin proxy; production-style local validation uses the combined image.

## Non-production gaps

- no TLS termination, OIDC/SSO, rate limiting, password reset, distributed session store/revocation, or account API
- no production secret manager/resolver, persistent provider rate/budget ledger, or fake/local provider transport
- no row-level security
- no external immutable audit sink/outbox
- no production deployment or operational backup/restore design

## Verification direction

Use claims and risk to choose evidence. Unit/domain checks, MockMvc integration tests, H2 compatibility tests, Testcontainers, Compose PostgreSQL checks, and focused adversarial tests are available choices. A broad suite is appropriate at integration/release checkpoints, not a mandatory response to every edit.
