package com.investment.metal.infrastructure.util;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Infrastructure utility class for common operations.
 * Provides static utility methods for various infrastructure concerns.
 * Follows Clean Architecture principles by keeping infrastructure utilities separate.
 */
public class Util {
    
    /**
     * Conversion factor from ounces to kilograms.
     * 1 ounce = 0.0283495231 kg
     */
    public static final double OUNCE = 0.0283495231;

    /**
     * Regular expression pattern for validating email addresses.
     * Supports standard email format with case-insensitive matching.
     */
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /**
     * Logger instance for this utility class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    /**
     * Random number generator for generating random values.
     * Thread-safe and reusable across the application.
     */
    @Getter
    private static final Random randomGenerator = new Random();

    /**
     * HTTP headers to check when extracting client IP address.
     * Ordered by priority - checks these headers in sequence to find the real client IP.
     * Useful for applications behind proxies, load balancers, or CDNs.
     */
    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",        // Standard proxy header
            "Proxy-Client-IP",        // Proxy client IP
            "WL-Proxy-Client-IP",     // WebLogic proxy header
            "HTTP_X_FORWARDED_FOR",   // Alternative forwarded header
            "HTTP_X_FORWARDED",       // Alternative forwarded header
            "HTTP_X_CLUSTER_CLIENT_IP", // Cluster client IP
            "HTTP_CLIENT_IP",         // Client IP header
            "HTTP_FORWARDED_FOR",     // Forwarded for header
            "HTTP_FORWARDED",         // Forwarded header
            "HTTP_VIA",               // Via header
            "REMOTE_ADDR",            // Remote address (fallback)
            "HOST"                    // Host header (last resort)
    };

    /**
     * Utility method to pause execution for a specified duration.
     * Handles InterruptedException gracefully by logging the error.
     * 
     * @param duration the duration to sleep in milliseconds
     */
    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            LOGGER.error("Thread sleep interrupted: {}", e.getMessage(), e);
            // Restore interrupted status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Extract the real client IP address from HTTP request.
     * Checks various proxy headers in order of priority to find the actual client IP.
     * This is essential for applications behind proxies, load balancers, or CDNs.
     * 
     * @param request the HTTP servlet request
     * @return the client IP address, or remote address if no proxy headers found
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (Strings.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Extract JWT token from HTTP request Authorization header.
     * Removes "Bearer " prefix and trims whitespace.
     * 
     * @param httpServletRequest the HTTP servlet request
     * @return the JWT token string, or empty string if no token found
     */
    public static String getTokenFromRequest(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader(AUTHORIZATION);
        if (token == null) {
            token = "";
        }
        return StringUtils.removeStart(token, "Bearer").trim();
    }

    /**
     * Validate email address format using regex pattern.
     * Checks if the email string matches the standard email format.
     * 
     * @param emailStr the email string to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean isValidEmailAddress(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    /**
     * Reduce the number of decimal places in a double value.
     * Formats the number to specified decimal places and parses back to double.
     * 
     * @param value the double value to format
     * @param dec the number of decimal places
     * @return the formatted double value
     */
    public static double reduceDecimals(double value, int dec) {
        return Double.parseDouble(String.format("%." + dec + "f", value, dec));
    }
}
