package com.investment.metal.infrastructure.exception;

import com.investment.metal.infrastructure.service.FloodingMonitorService;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Resilience4j violations.
 * Handles bulkhead, rate limiter, and time limiter exceptions to provide
 * proper HTTP responses for flooding attack protection.
 */
@RestControllerAdvice
public class ResilienceExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceExceptionHandler.class);
    
    @Autowired
    private FloodingMonitorService floodingMonitorService;

    /**
     * Handle bulkhead violations - too many concurrent requests.
     * 
     * @param ex the bulkhead exception
     * @param request the web request
     * @return HTTP 429 Too Many Requests with appropriate message
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<Map<String, Object>> handleBulkheadFullException(
            BulkheadFullException ex, WebRequest request) {
        
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getDescription(false).replace("uri=", "");
        
        logger.warn("Bulkhead violation detected - too many concurrent requests from IP: {}", clientIp);
        
        // Record the violation for monitoring
        floodingMonitorService.recordBulkheadViolation(clientIp, endpoint);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("error", "Too Many Concurrent Requests");
        response.put("message", "Server is experiencing high load. Please try again later.");
        response.put("path", endpoint);
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * Handle rate limiter violations - too many requests per time window.
     * 
     * @param ex the rate limiter exception
     * @param request the web request
     * @return HTTP 429 Too Many Requests with appropriate message
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, Object>> handleRequestNotPermitted(
            RequestNotPermitted ex, WebRequest request) {
        
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getDescription(false).replace("uri=", "");
        
        logger.warn("Rate limiter violation detected - too many requests from IP: {}", clientIp);
        
        // Record the violation for monitoring
        floodingMonitorService.recordRateLimiterViolation(clientIp, endpoint);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("error", "Rate Limit Exceeded");
        response.put("message", "Too many requests. Please slow down and try again later.");
        response.put("path", endpoint);
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    /**
     * Handle time limiter violations - request timeout.
     * 
     * @param ex the time limiter exception
     * @param request the web request
     * @return HTTP 504 Gateway Timeout with appropriate message
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleTimeLimiterTimeoutException(
            TimeoutException ex, WebRequest request) {
        
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getDescription(false).replace("uri=", "");
        
        logger.warn("Time limiter violation detected - request timeout from IP: {}", clientIp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.GATEWAY_TIMEOUT.value());
        response.put("error", "Request Timeout");
        response.put("message", "Request took too long to process. Please try again.");
        response.put("path", endpoint);
        
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
    }

    /**
     * Extract client IP address from the request for logging and monitoring.
     * 
     * @param request the web request
     * @return the client IP address
     */
    private String getClientIpAddress(WebRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteUser() != null ? request.getRemoteUser() : "unknown";
    }
}
