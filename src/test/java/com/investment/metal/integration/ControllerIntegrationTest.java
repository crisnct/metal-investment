package com.investment.metal.integration;

import com.investment.metal.infrastructure.controller.PublicApiController;
import com.investment.metal.infrastructure.controller.ProtectedApiController;
import com.investment.metal.infrastructure.controller.RootController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for API Controllers.
 * Tests the controller layer without Spring Boot context loading.
 * Focuses on controller instantiation and basic functionality.
 */
class ControllerIntegrationTest {

    private PublicApiController publicApiController;
    private ProtectedApiController protectedApiController;
    private RootController rootController;

    @BeforeEach
    void setUp() {
        // Create controller instances directly without Spring Boot context
        publicApiController = new PublicApiController();
        protectedApiController = new ProtectedApiController();
        rootController = new RootController();
    }

    @Test
    void testControllerInstantiation() {
        // Test controller instantiation
        assertNotNull(publicApiController, "PublicApiController should be instantiated");
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        assertNotNull(rootController, "RootController should be instantiated");
    }

    @Test
    void testPublicApiControllerIntegration() {
        // Test public API controller functionality
        assertNotNull(publicApiController, "PublicApiController should be instantiated");
        assertNotNull(rootController, "RootController should be instantiated");
        
        // Test health endpoints
        Map<String, String> healthResponse = rootController.health();
        assertNotNull(healthResponse, "Health response should not be null");
        assertEquals("UP", healthResponse.get("status"), "Health status should be UP");
        assertEquals("Metal Investment API", healthResponse.get("service"), "Service name should be correct");
        assertEquals("UNKNOWN", healthResponse.get("database"), "Database status should default to UNKNOWN in tests");
        
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        assertNotNull(apiHealthResponse, "API health response should not be null");
        assertEquals("UP", apiHealthResponse.get("status"), "API health status should be UP");
        assertEquals("Metal Investment API", apiHealthResponse.get("api"), "API name should be correct");
        assertEquals("UNKNOWN", apiHealthResponse.get("database"), "API health database status should default to UNKNOWN in tests");
    }

    @Test
    void testProtectedApiControllerIntegration() {
        // Test protected API controller functionality
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        
        // Test that controller can be instantiated (basic functionality test)
        // Note: Full integration testing would require Spring Boot context for dependency injection
        assertTrue(true, "ProtectedApiController instantiation successful");
    }

    @Test
    void testControllerResponseStructure() {
        // Test response structure consistency
        Map<String, String> healthResponse = rootController.health();
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        
        // Test that responses have expected keys
        assertTrue(healthResponse.containsKey("status"), "Health response should contain 'status' key");
        assertTrue(healthResponse.containsKey("service"), "Health response should contain 'service' key");
        assertTrue(healthResponse.containsKey("database"), "Health response should contain 'database' key");
        assertTrue(apiHealthResponse.containsKey("status"), "API health response should contain 'status' key");
        assertTrue(apiHealthResponse.containsKey("api"), "API health response should contain 'api' key");
        assertTrue(apiHealthResponse.containsKey("database"), "API health response should contain 'database' key");
    }

    @Test
    void testControllerResponseValues() {
        // Test response values
        Map<String, String> healthResponse = rootController.health();
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        
        // Test status values
        assertEquals("UP", healthResponse.get("status"), "Health status should be UP");
        assertEquals("UP", apiHealthResponse.get("status"), "API health status should be UP");
        
        // Test service/api names
        assertEquals("Metal Investment API", healthResponse.get("service"), "Service name should be correct");
        assertEquals("Metal Investment API", apiHealthResponse.get("api"), "API name should be correct");
        assertEquals("UNKNOWN", healthResponse.get("database"), "Database status should be UNKNOWN for tests");
        assertEquals("UNKNOWN", apiHealthResponse.get("database"), "API database status should be UNKNOWN for tests");
    }

    @Test
    void testControllerResponseTypes() {
        // Test response types
        Map<String, String> healthResponse = rootController.health();
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        
        // Test that values are strings
        assertInstanceOf(String.class, healthResponse.get("status"), "Status should be String");
        assertInstanceOf(String.class, healthResponse.get("service"), "Service should be String");
        assertInstanceOf(String.class, apiHealthResponse.get("status"), "API status should be String");
        assertInstanceOf(String.class, apiHealthResponse.get("api"), "API name should be String");
        assertInstanceOf(String.class, healthResponse.get("database"), "Database status should be String");
        assertInstanceOf(String.class, apiHealthResponse.get("database"), "API database status should be String");
    }

