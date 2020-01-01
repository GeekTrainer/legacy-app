package com.contoso.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Contract tests for the auth endpoints (health, token issuance, JWKS). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TokenControllerTest extends AuthTestSupport {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void health_returnsOk() {
        ResponseEntity<Map> res = rest.getForEntity("/health", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsEntry("status", "ok").containsEntry("service", "auth-svc");
    }

    @Test
    void jwks_returnsKeySet() {
        ResponseEntity<Map> res = rest.getForEntity("/.well-known/jwks", Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsKey("keys");
        assertThat((List<?>) res.getBody().get("keys")).isNotEmpty();
    }

    @Test
    void token_withValidCredentials_returnsAccessToken() {
        Map<String, String> body = Map.of("username", "admin", "password", "password");
        ResponseEntity<Map> res = rest.postForEntity("/token", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsKey("access_token");
        assertThat((String) res.getBody().get("access_token")).isNotBlank();
    }

    @Test
    void token_withBadCredentials_returns401() {
        Map<String, String> body = Map.of("username", "admin", "password", "wrong");
        ResponseEntity<Map> res = rest.postForEntity("/token", body, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void token_withMissingFields_returns400() {
        ResponseEntity<Map> res = rest.postForEntity("/token", Map.of("username", "admin"), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
