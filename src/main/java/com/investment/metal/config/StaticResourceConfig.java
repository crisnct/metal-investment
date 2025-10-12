package com.investment.metal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Allow .js files for Swagger UI
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
        .setCachePeriod(0);

    // Allow .json files for OpenAPI spec
    registry.addResourceHandler("/api-docs/**")
        .addResourceLocations("classpath:/static/")
        .setCachePeriod(0);

    // Allow all static resources including .js and .json
    registry.addResourceHandler("/**")
        .addResourceLocations(
            "classpath:/META-INF/resources/",
            "classpath:/META-INF/resources/webjars/",
            "classpath:/static/")
        .setCachePeriod(0);

    // Explicitly expose swagger-ui.html so the UI assets can be served by SpringDoc
    registry.addResourceHandler("/swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/")
        .setCachePeriod(0);

    registry.addResourceHandler("/swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
        .setCachePeriod(0);

    // Serve React static files
    registry.addResourceHandler("/static/**")
        .addResourceLocations("classpath:/static/static/")
        .setCachePeriod(0);

  }
}
