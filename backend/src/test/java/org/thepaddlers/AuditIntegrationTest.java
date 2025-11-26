package org.thepaddlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.thepaddlers.repository.AuditLogRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuditIntegrationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Test
    void annotatedEndpointCreatesAudit() {
        // Ensure no logs
        auditLogRepository.deleteAll();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-User-Id","123");

        String body = "{\"name\":\"TestOrg\", \"timezone\":\"UTC\"}";
        HttpEntity<String> req = new HttpEntity<>(body, headers);

        Map resp = restTemplate.postForObject("/api/orgs", req, Map.class);

        Object idObj = resp != null ? resp.get("id") : null;
        Long orgId = null;
        if (idObj instanceof Number) orgId = ((Number) idObj).longValue();
        else if (idObj instanceof String) orgId = Long.parseLong((String) idObj);

        assertThat(orgId).isNotNull();

        var logs = auditLogRepository.findByOrgIdOrderByCreatedAtDesc(orgId);
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getActionType()).isEqualTo("org.create");
    }
}
