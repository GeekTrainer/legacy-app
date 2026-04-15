# Asset Tracker Legacy App — Implementation Plan

## Overview

Build a fictitious internal asset tracking application ("AssetTrack") for a made-up company called **Contoso Industries**. The app tracks hardware assets (laptops, monitors, etc.) assigned to employees across departments. It's intentionally built with legacy patterns and older frameworks to serve as a brownfield demo environment for a developer course on agentic workflows.

## Tech Stack

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| Backend | Java / Spring Boot | 2.7.x / Java 11 | Intentionally legacy |
| Frontend | jQuery + Bootstrap | jQuery 3.x + Bootstrap 3.3.7 | Server-rendered via Thymeleaf |
| Database | SQLite | Via sqlite-jdbc | Single file, zero config |
| Scripts | Python (2-style) | Python 3 with old-style patterns | ETL, reporting, admin utilities |

## Company & Data Design

### Fictitious Company: **Contoso Industries**
- Mid-size company, ~60 employees across 6 departments

### Departments (6)
1. Engineering
2. Marketing
3. Finance
4. Human Resources
5. Operations
6. Sales

### Employee Data (~60 records)
- Diverse spread of names across genders and ethnicities
- Fields: employee_id, first_name, last_name, email, department, title, hire_date, is_active
- ~5-8 inactive employees (left the company) to create data messiness

### Asset Types (6)
1. Laptop
2. Monitor
3. Keyboard
4. Phone (desk phone)
5. Keycard/Badge
6. Docking Station

### Asset Data (~150-200 records)
- Fields: asset_id, asset_tag (e.g., "CON-LPT-001"), asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status (available/assigned/retired/lost), notes
- Intentional messiness: some missing serial numbers, inconsistent manufacturer names ("Dell" vs "DELL" vs "Dell Inc."), a few assets marked assigned but employee no longer active

### Assignment History (~300-400 records)
- Fields: assignment_id, asset_id, employee_id, assigned_date, returned_date, assigned_by, notes
- Some assets with long chains of reassignment
- Some assignments with null returned_date for active assignments

### Business Rules (partially enforced)
These rules *should* be enforced but the legacy code only enforces some of them, creating realistic bugs:
- An asset can only have one active assignment at a time (enforced in service layer)
- Inactive employees cannot receive new assets (**NOT enforced** — bug)
- Lost/retired assets cannot be assigned (**NOT enforced** — bug)
- returned_date must be >= assigned_date (**NOT enforced** — no validation)

## Project Structure

```
legacy-app/
├── .devcontainer/
│   └── devcontainer.json               # Codespaces config (Java 11, Python 3, Maven)
├── pom.xml                           # Maven project (Spring Boot 2.7.x)
├── src/
│   ├── main/
│   │   ├── java/com/contoso/assettracker/
│   │   │   ├── AssetTrackerApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── DashboardController.java    # Home page with summary stats
│   │   │   │   ├── AssetController.java        # CRUD for assets
│   │   │   │   ├── EmployeeController.java     # CRUD for employees
│   │   │   │   └── AssignmentController.java   # Asset assignment/return
│   │   │   ├── model/
│   │   │   │   ├── Asset.java
│   │   │   │   ├── Employee.java
│   │   │   │   └── Assignment.java
│   │   │   ├── repository/
│   │   │   │   ├── AssetRepository.java        # Raw JDBC (no JPA on purpose)
│   │   │   │   ├── EmployeeRepository.java
│   │   │   │   └── AssignmentRepository.java
│   │   │   └── service/
│   │   │       ├── AssetService.java
│   │   │       ├── EmployeeService.java
│   │   │       └── AssignmentService.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── schema.sql                      # Table creation
│   │   │   ├── data.sql                        # Seed data
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── app.css
│   │   │   │   └── js/
│   │   │   │       └── app.js                  # jQuery code
│   │   │   └── templates/
│   │   │       ├── layout.html                 # Base template (Bootstrap 3)
│   │   │       ├── dashboard.html              # Home page — summary stats
│   │   │       ├── assets.html                 # Asset list
│   │   │       ├── asset-detail.html           # Single asset view
│   │   │       ├── employees.html              # Employee list
│   │   │       ├── employee-detail.html        # Single employee view
│   │   │       └── assignments.html            # Assignment management
│   │   └── test/
│   │       └── java/com/contoso/assettracker/
│   │           └── AssetTrackerApplicationTests.java  # Minimal — just context loads
├── scripts/
│   ├── import_assets.py                # Python (old-style) — bulk CSV import
│   ├── generate_report.py              # Python (old-style) — warranty/compliance report
│   └── data/
│       └── sample_import.csv           # Sample CSV for import script
├── README.md                           # Incomplete project docs (intentional gaps & errors)
├── exercises.md                        # Course exercise list (8 atomic scenarios)
```

