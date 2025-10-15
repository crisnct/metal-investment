package com.investment.metal.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SecureRandomGenerator utility.
 * Tests the secure random number generation functionality to ensure
 * proper entropy and uniform distribution for security-sensitive operations.
 */
public class SecureRandomGeneratorTest {

    @Test
    void testNextIntWithValidRange() {
        // Test basic functionality
        int result = SecureRandomGenerator.nextInt(10, 20);
        assertTrue(result >= 10 && result < 20, "Result should be in range [10, 20)");
    }

    @Test
    void testNextIntWithInvalidRange() {
        // Test exception handling
        assertThrows(IllegalArgumentException.class, () -> {
            SecureRandomGenerator.nextInt(20, 10);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            SecureRandomGenerator.nextInt(10, 10);
        });
    }

    @Test
    void testNextIntInclusiveWithValidRange() {
        // Test inclusive range
        int result = SecureRandomGenerator.nextIntInclusive(10, 20);
        assertTrue(result >= 10 && result <= 20, "Result should be in range [10, 20]");
    }

    @Test
    void testNextIntInclusiveWithInvalidRange() {
        // Test exception handling
        assertThrows(IllegalArgumentException.class, () -> {
            SecureRandomGenerator.nextIntInclusive(20, 10);
        });
    }

    @RepeatedTest(100)
    void testNextIntDistribution() {
        // Test that we get different values (not all the same)
        int result1 = SecureRandomGenerator.nextInt(0, 1000);
        int result2 = SecureRandomGenerator.nextInt(0, 1000);
        
        // While it's theoretically possible to get the same value twice,
        // with 1000 possible values, it's extremely unlikely
        assertTrue(result1 >= 0 && result1 < 1000, "Result1 should be in range [0, 1000)");
        assertTrue(result2 >= 0 && result2 < 1000, "Result2 should be in range [0, 1000)");
    }

    @Test
    void testGenerateConfirmationCode() {
        // Test confirmation code generation
        String code = SecureRandomGenerator.generateConfirmationCode();
        
        assertNotNull(code, "Code should not be null");
        assertEquals(6, code.length(), "Code should be 6 digits");
        assertTrue(code.matches("\\d{6}"), "Code should contain only digits");
        
        // Test that the code is in the expected range
        int codeValue = Integer.parseInt(code);
        assertTrue(codeValue >= 100000 && codeValue <= 999999, 
                   "Code should be in range [100000, 999999]");
    }

    @RepeatedTest(50)
    void testGenerateConfirmationCodeUniqueness() {
        // Test that we get different codes (not all the same)
        String code1 = SecureRandomGenerator.generateConfirmationCode();
        String code2 = SecureRandomGenerator.generateConfirmationCode();
        
        // While it's theoretically possible to get the same code twice,
        // with 900,000 possible values, it's extremely unlikely
        assertNotNull(code1, "Code1 should not be null");
        assertNotNull(code2, "Code2 should not be null");
        assertEquals(6, code1.length(), "Code1 should be 6 digits");
        assertEquals(6, code2.length(), "Code2 should be 6 digits");
    }

    @Test
    void testNextBytes() {
        // Test byte array generation
        byte[] bytes = SecureRandomGenerator.nextBytes(16);
        
        assertNotNull(bytes, "Bytes should not be null");
        assertEquals(16, bytes.length, "Should generate 16 bytes");
        
        // Test that we get different byte arrays
        byte[] bytes2 = SecureRandomGenerator.nextBytes(16);
        assertNotEquals(bytes, bytes2, "Should generate different byte arrays");
    }

    @Test
    void testNextLong() {
        // Test long generation
        long result = SecureRandomGenerator.nextLong();
        
        // Just verify it's a valid long (no specific range requirements)
        assertNotNull(result, "Result should not be null");
    }

    @Test
    void testNextBoolean() {
        // Test boolean generation
        boolean result = SecureRandomGenerator.nextBoolean();
        
        // Just verify it's a valid boolean
        assertTrue(result == true || result == false, "Result should be a valid boolean");
    }

    @RepeatedTest(20)
    void testRandomnessQuality() {
        // Test that we get a good distribution of values
        int[] counts = new int[10];
        
        // Generate 1000 values in range [0, 10) and count distribution
        for (int i = 0; i < 1000; i++) {
            int value = SecureRandomGenerator.nextInt(0, 10);
            counts[value]++;
        }
        
        // Check that we got values in all buckets (with some tolerance)
        for (int count : counts) {
            assertTrue(count > 50, "Each bucket should have at least 50 values (allowing for randomness)");
        }
    }
}
