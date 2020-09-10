package com.investment.metal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@SpringBootApplication
public class MetalApplication {

    public static void main(String[] args) {
        disableSSLCheck();
        SpringApplication.run(MetalApplication.class, args);
    }

    private static void disableSSLCheck() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
            //        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext);
            //          CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
//            Unirest.config().httpClient(httpclient);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
