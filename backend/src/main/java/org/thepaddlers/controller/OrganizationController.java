package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.model.Court;
import org.thepaddlers.model.Organization;
import org.thepaddlers.model.AuditLog;
import org.thepaddlers.repository.AuditLogRepository;
import org.thepaddlers.repository.CourtRepository;
import org.thepaddlers.repository.OrganizationRepository;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orgs")
public class OrganizationController {
    private final OrganizationRepository organizationRepository;
    private final CourtRepository courtRepository;
    private final AuditLogRepository auditLogRepository;

    public OrganizationController(OrganizationRepository organizationRepository, CourtRepository courtRepository, AuditLogRepository auditLogRepository) {
        this.organizationRepository = organizationRepository;
        this.courtRepository = courtRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    @Audit(action = "org.create", entity = "organization")
    public ResponseEntity<?> createOrg(@RequestBody Organization org) {
        Organization saved = organizationRepository.save(org);
        return ResponseEntity.created(URI.create("/api/orgs/" + saved.getId())).body(saved);
    }

    @PostMapping("/{orgId}/courts")
    @Audit(action = "court.create", entity = "court")
    public ResponseEntity<?> createCourt(@PathVariable Long orgId, @RequestBody Map<String,String> body) {
        var orgOpt = organizationRepository.findById(orgId);
        if (orgOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid orgId"));
        }
        Organization org = orgOpt.get();
        Court c = new Court();
        c.setName(body.getOrDefault("name","Court"));
        c.setOrg(org);
        Court saved = courtRepository.save(c);
        return ResponseEntity.created(URI.create("/api/orgs/" + orgId + "/courts/" + saved.getId())).body(saved);
    }

    @GetMapping("/{orgId}/admin/audit")
    public ResponseEntity<?> getAudit(@PathVariable Long orgId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size,
                                      @RequestParam(required = false) String action,
                                      @RequestParam(required = false) String entity,
                                      @RequestParam(required = false) Long userId) {
        // Use pageable
        org.springframework.data.domain.Pageable pg = org.springframework.data.domain.PageRequest.of(page, size);
        var p = auditLogRepository.findByOrgIdOrderByCreatedAtDesc(orgId, pg);
        // optionally filter in-memory for action/entity/user
        var content = p.getContent();
        if (action != null) content = content.stream().filter(l -> action.equals(l.getActionType())).toList();
        if (entity != null) content = content.stream().filter(l -> entity.equals(l.getEntityType())).toList();
        if (userId != null) content = content.stream().filter(l -> userId.equals(l.getUserId())).toList();
        return ResponseEntity.ok(Map.of("total", p.getTotalElements(), "page", p.getNumber(), "size", p.getSize(), "items", content));
    }

    @GetMapping("/{orgId}/admin/audit.csv")
    public ResponseEntity<?> exportAuditCsv(@PathVariable Long orgId,
                                            @RequestParam(required = false) String action,
                                            @RequestParam(required = false) String entity,
                                            @RequestParam(required = false) Long userId,
                                            @RequestParam(defaultValue = "1000") int limit) {
        // fetch up to `limit` logs (default 1000) for export
        var all = auditLogRepository.findByOrgIdOrderByCreatedAtDesc(orgId);
        var filtered = all.stream()
                .filter(l -> action == null || action.equals(l.getActionType()))
                .filter(l -> entity == null || entity.equals(l.getEntityType()))
                .filter(l -> userId == null || userId.equals(l.getUserId()))
                .limit(limit)
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("id,created_at,user_id,action,entity,entity_id,ip,user_agent,details\n");
        for (var l : filtered) {
            // escape double quotes in details
            String details = l.getDetails() == null ? "" : l.getDetails().replace("\"","\"\"");
            sb.append(l.getId()).append(',')
              .append(l.getCreatedAt()).append(',')
              .append(l.getUserId() == null ? "" : l.getUserId()).append(',')
              .append('"').append(l.getActionType() == null ? "" : l.getActionType()).append('"').append(',')
              .append('"').append(l.getEntityType() == null ? "" : l.getEntityType()).append('"').append(',')
              .append(l.getEntityId() == null ? "" : l.getEntityId()).append(',')
              .append(l.getIpAddress() == null ? "" : '"' + l.getIpAddress() + '"').append(',')
              .append(l.getUserAgent() == null ? "" : '"' + l.getUserAgent() + '"').append(',')
              .append('"').append(details).append('"').append('\n');
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=thepaddlers_audit_"+orgId+".csv")
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(sb.toString());
    }
}
