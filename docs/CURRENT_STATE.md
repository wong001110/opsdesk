# Current State

## Phase

**Bootstrap / pre-implementation**

OpsDesk has a defined product boundary and engineering test goals, but no application code has been implemented yet.

## Product goal

Build a compact enterprise operations application that is easy to understand while naturally exercising authorization, multi-tenancy, AI provider credentials, auditability, and AI-assisted actions.

## MVP scope

Planned first slice:

- user authentication
- workspace / tenant membership
- roles: Admin, Manager, Member
- ticket/request creation and status
- AI provider configuration
- AI classify/summarize action for a ticket
- audit log

Keep workflow intentionally simple at first:

```text
OPEN -> IN_PROGRESS -> DONE
```

## Not in MVP

Defer until the core proving ground is stable:

- complex approval/workflow engine
- notifications
- scheduling
- attachments
- billing
- mobile app
- microservices
- Kubernetes/message-bus infrastructure
- autonomous AI actions with broad authority

## Agent Lore validation goals

OpsDesk should quickly create evidence around:

1. small-change single-agent behavior
2. parallel vs serial task decomposition
3. proportional verification depth
4. credential/origin isolation
5. RBAC and cross-workspace isolation
6. audit/log secret handling
7. prompt injection and AI action authority
8. semantic commit batching
9. project-local context maintenance
10. cross-stack engineering performance

## Next implementation checkpoint

Create the first runnable Spring Boot application with PostgreSQL-backed persistence and the smallest vertical slice required to prove:

```text
authentication
+ workspace membership
+ basic ticket
```

Do not add AI provider credentials until the identity/tenant boundary is testable; provider configuration should become the next deliberate security-sensitive feature.

## Context freshness

This document should be updated at meaningful integration/feature checkpoints. If source code or tests contradict this file, trust the source and repair this document.
