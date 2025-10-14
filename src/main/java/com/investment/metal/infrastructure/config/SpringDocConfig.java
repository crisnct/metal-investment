package com.investment.metal.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.investment.metal.infrastructure.controller")
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Metal Investment API")
                        .description("API for monitoring precious metals prices and managing investment alerts for Revolut users in Romania")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cristian Țone")
                                .email("nelucristian2005@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://metal-investment-635786220311.europe-west1.run.app")
                                .description("Production Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                .tags(List.of(
                        new Tag().name("Public API").description("Public endpoints for user registration, login, and account management"),
                        new Tag().name("Protected API").description("Protected endpoints requiring JWT authentication")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/userRegistration", "/validateAccount", "/login", "/resetPassword", "/changePassword", "/checkUserPendingValidation", "/resendValidationEmail", "/health", "/")
                .packagesToScan("com.investment.metal.infrastructure.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi protectedApi() {
        return GroupedOpenApi.builder()
                .group("protected")
                .pathsToMatch("/api/**")
                .packagesToScan("com.investment.metal.infrastructure.controller")
                .build();
    }

}
