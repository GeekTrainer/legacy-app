# Module 06 delta — LANDED

**Produces:** `start-of-module-07` (= cumulative end state of Module 06)

**Adds** (seed — Copilot-authored, performing ACC module 06; incl. deterministic pom/package edits):
- Modernization of `services/audit-svc` and `services/auth-svc`: Spring Boot `2.7.18` → `3.5.3`, Java `11` → `21`, `javax.annotation.PostConstruct` → `jakarta.annotation.PostConstruct` (auth-svc), baseline test suites, `package.json` drops the `with-java11` shim.
- `.github/lsp.json` (Java `jdtls`), `.github/agents/java-migrator.agent.md`, `docs/modernization/audit-svc-plan.md`, `docs/modernization/migration-playbook.md`, extended `.github/hooks/scripts/test-router.sh`.

**Verified:** `build-branches.mjs --check` reproduces `expectedTreeSha` `f4bf2d4326384507e78ac8bf20e959de9922d650`. audit-svc: 8/8, auth-svc: 6/6 `mvn verify` on Boot 3.5.3/Java 21. See `manifest.json` (module 6).
