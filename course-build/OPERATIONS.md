# Operations runbook — provisioning the ACC branch system

This system needs repo-level configuration that **cannot** be created by a committed file: GitHub Actions **environments**, **variables**, and **secrets** live in repository settings and must be provisioned by a human with admin rights. (`.github/github-app.yml` configures the Copilot *app*, not Actions environments — so it is not the right vehicle here.)

Everything below is the exact provisioning surface. Items marked **TODO(owner)** require decisions/identities this automation must not invent.

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

## 2. Repository variables

```bash
gh variable set ACC_REPO --repo GeekTrainer/legacy-app --body 'GeekTrainer/advanced-copilot-cli'

# Module-runner invocation template. module-runner is a Copilot skill invoked via
# Copilot CLI in seed mode, not a standalone script. Placeholders expanded by the
# receiver: {module} {base-ref} {acc_ref} {repo} {target} {out} (base-ref is
# injected zero-padded, e.g. start-of-module-04; acc_ref is the pinned acc_sha).
# The receiver defaults to the invocation below when this var is unset; override
# it only to pin a specific --model.
# gh variable set ACC_MODULE_RUNNER_CMD --repo GeekTrainer/legacy-app \
#   --body 'copilot -p "mode=seed module={module} base-ref={base-ref} acc-ref={acc_ref} repo={target} out={out}" --allow-all --log-level error'
```

If `ACC_REPO` is unset the receiver defaults to `GeekTrainer/advanced-copilot-cli`. If `ACC_MODULE_RUNNER_CMD` is unset the receiver skips the runner step (no auto-proposal) but still regenerates from stored deltas.

## 3. Secrets

```bash
# Optional: read-only token to clone ACC at acc_sha (rate-limit robustness).
# ACC is public, so the receiver falls back to anonymous / github.token when absent.
# TODO(owner): provision a fine-grained PAT or GitHub App token (contents:read on ACC).
# gh secret set ACC_READ_TOKEN --repo GeekTrainer/legacy-app --body '<token>'

# Token ACC uses to POST /repos/GeekTrainer/legacy-app/dispatches (fires the receiver).
# Lives on the ACC side, not here, but is part of the contract.
# TODO(owner): provision LEGACY_APP_DISPATCH_TOKEN (fine-grained PAT or App with
#   contents:write on legacy-app) and store it as a secret in the ACC repo.
```

## 4. Module-runner contract (ACC PR #18)

The receiver treats the runner as: named inputs `module` / `base-ref` / `acc-ref` / `repo` / `out`; a machine-readable `result.json` written to `out`; exit codes:

| exit | meaning | receiver behavior |
| ---- | ------- | ----------------- |
| 0 | PASS | stage produced `*.patch`, continue |
| 1 | FAIL | warn, no patches staged, PR still opened from stored deltas |
| 2 | usage | hard error — fix `ACC_MODULE_RUNNER_CMD` |
| 3 | BLOCKED | needs human input — flagged, no auto-proposal |

Coordinate the exact `ACC_MODULE_RUNNER_CMD` wording and `result.json` schema with the ACC session before enabling auto-proposal.

## Provisioning checklist

- [ ] Create `production-branches` environment
- [ ] **TODO(owner):** add required reviewer(s) to `production-branches`
- [ ] Set `ACC_REPO` variable (or rely on default)
- [ ] **TODO(owner/ACC):** set `ACC_MODULE_RUNNER_CMD` from the relayed invocation
- [ ] **TODO(owner):** provision `ACC_READ_TOKEN` (optional)
- [ ] **TODO(owner):** provision `LEGACY_APP_DISPATCH_TOKEN` in the ACC repo
