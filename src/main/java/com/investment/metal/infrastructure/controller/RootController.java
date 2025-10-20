package com.investment.metal.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller that forwards root requests to the SPA entry point. Any non-API path is forwarded to index.html so the frontend router can handle it.
 */
@Controller
public class RootController {

  @GetMapping("/")
  @Operation(
      summary = "Serve React application",
      description = "Serves the main React application HTML page"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "React app served successfully",
          content = @Content(mediaType = "text/html")),
      @ApiResponse(responseCode = "404", description = "React app not found")
  })
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

  @GetMapping("/health")
  @Operation(
      summary = "Health check",
      description = "Returns the health status of the application"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Application is healthy",
          content = @Content(schema = @Schema(implementation = Map.class)))
  })
  public Map<String, String> health() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "Metal Investment API");
    response.put("version", "1.0.0");
    return response;
  }

  @GetMapping("/api/health")
  @Operation(
      summary = "API health check",
      description = "Returns the health status of the API with additional information"
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API is healthy",
          content = @Content(schema = @Schema(implementation = Map.class)))
  })
  public Map<String, String> apiHealth() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "UP");
    response.put("api", "Metal Investment API");
    response.put("swagger", "/swagger-ui.html");
    response.put("docs", "/api-docs");
    return response;
  }

}
