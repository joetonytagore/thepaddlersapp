package org.thepaddlers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thepaddlers.model.Court;
import org.thepaddlers.repository.CourtRepository;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    private final CourtRepository courtRepository;

    public CourtController(CourtRepository courtRepository) {
        this.courtRepository = courtRepository;
    }

    @GetMapping
    public List<Court> list() {
        return courtRepository.findAll();
    }
}

