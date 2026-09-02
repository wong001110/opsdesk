# OpsDesk

OpsDesk is a compact multi-tenant operations platform used to exercise real-world enterprise concerns such as RBAC, AI provider credentials, tenant isolation, auditability, and agent-driven engineering workflows.

The product is intentionally small at first. The goal is to create a realistic system that can grow feature by feature while remaining useful as a living validation project for Agent Lore.

## Initial product scope

- authentication
- workspaces / tenant boundaries
- member roles: Admin, Manager, Member
- lightweight tickets / requests
- AI provider configuration
- AI-assisted ticket classification
- audit log

## Why this project exists

OpsDesk serves two purposes:

1. A normal, easy-to-understand enterprise operations application.
2. A greenfield proving ground for AI coding agents using Agent Lore.

The repository should generate useful engineering situations naturally: small local changes, parallel feature work, database migrations, authorization rules, credential handling, prompt/tool security, verification-depth decisions, and semantic commit batching.

## Engineering direction

Initial target stack:

- Java
- Spring Boot
- Spring Security
- PostgreSQL
- Flyway
- JUnit
- Testcontainers

The exact implementation may evolve if repository evidence justifies it. Avoid introducing architecture or infrastructure only to make the project appear more complex.

## Start here

For coding agents and contributors:

- [`AGENTS.md`](AGENTS.md) — agent working contract
- [`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md) — current project truth / progress
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — current architecture and module boundaries
- [`docs/DECISIONS.md`](docs/DECISIONS.md) — accepted material decisions
- [`docs/SECURITY.md`](docs/SECURITY.md) — current assets, trust boundaries, and invariants
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — incremental product and validation slices

These files are maintained inside OpsDesk. Agent Lore does not store or synchronize them.

## Development principle

Use the smallest execution topology and verification depth that is sufficient for the current change. Small edits should remain small; high-risk changes should receive stronger evidence. Project documentation is updated at meaningful checkpoints rather than after every edit.

## Status

Planning / repository bootstrap. The next checkpoint is the first runnable Spring Boot + PostgreSQL vertical slice covering authentication, workspace membership, and basic tickets.
