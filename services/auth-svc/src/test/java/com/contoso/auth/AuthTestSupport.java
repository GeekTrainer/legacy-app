package com.contoso.auth;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;
import java.util.UUID;

/** Point each test context at an isolated temporary SQLite database. */
abstract class AuthTestSupport {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        Path db = Path.of(System.getProperty("java.io.tmpdir"),
                "auth-test-" + UUID.randomUUID().toString().replace("-", "") + ".db");
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db);
        registry.add("spring.datasource.driver-class-name", () -> "org.sqlite.JDBC");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "1");
    }
}
