package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.model.Event;
import org.thepaddlers.repository.InvoiceRepository;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orgs/{orgId}")
public class EventController {
    private final InvoiceRepository invoiceRepository; // reuse to show example

    public EventController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping("/events")
    @Audit(action = "event.create", entity = "event")
    public ResponseEntity<?> createEvent(@PathVariable Long orgId, @RequestBody Event event) {
        // naive: echo back
        event.setOrgId(orgId);
        return ResponseEntity.created(URI.create("/api/orgs/" + orgId + "/events/1")).body(event);
    }

    @PostMapping("/events/{id}/register")
    @Audit(action = "event.register", entity = "event_registration")
    public ResponseEntity<?> register(@PathVariable Long orgId, @PathVariable Long id, @RequestBody(required = false) Object body) {
        return ResponseEntity.ok(Map.of("registered", true, "eventId", id));
    }
}
