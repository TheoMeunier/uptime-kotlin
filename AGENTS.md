# Agent instructions

Read `CONTEXT.md` before exploring, planning, reviewing, or modifying this repository. It is the source of truth for the product vocabulary, runtime topology, module responsibilities, important flows, and current constraints.

## Working rules

- Preserve the modular Gradle structure and keep dependencies flowing toward the shared libraries described in `CONTEXT.md`.
- Use the domain terms defined in `CONTEXT.md` in code, tests, issues, and documentation.
- Before changing an architectural decision, inspect the relevant records under `docs/adr/`. Create an ADR when a durable, non-obvious architectural choice is made.
- Database schema changes must be additive Flyway migrations under `backend/api/src/main/resources/db/migration/`; never rewrite a migration that may already have been applied.
- Keep standalone mode (`db-lock`) and cluster mode (`rabbitmq`) behavior aligned when changing probe execution or notifications.
- Never expose encrypted notification credentials or internal monitor targets through public endpoints.
- Add tests for new domain behavior. Monitoring state transitions, retry behavior, incident behavior, and notification rules require backend tests; interactive frontend behavior requires frontend tests.
- Do not modify generated or runtime data under `storage-db/`, `.gradle/`, `build/`, or `node_modules/`.
- Preserve unrelated user changes in the worktree.

## Verification

- Kotlin formatting: `./gradlew ktlintCheck`
- Kotlin tests: `./gradlew test`
- Application frontend: from `apps/app`, run `npm run lint` and `npm run build`
- Marketing website: from `apps/website`, run `npm run build`

Run the smallest relevant checks while iterating, then the broader checks appropriate to the change before handing work back.

## Agent skills

### Issue tracker

Issues and product requirements are tracked in GitHub Issues for `TheoMeunier/uptime-kotlin`. See `docs/agents/issue-tracker.md`.

### Triage labels

The repository uses the standard five-role triage vocabulary. See `docs/agents/triage-labels.md`.

### Domain docs

This is a single-context repository with `CONTEXT.md` at its root and architectural decisions under `docs/adr/`. See `docs/agents/domain.md`.
