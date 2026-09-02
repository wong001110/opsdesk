# OpsDesk MVP Product Specification

## Purpose

OpsDesk is a multi-tenant operations workspace for handling support and internal-service tickets with controlled AI assistance. It must remain understandable as a compact modular monolith while exercising real identity, authorization, provider-credential, audit, and agent-engineering concerns.

## Product scope

- browser sign-in with server-side session and CSRF protection
- workspaces with `ADMIN`, `MANAGER`, and `MEMBER` membership roles
- tenant-scoped tickets with `OPEN -> IN_PROGRESS -> DONE`
- manager/admin-visible provider profiles and audit activity
- read-only ticket classification and summarization
- a deterministic local provider for repeatable tests
- an opt-in server-configured live provider import path

## Live model boundary

The browser must never submit, read, persist, or receive an API key or credential reference. A workspace admin selects only a server-approved import option. The server binds a fixed trusted HTTPS origin, model, and dedicated credential environment at execution time.

The first live capability is strictly read-only ticket analysis. It must be role-gated, bounded by timeout/input/output limits, use no tools/function calls, follow no redirects, and not automatically retry. Future write or external-action capabilities require a separate explicit policy, authorization design, and focused verification.

## Security and audit requirements

- tenant and object lookups are workspace-scoped
- lower-privileged members cannot access manager/admin operations
- provider credentials and references stay outside API responses, UI state, audit records, and errors
- ticket text is untrusted data and cannot expand AI authority
- audit events record fixed metadata rather than arbitrary request/provider payloads

## Delivery tracks

`main` is the product-specification and baseline-decision track. Working product code, migrations, and runtime evidence are delivered through dedicated feature branches and reviewed before merge. `docs/CURRENT_STATE.md` on such branches records implemented truth; this specification deliberately describes the intended MVP rather than claiming branch implementation is present on `main`.

## Acceptance evidence

The MVP is ready for review when focused backend/frontend checks, database migration validation, local Compose health, tenant/RBAC/provider-boundary tests, and one explicitly authorized low-cost live-provider smoke test have passed. Production readiness remains out of scope until identity deployment, secret management, persistent rate/budget controls, and operational controls are designed.
