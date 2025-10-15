package com.investment.metal.infrastructure.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience configuration following Single Responsibility Principle.
 * Handles only resilience patterns (circuit breaker, bulkhead, time limiter).
 */
@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .permittedNumberOfCallsInHalfOpenState(3)
            .minimumNumberOfCalls(5)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
            .slidingWindowSize(10)
            .recordExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class, RuntimeException.class)
            .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadConfig config = BulkheadConfig.custom()
            .maxConcurrentCalls(5)  // Reduced for better flooding protection
            .maxWaitDuration(Duration.ofMillis(500))  // Reduced wait time
            .build();

        return BulkheadRegistry.of(config);
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
            .limitForPeriod(10)  // 10 requests per period
            .limitRefreshPeriod(Duration.ofSeconds(1))  // 1 second window
            .timeoutDuration(Duration.ofMillis(100))  // 100ms timeout
            .build();

        return RateLimiterRegistry.of(config);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))  // Reduced timeout for better protection
            .cancelRunningFuture(true)  // Cancel running futures on timeout
            .build();

        return TimeLimiterRegistry.of(config);
    }
}
