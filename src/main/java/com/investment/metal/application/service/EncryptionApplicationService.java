package com.investment.metal.application.service;

import com.investment.metal.infrastructure.encryption.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Application service for encryption operations.
 * Orchestrates domain services and infrastructure concerns.
 */
@Service
public class EncryptionApplicationService {

    @Autowired
    private EncryptionService encryptionService;

    /**
     * Encrypts a value using the domain encryption service.
     * 
     * @param value the plain text value to encrypt
     * @return the encrypted value
     */
    public String encrypt(String value) {
        return encryptionService.encrypt(value);
    }

    /**
     * Decrypts an encrypted value using the domain encryption service.
     * 
     * @param encryptedValue the encrypted value to decrypt
     * @return the decrypted plain text value
     */
    public String decrypt(String encryptedValue) {
        return encryptionService.decrypt(encryptedValue);
    }
}
