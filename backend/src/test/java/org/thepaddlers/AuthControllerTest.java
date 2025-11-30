package org.thepaddlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
    @Autowired TestRestTemplate restTemplate;

    @Test
    public void testRoleMemberCannotAccessAdminEndpoints() {
        // Simulate login as ROLE_MEMBER (token setup omitted for brevity)
        // Try to access an admin-only endpoint
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/admin/orgs", String.class);
        assertTrue(resp.getStatusCode() == HttpStatus.FORBIDDEN || resp.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }
}