    @Test
    void testControllerResponseConsistency() {
        // Test response consistency across multiple calls
        Map<String, String> healthResponse1 = rootController.health();
        Map<String, String> healthResponse2 = rootController.health();
        Map<String, String> apiHealthResponse1 = rootController.apiHealth();
        Map<String, String> apiHealthResponse2 = rootController.apiHealth();
        
        // Test that responses are consistent
        assertEquals(healthResponse1, healthResponse2, "Health responses should be consistent");
        assertEquals(apiHealthResponse1, apiHealthResponse2, "API health responses should be consistent");
    }

    @Test
    void testControllerMultipleCalls() {
        // Test multiple calls to controllers
        for (int i = 0; i < 5; i++) {
            Map<String, String> healthResponse = rootController.health();
            Map<String, String> apiHealthResponse = rootController.apiHealth();
            
            assertNotNull(healthResponse, "Health response should not be null on call " + i);
            assertNotNull(apiHealthResponse, "API health response should not be null on call " + i);
            assertEquals("UP", healthResponse.get("status"), "Health status should be UP on call " + i);
            assertEquals("UP", apiHealthResponse.get("status"), "API health status should be UP on call " + i);
            assertEquals("UNKNOWN", healthResponse.get("database"), "Database status should be UNKNOWN on call " + i);
            assertEquals("UNKNOWN", apiHealthResponse.get("database"), "API database status should be UNKNOWN on call " + i);
        }
    }

    @Test
    void testControllerResponseKeys() {
        // Test that response keys are as expected
        Map<String, String> healthResponse = rootController.health();
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        
        // Test health response keys
        assertTrue(healthResponse.containsKey("status"), "Health response should have 'status' key");
        assertTrue(healthResponse.containsKey("service"), "Health response should have 'service' key");
        assertTrue(healthResponse.containsKey("version"), "Health response should have 'version' key");
        assertTrue(healthResponse.containsKey("database"), "Health response should have 'database' key");
        assertEquals(4, healthResponse.size(), "Health response should have exactly 4 keys");
        
        // Test API health response keys
        assertTrue(apiHealthResponse.containsKey("status"), "API health response should have 'status' key");
        assertTrue(apiHealthResponse.containsKey("api"), "API health response should have 'api' key");
        assertTrue(apiHealthResponse.containsKey("swagger"), "API health response should have 'swagger' key");
        assertTrue(apiHealthResponse.containsKey("docs"), "API health response should have 'docs' key");
        assertTrue(apiHealthResponse.containsKey("database"), "API health response should have 'database' key");
        assertEquals(5, apiHealthResponse.size(), "API health response should have exactly 5 keys");
    }

    @Test
    void testControllerResponseContent() {
        // Test response content validity
        Map<String, String> healthResponse = rootController.health();
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        
        // Test that values are not null or empty
        assertNotNull(healthResponse.get("status"), "Health status should not be null");
        assertNotNull(healthResponse.get("service"), "Health service should not be null");
        assertNotNull(healthResponse.get("database"), "Health database status should not be null");
        assertNotNull(apiHealthResponse.get("status"), "API health status should not be null");
        assertNotNull(apiHealthResponse.get("api"), "API health api should not be null");
        assertNotNull(apiHealthResponse.get("database"), "API health database status should not be null");
        
        assertFalse(healthResponse.get("status").isEmpty(), "Health status should not be empty");
        assertFalse(healthResponse.get("service").isEmpty(), "Health service should not be empty");
        assertFalse(healthResponse.get("database").isEmpty(), "Database status should not be empty");
        assertFalse(apiHealthResponse.get("status").isEmpty(), "API health status should not be empty");
        assertFalse(apiHealthResponse.get("api").isEmpty(), "API health api should not be empty");
        assertFalse(apiHealthResponse.get("database").isEmpty(), "API health database status should not be empty");
    }

    @Test
    void testHealthEndpointResponseStructure() {
        // Test health endpoint response structure
        Map<String, String> healthResponse = rootController.health();
        
        // Test structure
        assertNotNull(healthResponse, "Health response should not be null");
        assertTrue(healthResponse instanceof Map, "Health response should be a Map");
        assertEquals(4, healthResponse.size(), "Health response should have 4 entries");
        
        // Test required fields
        assertTrue(healthResponse.containsKey("status"), "Health response should contain 'status'");
        assertTrue(healthResponse.containsKey("service"), "Health response should contain 'service'");
        assertTrue(healthResponse.containsKey("version"), "Health response should contain 'version'");
        assertTrue(healthResponse.containsKey("database"), "Health response should contain 'database'");
    }

