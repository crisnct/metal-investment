package com.investment.metal.infrastructure.encryption;

import com.investment.metal.infrastructure.util.SecureRandomGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the key file for MultipleKeysEncoder
 *
 * @author cristian.tone
 */
class KeyGenerator {

    public static final String SYMBOLS = "v$ ?J<#uy,'9Ak6hFVW(1nLb[2`gQ>Yt!GP^\"OI}lMri-_\\USd4{]R/KZHE)fsX.=|N*DBpw%5~qe@x&3c:+;z0TCo78maj"; //$NON-NLS-1$

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyGenerator.class);

    private final int keySize;

    public KeyGenerator(int keySize) {
        super();
        this.keySize = keySize;
    }

    public static void main(String[] args) {
        KeyGenerator gen = new KeyGenerator(255);
        gen.generateKey(new File("metal-investment.key"));
        LOGGER.info("key was created");
    }

    public void generateKey(File file) {
        AESEncryptor aesEncryptor = new AESEncryptor(StandardCharsets.UTF_8);
        // SECURITY FIX: Use secure key generation instead of hardcoded key
        String secureKey = getSecureKey();
        aesEncryptor.setKey(secureKey);

        StringBuilder key = new StringBuilder();
        // lines
        for (int line = 1; line <= this.keySize; line++) {
            if (line != 1) {
                key.append("\n");
            }
            key.append(this.randomSymbols());
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(aesEncryptor.encrypt(key.toString()));
            writer.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private String randomSymbols() {
        StringBuilder strReturn = new StringBuilder();
        for (int i = 0; i < KeyGenerator.SYMBOLS.length(); i++) {
            // SECURITY FIX: Use secure random generation instead of Math.random()
            // This prevents predictable patterns and ensures uniform distribution
            int insertPosition = SecureRandomGenerator.nextInt(0, strReturn.length() + 1);
            strReturn.insert(insertPosition, KeyGenerator.SYMBOLS.charAt(i));
        }
        return strReturn.toString();
    }
    
    /**
     * Get a secure AES key for encryption.
     * This method generates a cryptographically secure random key.
     * 
     * @return a secure AES key
     */
    private String getSecureKey() {
        // Check for environment variable first
        String envKey = System.getenv("METAL_INVESTMENT_AES_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            LOGGER.info("Using AES key from environment variable");
            return envKey;
        }
        
        // Generate a secure random key
        LOGGER.warn("No AES key found in environment variable METAL_INVESTMENT_AES_KEY. " +
                   "Generating a new key. This should be set in production!");
        
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256-bit key
        random.nextBytes(keyBytes);
        String generatedKey = Base64.getEncoder().encodeToString(keyBytes);
        
        LOGGER.warn("Generated AES key: {}. Please set METAL_INVESTMENT_AES_KEY environment variable " +
                   "with this value for production use.", generatedKey);
        
        return generatedKey;
    }
}
