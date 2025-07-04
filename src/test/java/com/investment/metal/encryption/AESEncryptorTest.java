package com.investment.metal.encryption;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptorTest {

    @Test
    public void encryptDecryptShouldPreserveStringWithCharset() {
        AESEncryptor encryptor = new AESEncryptor(StandardCharsets.UTF_8);
        encryptor.setKey(AbstractHandShakeEncryptor.AES_KEY_HANDSHAKE);
        String original = "Привет, мир!"; // contains non-ASCII characters
        String encrypted = encryptor.encrypt(original);
        assertNotNull(encrypted);
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
}
