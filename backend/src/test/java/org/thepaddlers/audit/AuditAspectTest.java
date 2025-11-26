package org.thepaddlers.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.thepaddlers.model.AuditLog;
import org.thepaddlers.service.AuditService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AuditAspectTest {
    AuditService auditService;
    HttpServletRequest request;
    AuditAspect aspect;

    static class Dummy {
        @org.thepaddlers.audit.Audit(action = "test.action", entity = "test_entity")
        public Map<String,Object> annotatedMethod(Map<String,Object> payload){ return Map.of("id",7, "card_number","4242424242424242"); }
    }

    @BeforeEach
    void setup(){
        auditService = Mockito.mock(AuditService.class);
        request = Mockito.mock(HttpServletRequest.class);
        aspect = new AuditAspect(auditService, request);
    }

    @Test
    void masksSensitiveFieldsAndExtractsPrincipal() throws Exception{
        // set security principal as map with id
        var principal = Map.of("id", 42);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));

        // prepare a mocked JoinPoint and MethodSignature that returns our Dummy.annotatedMethod
        JoinPoint jp = Mockito.mock(JoinPoint.class);
        MethodSignature sig = Mockito.mock(MethodSignature.class);
        Method m = Dummy.class.getMethod("annotatedMethod", Map.class);
        Mockito.when(jp.getSignature()).thenReturn(sig);
        Mockito.when(sig.getMethod()).thenReturn(m);
        Mockito.when(jp.getArgs()).thenReturn(new Object[] { Map.of("foo","bar") });

        // call afterReturning with result map
        Map<String,Object> result = Map.of("id", 7, "card_number", "4242424242424242", "name", "Alice");
        aspect.afterReturning(jp, result);

        // verify auditService.record called once
        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditService, times(1)).record(cap.capture());
        AuditLog saved = cap.getValue();
        assertThat(saved.getEntityId()).isEqualTo("7");
        assertThat(saved.getDetails()).contains("[REDACTED]");
    }
}
