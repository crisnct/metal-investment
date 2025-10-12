package com.investment.metal.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaticResourceController {

    @GetMapping("/")
    public ResponseEntity<Resource> index() {
        try {
            Resource resource = new ClassPathResource("static/index.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/static/js/{filename}")
    public ResponseEntity<Resource> getJsFile(@PathVariable String filename) {
        try {
            Resource resource = new ClassPathResource("static/static/js/" + filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/javascript")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/static/css/{filename}")
    public ResponseEntity<Resource> getCssFile(@PathVariable String filename) {
        try {
            Resource resource = new ClassPathResource("static/static/css/" + filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/css")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Resource> getFavicon() {
        try {
            Resource resource = new ClassPathResource("static/favicon.ico");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/x-icon")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/asset-manifest.json")
    public ResponseEntity<Resource> getAssetManifest() {
        try {
            Resource resource = new ClassPathResource("static/asset-manifest.json");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
