package com.contoso.audit;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Shared test configuration: point every test context at an isolated, temporary
 * SQLite database (never production data) and force a single connection so
 * connection-scoped {@code last_insert_rowid()} behaves deterministically.
 */
abstract class AuditTestSupport {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        Path db = Path.of(System.getProperty("java.io.tmpdir"),
                "audit-test-" + UUID.randomUUID().toString().replace("-", "") + ".db");
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
        registry.add("spring.datasource.driver-class-name", () -> "org.sqlite.JDBC");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "1");
    }
}