## Intentional Legacy Patterns (by design)

These are deliberately included to create realistic brownfield scenarios:

- **Raw JDBC** instead of JPA/Hibernate — manual SQL string building in repositories
- **No input validation** — controllers accept whatever comes in
- **Minimal error handling** — exceptions bubble up as 500s
- **No tests** beyond a basic context-load test
- **Mixed concerns** — some business logic in controllers instead of services
- **Inline SQL** — queries built with string concatenation (SQL injection risk)
- **No API docs** — no Swagger/OpenAPI
- **Python 2-style scripts** — old string formatting (% operator), no f-strings, no type hints, no pathlib — runs on Python 3 but looks dated
- **Inconsistent code style** — some files use different conventions (as if multiple devs touched them over the years)
- **Bootstrap 3 patterns** — `col-xs-*`, panels instead of cards, glyphicons
- **Unenforced business rules** — some invariants are only partially checked, creating realistic data integrity gaps

## Documentation (intentionally imperfect)

The app should include documentation that feels like a real internal project — present but incomplete, with a few inaccuracies:

### README.md
- Project overview, how to build/run, basic architecture description
- **Build/run instructions are accurate** — students must be able to set up the app without issues
- **Incomplete**: Missing info on the Python scripts, doesn't mention the assignment history feature or dashboard stats
- **Inaccurate (non-blocking)**: References a `/api/reports` endpoint that doesn't exist; mentions "PostgreSQL" in one place even though it's SQLite — these are in architecture description, not setup
- Tone: Written by someone who intended to finish it but never did ("TODO: add more details here")

### Inline Code Comments
- Some methods have Javadoc, others don't
- A couple of comments are outdated (describe old behavior that was changed)
- Python scripts have a file-level docstring but sparse inline comments

### exercises.md (deliverable for the course)
- A standalone markdown file at the project root
- Lists 9 exercise ideas with a short paragraph each describing what to do
- No step-by-step instructions — just the scenario and goal
- Each exercise is fully atomic (no dependencies on other exercises)

## Course Exercises (9 atomic operations)

Each exercise is fully independent — no exercise depends on completion of another. All are self-contained and target a specific language/technology. Scopes are tightly defined so learners know exactly what to touch.

1. **"Understand the Codebase"** (Full Stack) — Use the agent to explore and explain the app architecture, trace a request from the dashboard to the database, and identify potential issues. Read-only, no code changes.

2. **"Add Unit Tests to the Asset Service"** (Java) — Write JUnit tests for `AssetService.java`. The service has real business logic (e.g., checking for active assignments before allowing a new one). Test the happy path and edge cases. **Scope**: Create test file(s) only; don't modify source code.

3. **"Fix the SQL Injection Vulnerability"** (Java) — `AssetRepository.findByAssetTag()` and `AssetRepository.searchAssets()` use string concatenation to build queries. Refactor these two specific methods to use parameterized queries. **Scope**: `AssetRepository.java` only, two methods.

4. **"Modernize the Python Scripts"** (Python) — The import and report scripts use old-style string formatting (% operator), `urllib2`-style patterns, and no modern Python idioms. Update to use f-strings, pathlib, and modern conventions. **Scope**: `import_assets.py` and `generate_report.py`.

5. **"Add Asset Search/Filter Feature"** (Java + jQuery) — Add a server-side filter to the assets list page. Filter by asset type and status only (dropdown selects, not free-text search). Requires a new query in the repository, an endpoint parameter in the controller, and jQuery to submit the filter form. **Scope**: `AssetRepository`, `AssetController`, `assets.html`, `app.js`.

6. **"Add Error Handling to Python Import Script"** (Python) — The import script crashes on malformed CSV rows (missing columns, bad dates). Add row-level validation, skip bad rows with logging, and print a summary of successes/failures at the end. **Scope**: `import_assets.py` only.

