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

    @Test
    void testDeleteAccountMethodExists() {
        // Test that the deleteAccount method exists in ProtectedApiController
        try {
            java.lang.reflect.Method deleteAccountMethod = ProtectedApiController.class.getMethod("deleteAccount", String.class, String.class);
            assertNotNull(deleteAccountMethod, "deleteAccount method should exist");
            assertEquals("deleteAccount", deleteAccountMethod.getName(), "Method name should be deleteAccount");
        } catch (NoSuchMethodException e) {
            fail("deleteAccount method should exist in ProtectedApiController");
        }
    }

    @Test
    void testDeleteAccountMethodSignature() {
        // Test that the deleteAccount method has the correct signature
        try {
            java.lang.reflect.Method deleteAccountMethod = ProtectedApiController.class.getMethod("deleteAccount", String.class, String.class);
            assertEquals(2, deleteAccountMethod.getParameterCount(), "deleteAccount should have 2 parameters");
            assertEquals(String.class, deleteAccountMethod.getParameterTypes()[0], "First parameter should be String (password)");
            assertEquals(String.class, deleteAccountMethod.getParameterTypes()[1], "Second parameter should be String (code)");
        } catch (NoSuchMethodException e) {
            fail("deleteAccount method should exist with correct signature");
        }
    }

    @Test
    void testDeleteAccountMethodReturnType() {
        // Test that the deleteAccount method returns the correct type
        try {
            java.lang.reflect.Method deleteAccountMethod = ProtectedApiController.class.getMethod("deleteAccount", String.class, String.class);
            assertEquals(org.springframework.http.ResponseEntity.class, deleteAccountMethod.getReturnType(), "deleteAccount should return ResponseEntity");
        } catch (NoSuchMethodException e) {
            fail("deleteAccount method should exist with correct return type");
        }
    }

    @Test
    void testControllerInstantiationWithDeleteAccount() {
        // Test that ProtectedApiController can be instantiated and has deleteAccount method
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        
        // Verify the method exists
        try {
            java.lang.reflect.Method deleteAccountMethod = ProtectedApiController.class.getMethod("deleteAccount", String.class, String.class);
            assertNotNull(deleteAccountMethod, "deleteAccount method should exist");
        } catch (NoSuchMethodException e) {
            fail("deleteAccount method should exist in ProtectedApiController");
        }
    }

    @Test
    void testDeleteAccountPreparationMethodExists() {
        // Test that the deleteAccountPreparation method exists in ProtectedApiController
        try {
            java.lang.reflect.Method deleteAccountPreparationMethod = ProtectedApiController.class.getMethod("deleteAccountPreparation");
            assertNotNull(deleteAccountPreparationMethod, "deleteAccountPreparation method should exist");
            assertEquals("deleteAccountPreparation", deleteAccountPreparationMethod.getName(), "Method name should be deleteAccountPreparation");
        } catch (NoSuchMethodException e) {
            fail("deleteAccountPreparation method should exist in ProtectedApiController");
        }
    }

    @Test
    void testDeleteAccountPreparationMethodSignature() {
        // Test that the deleteAccountPreparation method has the correct signature
        try {
            java.lang.reflect.Method deleteAccountPreparationMethod = ProtectedApiController.class.getMethod("deleteAccountPreparation");
            assertEquals(0, deleteAccountPreparationMethod.getParameterCount(), "deleteAccountPreparation should have 0 parameters");
        } catch (NoSuchMethodException e) {
            fail("deleteAccountPreparation method should exist with correct signature");
        }
    }

    @Test
    void testDeleteAccountPreparationMethodReturnType() {
        // Test that the deleteAccountPreparation method returns the correct type
        try {
            java.lang.reflect.Method deleteAccountPreparationMethod = ProtectedApiController.class.getMethod("deleteAccountPreparation");
            assertEquals(org.springframework.http.ResponseEntity.class, deleteAccountPreparationMethod.getReturnType(), "deleteAccountPreparation should return ResponseEntity");
        } catch (NoSuchMethodException e) {
            fail("deleteAccountPreparation method should exist with correct return type");
        }
    }

    @Test
    void testControllerInstantiationWithDeleteAccountPreparation() {
        // Test that ProtectedApiController can be instantiated and has deleteAccountPreparation method
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
        
        // Verify the method exists
        try {
            java.lang.reflect.Method deleteAccountPreparationMethod = ProtectedApiController.class.getMethod("deleteAccountPreparation");
            assertNotNull(deleteAccountPreparationMethod, "deleteAccountPreparation method should exist");
        } catch (NoSuchMethodException e) {
            fail("deleteAccountPreparation method should exist in ProtectedApiController");
        }
    }
}
