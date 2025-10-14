package com.investment.metal.encryption;

import com.investment.metal.infrastructure.encryption.AESEncryptor;
import com.investment.metal.infrastructure.encryption.AbstractHandShakeEncryptor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class AESEncryptorTest {

    @Test
    public void encryptDecryptShouldPreserveStringWithCharset() {
        AESEncryptor encryptor = new AESEncryptor(StandardCharsets.UTF_8);
        encryptor.setKey(AbstractHandShakeEncryptor.AES_KEY_HANDSHAKE);
        String original = "testățțăî!"; // contains non-ASCII characters
        String encrypted = encryptor.encrypt(original);
        assertNotNull(encrypted);
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals(original, decrypted);
    }
}
