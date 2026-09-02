# Decisions

This file records material project decisions, not every implementation detail.

## D-001 — OpsDesk is a product first, testbed second

**Status:** Accepted

OpsDesk should remain understandable as a normal enterprise operations application. Agent Lore validation happens through development of real product features rather than benchmark-only screens or synthetic architecture.

## D-002 — Start as a modular monolith

**Status:** Accepted

Use one Spring Boot application and PostgreSQL. Do not introduce microservices, message brokers, or orchestration infrastructure without a concrete requirement.

Reason: the initial goal is to validate engineering decisions, authorization/security, and AI provider behavior without adding unrelated distributed-systems noise.

## D-003 — Technology stack is a validation variable

**Status:** Accepted

The project intentionally uses a Java/Spring enterprise stack even if the human owner is more familiar with other stacks. The purpose is to evaluate whether coding agents can work effectively from repository evidence, conventions, tests, and documentation rather than relying on the owner's implementation familiarity.

## D-004 — Project context stays inside OpsDesk

**Status:** Accepted

Use an OpenWiki-style concept only: coding agents maintain concise project-local state/docs inside this repository.

Do **not** depend on the OpenWiki product/service and do not require an extra AI API key for documentation generation.

Agent Lore must not store or synchronize OpsDesk's current project wiki/state.

## D-005 — Avoid full-repository re-analysis by default

**Status:** Accepted

Agents should first read project-local context, then inspect affected source/tests/contracts. Full-repo understanding is exceptional when context is stale/missing, architecture changes materially, a security incident has unknown blast radius, task impact cannot be bounded, or the user explicitly asks for it.

## D-006 — AI provider configuration is an intentional security slice

**Status:** Accepted

Do not hide provider credentials inside generic application settings. Provider, trusted origin/base URL, and credential authority should be modeled deliberately enough to test credential isolation.

The initial provider feature should support future tests for:

- provider switching
- custom base URL
- stale persisted configuration
- redirect/retry/fallback behavior
- secret logging

Real production secrets must not be required for regression tests; use synthetic canaries/fake endpoints.

## D-007 — Verification is proportional

**Status:** Accepted

V0–V4-style depth is a reasoning signal, not a fixed recipe. Small changes should not trigger full regression/security/mutation automatically. Security-sensitive or cross-boundary changes should prove their applicable invariants.

## D-008 — Semantic commits, not agent commits

**Status:** Accepted

A child agent finishing work does not require a final Git commit. Related changes should be integrated, proportionally verified, and committed at meaningful semantic boundaries. Internal worktree/checkpoint commits may still be used by the host harness.
