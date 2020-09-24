package com.investment.metal;

import com.investment.metal.encryption.AESEncryptor;
import com.investment.metal.encryption.AbstractHandShakeEncryptor;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Flooding {

    final static ExecutorService es = Executors.newFixedThreadPool(20);

    public static void main(String args[]) throws InterruptedException {
        Unirest.config()
                .socketTimeout(50000000)
                //.concurrency(500, 500)
                .connectTimeout(1000000000);
        Unirest.config().enableCookieManagement(true);

        AESEncryptor encryptor = new AESEncryptor(StandardCharsets.UTF_8);
        encryptor.setKey(AbstractHandShakeEncryptor.AES_KEY_HANDSHAKE);

        for (int i = 0; i < 20; i++) {
            es.submit((Runnable) () -> {
                String hs = encryptor.encrypt(String.valueOf(System.currentTimeMillis()));
                while (true) {
                    HttpResponse<String> response = Unirest.get("http://localhost:8080/api/profit")
                            .header("hs", hs)
                            .header("Authorization", "Bearer 4594ac0c-4fd2-471a-93c7-c89b95b3a555")
                            .asString()
                            .ifFailure(stringHttpResponse -> {
                                System.err.println("Error " + stringHttpResponse.getBody());
                            });
                    hs = response.getHeaders().getFirst("hijack");
                    System.out.println(response.getStatus());
                }
            });
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.DAYS);
    }

}
