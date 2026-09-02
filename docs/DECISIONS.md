# Decisions

This file records material decisions, not every implementation detail.

## D-001 — Product first, proving ground second

**Status:** Accepted

OpsDesk remains understandable as a compact operations product. Agent Lore is validated through real feature work rather than benchmark-only complexity.

## D-002 — Modular monolith

**Status:** Accepted

Use one Spring Boot application and PostgreSQL until concrete evidence justifies additional services or infrastructure.

## D-003 — Java/Spring is a validation variable

**Status:** Accepted

Use repository evidence, tests, and framework conventions rather than depending on the owner's usual stack familiarity.

## D-004 — Project context stays in OpsDesk

**Status:** Accepted

Maintain current state, architecture, security, and roadmap inside this repository. Agent Lore stores concise evidence and recommendations, not a synchronized copy of project docs.

## D-005 — Context-first repository reading

**Status:** Accepted

Read trustworthy local context, then affected source/tests/contracts. Full repository analysis is exceptional when impact, freshness, or security blast radius cannot be bounded.

## D-006 — Provider, origin, and credential authority stay separate

**Status:** Accepted; local MVP implemented

Provider profiles use an explicit trusted HTTPS origin and sanitized credential reference. The API never returns that reference. Real provider calls wait for a resolver and transport that can prove origin binding and secret non-disclosure.

## D-007 — Verification is proportional

**Status:** Accepted

Verification depth follows claims, blast radius, novelty, trust-boundary change, and failure cost. H2, PostgreSQL, MockMvc, Testcontainers, Compose, mutation, and adversarial checks are methods to select—not a fixed checklist.

## D-008 — Semantic commits, not agent commits

**Status:** Accepted

Agent completion is not a final commit boundary. Integrate related changes and commit coherent, verified product checkpoints.

## D-009 — Java 21, Spring Boot 4.1, Maven

**Status:** Accepted

Docker Compose provides PostgreSQL 17 and a reproducible runtime when local Java/Maven is unavailable.

## D-010 — Browser sessions are primary; Basic is a compatibility adapter

**Status:** Accepted; amended by D-016

The initial API slice used stateless HTTP Basic. The interactive browser product now uses a server-side Session Cookie with Cookie-to-Header CSRF protection; explicit Basic remains for CLI compatibility. Database users and Spring password encoding remain authoritative. Workspace roles stay dynamic domain data rather than global authentication authorities.

## D-011 — Hide tenant existence before role authorization

**Status:** Accepted

Inactive/non-members and cross-tenant object lookups return `404`. An active member who lacks the required role receives `403`. Repositories scope protected object retrieval by tenant as well as object ID.

## D-012 — The first AI implementation is deterministic and read-only

**Status:** Accepted

Support only a local `MOCK` profile at `https://mock.invalid`. Classification and summarization perform no network call and receive no product write authority. This gives deterministic evidence before adding credential resolution or external-provider failure modes.

## D-013 — Audit accepts structured metadata only

**Status:** Accepted

Audit records accept action, actor, target, outcome, correlation, and time—not arbitrary payloads. Domain audit writes share the business transaction; workspace events are persisted before commit. External durability/outbox work is deferred until required.

## D-014 — Local bootstrap is explicit and fail-closed

**Status:** Accepted

Bootstrap defaults off. When enabled it requires configured email, display name, and password; the password is encoded before storage. Compose requires `OPSDESK_BOOTSTRAP_PASSWORD` rather than committing a default.

## D-015 — React/TypeScript is the interactive product boundary

**Status:** Accepted

OpsDesk is expected to become a complex interactive product, so use a separately structured React 19 + TypeScript application instead of server-rendered template pages. React Router owns client navigation and TanStack Query owns server-state caching. Spring Boot remains authoritative for identity, tenant isolation, roles, workflow, AI authority, and persistence.

For the modular-monolith MVP, Vite development requests proxy to Spring and the production Docker build embeds static assets in the Spring Boot image. This keeps deployment simple without coupling domain behavior to presentation. Hash routing is an MVP static-serving choice, not a permanent public-URL constraint.

## D-016 — Browser authentication uses server-side Session + CSRF

**Status:** Accepted

The browser submits credentials only to `POST /api/v1/auth/login`. Spring rotates the session ID, erases credential material from the stored principal, and uses an HttpOnly session Cookie. Mutations copy the intentionally readable `XSRF-TOKEN` Cookie into `X-XSRF-TOKEN`. Logout invalidates the session and client code atomically clears tenant-scoped query caches.

Ordinary API 401 responses do not send a Basic challenge, avoiding browser-native credential dialogs. Only failed explicit Basic authentication sends `WWW-Authenticate`.

## D-017 — Live model configuration is server-owned and imported by reference

**Status:** Accepted; local implementation and controlled direct smoke test complete

The browser selects a server-approved DeepSeek import option but never submits or receives an API key, credential-reference value, arbitrary origin, or arbitrary model. The deployment injects one dedicated credential environment variable into the application container, and the initial live capability is limited to Manager/Admin read-only ticket analysis through a fixed endpoint with bounded request settings. `ProviderExecutionPolicy` is the default-deny extension point for future sensitive model actions.
