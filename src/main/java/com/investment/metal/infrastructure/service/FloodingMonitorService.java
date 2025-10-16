package com.investment.metal.infrastructure.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for monitoring and detecting potential flooding attacks.
 * Tracks request patterns and provides alerting for suspicious activity.
 */
@Service
@Slf4j
public class FloodingMonitorService {
    
    // Thresholds for flooding detection
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int BULKHEAD_VIOLATIONS_THRESHOLD = 10;
    private static final int RATE_LIMITER_VIOLATIONS_THRESHOLD = 20;
    
    // In-memory tracking (in production, consider using Redis or database)
    private final ConcurrentHashMap<String, RequestTracker> requestCounters = new ConcurrentHashMap<>();
    private final AtomicInteger bulkheadViolations = new AtomicInteger(0);
    private final AtomicInteger rateLimiterViolations = new AtomicInteger(0);
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Track a request from a specific IP address.
     * 
     * @param ipAddress the client IP address
     * @param endpoint the API endpoint being accessed
     * @return true if the request is allowed, false if it should be blocked
     */
    public boolean trackRequest(String ipAddress, String endpoint) {
        LocalDateTime now = LocalDateTime.now();
        RequestTracker tracker = requestCounters.computeIfAbsent(ipAddress, k -> new RequestTracker());
        
        // Clean up old entries
        tracker.cleanupOldEntries(now);
        
        // Increment counters
        tracker.incrementRequest(now, endpoint);
        
        // Check for flooding patterns
        boolean isFlooding = checkForFloodingPattern(tracker, ipAddress);
        
        if (isFlooding) {
            log.warn("Potential flooding attack detected from IP: {} - Requests: {}/min, {}/hour", 
                       ipAddress, tracker.getRequestsLastMinute(), tracker.getRequestsLastHour());
            
            // Send alert to administrators
            sendFloodingAlert(ipAddress, tracker);
            
            return false; // Block the request
        }
        
        return true; // Allow the request
    }
    
    /**
     * Record a bulkhead violation for monitoring.
     * 
     * @param ipAddress the client IP address
     * @param endpoint the API endpoint
     */
    public void recordBulkheadViolation(String ipAddress, String endpoint) {
        int violations = bulkheadViolations.incrementAndGet();
        
        log.warn("Bulkhead violation from IP: {} on endpoint: {} (Total violations: {})", 
                   ipAddress, endpoint, violations);
        
        if (violations >= BULKHEAD_VIOLATIONS_THRESHOLD) {
            sendBulkheadAlert(ipAddress, endpoint, violations);
        }
    }
    
    /**
     * Record a rate limiter violation for monitoring.
     * 
     * @param ipAddress the client IP address
     * @param endpoint the API endpoint
     */
    public void recordRateLimiterViolation(String ipAddress, String endpoint) {
        int violations = rateLimiterViolations.incrementAndGet();
        
        log.warn("Rate limiter violation from IP: {} on endpoint: {} (Total violations: {})", 
                   ipAddress, endpoint, violations);
        
        if (violations >= RATE_LIMITER_VIOLATIONS_THRESHOLD) {
            sendRateLimiterAlert(ipAddress, endpoint, violations);
        }
    }
    
    /**
     * Check if the request pattern indicates flooding.
     * 
     * @param tracker the request tracker for the IP
     * @param ipAddress the client IP address
     * @return true if flooding is detected
     */
    private boolean checkForFloodingPattern(RequestTracker tracker, String ipAddress) {
        // Check per-minute threshold
        if (tracker.getRequestsLastMinute() > MAX_REQUESTS_PER_MINUTE) {
            log.error("Flooding attack detected: IP {} exceeded {} requests per minute", 
                        ipAddress, MAX_REQUESTS_PER_MINUTE);
            return true;
        }
        
        // Check per-hour threshold
        if (tracker.getRequestsLastHour() > MAX_REQUESTS_PER_HOUR) {
            log.error("Flooding attack detected: IP {} exceeded {} requests per hour", 
                        ipAddress, MAX_REQUESTS_PER_HOUR);
            return true;
        }
        
        return false;
    }
    
