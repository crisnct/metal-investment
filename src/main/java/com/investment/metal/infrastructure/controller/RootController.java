package com.investment.metal.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller that forwards root requests to the SPA entry point. Any non-API path is forwarded to index.html so the frontend router can handle it.
 */
@Controller
public class RootController {

  private static final long DEFAULT_DB_HEALTH_TIMEOUT_MS = 1000;

  @Autowired(required = false)
  private DataSource dataSource;

  private ExecutorService healthCheckExecutor;

  @PostConstruct
  private void init(){
    this.healthCheckExecutor = Executors.newCachedThreadPool(new HealthThreadFactory());
  }

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
  @ResponseBody
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
    response.put("database", determineDatabaseStatus());
    return response;
  }

  @GetMapping("/api/health")
  @ResponseBody
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
    response.put("database", determineDatabaseStatus());
    return response;
  }

  private String determineDatabaseStatus() {
    if (this.dataSource == null) {
      return "DOWN";
    }

    CompletableFuture<String> future =
        CompletableFuture.supplyAsync(
            () -> {
              try (Connection connection = this.dataSource.getConnection()) {
                if (connection != null && connection.isValid(1)) {
                  return "UP";
                }
                return "DOWN";
              } catch (SQLException ex) {
                return "DOWN";
              }
            },
            this.healthCheckExecutor);

    try {
      return future.get(DEFAULT_DB_HEALTH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    } catch (TimeoutException ex) {
      future.cancel(true);
      return "DOWN";
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return "DOWN";
    } catch (ExecutionException ex) {
      return "DOWN";
    }
  }

  @PreDestroy
  void shutdownHealthExecutor() {
    this.healthCheckExecutor.shutdownNow();
  }

  private static class HealthThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable);
      thread.setName("health-check-" + thread.getId());
      thread.setDaemon(true);
      return thread;
    }
  }
}
