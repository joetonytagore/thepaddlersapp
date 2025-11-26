package org.thepaddlers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thepaddlers.model.WaitlistEntry;
import org.thepaddlers.repository.WaitlistRepository;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {
    private final WaitlistRepository waitlistRepository;

    public WaitlistController(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    @GetMapping
    public List<WaitlistEntry> list() {
        return waitlistRepository.findAll();
    }
}

