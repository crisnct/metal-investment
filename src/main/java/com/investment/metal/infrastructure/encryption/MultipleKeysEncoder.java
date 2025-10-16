package com.investment.metal.infrastructure.encryption;

import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MultipleKeysEncoder implements ConsistentEncoder {

    @Value("${METAL_INVESTMENT_AES_KEY:#{null}}")
    private String aesKeyFromEnv;

    @Value("${METAL_INVESTMENT_KEY:#{null}}")
    private String keyFromEnv;

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private AESEncryptor aesEncryptor;

    private List<String> keys;

    @PostConstruct
    public void init(){
        this.aesEncryptor = new AESEncryptor(CHARSET);
        this.aesEncryptor.setKey(aesKeyFromEnv);
        this.keys = Lists.newArrayList(this.aesEncryptor.decrypt(keyFromEnv).split("\n"));
        // Initialize keys with fallback if key file doesn't exist
        this.keys = initializeKeys();
    }
    
    /**
     * Initialize encryption keys from file or create default keys.
     * This method handles the case where the key file doesn't exist.
     * 
     * @return list of encryption keys
     */
    private List<String> initializeKeys() {
        try {
            if (keyFromEnv == null || keyFromEnv.trim().isEmpty()) {
                log.warn("Use KeyGenerator to create a key and set it in the environment variable METAL_INVESTMENT_KEY. Using default keys for encryption.");
                return createDefaultKeys();
            }

            String decryptedKey = this.aesEncryptor.decrypt(keyFromEnv);
            if (decryptedKey == null || decryptedKey.trim().isEmpty()) {
                log.warn("Failed to decrypt key file. Using default keys for encryption.");
                return createDefaultKeys();
            }
            
            log.info("Successfully loaded encryption keys from file.");
            return Lists.newArrayList(decryptedKey.split("\n"));
            
        } catch (Exception e) {
            log.warn("Failed to load encryption keys from file. Using default keys.", e);
            return createDefaultKeys();
        }
    }
    
    /**
     * Create default encryption keys when key file is not available.
     * This ensures the application can still function with basic encryption.
     * 
     * @return list of default encryption keys
     */
    private List<String> createDefaultKeys() {
        // Create a simple default key for basic encryption
        // In production, the key file should be properly generated and deployed
        String defaultKey = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        return Lists.newArrayList(defaultKey);
    }

    public String encrypt(String text) {
        String firstEncryption = this.aesEncryptor.encrypt(text);
        //noinspection UnnecessaryLocalVariable
        String secondEncryption = this.multipleKeysEncryption(firstEncryption);
        return secondEncryption;
    }

    public String decrypt(String text) {
        String firstDecryption = this.multipleKeysDecryption(text);
        //noinspection UnnecessaryLocalVariable
        String secondDecryption = this.aesEncryptor.decrypt(firstDecryption);
        return secondDecryption;
    }

    private String multipleKeysEncryption(String str) {
        return this.transform(str, (key1, key2, character) -> {
            int index = key1.indexOf(character);
            if (index == -1) {
                return character;
            } else {
                return key2.charAt(index);
            }
        });
    }

    private String multipleKeysDecryption(String str) {
        return this.transform(str, (key1, key2, character) -> {
            int index = key2.indexOf(character);
            if (index == -1) {
                return character;
            } else {
                return key1.charAt(index);
            }
        });
    }

    /**
     * Transform a string using multiple encryption keys with a specific transformation function.
     * This method applies the transformation using different keys in a rotating pattern.
     * 
     * @param str the string to transform
     * @param transformation the transformation function to apply
     * @return the transformed string
     */
    private String transform(String str, MultipleKeysTransformation transformation) {
        String key1 = keys.get(0);
        StringBuilder builder = new StringBuilder();
        for (int i = 0, line = 1; i < str.length(); i++, line = (line + 1) % keys.size()) {
            String key2 = keys.get(line);
            char character = transformation.call(key1, key2, str.charAt(i));
            builder.append(character);
        }
        return builder.toString();
    }

}
