package com.investment.metal;

import com.investment.metal.external.MetalFetchPriceBean;
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
import java.lang.reflect.Constructor;
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

    @Value("${encoder.secrete}")
    private String encoderSecrete;

    @Value("${spring.metal.price.bean}")
    private String metalPriceBean;

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
    public MetalFetchPriceBean createExternalPriceService() {
        try {
            Constructor<?>[] ctors = Class.forName(metalPriceBean).getDeclaredConstructors();
            Constructor<?> ctor = null;
            for (Constructor<?> constructor : ctors) {
                ctor = constructor;
                if (ctor.getGenericParameterTypes().length == 0)
                    break;
            }
            assert ctor != null;
            ctor.setAccessible(true);
            return (MetalFetchPriceBean) ctor.newInstance();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
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