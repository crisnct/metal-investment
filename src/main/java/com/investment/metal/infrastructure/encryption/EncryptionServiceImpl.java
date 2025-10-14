package com.investment.metal.infrastructure.encryption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Infrastructure implementation of the domain EncryptionService.
 * This class bridges the domain interface with the infrastructure implementation.
 */
@Service
public class EncryptionServiceImpl implements EncryptionService {

    @Autowired
    private ConsistentEncoder consistentEncoder;

    @Override
    public String encrypt(String value) {
        return consistentEncoder.encrypt(value);
    }

    @Override
    public String decrypt(String encryptedValue) {
        return consistentEncoder.decrypt(encryptedValue);
    }
}
