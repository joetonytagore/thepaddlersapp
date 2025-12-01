package org.thepaddlers.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class RateLimitConfig {
    @Bean
    public HandlerInterceptor rateLimitInterceptor() {
        return new HandlerInterceptor() {
            private final Bucket bucket = Bucket4j.builder()
                .addLimit(Bandwidth.classic(100, Refill.greedy(100, java.time.Duration.ofMinutes(1))))
                .build();
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (!bucket.tryConsume(1)) {
                    response.setStatus(429);
                    response.setHeader("Retry-After", "60");
                    response.setContentType("application/json");
                    try {
                        response.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                    } catch (Exception ignored) {}
                    return false;
                }
                return true;
            }
        };
    }
}

