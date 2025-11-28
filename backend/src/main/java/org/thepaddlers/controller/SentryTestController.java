package org.thepaddlers.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryTestController {
    @GetMapping("/debug/sentry")
    public String trigger() {
        // throw a runtime exception to test Sentry reporting
        throw new RuntimeException("Sentry test exception - ThePaddlers");
    }
}

