package com.investment.metal;

import com.investment.metal.common.PropertyFile;
import com.investment.metal.common.Util;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import kong.unirest.Unirest;
import kong.unirest.apache.ApacheClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.cert.X509Certificate;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm;

@Slf4j
@SpringBootApplication
public class MetalApplication {
    public static final String APPLICATION_PROPERTIES = "application.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(MetalApplication.class);

    public static void main(String[] args) {
        if (args.length > 0 && StringUtils.equals(args[0], "-update.properties")) {
            updateApplicationProperties();
            LogManager.shutdown();
            return;
        }

        // Set JAXB system properties to fix JAXB dependency issues
        System.setProperty("javax.xml.bind.JAXBContext", "jakarta.xml.bind.JAXBContext");
        System.setProperty("javax.xml.bind.context.factory", "jakarta.xml.bind.JAXBContextFactory");
        
        // Completely disable Hibernate XML mapping and JAXB
        System.setProperty("hibernate.xml_mapping_enabled", "false");
        System.setProperty("hibernate.jaxb.enabled", "false");
        System.setProperty("hibernate.hbm2ddl.import_files", "");
        System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor", "");
        System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor", "");
        System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor", "");

        //This is disabled temporary because RSS FEED parser fails
        disableSSLCheck();

        //This is to prevent warnings about invalid cookie
        Unirest.config().httpClient(config -> {
            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .build();
            CloseableHttpClient closeableClient = HttpClients
                    .custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            return ApacheClient.builder(closeableClient).apply(config);
        });

        //Starts the spring boot application
        SpringApplication.run(MetalApplication.class, args);
    }

    private static void updateApplicationProperties() {
        try {
            File applicationPropFile = new File(APPLICATION_PROPERTIES);
            if (!applicationPropFile.exists()) {
                LOGGER.warn(APPLICATION_PROPERTIES + " is missing");
                return;
            }

            LOGGER.info("Updating " + APPLICATION_PROPERTIES);
            final PropertyFile currentFileWorkDir = new PropertyFile();
            currentFileWorkDir.load(new FileInputStream(applicationPropFile));

            final PropertyFile sourceCodeProperties = new PropertyFile();
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            sourceCodeProperties.load(contextClassLoader.getResourceAsStream(APPLICATION_PROPERTIES));

            //Add missing properties
            for (String propName : currentFileWorkDir.getKeys()) {
                String propValue = currentFileWorkDir.getProperty(propName);
                if (StringUtils.isBlank(sourceCodeProperties.getProperty(propName))) {
                    sourceCodeProperties.addProperty(propName, propValue);
                }
            }

            //Add to sourceCodeProperties the values which are different in currentFileWorkDir
            for (String propName : sourceCodeProperties.getKeys()) {
                String sourceCodeValue = sourceCodeProperties.getProperty(propName);
                String propFileValue = currentFileWorkDir.getProperty(propName);
                if (!StringUtils.equals(sourceCodeValue, propFileValue) && StringUtils.isNotBlank(propFileValue)) {
                    sourceCodeProperties.setProperty(propName, propFileValue);
                }
            }

            sourceCodeProperties.save(new FileWriter(APPLICATION_PROPERTIES));
            LOGGER.info("Updated successfully");
        } catch (Exception e) {
            LOGGER.error("Can not update " + APPLICATION_PROPERTIES + " file", e);
        } finally {
            Util.sleep(2000);
        }
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
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
