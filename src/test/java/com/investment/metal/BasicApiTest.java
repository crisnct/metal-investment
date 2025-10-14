package com.investment.metal;

import com.investment.metal.infrastructure.controller.PublicApiController;
import com.investment.metal.infrastructure.controller.ProtectedApiController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic API tests for Metal Investment application.
 * Tests the main API endpoints with basic functionality.
 * Uses JUnit 5 for testing.
 */
@ExtendWith(MockitoExtension.class)
class BasicApiTest {

    private PublicApiController publicApiController = new PublicApiController();
    private ProtectedApiController protectedApiController = new ProtectedApiController();

    @Test
    void testHealthCheck() {
        // When
        Map<String, String> response = publicApiController.health();

        // Then
        assertNotNull(response);
        assertEquals("UP", response.get("status"));
        assertEquals("Metal Investment API", response.get("service"));
    }

    @Test
    void testApiHealthCheck() {
        // When
        Map<String, String> response = publicApiController.apiHealth();

        // Then
        assertNotNull(response);
        assertEquals("UP", response.get("status"));
        assertEquals("Metal Investment API", response.get("api"));
    }

    @Test
    void testControllersNotNull() {
        // Then
        assertNotNull(publicApiController, "PublicApiController should not be null");
        assertNotNull(protectedApiController, "ProtectedApiController should not be null");
    }

    @Test
    void testResponseStructure() {
        // When
        Map<String, String> response = publicApiController.health();

        // Then
        assertNotNull(response, "Response should not be null");
        assertTrue(response.containsKey("status"), "Response should contain 'status' key");
        assertTrue(response.containsKey("service"), "Response should contain 'service' key");
        assertNotNull(response.get("status"), "Status should not be null");
        assertNotNull(response.get("service"), "Service should not be null");
    }

    @Test
    void testResponseContent() {
        // When
        Map<String, String> healthResponse = publicApiController.health();
        Map<String, String> apiHealthResponse = publicApiController.apiHealth();

        // Then
        assertEquals("UP", healthResponse.get("status"), "Health status should be UP");
        assertEquals("UP", apiHealthResponse.get("status"), "API health status should be UP");
        assertTrue(healthResponse.get("service").contains("Metal Investment"), 
                  "Service name should contain 'Metal Investment'");
        assertTrue(apiHealthResponse.get("api").contains("Metal Investment"), 
                  "API name should contain 'Metal Investment'");
    }

    @Test
    void testResponseKeys() {
        // When
        Map<String, String> response = publicApiController.health();

        // Then
        assertTrue(response.size() >= 2, "Response should have at least 2 keys");
        assertTrue(response.containsKey("status"), "Response should contain 'status' key");
        assertTrue(response.containsKey("service"), "Response should contain 'service' key");
    }

    @Test
    void testResponseBodyType() {
        // When
        Map<String, String> response = publicApiController.health();

        // Then
        assertTrue(response instanceof Map, "Response should be of type Map");
        assertTrue(response instanceof java.util.HashMap, "Response should be of type HashMap");
    }

    @Test
    void testMultipleHealthCalls() {
        // When - call health endpoint multiple times
        Map<String, String> response1 = publicApiController.health();
        Map<String, String> response2 = publicApiController.health();
        Map<String, String> response3 = publicApiController.apiHealth();

        // Then - all should return the same result
        assertEquals(response1.get("status"), response2.get("status"));
        assertEquals(response1.get("service"), response2.get("service"));
        assertEquals("UP", response3.get("status"));
    }

    @Test
    void testControllerInstantiation() {
        // When - create new instances
        PublicApiController newPublicController = new PublicApiController();
        ProtectedApiController newProtectedController = new ProtectedApiController();

        // Then
        assertNotNull(newPublicController, "New PublicApiController should not be null");
        assertNotNull(newProtectedController, "New ProtectedApiController should not be null");
    }

    @Test
    void testBasicFunctionality() {
        // Test that basic controller methods can be called without exceptions
        assertDoesNotThrow(() -> {
            publicApiController.health();
            publicApiController.apiHealth();
        }, "Basic controller methods should not throw exceptions");
    }
}
