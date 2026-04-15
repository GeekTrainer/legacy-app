# Course Exercises

1. **Understand the Codebase** — Use the agent to explain architecture, follow a request from dashboard to repository query, and surface risks or technical debt without changing source.

2. **Add Unit Tests to the Asset Service** — Create focused tests around `AssetService` behavior (happy paths and edge cases) without changing production classes.

3. **Fix the SQL Injection Vulnerability** — Refactor `AssetRepository.findByAssetTag()` and `AssetRepository.searchAssets()` to use parameterized SQL only.

4. **Modernize the Python Scripts** — Update `scripts/import_assets.py` and `scripts/generate_report.py` to modern Python conventions (f-strings, pathlib, cleaner structure).

5. **Add Asset Search/Filter Feature** — Extend the assets flow with server-side filtering by type/status and connect the form behavior in jQuery.

6. **Add Error Handling to Python Import Script** — Add row-level validation, skip malformed CSV records, and print success/failure totals.

7. **Add Input Validation to the Asset Create Form** — Add controller-side validation for required fields and date sanity, then render user-friendly errors in the form.

8. **Fix the Dashboard Display Bug** — Correct the status badge color mapping so retired appears gray and lost appears red.

9. **Upgrade Spring Boot 2.7 to 3.x** — Move the app to Spring Boot 3.x and Java 17, migrate `javax` imports, and ensure app startup/tests still pass.
