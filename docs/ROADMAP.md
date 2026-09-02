# Roadmap

OpsDesk should grow through small, meaningful product slices. Each slice should be understandable as normal product work while creating new engineering evidence for Agent Lore.

## Phase 0 — Runnable foundation

Goal: establish a minimal Spring Boot application and PostgreSQL development/test environment.

Expected outcomes:

- application boots locally
- database migration path exists
- baseline automated test runs
- project structure reflects the modular-monolith direction

Avoid product complexity in this phase.

## Phase 1 — Identity, workspace, ticket

Goal: first usable enterprise vertical slice.

Features:

- authentication
- workspace membership
- Admin / Manager / Member roles
- create/list/view ticket
- `OPEN -> IN_PROGRESS -> DONE`

Validation focus:

- tenant ownership
- server-side authorization
- local vs integration verification choices
- project-context update after the vertical slice is stable

## Phase 2 — AI provider configuration

Goal: introduce the first deliberate external trust boundary.

Features:

- provider profile
- API credential input/storage strategy
- trusted provider/base URL configuration
- small test endpoint/action

Validation focus:

- provider/origin credential isolation
- custom base URL behavior
- stale configuration
- secret logs/errors/audit handling
- targeted security verification without running unrelated attack families

This is the first major Agent Lore security regression slice.

## Phase 3 — AI classify / summarize

Goal: use configured AI provider against a ticket.

Features:

- classify ticket
- summarize ticket
- record safe audit metadata

Validation focus:

- read-only AI authority
- provider failure paths
- untrusted ticket content
- no automatic privilege expansion

## Phase 4 — Bounded AI action

Goal: introduce a controlled write action only after read-only AI behavior is stable.

Possible first action:

- propose assignment or priority change

Prefer proposal/confirmation before direct privileged mutation initially.

Validation focus:

- prompt injection
- role/permission enforcement
- approval boundary
- auditability
- security-control mutation where it adds evidence

## Phase 5+ — Expansion only when useful

Possible future product growth:

- richer workflows / approvals
- notifications
- attachments
- SLA/escalation
- scheduling
- webhooks
- optimistic locking / concurrency cases
- multi-step AI tools
- separate frontend
- Java/backend architecture evolution

Add these because they improve the product or create a useful engineering test, not merely to increase complexity.

## Evaluation principle

Do not prescribe an expected agent topology for a phase. Observe whether Agent Lore and the current coding agent choose an efficient execution/verification shape and whether the delivered result is correct, secure, maintainable, and accepted.
