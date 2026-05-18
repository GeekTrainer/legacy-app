# auth-svc (Legacy Java 11 / Spring Boot 2.7)

> [!IMPORTANT]
> This service is **intentionally legacy** — Spring Boot 2.7, Java 11, raw JDBC, plain-text passwords. It exists to give course learners a realistic modernization/security target.

Issues RS256 JWTs and exposes a JWKs document so other services can validate tokens.

## Endpoints

| Method | Path                  | Description                              |
|--------|-----------------------|------------------------------------------|
| GET    | `/health`             | Liveness check                           |
| POST   | `/token`              | Exchange username/password for a JWT     |
| GET    | `/.well-known/jwks`   | Public JWKs document for token validation |
| GET    | `/users/{id}`         | Get a user by id                         |

## Seeded users

| username   | password   | role     |
|------------|------------|----------|
| `admin`    | `password` | admin    |
| `helpdesk` | `password` | helpdesk |
| `viewer`   | `password` | viewer   |

## Run locally

```bash
mvn spring-boot:run
```

## Known smells (course material)

- **SQL injection** in `UserRepository.findByUsername` (string concatenation). Course exercise target.
- **Plain-text passwords** in the seeded database.
- **No tests** in this module.
- **Spring Boot 2.7 / Java 11** — target of an "upgrade to Spring Boot 3 + Java 21" exercise.
