package com.investment.metal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Test", description = "Test endpoints for Swagger verification")
public class SwaggerTestController {

    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Simple test endpoint to verify Swagger is working")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test endpoint is working!");
        response.put("swagger", "/swagger-ui.html");
        response.put("api-docs", "/api-docs");
        return response;
    }

    @GetMapping("/swagger-test")
    @Operation(summary = "Swagger test", description = "Test endpoint specifically for Swagger documentation")
    public Map<String, Object> swaggerTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("swagger_ui_url", "/swagger-ui.html");
        response.put("openapi_json_url", "/api-docs");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
