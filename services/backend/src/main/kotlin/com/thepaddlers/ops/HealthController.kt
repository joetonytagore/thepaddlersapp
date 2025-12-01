package com.thepaddlers.ops

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
class HealthController {
    @GetMapping("/actuator/health")
    fun health() = ResponseEntity.ok(mapOf("status" to "UP"))

    @GetMapping("/actuator/readiness")
    fun readiness() = ResponseEntity.ok(mapOf("ready" to true))

    @GetMapping("/actuator/liveness")
    fun liveness() = ResponseEntity.ok(mapOf("alive" to true))
}

