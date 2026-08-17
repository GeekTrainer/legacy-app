# ACC learner branch system — ref model & contract

This directory (`course-build/`) owns the machinery that produces **checkout-ready learner branches** for the [Advanced Copilot CLI (ACC)](https://github.com/GeekTrainer/advanced-copilot-cli) course. It lives in `legacy-app` and is **never** part of a learner's checked-out state.

> [!IMPORTANT]
> This document is the authoritative contract. The ACC repo builds its trigger workflow against the naming scheme and `repository_dispatch` payload defined here. Changes here are breaking changes for ACC — coordinate before editing.

## Why this exists

ACC learners drop into any module without doing the prior ones. Each module's "Starting state" assumes the cumulative output of every earlier module already exists in `legacy-app`. We give learners a branch to `git checkout` that lands them in exactly that state.

`start-of-module-N` = the cumulative **END** state of module `N-1`.

## The module chain

The state-changing chain is `base -> M01 -> M02 -> ... -> M07`. `M00` is the pristine app; `M08` is a wrap-up with no state change; `M07` is fork-only infra management (its end state is still a learner branch target for anyone entering M07).

| Module | What it adds (cumulative delta) | End state = start of |
| ------ | ------------------------------- | -------------------- |
| M01 | Docs: `ARCHITECTURE.md`, README fill-in, devcontainer postCreate | `start-of-module-02` |
| M02 | `.github` instructions + Accessibility Expert agent + make-repo-contribution skill + copilot-instructions + a11y tweaks | `start-of-module-03` |
| M03 | Playwright test foundation + delegation brief + test backfill | `start-of-module-04` |
| M04 | `.github/hooks` lifecycle hooks | `start-of-module-05` |
| M05 | Playwright MCP registration + QA custom agent + REAL barcode feature (assets-svc + web) | `start-of-module-06` |
| M06 | LSP + MCP config + research report + REAL modernization app code | `start-of-module-07` |
| M07 | Fork-only infra management (no cumulative app-state delta) | — |

## Ref layers

Three layers, from most to least durable:

1. **Base (immutable tag) — `acc-base`**
   Pinned at the pristine `legacy-app` app tree (origin/main HEAD before course tooling landed: `3689288d1f5a9ed6871db94a09c2e651bc20af3e`). Every module delta applies on top of this. Learner branches contain the app + module content only — never `course-build/` or the course automation workflows.

2. **Mutable convenience branches — `start-of-module-N`**
   The learner-facing `git checkout` targets. They **move** (only via an approved, all-or-nothing promotion). `N` runs `02..07`.

3. **Immutable version tags — `acc-<YYYY-MM>/start-of-module-N`**
   Cut on every promotion (e.g. `acc-2026-08/start-of-module-05`). They **never move**. They let a learner (or a course revision) pin the exact state shipped in a given month even after the mutable branch advances.

Plus one ephemeral layer used only during a rebuild:

4. **Staging refs — `regen/<dispatch_id>/start-of-module-K`**
   Where a regeneration run assembles candidate branches. Promotion is atomic: validate every staging ref in the affected downstream range, then move all mutable aliases and cut all tags in one step, or abort and touch nothing.

## Deprecation mapping

Two hand-authored solution branches predate this system. They are **kept** (frozen, with a deprecation notice) — not deleted — so existing links keep working.

| Legacy branch | Equivalent to | New canonical branch |
| ------------- | ------------- | -------------------- |
| `02-building-ai-infra-solution` | end of M02 | `start-of-module-03` |
| `03-test-suite-remote-delegation-solution` | end of M03 | `start-of-module-04` |

See `deprecated-branches.md` for the learner-facing notice text.

## Delta store — source of truth

Deltas are deterministic, stored, and canonical. Each module is an **ordered patch series** (`git format-patch`, `--zero-commit`) under `course-build/deltas/module-NN/`, indexed by `course-build/manifest.json`.

- M01–M03 are **backfilled** from the stacked solution-branch commit ranges (verified to reproduce each solution tree byte-for-byte via `expectedTreeSha`).
- M04–M06 are `pending-acc-content`: their app-state is produced by ACC's module-runner. Once a runner-proposed delta is reviewed and merged, the committed patch series becomes canonical and future rebuilds re-apply the stored patches (the runner is not re-invoked to rebuild an unchanged module).

A rebuild is: `checkout acc-base` → for each module in order, `git am` its patch series → the resulting tree is that module's end state / the next module's start branch.

## `repository_dispatch` contract (ACC → legacy-app)

ACC signals a content change by dispatching to this repo. ACC holds no broad push token; it only fires the event.

```
POST /repos/GeekTrainer/legacy-app/dispatches
Accept: application/vnd.github+json

{
  "event_type": "acc-content-changed",
  "client_payload": {
    "acc_sha":         "<full 40-char ACC commit SHA>",   // REQUIRED: receiver checks out ACC at this SHA
    "acc_ref":         "refs/heads/main",                  // REQUIRED: ACC ref the change landed on
    "affected_module": 4,                                  // REQUIRED int 1..7: module whose content changed
    "acc_version":     "2026-08",                          // OPTIONAL: YYYY-MM for immutable tags; default = run-time YYYY-MM
    "reason":          "content-update",                   // content-update | manual | scheduled
    "dispatch_id":     "<uuid>"                            // OPTIONAL: correlates staging refs/run; receiver generates if absent
  }
}
```

### Cascade semantics

Changing module `N` re-derives `delta_N`, which changes `end-of-N` (= `start-of-module-(N+1)`) and **every** downstream branch. The receiver regenerates the range `start-of-module-(N+1) .. start-of-module-07`.

App-code cascade conflicts (M05/M06 carry real code) are **flagged for human resolution and never auto-resolved**.

## Module-runner contract (ACC seed/validator skill)

`module-runner` is a **Copilot skill**, invoked by running Copilot CLI in seed mode — not a standalone script. The dispatch receiver expands an invocation template (`ACC_MODULE_RUNNER_CMD`) and runs it from the ACC checkout. Recommended pinned invocation:

```
copilot -p "Run module-runner in validator/seed mode with: mode=seed module={module} base-ref={base-ref} acc-ref={acc_ref} repo={target} out={out}" --allow-all --log-level error
```

Placeholders expanded by the receiver (both hyphen and underscore forms accepted):

| placeholder | value |
| ----------- | ----- |
| `{module}` | affected module number (1..7) |
| `{base-ref}` / `{base_ref}` | starting state to seed from — **zero-padded** `start-of-module-NN` (e.g. `start-of-module-04`), computed by the receiver |
| `{acc-ref}` / `{acc_ref}` | pinned 40-char `acc_sha` from the payload (reproducible seeds) |
| `{repo}` / `{target}` | the legacy-app checkout the runner writes into |
| `{out}` | output dir for the proposed patch series + `result.json` |

> [!NOTE]
> ACC's suggested wording `base-ref=start-of-module-{module}` is **not** zero-padded and would produce `start-of-module-4`; our branch names are two-digit. Use `{base-ref}`, which the receiver injects zero-padded, rather than `start-of-module-{module}`.

The runner runs **inside** a Copilot session, so its process exit code is **not** authoritative — the receiver reads `{out}/result.json` and maps its `result` field. `result.json` is written as the final action of every run (including failures):

```json
{ "schema_version": 1, "mode": "seed", "module": 4,
  "base_ref": "start-of-module-04", "acc_ref": "<full-40-char-acc-sha>",
  "produced_branch": "start-of-module-05", "result": "PASS",
  "patches": ["patches/0001-....patch"],
  "verification": [ { "name": "playwright a11y suite", "command": "npm run test:e2e", "status": "pass" } ],
  "issues_report": "issues/04-issues.md",
  "started_at": "<ISO-8601>", "finished_at": "<ISO-8601>" }
```

| `result.json` `result` | meaning | receiver behavior | mapped exit |
| ---------------------- | ------- | ----------------- | ----------- |
| `PASS` | produced + all verification passed | stage `{out}/patches/*.patch`, continue to regen + PR | 0 |
| `FAIL` | a verification step failed | warn; no patches staged; PR still opened from stored deltas | 1 |
| `BLOCKED` | prereq/credential/external service prevented a valid run | flag for human input (`needs-human-resolution`, `runner-blocked`); no auto-proposal | 2 |
| missing / unparseable | no/!valid `result.json` | hard error, fail the run | 3 |

In `mode=validate` (pure gate) the runner omits `patches` and `produced_branch`. Seed patches are a `git format-patch` series in commit order under `{out}/patches/`, with stable messages `chore(seed): module <N> produced-state for start-of-module-<N+1> [acc:<short-sha>]`. The skill never pushes, opens PRs, or promotes — the receiver consumes `patches/` + `result.json`.

Provisioning of `ACC_MODULE_RUNNER_CMD`, `ACC_REPO`, tokens, and the promotion environment is documented in [`OPERATIONS.md`](./OPERATIONS.md).

## Lifecycle summary

```
ACC content change
      │  repository_dispatch: acc-content-changed
      ▼
acc-content-changed.yml (receiver)
      │  checkout ACC@acc_sha → invoke module-runner for affected module
      │  regenerate range (N+1..07) into regen/<dispatch_id>/start-of-module-K
      │  commit updated delta patches → open PR (flag app-code conflicts)
      ▼
validate-branches.yml (gate)  ── runs on the regen PR
      │  build + all suites + assets/ancestry/secret checks + runner pass
      ▼
human review + approve + merge PR
      ▼
promote-branches.yml (promotion)
      │  atomic: move start-of-module-N aliases + cut acc-<version>/start-of-module-N tags
      ▼
learners `git checkout start-of-module-N`
```

## Files in this directory

| Path | Purpose |
| ---- | ------- |
| `REFS.md` | This document — ref model + contract. |
| `manifest.json` | Machine-readable delta store index (base, per-module patch order, expected trees/assets). |
| `deltas/module-NN/*.patch` | Ordered `git format-patch` series per module. |
| `deprecated-branches.md` | Deprecation notice text for the two legacy `-solution` branches. |
| `scripts/build-branches.mjs` | Generator: assembles base + ordered deltas into staging refs. |
| `../.github/workflows/validate-branches.yml` | CI validation gate. |
| `../.github/workflows/promote-branches.yml` | Atomic promotion of aliases + tags. |
| `../.github/workflows/acc-content-changed.yml` | `repository_dispatch` receiver. |
