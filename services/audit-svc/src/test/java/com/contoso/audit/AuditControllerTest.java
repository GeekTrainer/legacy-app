package com.contoso.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Contract tests for the audit HTTP endpoints. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuditControllerTest extends AuditTestSupport {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void health_returnsOk() {
        ResponseEntity<Map> res = rest.getForEntity("/health", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("status", "ok").containsEntry("service", "audit-svc");
    }

    @Test
    void postEvents_withValidBody_returnsNumericId() {
        Map<String, String> body = Map.of(
                "actor", "tester", "action", "create", "entityType", "asset",
                "entityId", "CON-TST-001", "details", "created in test");

        ResponseEntity<Map> res = rest.postForEntity("/events", body, Map.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsKey("id");
        Number id = (Number) res.getBody().get("id");
        assertThat(id.longValue()).isPositive();
    }

    @Test
    void getEvents_returnsJsonArray() {
        ResponseEntity<List> res = rest.getForEntity("/events", List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        // Seed data is inserted on startup.
        assertThat(res.getBody()).isNotEmpty();
    }
}
