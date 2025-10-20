package com.investment.metal;

import com.investment.metal.infrastructure.encryption.AESEncryptor;
import com.investment.metal.infrastructure.encryption.AbstractHandShakeEncryptor;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Flooding {

    final static ExecutorService es = Executors.newFixedThreadPool(20);
    private static final Logger LOGGER = LoggerFactory.getLogger(Flooding.class);

    /**
     * Main method for load testing the metal investment API.
     * Creates multiple threads to continuously send requests to the profit endpoint
     * to test system performance and stability under load.
     * 
     * @param args command line arguments (not used)
     * @throws InterruptedException if thread operations are interrupted
     */
    public static void main(String args[]) throws InterruptedException {
        // Configure HTTP client with extended timeouts for load testing
        Unirest.config()
                .socketTimeout(50000000)
                //.concurrency(500, 500)
                .connectTimeout(1000000000);
        Unirest.config().enableCookieManagement(true);

        // Set up encryption for handshake authentication
        AESEncryptor encryptor = new AESEncryptor(StandardCharsets.UTF_8);
        encryptor.setKey(AbstractHandShakeEncryptor.AES_KEY_HANDSHAKE);

        // Create 20 concurrent threads for load testing
        for (int i = 0; i < 20; i++) {
            es.submit((Runnable) () -> {
                String hs = encryptor.encrypt(String.valueOf(System.currentTimeMillis()));
                while (true) {
                    // Continuously send requests to the profit endpoint
                    HttpResponse<String> response = Unirest.get("http://localhost:8080/api/private/profit")
                            .header("hs", hs)
                            .header("Authorization", "Bearer 4594ac0c-4fd2-471a-93c7-c89b95b3a555")
                            .asString()
                            .ifFailure(stringHttpResponse -> {
                                LOGGER.error("Error {}", stringHttpResponse.getBody());
                            });
                    hs = response.getHeaders().getFirst("hijack");
                    LOGGER.info(String.valueOf(response.getStatus()));
                }
            });
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);
    }

}
