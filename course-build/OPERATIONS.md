# Operations runbook — provisioning the ACC branch system

This system runs entirely on the built-in `GITHUB_TOKEN` and needs **no secrets**. The only human provisioning is one Actions environment (for the promotion approval gate) and two optional repository variables. Items marked **TODO(owner)** require decisions/identities this automation must not invent.

## How regeneration is triggered (pull model)

`regenerate-branches.yml` is self-contained — legacy-app **pulls** from the public ACC repo; nothing is pushed into legacy-app from outside.

- **Manual button:** Actions → *Regenerate learner branches* → *Run workflow*. Optional inputs: `acc_sha` (blank = ACC `main` HEAD), `affected_module` (blank = auto-detect), `acc_version` (blank = current `YYYY-MM`).
- **Daily cron backstop:** `07:17 UTC` — catches ACC changes with no manual run.

It clones public ACC anonymously with `github.token` (no secret), diffs the target SHA against `course-build/.last-acc-sha` to find affected modules, regenerates the downstream chain, validates in-run, and opens a regen PR.

> [!IMPORTANT]
> Two GitHub-settings caveats:
> 1. **PR creation toggle** — opening a PR from Actions requires *Settings → Actions → General → Allow GitHub Actions to create and approve pull requests*. If it is disabled, the workflow still pushes the regen branch and prints a compare URL in the run summary for manual PR creation.
> 2. **No cascade from `GITHUB_TOKEN` PRs** — PRs opened by the built-in token do **not** trigger other workflows, so validation is invoked **in-run** via `workflow_call` (not the `pull_request` event) against the pushed regen branch.

## 1. `production-branches` environment (gates promotion)

`promote-branches.yml` runs in the `production-branches` environment so promotion waits on human approval before any alias/tag is pushed.

```bash
# Create the environment
gh api -X PUT repos/GeekTrainer/legacy-app/environments/production-branches

# TODO(owner): add required reviewers (users/teams) — identities NOT invented here.
# gh api -X PUT repos/GeekTrainer/legacy-app/environments/production-branches \
#   -f 'reviewers[][type]=User' -F 'reviewers[][id]=<REVIEWER_USER_ID>'
```

> [!IMPORTANT]
> Until at least one required reviewer is added, the environment does not actually gate anything. Adding reviewers is a **TODO(owner)** step.

## 2. Repository variables (both optional)

```bash
# Optional: override the ACC repo (defaults to GeekTrainer/advanced-copilot-cli).
gh variable set ACC_REPO --repo GeekTrainer/legacy-app --body 'GeekTrainer/advanced-copilot-cli'

# Optional: override the module-runner (Copilot skill) invocation. Placeholders
# expanded by the workflow: {module} {base-ref} {acc_ref} {repo} {target} {out}
# (base-ref injected zero-padded, e.g. start-of-module-04; acc_ref = target SHA).
# When unset, the workflow uses the pinned default below. Override only to pin --model.
# gh variable set ACC_MODULE_RUNNER_CMD --repo GeekTrainer/legacy-app \
#   --body 'copilot -p "Run module-runner in validator/seed mode with: mode=seed module={module} base-ref={base-ref} acc-ref={acc_ref} repo={target} out={out}" --allow-all --log-level error'
```

If `ACC_MODULE_RUNNER_CMD` is unset the workflow still runs the pinned default when there are modules to re-seed; for a full rebuild from stored deltas no runner is invoked.

## 3. Secrets

**None.** ACC is public (anonymous clone via `github.token`), and there is no inbound dispatch, so neither an ACC read token nor a cross-repo dispatch token is required.

## 4. Module-runner contract

The runner runs inside a Copilot session, so **`{out}/result.json` is authoritative, not the `copilot` process exit code**. The workflow reads `result.json.result` and maps:

| `result` | behavior | mapped exit |
| -------- | -------- | ----------- |
| `PASS` | stage `{out}/patches/*.patch`, continue | 0 |
| `FAIL` | warn, no patches staged, PR still opened from stored deltas | 1 |
| `BLOCKED` | flag for human input (`runner-blocked`), no auto-proposal | 2 |
| missing / unparseable | skip re-seed for that module (keep stored delta) | 3 |

The full `result.json` schema is documented in `REFS.md`.

## Provisioning checklist (zero secrets)

- [ ] Create `production-branches` environment
- [ ] **TODO(owner):** add required reviewer(s) to `production-branches`
- [ ] **TODO(owner):** enable *Allow GitHub Actions to create and approve pull requests* (or accept the compare-URL fallback)
- [ ] (optional) set `ACC_REPO` variable (else default is used)
- [ ] (optional) set `ACC_MODULE_RUNNER_CMD` variable (else pinned default is used)
- [ ] No secrets to provision ✅
