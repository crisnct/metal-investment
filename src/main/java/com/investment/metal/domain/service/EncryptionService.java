package com.investment.metal.domain.service;

/**
 * Domain service interface for encryption operations.
 * This interface defines the contract for encryption/decryption operations
 * from a business perspective, independent of implementation details.
 */
public interface EncryptionService {

    /**
     * Encrypts the given value using the configured encryption strategy.
     * 
     * @param value the plain text value to encrypt
     * @return the encrypted value, or null if encryption fails
     */
    String encrypt(String value);

    /**
     * Decrypts the given encrypted value using the configured decryption strategy.
     * 
     * @param encryptedValue the encrypted value to decrypt
     * @return the decrypted plain text value, or null if decryption fails
     */
    String decrypt(String encryptedValue);
}
