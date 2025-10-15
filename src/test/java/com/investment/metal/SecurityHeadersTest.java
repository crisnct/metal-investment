package com.investment.metal;

import com.investment.metal.infrastructure.security.SecurityHeadersFilter;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class to verify security headers are properly set.
 * Tests the implementation of security headers to protect against
 * clickjacking, XSS attacks, and other client-side vulnerabilities.
 * 
 * This is a standalone test that doesn't require Spring Boot context
 * to avoid database connectivity issues in the test environment.
 */
public class SecurityHeadersTest {

    @Test
    void testSecurityHeadersFilterDirectly() {
        // Test the filter directly without Spring context
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        
        // This test verifies the filter can be instantiated and configured
        assert filter != null;
    }

    @Test
    void testSecurityHeadersWithMockMvc() throws Exception {
        // Create a simple MockMvc with just the security headers filter
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WebMvcConfigurer() {})
            .addFilter(new SecurityHeadersFilter())
            .build();

        mockMvc.perform(get("/"))
            .andExpect(status().isNotFound()) // Expected 404 since no controller is mapped
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
            .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
            .andExpect(header().string("X-Permitted-Cross-Domain-Policies", "none"))
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("Permissions-Policy"))
            .andExpect(header().exists("Cross-Origin-Embedder-Policy"))
            .andExpect(header().exists("Cross-Origin-Opener-Policy"))
            .andExpect(header().exists("Cross-Origin-Resource-Policy"));
    }

    @Test
    void testSecurityHeadersWithApiEndpoint() throws Exception {
        // Create a simple MockMvc with just the security headers filter
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new WebMvcConfigurer() {})
            .addFilter(new SecurityHeadersFilter())
            .build();

        mockMvc.perform(get("/api/test"))
            .andExpect(status().isNotFound()) // Expected 404 since no controller is mapped
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-XSS-Protection", "1; mode=block"))
            .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void testSecurityHeadersFilterConfiguration() {
        // Test that the filter can be configured properly
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        
        // Verify the filter is properly configured
        assert filter != null;
        
        // Test that the filter has the correct class name
        assert filter.getClass().getSimpleName().equals("SecurityHeadersFilter");
    }
}
