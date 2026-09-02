# Security Model

This file records current invariants and controls. It does not claim production readiness or require every security technique for every change.

## Assets and boundaries

Protected assets include user password hashes, workspace membership/authority, tenant tickets, credential references and future resolved secrets, and audit records.

Primary boundaries are workspace/tenant, workspace role, client/server, application/provider origin, AI read/product write authority, and secret/audit/log sinks.

## Implemented controls

### SEC-OPS-001 — Tenant isolation

- Workspace-owned entities carry explicit tenant IDs.
- Authorization resolves active membership server-side.
- Ticket and provider object queries include workspace ID and object ID.
- Non-members and cross-tenant object probes return `404` to avoid confirming existence.
- Composite database foreign keys prevent cross-workspace ticket participants/provider creators.

Application authorization is still the primary control; PostgreSQL row-level security is not enabled.

### SEC-OPS-002 — Role authority

- Roles are workspace-local, never global Spring authorities.
- Members can read/create tickets and use read-only MOCK AI.
- Managers/admins can transition tickets and read provider/audit data.
- Admins manage memberships and provider profiles.
- Role-insufficient active members receive `403`.
- A pessimistic membership lock prevents concurrent removal or demotion of the final active admin.

### SEC-OPS-003 — Provider credential/origin isolation

- Profiles bind a provider type, normalized HTTPS origin, model, and credential reference to one workspace.
- References must use `env:NAME` or `secret://...`; raw credential input is rejected.
- Responses omit the reference and return only `credentialConfigured`.
- MOCK profiles require exactly `https://mock.invalid` after normalization.
- DeepSeek profiles are imported only from a server-owned option. The browser never submits a key/reference and cannot choose its origin, model, or environment variable.
- Live DeepSeek requires a dedicated nonblank environment credential and explicit feature flag. It uses one fixed endpoint, no redirects/retries/tools, bounded timeout/input/output, and Manager/Admin authority.
- Ticket/provider lookup is tenant-scoped before AI use.

The live path resolves only the configured dedicated environment name at the narrow outbound-request boundary. `secret://` remains reference-only; there is no production secret-manager resolver. Persistent rate/budget, redirect/failure, and credential rotation controls remain future work.

### SEC-OPS-004 — Secret non-disclosure

- There is no provider secret-value column and no arbitrary audit payload.
- AI/provider responses do not expose credential references.
- Tests use synthetic canaries rather than production secrets.

Credential reference names can themselves carry operational information; clients should keep them non-sensitive. Log/error sink review is still required before a real provider.

### SEC-OPS-005 — AI authority separation

- AI actions only classify or summarize.
- Ticket content is treated as text, not instructions.
- MOCK AI cannot modify a ticket, membership, role, or provider profile.
- `ProviderExecutionPolicy` grants only `READ_ONLY_ANALYSIS`; sensitive capabilities require an explicit policy extension and are denied by default.
- The opt-in external client has no tool/function capability and its output is treated as data: classification is allowlisted and summaries are length-limited.

### SEC-OPS-006 — Audit integrity

- Records contain fixed action/actor/target/outcome fields without request text or secret payload.
- Ticket, provider, and AI audit writes join their business transaction.
- Workspace events are consumed before commit for atomic persistence.
- Audit reads require Manager or Admin.

There is no append-only external sink, signing, retention policy, or transactional outbox.

### Authentication

- Browser authentication uses a server-side HTTP session backed by database identities.
- Spring's BCrypt encoder handles password verification.
- Login requires Cookie-to-Header CSRF, rotates the session ID, and stores an erased-credential principal.
- Mutating browser requests require `XSRF-TOKEN` in the `X-XSRF-TOKEN` header; logout invalidates the session.
- Ordinary API 401/403 responses are structured JSON with `Cache-Control: no-store`; only failed explicit Basic requests receive a Basic challenge.
- Explicit Basic remains available for CLI compatibility and bypasses CSRF only when a Basic Authorization header is present.
- The React client writes neither passwords nor identities to local/session storage and clears tenant-scoped query caches on logout.
- Local bootstrap is disabled by default and requires `OPSDESK_BOOTSTRAP_PASSWORD` when enabled.
- Health/info, fixed SPA assets, CSRF initialization, and login are public; other `/api/v1/**` endpoints require authentication.

The current session store is local to one application process. Production requires TLS, secure-cookie deployment configuration, an identity design such as OIDC/SSO, rate limiting, account lifecycle controls, session revocation, and a shared session strategy if horizontal scaling requires it.

## Validation state

The Docker Maven clean test run passed `11/11` tests, including browser session/CSRF behavior, session fixation protection, credential erasure, Basic compatibility, unauthenticated access, tenant hiding, ticket role/workflow behavior, provider origin/reference validation, fail-closed live-import behavior, response sanitization, deterministic prompt-injection content handling, and audit redaction. The frontend passed lint, `6/6` tests, and a production build. A complete live browser flow passed against Docker Compose PostgreSQL 17; application logs contained none of the synthetic password, credential reference, injected ticket instructions, or runtime error markers. One explicitly authorized, low-cost direct DeepSeek classification also passed using a server-imported profile: the ticket remained `OPEN`, profile output omitted credential fields, and audit output omitted ticket text. This evidence is not equivalent to a production penetration test.

Use synthetic secrets and local/test environments for adversarial work. Escalate verification when authority, credential handling, origins, data migration, or external actions change; do not run unrelated attack families by ritual.

## Next security work

1. Membership/final-admin concurrency tests on PostgreSQL.
2. Production OIDC/session threat model, TLS/secure-cookie deployment boundary, rate limiting, and revocation.
3. Fake/local provider transport plus production secret resolver, origin-binding, and controlled live smoke tests.
4. Persistent rate/concurrency/budget limits, redirect/failure, logs, errors, and credential rotation tests.
5. Outbox/external audit design if audit durability becomes a requirement.
