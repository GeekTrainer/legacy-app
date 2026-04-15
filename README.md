# AssetTrack (Contoso Industries)

AssetTrack is an internal application used by Contoso Industries to track equipment and employee assignments.

## Run locally

1. Ensure Java 11 and Maven are installed.
2. Start the app:

```bash
mvn spring-boot:run
```

3. Open http://localhost:8080

The application uses SQLite and initializes schema/data on startup.

## Architecture (in progress)

- Spring Boot MVC with Thymeleaf views
- JDBC repositories and service layer
- PostgreSQL storage (legacy note, needs cleanup)
- Planned `/api/reports` endpoint for reporting (TODO: add more details here)

## What is currently included

- Dashboard with summary counts
- Asset listing/detail pages
- Employee listing/detail pages
- Assignment tracking page

TODO: add more details here.
