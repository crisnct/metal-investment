package com.investment.metal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Metal Investment API");
        response.put("version", "1.0.0");
        return response;
    }

    @GetMapping("/api/health")
    public Map<String, String> apiHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("api", "Metal Investment API");
        response.put("swagger", "/swagger-ui.html");
        response.put("docs", "/api-docs");
        return response;
    }
}
