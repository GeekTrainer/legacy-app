package com.contoso.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Behavioral tests for the JdbcTemplate-backed repository. */
@SpringBootTest
class AuditRepositoryTest extends AuditTestSupport {

    @Autowired
    private AuditRepository repo;

    @Test
    void insert_persistsRow_andReturnsPositiveId() {
        long id = repo.insert("tester", "assign", "asset", "CON-RPT-001", "repo test");
        assertThat(id).isPositive();
    }

    @Test
    void recent_returnsAtMostLimitRows() {
        for (int i = 0; i < 5; i++) {
            repo.insert("tester", "create", "asset", "CON-RPT-" + i, "row " + i);
        }
        List<Map<String, Object>> rows = repo.recent(3);
        assertThat(rows).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void search_returnsMatchingRows() {
        repo.insert("tester", "assign", "asset", "CON-RPT-777", "assign match");
        List<Map<String, Object>> rows = repo.search("assign");
        assertThat(rows).isNotEmpty();
    }
}
