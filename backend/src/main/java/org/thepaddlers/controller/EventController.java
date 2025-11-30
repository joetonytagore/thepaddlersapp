package org.thepaddlers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thepaddlers.audit.Audit;
import org.thepaddlers.model.Event;
import org.thepaddlers.model.EventRegistration;
import org.thepaddlers.model.User;
import org.thepaddlers.repository.EventRegistrationRepository;
import org.thepaddlers.repository.EventRepository;
import org.thepaddlers.repository.InvoiceRepository;
import org.thepaddlers.repository.UserRepository;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orgs/{orgId}")
public class EventController {
    private final InvoiceRepository invoiceRepository; // reuse to show example
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventController(InvoiceRepository invoiceRepository, EventRegistrationRepository eventRegistrationRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/events")
    @Audit(action = "event.create", entity = "event")
    public ResponseEntity<?> createEvent(@PathVariable Long orgId, @RequestBody Event event) {
        // naive: echo back
        event.setOrgId(orgId);
        return ResponseEntity.created(URI.create("/api/orgs/" + orgId + "/events/1")).body(event);
    }

    @PostMapping("/events/{id}/register")
    public ResponseEntity<?> register(@PathVariable Long orgId, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventRepository.findById(id).orElseThrow();
        long registeredCount = eventRegistrationRepository.countByEventAndStatus(event, EventRegistration.Status.REGISTERED);
        EventRegistration reg = new EventRegistration();
        reg.setEvent(event);
        reg.setUser(user);
        if (registeredCount < event.getCapacity()) {
            reg.setStatus(EventRegistration.Status.REGISTERED);
        } else {
            reg.setStatus(EventRegistration.Status.WAITLISTED);
            reg.setWaitlistPosition((int) (eventRegistrationRepository.findByEventOrderByWaitlistPositionAsc(event).size() + 1));
        }
        eventRegistrationRepository.save(reg);
        return ResponseEntity.ok(Map.of("registered", true, "eventId", id, "status", reg.getStatus()));
    }

    @PostMapping("/events/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long orgId, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Event event = eventRepository.findById(id).orElseThrow();
        List<EventRegistration> regs = eventRegistrationRepository.findByEventOrderByWaitlistPositionAsc(event);
        EventRegistration reg = regs.stream().filter(r -> r.getUser().getId().equals(userId)).findFirst().orElse(null);
        if (reg == null) return ResponseEntity.status(404).body("Registration not found");
        reg.setStatus(EventRegistration.Status.CANCELLED);
        eventRegistrationRepository.save(reg);
        // Waitlist automation: offer next spot
        List<EventRegistration> waitlist = eventRegistrationRepository.findByEventAndStatus(event, EventRegistration.Status.WAITLISTED);
        if (!waitlist.isEmpty()) {
            EventRegistration next = waitlist.get(0);
            next.setStatus(EventRegistration.Status.OFFERED);
            next.setWaitlistOfferExpiresAt(java.time.OffsetDateTime.now().plusHours(12));
            eventRegistrationRepository.save(next);
        }
        return ResponseEntity.ok(Map.of("cancelled", true));
    }

    @PostMapping("/events/{id}/accept-offer")
    public ResponseEntity<?> acceptOffer(@PathVariable Long orgId, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Event event = eventRepository.findById(id).orElseThrow();
        List<EventRegistration> regs = eventRegistrationRepository.findByEventOrderByWaitlistPositionAsc(event);
        EventRegistration reg = regs.stream().filter(r -> r.getUser().getId().equals(userId)).findFirst().orElse(null);
        if (reg == null || reg.getStatus() != EventRegistration.Status.OFFERED) return ResponseEntity.status(404).body("Offer not found");
        if (reg.getWaitlistOfferExpiresAt() != null && reg.getWaitlistOfferExpiresAt().isBefore(java.time.OffsetDateTime.now())) {
            reg.setStatus(EventRegistration.Status.CANCELLED);
            eventRegistrationRepository.save(reg);
            return ResponseEntity.status(410).body("Offer expired");
        }
        reg.setStatus(EventRegistration.Status.REGISTERED);
        reg.setWaitlistOfferExpiresAt(null);
        eventRegistrationRepository.save(reg);
        return ResponseEntity.ok(Map.of("accepted", true));
    }
}
