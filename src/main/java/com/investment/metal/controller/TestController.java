package com.investment.metal.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test-static")
    public Map<String, Object> testStaticFiles() {
        Map<String, Object> response = new HashMap<>();
        
        // Check if index.html exists
        try {
            Resource indexHtml = new ClassPathResource("static/index.html");
            response.put("index.html", indexHtml.exists() ? "EXISTS" : "NOT FOUND");
            response.put("index.html_path", "static/index.html");
        } catch (Exception e) {
            response.put("index.html_error", e.getMessage());
        }
        
        // Check if JS file exists
        try {
            Resource jsFile = new ClassPathResource("static/static/js/main.170790d1.js");
            response.put("main.js", jsFile.exists() ? "EXISTS" : "NOT FOUND");
            response.put("main.js_path", "static/static/js/main.170790d1.js");
        } catch (Exception e) {
            response.put("main.js_error", e.getMessage());
        }
        
        // Check if CSS file exists
        try {
            Resource cssFile = new ClassPathResource("static/static/css/main.c4798db7.css");
            response.put("main.css", cssFile.exists() ? "EXISTS" : "NOT FOUND");
            response.put("main.css_path", "static/static/css/main.c4798db7.css");
        } catch (Exception e) {
            response.put("main.css_error", e.getMessage());
        }
        
        return response;
    }
}
