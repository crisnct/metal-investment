package com.investment.metal.encryption;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Component
public class MultipleKeysEncoder implements ConsistentEncoder {

    public static final String AES_KEY = "metal-investment";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleKeysEncoder.class);

    private final AESEncryptor aesEncryptor;

    private List<String> keys;

    public MultipleKeysEncoder() {
        this.aesEncryptor = new AESEncryptor(CHARSET);
        this.aesEncryptor.setKey(AES_KEY);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            String key = IOUtils.toString(
                    Objects.requireNonNull(contextClassLoader.getResourceAsStream("metal-investment.key")),
                    CHARSET);
            key = this.aesEncryptor.decrypt(key);
            this.keys = Lists.newArrayList(key.split("\n"));
        } catch (IOException e) {
            LOGGER.error("Can not parse the key for " + MultipleKeysEncoder.class);
            this.keys = null;
        }
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