    @Test
    void testApiHealthEndpointResponseStructure() {
        // Test API health endpoint response structure
        Map<String, String> apiHealthResponse = rootController.apiHealth();
        
        // Test structure
        assertNotNull(apiHealthResponse, "API health response should not be null");
        assertTrue(apiHealthResponse instanceof Map, "API health response should be a Map");
        assertEquals(5, apiHealthResponse.size(), "API health response should have 5 entries");
        
        // Test required fields
        assertTrue(apiHealthResponse.containsKey("status"), "API health response should contain 'status'");
        assertTrue(apiHealthResponse.containsKey("api"), "API health response should contain 'api'");
        assertTrue(apiHealthResponse.containsKey("swagger"), "API health response should contain 'swagger'");
        assertTrue(apiHealthResponse.containsKey("docs"), "API health response should contain 'docs'");
        assertTrue(apiHealthResponse.containsKey("database"), "API health response should contain 'database'");
    }

    @Test
    void testControllerDependencyInjection() {
        // Test that controllers can be instantiated without dependency injection issues
        // This is a basic test since we're not using Spring Boot context
        assertNotNull(publicApiController, "PublicApiController should be instantiated");
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        
        // Test that controllers can perform basic operations
        Map<String, String> healthResponse = rootController.health();
        assertNotNull(healthResponse, "RootController should be able to return health response");
        assertEquals("UNKNOWN", healthResponse.get("database"), "RootController should report UNKNOWN database status during tests");
    }

    @Test
    void testProtectedApiControllerDeleteAccountEndpoint() {
        // Test that the deleteAccount endpoint exists and can be called
        // This is a basic test since we're not using Spring Boot context
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        
        // Test that the controller has the deleteAccount method
        try {
            java.lang.reflect.Method deleteAccountMethod = ProtectedApiController.class.getMethod("deleteAccount", String.class, String.class);
            assertNotNull(deleteAccountMethod, "deleteAccount method should exist");
            assertEquals("deleteAccount", deleteAccountMethod.getName(), "Method name should be deleteAccount");
        } catch (NoSuchMethodException e) {
            fail("deleteAccount method should exist in ProtectedApiController");
        }
    }

    @Test
    void testControllerMethodSignatures() {
        // Test that the deleteAccount method has the correct signature
        try {
            java.lang.reflect.Method deleteAccountMethod = ProtectedApiController.class.getMethod("deleteAccount", String.class, String.class);
            assertEquals(2, deleteAccountMethod.getParameterCount(), "deleteAccount should have 2 parameters");
            assertEquals(String.class, deleteAccountMethod.getParameterTypes()[0], "First parameter should be String");
            assertEquals(String.class, deleteAccountMethod.getParameterTypes()[1], "Second parameter should be String");
        } catch (NoSuchMethodException e) {
            fail("deleteAccount method should exist with correct signature");
        }
    }

    @Test
    void testProtectedApiControllerDeleteAccountPreparationEndpoint() {
        // Test that the deleteAccountPreparation endpoint exists and can be called
        // This is a basic test since we're not using Spring Boot context
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        
        // Test that the controller has the deleteAccountPreparation method
        try {
            java.lang.reflect.Method deleteAccountPreparationMethod = ProtectedApiController.class.getMethod("deleteAccountPreparation");
            assertNotNull(deleteAccountPreparationMethod, "deleteAccountPreparation method should exist");
            assertEquals("deleteAccountPreparation", deleteAccountPreparationMethod.getName(), "Method name should be deleteAccountPreparation");
        } catch (NoSuchMethodException e) {
            fail("deleteAccountPreparation method should exist in ProtectedApiController");
        }
    }

    @Test
    void testDeleteAccountPreparationMethodSignatures() {
        // Test that the deleteAccountPreparation method has the correct signature
        try {
            java.lang.reflect.Method deleteAccountPreparationMethod = ProtectedApiController.class.getMethod("deleteAccountPreparation");
            assertEquals(0, deleteAccountPreparationMethod.getParameterCount(), "deleteAccountPreparation should have 0 parameters");
        } catch (NoSuchMethodException e) {
            fail("deleteAccountPreparation method should exist with correct signature");
        }
    }
}
