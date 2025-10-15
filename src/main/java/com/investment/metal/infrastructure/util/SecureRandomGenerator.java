package com.investment.metal.infrastructure.util;

import java.security.SecureRandom;

/**
 * Secure random number generator utility for cryptographic operations.
 * Provides cryptographically secure random number generation to prevent
 * predictable patterns and ensure proper entropy for security-sensitive operations.
 * 
 * This class addresses the WEAK RANDOM NUMBER GENERATION vulnerability by
 * replacing Math.abs(Random.nextInt()) patterns with secure alternatives.
 */
public class SecureRandomGenerator {
    
    /**
     * Thread-safe SecureRandom instance for generating cryptographically secure random numbers.
     * Uses the default SecureRandom implementation which provides high-quality entropy.
     */
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate a secure random integer within the specified range [min, max).
     * This method provides uniform distribution without bias, addressing the
     * Math.abs(Random.nextInt()) vulnerability.
     * 
     * @param min the minimum value (inclusive)
     * @param max the maximum value (exclusive)
     * @return a secure random integer in the range [min, max)
     * @throws IllegalArgumentException if min >= max
     */
    public static int nextInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("min must be less than max");
        }
        
        int range = max - min;
        if (range <= 0) {
            throw new IllegalArgumentException("range must be positive");
        }
        
        // Use SecureRandom.nextInt(bound) for uniform distribution
        return min + secureRandom.nextInt(range);
    }
    
    /**
     * Generate a secure random integer within the specified range [min, max].
     * This method provides uniform distribution without bias, addressing the
     * Math.abs(Random.nextInt()) vulnerability.
     * 
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a secure random integer in the range [min, max]
     * @throws IllegalArgumentException if min > max
     */
    public static int nextIntInclusive(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be less than or equal to max");
        }
        
        return nextInt(min, max + 1);
    }
    
    /**
     * Generate a secure random confirmation code for account operations.
     * Creates a 6-digit code with uniform distribution for security-sensitive operations.
     * 
     * @return a secure random 6-digit confirmation code as String
     */
    public static String generateConfirmationCode() {
        return String.valueOf(nextIntInclusive(100000, 999999));
    }
    
    /**
     * Generate secure random bytes for cryptographic operations.
     * 
     * @param length the number of bytes to generate
     * @return an array of secure random bytes
     */
    public static byte[] nextBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generate a secure random long value.
     * 
     * @return a secure random long value
     */
    public static long nextLong() {
        return secureRandom.nextLong();
    }
    
    /**
     * Generate a secure random boolean value.
     * 
     * @return a secure random boolean value
     */
    public static boolean nextBoolean() {
        return secureRandom.nextBoolean();
    }
}
