package com.investment.metal.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for static resource handling.
 * Handles React UI assets and Swagger UI resources.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Swagger UI resources
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(0);

        // React UI static files (JS, CSS, etc.)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/")
                .setCachePeriod(0);

        // General static resources (favicon, manifest, etc.)
        registry.addResourceHandler("/favicon.ico", "/asset-manifest.json")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
    }
}
