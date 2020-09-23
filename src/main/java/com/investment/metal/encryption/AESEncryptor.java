package com.investment.metal.encryption;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

public class AESEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AESEncryptor.class);

    private static final String AES_INIT_VECTOR = "GoldSilverPlatin";

    private static final String ALGORITHM = "AES";

    private final Charset charset;

    private String key;

    public AESEncryptor(Charset charset) {
        this.charset = charset;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AES_INIT_VECTOR.getBytes(charset));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(charset), ALGORITHM);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AES_INIT_VECTOR.getBytes(charset));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(charset), ALGORITHM);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
}
