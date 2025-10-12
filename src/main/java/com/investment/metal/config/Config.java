package com.investment.metal.config;

import com.investment.metal.common.PriceServiceType;
import com.investment.metal.price.BloombergPriceReader;
import com.investment.metal.price.ExternalMetalPriceReader;
import com.investment.metal.price.GalmarleyPriceReader;
import com.investment.metal.security.AuthenticationFilter;
import com.investment.metal.security.CustomAuthenticationProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@EnableJpaRepositories(basePackages = "com.investment.metal.database")
@EnableWebSecurity
@ComponentScan(basePackages = "com.investment.metal")
public class Config implements WebMvcConfigurer {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  private static final RequestMatcher PROTECTED_URLS = new OrRequestMatcher(
      new AntPathRequestMatcher("/api/**")
  );

  @Value("${METAL_INVESTMENT_ENCODER_SECRETE}")
  private String encoderSecrete;


  @Value("${service.metal.price.host}")
  private PriceServiceType servicePriceType;

  @Bean(name = "entityManagerFactory")
  @DependsOn("liquibaseChangelog")
  @Primary
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("com.investment.metal.database");

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
    vendorAdapter.setShowSql(false);
    vendorAdapter.setGenerateDdl(false);

    em.setJpaVendorAdapter(vendorAdapter);
    em.setPersistenceUnitName("default");

    // Set Hibernate properties to completely disable XML mapping and JAXB
    Properties jpaProperties = new Properties();
    jpaProperties.setProperty("hibernate.hbm2ddl.auto", "validate");
    jpaProperties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
    jpaProperties.setProperty("hibernate.jdbc.time_zone", "UTC");
    jpaProperties.setProperty("hibernate.format_sql", "false");
    jpaProperties.setProperty("hibernate.show_sql", "false");
    jpaProperties.setProperty("hibernate.use_sql_comments", "false");
    jpaProperties.setProperty("hibernate.generate_statistics", "false");
    jpaProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
    jpaProperties.setProperty("hibernate.cache.use_query_cache", "false");

    // Completely disable XML mapping and JAXB
    jpaProperties.setProperty("hibernate.xml_mapping_enabled", "false");
    jpaProperties.setProperty("hibernate.jaxb.enabled", "false");
    jpaProperties.setProperty("hibernate.hbm2ddl.import_files", "");
    jpaProperties.setProperty("hibernate.hbm2ddl.import_files_sql_extractor", "");

    // Configure naming strategies for camelCase to snake_case conversion
    jpaProperties.setProperty("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
    jpaProperties.setProperty("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl");

    em.setJpaProperties(jpaProperties);

    return em;
  }

  @Bean
  public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
    return transactionManager;
  }

  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public HikariConfig hikariConfig() {
    return new HikariConfig();
  }

  @Bean
  @Primary
  @ConditionalOnMissingBean
  public DataSource dataSource(HikariConfig hikariConfig) {
    hikariConfig.setJdbcUrl(dbUrl);
    hikariConfig.setUsername(username);
    hikariConfig.setPassword(password);
    hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
    hikariConfig.setAutoCommit(false);
    return new HikariDataSource(hikariConfig);
  }

  // JPA and Liquibase configuration is handled by Spring Boot auto-configuration

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
    Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(encoderSecrete, 16, 255, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    encoder.setEncodeHashAsBase64(true);
    encoder.setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    return encoder;
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


//  @Bean
//  public CallsInterceptor interceptor() {
//    return new CallsInterceptor();
//  }
//
//  @Override
//  public void addInterceptors(InterceptorRegistry registry) {
//    registry.addInterceptor(interceptor());
//  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/token/**");
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationProvider authenticationProvider, AuthenticationFilter authenticationFilter) throws Exception {
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/static/**",
                "/favicon.ico",
                "/actuator/health",
                "/actuator/health/**",
                "/actuator/info",
                "/actuator/**",
                "/swagger-ui",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-ui.html/**",
                "/api-docs",
                "/api-docs/**",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources",
                "/swagger-resources/**",
                "/webjars/**",
                "/swagger-ui/index.html",
                "/swagger-ui/index.html/**",
                "/userRegistration",
                "/validateAccount",
                "/login",
                "/resetPassword",
                "/changePassword",
                "/health",
                "/test-static",
                "/test",
                "/swagger-test",
                "/debug-swagger"
                ).permitAll()
            .requestMatchers(PROTECTED_URLS).authenticated()
        )
        .exceptionHandling(exception -> exception.authenticationEntryPoint(forbiddenEntryPoint()))
        .addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class)
        .authenticationProvider(authenticationProvider)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public AuthenticationFilter authenticationFilter(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    final AuthenticationFilter filter = new AuthenticationFilter(PROTECTED_URLS);
    filter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
    return filter;
  }

  @Bean
  public AuthenticationEntryPoint forbiddenEntryPoint() {
    return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
  }
}
