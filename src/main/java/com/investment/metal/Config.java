package com.investment.metal;

import com.investment.metal.common.PriceServiceType;
import com.investment.metal.price.BloombergPriceReader;
import com.investment.metal.price.ExternalMetalPriceReader;
import com.investment.metal.price.GalmarleyPriceReader;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "com.investment.metal")
public class Config implements WebMvcConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @Value("${liquibase.change-log}")
    private String liquibaseChangeLog;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${METAL_INVESTMENT_ENCODER_SECRETE}")
    private String encoderSecrete;

    @Value("${service.metal.price.host}")
    private PriceServiceType servicePriceType;

    @Bean
    public SpringLiquibase liquibase() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(liquibaseChangeLog);
        return liquibase;
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
    public PasswordEncoder encoder() {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(encoderSecrete, 300, 255);
        encoder.setEncodeHashAsBase64(true);
        encoder.setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        return encoder;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
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

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public CallsInterceptor interceptor() {
        return new CallsInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor());
    }
}