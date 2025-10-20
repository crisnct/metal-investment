package com.investment.metal.infrastructure.config;

import com.investment.metal.infrastructure.security.AuthenticationFilter;
import com.investment.metal.infrastructure.security.CustomAuthenticationProvider;
import com.investment.metal.infrastructure.security.SecurityHeadersFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration following Single Responsibility Principle.
 * Handles only authentication and authorization concerns.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Using string patterns directly in security configuration to avoid deprecation warnings

    @Value("${METAL_INVESTMENT_ENCODER_SECRETE}")
    private String encoderSecret;

    @Value("${metalinvestment.cors.allowed-origins:http://localhost:3000,https://metal-investment-635786220311.europe-west1.run.app}")
    private String allowedOriginsConfig;

    @Value("${metalinvestment.csrf.cookie-secure:true}")
    private boolean csrfCookieSecure;

    @Bean
    public PasswordEncoder passwordEncoder() {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(encoderSecret, 16, 255, SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        encoder.setEncodeHashAsBase64(true);
        encoder.setAlgorithm(SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
        return encoder;
    }

    // CustomAuthenticationProvider is discovered as a @Component

    @Value("${metalinvestment.csrf.cookie-samesite:Strict}")
    private String csrfCookieSameSite;

    @Bean
    public CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setParameterName("_csrf");
        repository.setCookieCustomizer(cookie -> {
            cookie.path("/");
            cookie.sameSite(csrfCookieSameSite);
            boolean secureFlag = csrfCookieSecure || "None".equalsIgnoreCase(csrfCookieSameSite);
            cookie.secure(secureFlag);
        });
        return repository;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CustomAuthenticationProvider customAuthenticationProvider,
            SecurityHeadersFilter securityHeadersFilter,
            CookieCsrfTokenRepository csrfTokenRepository) throws Exception {
        // Create AuthenticationFilter directly in the filter chain to avoid circular dependency
        AuthenticationFilter authFilter = new AuthenticationFilter(request -> {
            String uri = request.getRequestURI();
            String contextPath = request.getContextPath() != null ? request.getContextPath() : "";
            String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
            return path.startsWith("/api/private");
        });
        AuthenticationManager localAuthManager = new ProviderManager(customAuthenticationProvider);
        authFilter.setAuthenticationManager(localAuthManager);
        
        // SECURITY FIX: Configure CSRF for stateless JWT authentication
        // Use custom CSRF token repository that works with stateless sessions
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler(requestHandler)
                .csrfTokenRepository(csrfTokenRepository)
                .ignoringRequestMatchers("/api/public/**")
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            .addFilterBefore(securityHeadersFilter, AnonymousAuthenticationFilter.class)
            .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/public/csrf-token").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/ui-banner").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/private/**").authenticated()
                .anyRequest().permitAll()
            );
        
        return http.build();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            String errorMessage = "Access denied";
            if (accessDeniedException instanceof InvalidCsrfTokenException) {
                errorMessage = "Invalid CSRF token. Please refresh the page and try again.";
            } else if (accessDeniedException instanceof MissingCsrfTokenException) {
                errorMessage = "Missing CSRF token. Please refresh the page and try again.";
            }
            
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"" + errorMessage + "\",\"type\":\"" + accessDeniedException.getClass().getSimpleName() + "\"}");
        };
    }
}
