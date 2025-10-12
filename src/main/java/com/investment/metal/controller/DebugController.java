package com.investment.metal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DebugController {

    @GetMapping("/debug/api-docs")
    public Map<String, Object> debugApiDocs() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Debug endpoint is working");
        response.put("api_docs_url", "/v3/api-docs");
        response.put("swagger_ui_url", "/swagger-ui.html");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/debug/swagger-config")
    public Map<String, Object> debugSwaggerConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Swagger configuration debug");
        response.put("expected_urls", new String[]{
            "/v3/api-docs",
            "/swagger-ui.html",
            "/swagger-ui/index.html"
        });
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
