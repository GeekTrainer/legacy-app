# audit-svc (Legacy Java 11 / Spring Boot 2.7)

> [!IMPORTANT]
> This service is **intentionally legacy** — Spring Boot 2.7, Java 11, raw JDBC, no tests. It exists as a realistic modernization target for the course.

Append-only audit log. Other services POST events; humans GET them.

## Endpoints

| Method | Path                | Description                                            |
|--------|---------------------|--------------------------------------------------------|
| GET    | `/health`           | Liveness check                                         |
| POST   | `/events`           | Record an audit event                                  |
| GET    | `/events`           | List most recent events (`limit`, or `query` to search)|

### Event body shape

```json
{
  "actor": "helpdesk@contoso.example",
  "action": "assignment.create",
  "entityType": "assignment",
  "entityId": "42",
  "details": "Assigned CON-LPT-001 to employee 7"
}
```

## Run locally

```bash
mvn spring-boot:run
```

## Known smells (course material)

- **SQL injection** in `AuditRepository.search` (string concatenation across three `LIKE` clauses). Course exercise target.
- **No tests** in this module.
- **Spring Boot 2.7 / Java 11** — primary target of the "Upgrade Spring Boot 2.7 to 3.x" exercise.
- Nothing currently POSTs to this service from `workforce-svc` — wiring that up is also an exercise.
