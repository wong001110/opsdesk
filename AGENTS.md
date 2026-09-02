# Agent Working Contract

OpsDesk is designed to be developed by coding agents while remaining understandable to human reviewers.

## Start here

Before broad repository exploration, read the project-local context:

1. `docs/CURRENT_STATE.md`
2. `docs/ARCHITECTURE.md`
3. `docs/DECISIONS.md`
4. `docs/SECURITY.md` when the task touches auth, credentials, tenant boundaries, AI actions, external providers, logging, or privileged operations

Then inspect only the source, tests, contracts, migrations, and configuration relevant to the current task.

Do not re-read the entire repository by default. Expand to full-repository analysis only when the local context is missing/untrustworthy, the architecture changed materially, a security incident has unclear blast radius, task impact cannot be bounded, or the user explicitly requests a full audit.

## Execution

Use current repository evidence plus Agent Lore guidance to choose execution shape.

- Prefer a single agent when coordination does not clearly help.
- Parallelize only workstreams with useful independence and bounded mutable scopes.
- Allow nested delegation only when a child task itself benefits from decomposition.
- Main/Integrator owns integration and checkpoint decisions.

Avoid fixed agent counts or topology rules based only on task size.

## Verification

Verification should prove the claims introduced by the change, not maximize test count.

- Run cheap, targeted evidence first.
- Escalate integration, E2E, mutation, security, or adversarial depth only when change impact/risk warrants it.
- Security-sensitive changes must prove applicable invariants.
- Do not make every worker repeat the same full suite.

## Commits

Agent completion is not a semantic Git commit boundary.

Batch related edits into coherent, stable changes. Internal checkpoint commits may be used when the harness needs isolation, but final history should prefer meaningful semantic commits.

## Project context updates

Project state belongs in this repository, not Agent Lore.

At a meaningful checkpoint, Main/Integrator should update project-local docs if current project truth changed, for example:

- a feature/module became implemented or deprecated
- architecture or trust boundaries changed
- a major decision was made
- a known limitation/risk changed
- the current milestone/progress changed materially

Do not update wiki/state files for cosmetic or trivial edits that do not change project truth.

Source code, tests, migrations, contracts, and explicit specifications remain authoritative when they disagree with project context docs.