    /**
     * Send flooding alert to administrators.
     * 
     * @param ipAddress the suspicious IP address
     * @param tracker the request tracker
     */
    private void sendFloodingAlert(String ipAddress, RequestTracker tracker) {
        try {
            String subject = "🚨 FLOODING ATTACK DETECTED - Metal Investment App";
            String message = String.format(
                "Potential flooding attack detected!\n\n" +
                "IP Address: %s\n" +
                "Requests Last Minute: %d\n" +
                "Requests Last Hour: %d\n" +
                "Timestamp: %s\n\n" +
                "Please investigate and consider blocking this IP address.",
                ipAddress,
                tracker.getRequestsLastMinute(),
                tracker.getRequestsLastHour(),
                LocalDateTime.now()
            );
            
            // Send to admin email (you may want to configure this)
            emailService.sendEmail("admin@metalinvestment.com", subject, message);
            
        } catch (Exception e) {
            log.error("Failed to send flooding alert: {}", e.getMessage());
        }
    }
    
    /**
     * Send bulkhead violation alert.
     * 
     * @param ipAddress the IP address
     * @param endpoint the endpoint
     * @param violations the number of violations
     */
    private void sendBulkheadAlert(String ipAddress, String endpoint, int violations) {
        try {
            String subject = "⚠️ BULKHEAD VIOLATIONS - Metal Investment App";
            String message = String.format(
                "High number of bulkhead violations detected!\n\n" +
                "IP Address: %s\n" +
                "Endpoint: %s\n" +
                "Total Violations: %d\n" +
                "Timestamp: %s\n\n" +
                "This may indicate a flooding attack or system overload.",
                ipAddress, endpoint, violations, LocalDateTime.now()
            );
            
            emailService.sendEmail("admin@metalinvestment.com", subject, message);
            
        } catch (Exception e) {
            log.error("Failed to send bulkhead alert: {}", e.getMessage());
        }
    }
    
    /**
     * Send rate limiter violation alert.
     * 
     * @param ipAddress the IP address
     * @param endpoint the endpoint
     * @param violations the number of violations
     */
    private void sendRateLimiterAlert(String ipAddress, String endpoint, int violations) {
        try {
            String subject = "⚠️ RATE LIMITER VIOLATIONS - Metal Investment App";
            String message = String.format(
                "High number of rate limiter violations detected!\n\n" +
                "IP Address: %s\n" +
                "Endpoint: %s\n" +
                "Total Violations: %d\n" +
                "Timestamp: %s\n\n" +
                "This may indicate a flooding attack or system overload.",
                ipAddress, endpoint, violations, LocalDateTime.now()
            );
            
            emailService.sendEmail("admin@metalinvestment.com", subject, message);
            
        } catch (Exception e) {
            log.error("Failed to send rate limiter alert: {}", e.getMessage());
        }
    }
    
    /**
     * Get current statistics for monitoring dashboard.
     * 
     * @return a map of current statistics
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("activeIPs", requestCounters.size());
        stats.put("bulkheadViolations", bulkheadViolations.get());
        stats.put("rateLimiterViolations", rateLimiterViolations.get());
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }
    
    /**
     * Request tracker for monitoring individual IP addresses.
     */
    private static class RequestTracker {
        private final java.util.List<LocalDateTime> requests = new java.util.concurrent.CopyOnWriteArrayList<>();
        
        public void incrementRequest(LocalDateTime now, String endpoint) {
            requests.add(now);
        }
        
        public void cleanupOldEntries(LocalDateTime now) {
            requests.removeIf(requestTime -> 
                requestTime.isBefore(now.minusHours(1)) // Keep only last hour
            );
        }
        
        public int getRequestsLastMinute() {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            return (int) requests.stream()
                .filter(requestTime -> requestTime.isAfter(oneMinuteAgo))
                .count();
        }
        
        public int getRequestsLastHour() {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            return (int) requests.stream()
                .filter(requestTime -> requestTime.isAfter(oneHourAgo))
                .count();
        }
    }
}
