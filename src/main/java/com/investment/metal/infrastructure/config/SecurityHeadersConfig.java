package com.investment.metal.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration for security headers and content security policy.
 * Provides additional security measures beyond the filter-based approach.
 */
@Configuration
public class SecurityHeadersConfig implements WebMvcConfigurer {

    @Value("${security.headers.x-frame-options:DENY}")
    private String xFrameOptions;

    @Value("${security.headers.x-content-type-options:nosniff}")
    private String xContentTypeOptions;

    @Value("${security.headers.x-xss-protection:1; mode=block}")
    private String xXssProtection;

    @Value("${security.headers.strict-transport-security:max-age=31536000; includeSubDomains; preload}")
    private String strictTransportSecurity;

    @Value("${security.headers.referrer-policy:strict-origin-when-cross-origin}")
    private String referrerPolicy;

    @Value("${security.headers.x-permitted-cross-domain-policies:none}")
    private String xPermittedCrossDomainPolicies;

    @Value("${security.headers.permissions-policy:geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()}")
    private String permissionsPolicy;

    @Value("${security.headers.cross-origin-embedder-policy:require-corp}")
    private String crossOriginEmbedderPolicy;

    @Value("${security.headers.cross-origin-opener-policy:same-origin}")
    private String crossOriginOpenerPolicy;

    @Value("${security.headers.cross-origin-resource-policy:same-origin}")
    private String crossOriginResourcePolicy;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityHeadersInterceptor());
    }

    /**
     * Interceptor to add security headers to all responses.
     */
    private class SecurityHeadersInterceptor implements HandlerInterceptor {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // Add security headers to all responses
            addSecurityHeaders(request, response);
            return true;
        }

        private void addSecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
            // X-Frame-Options: Prevent clickjacking
            response.setHeader("X-Frame-Options", xFrameOptions);
            
            // X-Content-Type-Options: Prevent MIME sniffing
            response.setHeader("X-Content-Type-Options", xContentTypeOptions);
            
            // X-XSS-Protection: Enable XSS filtering
            response.setHeader("X-XSS-Protection", xXssProtection);
            
            // Strict-Transport-Security: Force HTTPS (only for secure connections)
            if (request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))) {
                response.setHeader("Strict-Transport-Security", strictTransportSecurity);
            }
            
            // Referrer-Policy: Control referrer information
            response.setHeader("Referrer-Policy", referrerPolicy);
            
            // X-Permitted-Cross-Domain-Policies: Restrict cross-domain policies
            response.setHeader("X-Permitted-Cross-Domain-Policies", xPermittedCrossDomainPolicies);
            
            // Permissions-Policy: Control browser features
            response.setHeader("Permissions-Policy", permissionsPolicy);
            
            // Cross-Origin-Embedder-Policy: Prevent cross-origin embedding
            response.setHeader("Cross-Origin-Embedder-Policy", crossOriginEmbedderPolicy);
            
            // Cross-Origin-Opener-Policy: Isolate browsing context
            response.setHeader("Cross-Origin-Opener-Policy", crossOriginOpenerPolicy);
            
            // Cross-Origin-Resource-Policy: Control cross-origin resource access
            response.setHeader("Cross-Origin-Resource-Policy", crossOriginResourcePolicy);
            
            // Content-Security-Policy: Comprehensive XSS protection
            String csp = buildContentSecurityPolicy(request);
            response.setHeader("Content-Security-Policy", csp);
        }

        private String buildContentSecurityPolicy(HttpServletRequest request) {
            StringBuilder csp = new StringBuilder();
            
            // Default source restrictions
            csp.append("default-src 'self'");
            
            // Script sources - allow self and inline scripts for React
            csp.append("; script-src 'self' 'unsafe-inline' 'unsafe-eval'");
            
            // Style sources - allow self and inline styles for React
            csp.append("; style-src 'self' 'unsafe-inline'");
            
            // Image sources - allow self and data URIs
            csp.append("; img-src 'self' data: https:");
            
            // Font sources - allow self
            csp.append("; font-src 'self'");
            
            // Connect sources - allow self and API endpoints
            csp.append("; connect-src 'self'");
            
            // Object sources - deny all
            csp.append("; object-src 'none'");
            
            // Base URI - restrict to self
            csp.append("; base-uri 'self'");
            
            // Form action - restrict to self
            csp.append("; form-action 'self'");
            
            // Frame ancestors - deny all
            csp.append("; frame-ancestors 'none'");
            
            // Upgrade insecure requests
            csp.append("; upgrade-insecure-requests");
            
            return csp.toString();
        }
    }
}
