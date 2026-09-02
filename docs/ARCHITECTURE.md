# Architecture

## Initial shape

Start as a **modular monolith**. The project should be deployable as one application plus one PostgreSQL database until real evidence justifies additional services.

```text
Browser
  |
  v
Spring Boot application
  |
  +-- identity / membership
  +-- workspace / tenant boundary
  +-- ticket operations
  +-- AI provider configuration
  +-- AI actions
  +-- audit
  |
  v
PostgreSQL
```

## Target stack

Initial direction:

- Java
- Spring Boot
- Spring MVC
- Spring Security
- PostgreSQL
- Spring Data JPA / Hibernate
- Flyway
- JUnit
- Testcontainers

UI technology may begin server-rendered if that keeps the first vertical slice small. Do not introduce a separate frontend deployment until it provides clear product or validation value.

## Module boundaries

Conceptual modules:

### identity
Authentication, users, sessions/security identity.

### workspace
Workspace ownership/membership, tenant boundary, role assignment.

### ticket
Ticket/request creation, status, assignment, business rules.

### provider
AI provider profiles, origin/base URL, credential references/configuration.

### ai
AI classify/summarize flows and later bounded AI actions.

### audit
Security/product audit events. Audit data must not become a secret leakage sink.

Module boundaries are working architecture, not immutable packages. Change them when implementation evidence justifies it and record material changes in `DECISIONS.md`.

## Important separation

Keep these concepts distinct:

```text
User identity
!= workspace membership
!= role/authority

AI provider
!= trusted origin
!= credential

AI read capability
!= AI write capability
```

These separations are intentional because later security tests depend on them.

## Data direction

Prefer workspace-scoped entities with explicit tenant ownership. Authorization must not rely only on UI filtering.

Conceptual entities:

```text
User
Workspace
Membership
Ticket
ProviderProfile
AuditEvent
```

Do not finalize the schema from this document alone; migrations and implemented domain constraints become authoritative.

## Runtime / deployment

Optimize for a small deployment footprint first:

```text
1 application
1 PostgreSQL database
```

External AI providers are introduced only when the provider-security slice begins.

## Testing direction

Use layered evidence rather than one universal test suite:

- domain/unit tests for local rules
- integration tests for persistence/security boundaries
- Testcontainers where real PostgreSQL behavior matters
- targeted security/adversarial tests for credential, tenant, authority, logging, or AI-tool boundaries

Broader E2E or mutation testing is added when the change justifies it, not as a requirement for every edit.
