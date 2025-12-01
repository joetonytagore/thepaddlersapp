package com.thepaddlers.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Counter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig {
    @Bean
    fun reservationLatencyHistogram(registry: MeterRegistry): Timer =
        Timer.builder("reservation_creation_latency_seconds")
            .description("Reservation creation latency in seconds")
            .publishPercentileHistogram()
            .register(registry)

    @Bean
    fun failedPaymentsCounter(registry: MeterRegistry): Counter =
        Counter.builder("failed_payments_total")
            .description("Total failed payments")
            .register(registry)
}

