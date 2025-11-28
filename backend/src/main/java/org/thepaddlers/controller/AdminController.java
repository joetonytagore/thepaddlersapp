package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orgs/{orgId}")
public class AdminController {

    @GetMapping("/reports/sales")
    public ResponseEntity<?> salesReport(@PathVariable Long orgId) {
        return ResponseEntity.ok(Map.of("orgId", orgId, "total", 0));
    }

    @PostMapping("/memberships")
    @Audit(action = "admin.membership.create", entity = "membership")
    public ResponseEntity<?> createMembership(@PathVariable Long orgId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("ok", true, "body", body));
    }

    @PutMapping("/settings")
    @Audit(action = "org.settings.update", entity = "organization_settings")
    public ResponseEntity<?> updateSettings(@PathVariable Long orgId, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("updated", true, "settings", body));
    }
}
