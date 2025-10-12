package com.investment.metal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Test API", description = "Test endpoints for debugging")
public class TestApiController {

    @GetMapping("/test-api")
    @Operation(summary = "Test API endpoint", description = "Simple test endpoint to verify SpringDoc is working")
    public Map<String, String> testApi() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test API is working!");
        response.put("status", "success");
        return response;
    }
}