7. **"Add Input Validation to the Asset Create Form"** (Java + HTML) — The create-asset form and its controller endpoint accept anything — empty strings, missing required fields, nonsensical dates. Add server-side validation for required fields (asset_tag, asset_type, manufacturer) and sensible date checks. **Scope**: `AssetController.java` (the create handler) and `asset-detail.html` (display validation errors).

8. **"Fix the Dashboard Display Bug"** (jQuery/HTML/CSS) — The dashboard page has a bug: the asset status summary counts are rendered with wrong Bootstrap label colors (e.g., "retired" shows as green/"success" instead of gray/"default", "lost" shows as blue/"info" instead of red/"danger"). Fix the color mapping in the template. **Scope**: `dashboard.html` and/or `app.js`.

9. **"Upgrade Spring Boot 2.7 to 3.x"** (Java) — Upgrade the application from Spring Boot 2.7.x to 3.x. This involves updating the pom.xml version, switching from `javax.*` to `jakarta.*` namespace imports across the codebase, updating any deprecated APIs, switching the project to Java 17, and verifying everything still compiles and runs. **Scope**: `pom.xml`, all Java files with `javax` imports, `application.properties` if needed.

These will be documented in `exercises.md` at the project root — one paragraph per exercise describing the scenario and goal, no step-by-step instructions.

## Devcontainer / Codespaces Setup

The app runs in GitHub Codespaces. The devcontainer must ensure **zero manual setup** — learners open the Codespace and everything works.

### `.devcontainer/devcontainer.json`
- **Base image**: `mcr.microsoft.com/devcontainers/java:11` (Java 11 base, includes Maven + SDKMAN)
- **Features**:
  - `ghcr.io/devcontainers/features/java:1` with `version: "17"` — installs Java 17 as a secondary JDK (available but not default)
  - `ghcr.io/devcontainers/features/python:1` — Python 3 for scripts
- **VS Code extensions** (pre-installed):
  - `vscjava.vscode-java-pack` — Java language support, debugging, Maven
  - `ms-python.python` — Python language support
- **Port forwarding**: 8080 (Spring Boot app)
- **postCreateCommand**: `mvn dependency:resolve` — pre-download all Maven dependencies so first build is fast
- **Settings**: Configure Java 11 as the default JDK

### Why this works
- Java 11 is the default (matches the legacy app's pom.xml)
- Java 17 is pre-installed and available — learners can switch to it as part of an upgrade exercise
- Python 3 is added via the feature
- SQLite needs no server — the JDBC driver is in pom.xml
- No Docker-in-Docker, no database containers, no extra services
- Learners type `mvn spring-boot:run` and the app starts

## Implementation Order

Build the app in this sequence:

1. **Devcontainer configuration** — `.devcontainer/devcontainer.json` for Codespaces
2. **Project configuration** — pom.xml (Spring Boot 2.7.x, Java 11, sqlite-jdbc), application.properties
3. **Database schema and seed data** — schema.sql, data.sql with diverse employee/asset data
4. **Java models** — POJOs for Asset, Employee, Assignment
5. **Repositories** — Raw JDBC with intentional bad patterns (string concat SQL)
6. **Services** — Business logic (some intentionally in wrong places)
7. **Controllers** — REST-ish endpoints, minimal validation
8. **Thymeleaf templates** — Bootstrap 3 UI with jQuery
9. **Static assets** — app.js (jQuery), app.css
10. **Python scripts** — Old-style Python 3 (% formatting, no f-strings, no pathlib, no type hints)
11. **README** — Incomplete docs with intentional gaps and a couple mistakes (setup instructions accurate)
12. **exercises.md** — Course exercise list with paragraph descriptions
13. **Intentional bugs** — Seed specific issues for exercises
14. **Verify** — Open in Codespaces, ensure app runs, all pages render, scripts execute

## Notes

- All people names must be fictitious with diverse representation across genders and ethnicities
- Company name: Contoso Industries (fictitious)
- Keep code realistic but not overwhelming — this is a teaching tool
- The app should actually run and be functional — it's not just dead code
- No authentication — unnecessary complexity for a demo
- Python scripts run on Python 3 but are written in deliberately old-fashioned style (pre-3.6 patterns)
- Core app flows (view assets, view employees, dashboard) must work correctly out of the box
- Bugs are seeded in specific, scoped areas tied to exercises — not pervasive chaos
