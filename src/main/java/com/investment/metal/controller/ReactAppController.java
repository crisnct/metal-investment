package com.investment.metal.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to serve the React application.
 * Handles the root path and forwards to index.html.
 */
@RestController
public class ReactAppController {

    @GetMapping("/")
    public ResponseEntity<Resource> serveReactApp() {
        try {
            Resource resource = new ClassPathResource("static/index.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
