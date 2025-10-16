package com.investment.metal;

import com.investment.metal.infrastructure.util.PropertyFile;
import com.investment.metal.infrastructure.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.cert.X509Certificate;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Metal Investment system.
 * Handles application startup, configuration, and SSL setup.
 */
@Slf4j
@SpringBootApplication
public class MetalApplication {
    
    public static final String APPLICATION_PROPERTIES = "application.properties";

    /**
     * Main entry point for the Metal Investment application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0 && StringUtils.equals(args[0], "-update.properties")) {
            updateApplicationProperties();
            LogManager.shutdown();
            return;
        }

        // Configure JAXB system properties to fix dependency issues
        configureJaxbProperties();
        
        // Configure Hibernate properties
        configureHibernateProperties();

        // Disable SSL certificate validation (temporary for RSS feed parser)
        disableSSLCheck();

        // Configure HTTP client to prevent cookie warnings
        configureHttpClient();

        // Start the Spring Boot application
        SpringApplication.run(MetalApplication.class, args);
    }

    /**
     * Configure JAXB system properties for Jakarta XML binding.
     */
    private static void configureJaxbProperties() {
        System.setProperty("javax.xml.bind.JAXBContext", "jakarta.xml.bind.JAXBContext");
        System.setProperty("javax.xml.bind.context.factory", "jakarta.xml.bind.JAXBContextFactory");
    }

    /**
     * Configure Hibernate properties to disable XML mapping and JAXB.
     */
    private static void configureHibernateProperties() {
        System.setProperty("hibernate.xml_mapping_enabled", "false");
        System.setProperty("hibernate.jaxb.enabled", "false");
        System.setProperty("hibernate.hbm2ddl.import_files", "");
        System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor", "");
    }

    /**
     * Configure HTTP client to prevent cookie warnings from AWS ALB.
     */
    private static void configureHttpClient() {
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
    }

    /**
     * Update application properties file by merging current and source properties.
     * This method is called when the application is started with -update.properties argument.
     */
    private static void updateApplicationProperties() {
        try {
            File applicationPropFile = new File(APPLICATION_PROPERTIES);
            if (!applicationPropFile.exists()) {
                log.warn("{} is missing", APPLICATION_PROPERTIES);
                return;
            }

            log.info("Updating {}", APPLICATION_PROPERTIES);
            
            // Load current properties from file system
            PropertyFile currentFileWorkDir = new PropertyFile();
            currentFileWorkDir.load(new FileInputStream(applicationPropFile));

            // Load source properties from classpath
            PropertyFile sourceCodeProperties = new PropertyFile();
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            sourceCodeProperties.load(contextClassLoader.getResourceAsStream(APPLICATION_PROPERTIES));

            // Add missing properties from current file to source
            for (String propName : currentFileWorkDir.getKeys()) {
                String propValue = currentFileWorkDir.getProperty(propName);
                if (StringUtils.isBlank(sourceCodeProperties.getProperty(propName))) {
                    sourceCodeProperties.addProperty(propName, propValue);
                }
            }

            // Update source properties with values from current file
            for (String propName : sourceCodeProperties.getKeys()) {
                String sourceCodeValue = sourceCodeProperties.getProperty(propName);
                String propFileValue = currentFileWorkDir.getProperty(propName);
                if (!StringUtils.equals(sourceCodeValue, propFileValue) && 
                    StringUtils.isNotBlank(propFileValue)) {
                    sourceCodeProperties.setProperty(propName, propFileValue);
                }
            }

            sourceCodeProperties.save(new FileWriter(APPLICATION_PROPERTIES));
            log.info("Properties updated successfully");
        } catch (Exception e) {
            log.error("Cannot update {} file", APPLICATION_PROPERTIES, e);
        } finally {
            Util.sleep(2000);
        }
    }

    /**
     * Disable SSL certificate validation.
     * WARNING: This is a security risk and should only be used for development.
     * This is temporarily disabled because RSS feed parser fails with SSL validation.
     */
    private static void disableSSLCheck() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Trust all client certificates
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Trust all server certificates
                }
            }};

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            log.warn("SSL certificate validation has been disabled - SECURITY RISK!");
        } catch (Throwable e) {
            log.error("Failed to disable SSL check", e);
        }
    }

}
