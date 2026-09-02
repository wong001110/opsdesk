# Security Model

OpsDesk intentionally includes a small number of security-sensitive product surfaces so they can be implemented and verified incrementally.

This file defines current project invariants. It is not a requirement to run every security test on every change.

## Assets

Initial protected assets:

- user identity/session
- workspace membership and role authority
- ticket data
- AI provider credentials
- audit records
- future AI action authority

## Trust boundaries

Important boundaries:

- workspace / tenant
- user role / authority
- browser vs server
- application vs external AI provider/origin
- AI read capability vs AI write/action capability
- application logs/audit vs secrets

## Core invariants

### SEC-OPS-001 — Tenant isolation

A user must not read or mutate another workspace's protected data unless an explicit cross-workspace capability exists.

Authorization must be enforced server-side, not only by UI filtering.

### SEC-OPS-002 — Role authority

Member, Manager, and Admin capabilities must remain distinct. A lower-privileged user must not gain a higher-privileged action by changing request parameters, object identifiers, routes, or client state.

### SEC-OPS-003 — Provider credential isolation

A credential associated with provider/trusted origin A must never be sent to an unauthorized provider/origin B.

Changing provider or base URL must not silently carry stale authority across the trust boundary.

### SEC-OPS-004 — Secret non-disclosure

Provider credentials/session secrets must not appear in logs, audit event payloads, user-visible errors, browser-rendered configuration, or unrelated persistence.

### SEC-OPS-005 — AI authority separation

AI ability to read/classify/summarize a ticket does not automatically grant authority to assign, modify, close, change roles, manage provider settings, or perform other privileged operations.

Untrusted ticket content must not expand AI authority.

### SEC-OPS-006 — Audit integrity

Privileged product/security actions should produce useful audit events without copying protected secret values into the audit trail.

## Planned validation slices

### Slice A — Auth / workspace

Validate:

- authenticated vs unauthenticated access
- same-workspace access
- cross-workspace denial
- role-specific operations

### Slice B — Provider credentials

Use synthetic credentials and fake/local endpoints to exercise:

- normal provider call
- provider switch
- custom base URL
- stale persisted provider state
- redirect/retry/fallback behavior when applicable
- logs/errors/audit sinks

Do not use real API keys for adversarial tests.

### Slice C — AI action authority

Start AI as read-only classify/summarize capability. Later, if write actions are added, test prompt injection/untrusted ticket text against permission and approval boundaries.

## Verification depth

Security depth should follow change impact.

Examples:

```text
copy/style change                 -> no security work normally
local ticket validation           -> focused relevant boundary check if needed
tenant authorization change       -> focused/deep auth isolation evidence
provider credential/baseURL       -> focused credential/origin evidence
AI privileged-action introduction -> focused/deep prompt/tool authority evidence
```

A high-impact invariant failure blocks the affected feature regardless of unrelated passing tests.
