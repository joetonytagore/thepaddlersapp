package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.model.Membership;
import org.thepaddlers.model.MembershipPlan;
import org.thepaddlers.service.MembershipService;

import java.net.URI;

@RestController
@RequestMapping("/api/orgs/{orgId}")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping("/membership-plans")
    @Audit(action = "membership.plan.create", entity = "membership_plan")
    public ResponseEntity<?> createPlan(@PathVariable Long orgId, @RequestBody MembershipPlan plan) {
        plan.setOrgId(orgId);
        MembershipPlan saved = membershipService.createPlan(plan);
        return ResponseEntity.created(URI.create("/api/orgs/" + orgId + "/membership-plans/" + saved.getId())).body(saved);
    }

    @PostMapping("/memberships")
    @Audit(action = "membership.subscribe", entity = "membership")
    public ResponseEntity<?> subscribe(@PathVariable Long orgId, @RequestBody SubscribeRequest req) {
        try {
            Membership m = membershipService.subscribeUser(orgId, req.planId, req.userId, req.email);
            return ResponseEntity.created(URI.create("/api/memberships/" + m.getId())).body(m);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(e.getMessage());
        }
    }

    public static class SubscribeRequest { public Long planId; public Long userId; public String email; }
}
