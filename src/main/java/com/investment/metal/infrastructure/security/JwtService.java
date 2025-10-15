package com.investment.metal.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT service for secure token generation and validation.
 * Replaces the insecure UUID-based token system with proper JWT implementation.
 * 
 * Security Features:
 * - Uses HMAC-SHA256 for signing
 * - Configurable expiration time
 * - Secure key generation
 * - Token validation with proper error handling
 */
@Service
public class JwtService {

    @Value("${METAL_INVESTMENT_JWT_SECRET:metal-investment-secret-key-256-bits-long}")
    private String jwtSecret;

    @Value("${METAL_INVESTMENT_JWT_EXPIRATION:86400000}") // 24 hours in milliseconds
    private long jwtExpiration;

    /**
     * Generate a secure JWT token for the given user ID.
     * 
     * @param userId the user ID to include in the token
     * @return a signed JWT token
     */
    public String generateToken(Integer userId) {
        return generateToken(new HashMap<>(), userId);
    }

    /**
     * Generate a secure JWT token with additional claims.
     * 
     * @param extraClaims additional claims to include in the token
     * @param userId the user ID to include in the token
     * @return a signed JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, Integer userId) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the user ID from a JWT token.
     * 
     * @param token the JWT token
     * @return the user ID as Integer
     */
    public Integer extractUserId(String token) {
        return Integer.valueOf(extractClaim(token, Claims::getSubject));
    }

    /**
     * Extract a specific claim from a JWT token.
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the specific claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Check if a JWT token is valid (not expired and properly signed).
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a JWT token is expired.
     * 
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract the expiration date from a JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract all claims from a JWT token.
     * 
     * @param token the JWT token
     * @return all claims from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get the signing key for JWT operations.
     * Uses HMAC-SHA256 with a secure key derived from the secret.
     * 
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
