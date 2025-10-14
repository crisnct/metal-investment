package com.investment.metal.infrastructure.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        aesEncryptor.setKey(MultipleKeysEncoder.AES_KEY);

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
            strReturn.insert((int) (Math.random() * (strReturn.length() + 1)), KeyGenerator.SYMBOLS.charAt(i));
        }
        return strReturn.toString();
    }
}
