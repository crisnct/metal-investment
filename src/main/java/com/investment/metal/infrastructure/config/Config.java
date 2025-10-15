package com.investment.metal.infrastructure.config;

import com.investment.metal.domain.model.PriceServiceType;
import com.investment.metal.infrastructure.service.price.BloombergPriceReader;
import com.investment.metal.infrastructure.service.price.ExternalMetalPriceReader;
import com.investment.metal.infrastructure.service.price.GalmarleyPriceReader;
import com.zaxxer.hikari.HikariConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
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
