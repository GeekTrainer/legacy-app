# Module 04 delta — pending ACC content

**Produces:** `start-of-module-05` (= cumulative end state of Module 04)

**Adds:** .github/hooks lifecycle hooks

**Status:** `pending-acc-content` — this module's app-state is produced by ACC's module-runner. No patches are stored yet.

## What is needed to author this delta

See `needsFromAcc` for module 04 in `course-build/manifest.json`. Once the module-runner proposes a delta (or the required ACC content is relayed), the ordered `*.patch` series will be committed here and `manifest.json` updated (`status` -> `backfilled`, `patches`, `expectedTreeSha`, `expectedAssets`).

Until then the branch generator (`course-build/scripts/build-branches.mjs`) treats this module as empty and stops the buildable range at the last backfilled module.
