# course-build — ACC learner branch system

This directory owns the machinery that produces checkout-ready **`start-of-module-N`** branches for the [Advanced Copilot CLI (ACC)](https://github.com/GeekTrainer/advanced-copilot-cli) course. It is course infrastructure and is **never** part of a learner's checked-out state.

Start with **[`REFS.md`](./REFS.md)** — the authoritative ref model + `repository_dispatch` contract.

## Layout

| Path | Purpose |
| ---- | ------- |
| [`REFS.md`](./REFS.md) | Ref/naming model, module map, dispatch payload contract, lifecycle. |
| [`manifest.json`](./manifest.json) | Machine-readable delta index: base ref, per-module patch order, expected trees/assets. |
| [`deprecated-branches.md`](./deprecated-branches.md) | Deprecation mapping for the two legacy `-solution` branches. |
| `deltas/module-NN/*.patch` | Ordered `git format-patch` series per module (M01–M03 backfilled; M04–M06 pending ACC content). |
| `scripts/build-branches.mjs` | Generator: base + ordered deltas → staging refs, with `--check` verification. |
| `scripts/validate-branch.sh` | Build one branch and run the suites present in that state. |

## Quick start

```bash
# Verify the whole delta store is deterministic (trees match, ancestry linear):
node course-build/scripts/build-branches.mjs --check

# Stage the buildable branches under a namespace (creates regen/<id>/start-of-module-K):
node course-build/scripts/build-branches.mjs --dispatch-id local

# Build + test a single learner branch end-to-end:
course-build/scripts/validate-branch.sh start-of-module-04
```

## Workflows (in `../.github/workflows/`)

- `validate-branches.yml` — CI gate: deterministic delta check + secret scan + build/test every buildable branch.
- `promote-branches.yml` — atomic promotion of mutable aliases + immutable version tags (environment-gated).
- `acc-content-changed.yml` — `repository_dispatch` receiver: regenerate downstream range, invoke ACC module-runner, open a PR, flag app-code conflicts for humans.

## Status

M01–M03 deltas are backfilled and verified. M04–M06 are `pending-acc-content` (need ACC hooks / barcode feature / modernization code + the module-runner interface). See each `deltas/module-0{4,5,6}/README.md` and `manifest.json` `needsFromAcc`.
