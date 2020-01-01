# Java modernization playbook

> A reusable recipe for moving an AssetTrack Java service from Spring Boot 2.7 / Java 11 to Spring Boot 3.5 / Java 21. Distilled from modernizing `audit-svc` and `auth-svc`. Use it with the `Java migrator` agent.

## The loop

Assess → Plan → Protect → Migrate → Validate → Document. An agent changes the speed of each step, not the need for it.

## Recipe

1. **Assess.** Confirm the current stack (`pom.xml` parent + `java.version`), the dependency surface, and whether the service imports any renamed `javax.*` packages. Use the LSP (`.github/lsp.json` → `jdtls`) for precise caller/symbol lookups rather than text search.
2. **Plan.** Target the exact versions `workforce-svc` already runs (`spring-boot-starter-parent` `3.5.3`, `java.version` `21`) rather than a generic "3.x". Keep the plan phased so each step is independently verifiable and revertible.
3. **Protect — build the safety net first.** Add `spring-boot-starter-test` (`test` scope) and characterization tests *before* touching the framework: a `@SpringBootTest` context-load test, endpoint contract tests, and repository behavior tests. Point them at an **isolated temporary SQLite database** (never `/data/*.db`) and set `spring.datasource.hikari.maximum-pool-size=1` so connection-scoped `last_insert_rowid()` is deterministic. Confirm green on the *old* stack first.
4. **Migrate in order.**
   - Phase 1 — toolchain: `java.version` `11` → `21`; drop the `../../scripts/with-java11` wrapper from the service's `dev:*` script in `package.json`.
   - Phase 2 — framework: `spring-boot-starter-parent` `2.7.18` → `3.5.3`.
   - Rename only Jakarta-affected imports (`javax.annotation.*`, `javax.servlet.*`, `javax.persistence.*` → `jakarta.*`). Leave `javax.sql.*` (Java SE) alone.
5. **Validate after every phase.** `mvn verify` from the service directory; the same test count must stay green. Run the Playwright e2e suite as the final cross-system gate. Wire the service into `.github/hooks/scripts/test-router.sh` so edits trigger its tests.
6. **Document.** Update this playbook and the `Java migrator` agent with anything the upgrade taught you, so the next service is a repeat rather than a fresh start.

## Lessons from the two services

- **`audit-svc`** was the clean case: raw `JdbcTemplate`, **no** application `javax` imports, three dependencies. Only two `pom.xml` edits (parent + `java.version`) plus the test starter, and one `package.json` edit. 8 tests green on 3.5.3 / 21.
- **`auth-svc`** is where the second-service value showed up: it carries the `jjwt` library for JWT issuance and is the one place that imports `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct`. A red test after the framework bump is the safety net doing its job — hand the precise signal ("this name moved") back and adapt. 6 tests green on 3.5.3 / 21.

## Explicitly out of scope for a framework bump

- Introducing Spring Data JPA / Hibernate (separate project, own branch).
- Fixing the intentional SQL-injection course-exercise code in the repositories (track separately).
- Hardening SQLite pool behavior beyond test isolation (evaluate under load, separately).
