package com.contoso.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/** Schema init is idempotent: re-running it neither throws nor duplicates seed rows. */
@SpringBootTest
class DataInitTest extends AuditTestSupport {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private DataInit dataInit;

    @Test
    void reinitialize_isIdempotent() throws Exception {
        Integer before = jdbc.queryForObject("SELECT COUNT(*) FROM audit_events", Integer.class);
        assertThat(before).isNotNull();

        // Run the initializer a second time; it must not throw or re-seed.
        dataInit.initSchema(jdbc).run();

        Integer after = jdbc.queryForObject("SELECT COUNT(*) FROM audit_events", Integer.class);
        assertThat(after).isEqualTo(before);
    }
}
