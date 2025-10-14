package com.investment.metal.config;

import com.investment.metal.common.PriceServiceType;
import com.investment.metal.price.BloombergPriceReader;
import com.investment.metal.price.ExternalMetalPriceReader;
import com.investment.metal.price.GalmarleyPriceReader;
import com.zaxxer.hikari.HikariConfig;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.investment.metal")
public class Config implements WebMvcConfigurer {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${service.metal.price.host}")
  private PriceServiceType servicePriceType;

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public HikariConfig hikariConfig() {
    return new HikariConfig();
  }

  @Bean
  public CircuitBreakerRegistry circuitBreaker() {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .slowCallRateThreshold(50)
        .waitDurationInOpenState(Duration.ofMillis(1000))
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        .permittedNumberOfCallsInHalfOpenState(3)
        .minimumNumberOfCalls(10)
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
        .slidingWindowSize(5)
        .recordExceptions(IOException.class, TimeoutException.class)
        .build();

    return CircuitBreakerRegistry.of(circuitBreakerConfig);
  }

  @Bean
  public BulkheadRegistry bulkhead() {
    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(10)
        .maxWaitDuration(Duration.ofMillis(1))
        .build();
    return BulkheadRegistry.of(config);
  }

  @Bean
  public TimeLimiterRegistry timeLimiter() {
    TimeLimiterConfig config = TimeLimiterConfig.custom()
        .cancelRunningFuture(true)
        .timeoutDuration(Duration.ofMillis(500))
        .build();

    return TimeLimiterRegistry.of(config);
  }

  @Bean
  public ExternalMetalPriceReader createMetalPriceReader() {
    ExternalMetalPriceReader priceService = null;
    switch (servicePriceType) {
      case GALMARLEY:
        priceService = new GalmarleyPriceReader();
        break;
      case BLOOMBERG:
        priceService = new BloombergPriceReader();
        break;
    }
    return priceService;
  }


  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(
        "/token/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/webjars/**"
    );
  }

}
