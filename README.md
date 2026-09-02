# OpsDesk

OpsDesk is a compact multi-tenant operations product and a living validation project for Agent Lore. The current full-stack MVP combines a React/TypeScript workspace with a Spring Boot API and PostgreSQL. It implements browser sessions, workspace RBAC, tickets, a safe mock AI path, provider-reference handling, and structured audit records.

It is intentionally small. Architecture, agent topology, and verification depth may change when current task evidence justifies it; the repository does not require one fixed agent count or one universal test method.

## MVP behavior

- Browser users authenticate through a server-side Session Cookie plus Cookie-to-Header CSRF protection. Passwords are submitted only for login, verified with Spring Security's password encoder, and never stored in browser storage.
- A responsive React workspace provides the complete MVP flow: workspaces, operational overview, tickets, members, provider profiles, read-only AI actions, and audit events.
- Explicit HTTP Basic remains available as a compatibility adapter for CLI clients; it is not used by the browser application.
- Roles are scoped to a workspace: `ADMIN`, `MANAGER`, and `MEMBER` are not global authorities.
- Non-members and cross-tenant object lookups receive `404`; authenticated members without sufficient role receive `403`.
- Any member can list, view, and create tickets. Managers and admins can advance `OPEN -> IN_PROGRESS -> DONE`; skipped or repeated transitions return `409`.
- Provider profiles accept only credential references such as `env:NAME` or `secret://...`. API responses expose only `credentialConfigured`, never the stored reference.
- MVP AI classify/summarize actions are deterministic and support only `MOCK` profiles at `https://mock.invalid`. They perform no external provider call and cannot modify tickets, memberships, roles, or provider settings.
- Audit records contain fixed action, actor, target, outcome, and timestamp fields. There is no arbitrary audit payload field.

## Run locally

Requirements: Docker Desktop with Docker Compose.

The Compose development profile enables a single local bootstrap user. A password is mandatory and has no checked-in fallback.

Bash:

```bash
export OPSDESK_BOOTSTRAP_PASSWORD='choose-a-local-password'
docker compose up --build
```

PowerShell:

```powershell
$env:OPSDESK_BOOTSTRAP_PASSWORD = 'choose-a-local-password'
docker compose up --build
```

The bootstrap login is `admin@opsdesk.local`. The supplied password is BCrypt-encoded before persistence. Bootstrap is disabled by default in application configuration and is enabled explicitly only by the local Compose profile.

Open the product at:

```text
http://127.0.0.1:8080
```

Health endpoint:

```text
http://127.0.0.1:8080/actuator/health
```

Stop with `docker compose down`. Add `-v` only when intentionally deleting the local PostgreSQL volume.

### Controlled DeepSeek import (optional)

OpsDesk never accepts a model API key in the browser and does not store one in PostgreSQL. To expose the server-approved DeepSeek option in **Provider profiles**, set these values in the shell that starts Compose, then restart the app:

```powershell
$env:OPSDESK_AI_LIVE_ENABLED = 'true'
$env:OPSDESK_DEEPSEEK_API_KEY = '<your-provider-key>'
# Optional when using a trusted OpenAI-compatible gateway:
# $env:OPSDESK_DEEPSEEK_BASE_URL = 'https://trusted-provider.example'
# $env:OPSDESK_DEEPSEEK_MODEL = 'deepseek-chat'
docker compose up --build -d
```

The UI imports only the resulting server configuration; it cannot enter, read, or return the key. The initial live capability is restricted to manager/admin read-only ticket classification and summarization, uses a single fixed endpoint, disables redirects/retries/tools, and caps timeout/input/output. Leave `OPSDESK_AI_LIVE_ENABLED` unset or `false` to keep real provider execution disabled.

Outside Compose, configure:

- `OPSDESK_DATABASE_URL`
- `OPSDESK_DATABASE_USERNAME`
- `OPSDESK_DATABASE_PASSWORD`
- optionally `OPSDESK_BOOTSTRAP_ENABLED`, `OPSDESK_BOOTSTRAP_EMAIL`, `OPSDESK_BOOTSTRAP_DISPLAY_NAME`, and `OPSDESK_BOOTSTRAP_PASSWORD`
- optional live model: `OPSDESK_AI_LIVE_ENABLED`, `OPSDESK_DEEPSEEK_API_KEY`, `OPSDESK_DEEPSEEK_BASE_URL`, and `OPSDESK_DEEPSEEK_MODEL`

Never enable bootstrap without supplying its password through environment or external configuration.

## Minimal curl flow (CLI compatibility)

The examples use Bash variables. On Windows, the same requests can be issued with `curl.exe`; substitute the IDs returned by each create request.

```bash
BASE=http://127.0.0.1:8080
AUTH='admin@opsdesk.local:choose-a-local-password'

curl -u "$AUTH" "$BASE/api/v1/auth/me"

curl -u "$AUTH" -H 'Content-Type: application/json' \
  -d '{"slug":"demo","name":"Demo Workspace"}' \
  "$BASE/api/v1/workspaces"

WORKSPACE_ID='<workspace UUID from the response>'

curl -u "$AUTH" -H 'Content-Type: application/json' \
  -d '{"title":"Production login error","description":"Users cannot sign in"}' \
  "$BASE/api/v1/workspaces/$WORKSPACE_ID/tickets"

TICKET_ID='<ticket UUID from the response>'

curl -u "$AUTH" -X PATCH -H 'Content-Type: application/json' \
  -d '{"status":"IN_PROGRESS"}' \
  "$BASE/api/v1/workspaces/$WORKSPACE_ID/tickets/$TICKET_ID/status"

curl -u "$AUTH" -H 'Content-Type: application/json' \
  -d '{"name":"Local deterministic mock","providerType":"MOCK","trustedOrigin":"https://mock.invalid","credentialReference":"env:OPSDESK_MOCK_REFERENCE"}' \
  "$BASE/api/v1/workspaces/$WORKSPACE_ID/provider-profiles"

PROFILE_ID='<provider profile UUID from the response>'

curl -u "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"providerProfileId\":\"$PROFILE_ID\"}" \
  "$BASE/api/v1/workspaces/$WORKSPACE_ID/tickets/$TICKET_ID/ai/classify"

curl -u "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"providerProfileId\":\"$PROFILE_ID\"}" \
  "$BASE/api/v1/workspaces/$WORKSPACE_ID/tickets/$TICKET_ID/ai/summarize"

curl -u "$AUTH" "$BASE/api/v1/workspaces/$WORKSPACE_ID/audit-events?limit=100"
```

