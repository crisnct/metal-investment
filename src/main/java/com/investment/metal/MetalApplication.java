package com.investment.metal;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Slf4j
@SpringBootApplication
public class MetalApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetalApplication.class);

    public static void main(String[] args) {
        //This is disabled temporary because RSS FEED parser fails
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
            LOGGER.error(e.getMessage(), e);
        }
    }

}
