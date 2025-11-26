package org.thepaddlers.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.thepaddlers.model.AuditLog;
import org.thepaddlers.service.AuditService;

import java.lang.reflect.Method;
import java.util.*;

@Aspect
@Component
public class AuditAspect {
    private final AuditService auditService;
    private final HttpServletRequest request;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuditAspect(AuditService auditService, HttpServletRequest request) {
        this.auditService = auditService;
        this.request = request;
    }

    @Before("@annotation(org.thepaddlers.audit.Audit) || within(@org.thepaddlers.audit.Audit *)")
    public void before(JoinPoint joinPoint) {
        // no-op
    }

    @AfterReturning(pointcut = "@annotation(org.thepaddlers.audit.Audit) || within(@org.thepaddlers.audit.Audit *)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Audit audit = method.getAnnotation(Audit.class);
            if (audit == null) {
                audit = joinPoint.getTarget().getClass().getAnnotation(Audit.class);
            }
            if (audit == null) return;

            Map<String, Object> details = new LinkedHashMap<>();
            Object[] args = joinPoint.getArgs();
            // avoid serializing servlet request/response
            List<Object> serializableArgs = new ArrayList<>();
            for (Object a : args) {
                if (a == null) { serializableArgs.add(null); continue; }
                String cls = a.getClass().getName();
                if (cls.startsWith("jakarta.servlet") || cls.startsWith("javax.servlet") || cls.contains("HttpServletRequest") || cls.contains("HttpServletResponse")) continue;
                serializableArgs.add(a);
            }
            details.put("args", serializableArgs);
            details.put("result", result);

            // mask sensitive fields
            Map<String, Object> masked = maskSensitive(details);
            String detailsJson = mapper.writeValueAsString(masked);

            AuditLog log = new AuditLog();
            log.setActionType(audit.action());
            String ent = audit.entity();
            if (ent != null && !ent.isEmpty()) log.setEntityType(ent);
            log.setDetails(detailsJson);

            // If the result contains an 'id', record it as the entity id. If creating an organization, also set orgId.
            try {
                if (result != null) {
                    Object idVal = null;
                    if (result instanceof Map) {
                        Object v = ((Map<?,?>) result).get("id");
                        if (v != null) idVal = v;
                    } else {
                        try {
                            java.lang.reflect.Method getId = result.getClass().getMethod("getId");
                            idVal = getId.invoke(result);
                        } catch (NoSuchMethodException ignored) {}
                    }
                    if (idVal != null) {
                        log.setEntityId(String.valueOf(idVal));
                        if ("organization".equalsIgnoreCase(ent)) {
                            try { log.setOrgId(Long.parseLong(String.valueOf(idVal))); } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {}

            // extract user/org from SecurityContext if available, otherwise fallback to headers
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    Object principal = auth.getPrincipal();
                    // principal may be a username string or a userDetails object containing id
                    if (principal instanceof String) {
                        String p = (String) principal;
                        try { long uid = Long.parseLong(p); log.setUserId(uid); } catch (Exception ignored) {}
                    } else if (principal instanceof Map) {
                        Map<?,?> pm = (Map<?,?>) principal;
                        Object idv = pm.get("id"); if (idv == null) idv = pm.get("user_id");
                        if (idv instanceof Number) log.setUserId(((Number) idv).longValue());
                        else if (idv instanceof String) { try { log.setUserId(Long.parseLong((String) idv)); } catch(Exception ignored){} }
                    }
                }
            } catch (Exception ignored) {}

            // fallback to headers
            try {
                if (log.getUserId() == null) {
                    String uid = request.getHeader("X-User-Id"); if (uid != null) log.setUserId(Long.parseLong(uid));
                }
            } catch (Exception ignored) {}
            try {
                String oid = request.getHeader("X-Org-Id"); if (oid != null) log.setOrgId(Long.parseLong(oid));
            } catch (Exception ignored) {}
            log.setIpAddress(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));

            auditService.record(log);
        } catch (Exception e) {
            // do not break business logic if auditing fails
            // log properly in future (Sentry/Logger)
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> maskSensitive(Map<String,Object> in) {
        // shallow walk and mask keys containing sensitive keywords
        Set<String> sensitive = new HashSet<>(Arrays.asList("password","pass","card","token","secret","stripe","payment_method","cvv","cc_number","card_number","number"));
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : in.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map) {
                Map<String,Object> sub = (Map<String,Object>) v;
                out.put(e.getKey(), maskMap(sub,sensitive));
            } else if (v instanceof List) {
                List<Object> lst = (List<Object>) v;
                List<Object> outList = new ArrayList<>();
                for (Object item : lst) {
                    if (item instanceof Map) outList.add(maskMap((Map<String,Object>)item,sensitive));
                    else outList.add(item);
                }
                out.put(e.getKey(), outList);
            } else {
                out.put(e.getKey(), v);
            }
        }
        return out;
    }

    private Map<String,Object> maskMap(Map<String,Object> map, Set<String> sensitive) {
        Map<String,Object> out = new LinkedHashMap<>();
        for (Map.Entry<String,Object> e : map.entrySet()) {
            String key = e.getKey(); Object v = e.getValue();
            boolean isSensitive = sensitive.stream().anyMatch(s -> key.toLowerCase().contains(s));
            if (isSensitive) {
                out.put(key, "[REDACTED]");
            } else if (v instanceof Map) {
                out.put(key, maskMap((Map<String,Object>)v, sensitive));
            } else {
                out.put(key, v);
            }
        }
        return out;
    }
}