The `env:OPSDESK_MOCK_REFERENCE` value is a sanitized reference example; the deterministic `MOCK` implementation does not resolve or transmit it.

## API surface

Product requests use the server-side browser session. Explicit HTTP Basic remains supported for the curl compatibility flow below. Mutating session requests require the `XSRF-TOKEN` Cookie value in the `X-XSRF-TOKEN` header.

| Method | Path | Minimum workspace access |
| --- | --- | --- |
| `GET` | `/api/v1/auth/csrf` | public; initializes/refreshes the CSRF Cookie |
| `POST` | `/api/v1/auth/login` | public + valid CSRF; creates server-side session |
| `POST` | `/api/v1/auth/logout` | authenticated session + valid CSRF |
| `GET` | `/api/v1/auth/me` | authenticated user |
| `GET` | `/api/v1/workspaces` | authenticated user; returns active memberships only |
| `POST` | `/api/v1/workspaces` | authenticated user; creator becomes `ADMIN` |
| `GET` | `/api/v1/workspaces/{workspaceId}` | Member |
| `GET` | `/api/v1/workspaces/{workspaceId}/memberships` | Manager or Admin |
| `POST` | `/api/v1/workspaces/{workspaceId}/memberships` | Admin; referenced user must already exist |
| `PATCH` | `/api/v1/workspaces/{workspaceId}/memberships/{userId}` | Admin; changes role |
| `DELETE` | `/api/v1/workspaces/{workspaceId}/memberships/{userId}` | Admin; deactivates membership |
| `GET` | `/api/v1/workspaces/{workspaceId}/tickets` | Member |
| `POST` | `/api/v1/workspaces/{workspaceId}/tickets` | Member |
| `GET` | `/api/v1/workspaces/{workspaceId}/tickets/{ticketId}` | Member |
| `PATCH` | `/api/v1/workspaces/{workspaceId}/tickets/{ticketId}/status` | Manager or Admin |
| `GET` | `/api/v1/workspaces/{workspaceId}/provider-profiles` | Manager or Admin |
| `POST` | `/api/v1/workspaces/{workspaceId}/provider-profiles` | Admin |
| `GET` | `/api/v1/workspaces/{workspaceId}/provider-profiles/{profileId}` | Manager or Admin |
| `PUT` | `/api/v1/workspaces/{workspaceId}/provider-profiles/{profileId}` | Admin |
| `POST` | `/api/v1/workspaces/{workspaceId}/tickets/{ticketId}/ai/classify` | Member |
| `POST` | `/api/v1/workspaces/{workspaceId}/tickets/{ticketId}/ai/summarize` | Member |
| `GET` | `/api/v1/workspaces/{workspaceId}/audit-events?limit=100` | Manager or Admin |

The last active admin cannot be demoted or deactivated. Membership removal is a soft deactivation.

## Verification status

Run the test suite with Java 21 and Maven:

```bash
mvn test
```

The backend suite contains 10 tests covering browser sessions/CSRF, Basic compatibility, tenant hiding, ticket authorization/workflow, provider response sanitization, deterministic read-only AI behavior, and structured auditing. The frontend suite contains 5 tests plus lint and TypeScript production build checks. The integrated product has also passed a real-browser flow against the Compose PostgreSQL 17 runtime, including login/logout, workspace and ticket creation, provider setup, AI classify/summarize, state transition, audit rendering, credential-reference hiding, and prompt-injection authority separation. This evidence does not claim that every production threat has been covered.

Verification should remain proportional: use the cheapest evidence that proves the current claim, then escalate for tenant, credential, authority, migration, or deployment risk. Testcontainers, Compose, focused adversarial checks, or other methods are options, not mandatory rituals.

## Non-production limitations

- Server-side sessions are the browser MVP authentication mechanism. There is no SSO, password reset, rate limiting, account administration API, distributed session store, or production session revocation console.
- TLS must be supplied by a trusted reverse proxy or deployment platform.
- Local bootstrap is for development only and creates one configured user. Membership APIs cannot create identities.
- Only deterministic `MOCK` AI exists. There is no real credential resolver, secret manager integration, outbound provider client, retry/fallback policy, or model configuration.
- Authorization is enforced in application services and tenant-scoped queries, with supporting database constraints; PostgreSQL row-level security is not configured.
- Audit delivery is transactional and in-process. There is no immutable external audit store or transactional outbox yet.
- There is no production deployment, observability stack, backup policy, or formal performance/load validation.

## Project context

- [`AGENTS.md`](AGENTS.md) — agent working contract
- [`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md) — current project truth
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — runtime and module boundaries
- [`docs/DECISIONS.md`](docs/DECISIONS.md) — material decisions
- [`docs/SECURITY.md`](docs/SECURITY.md) — assets, boundaries, and current controls
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — completed and next slices

Project truth stays in OpsDesk. Agent Lore stores concise engineering evidence and recommendations, not a copy of this repository's wiki.
