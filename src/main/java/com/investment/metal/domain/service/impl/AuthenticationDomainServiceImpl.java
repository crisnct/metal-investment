package com.investment.metal.domain.service.impl;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.AuthenticationDomainService;
import com.investment.metal.domain.valueobject.LoginAttempt;
import com.investment.metal.domain.valueobject.Token;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.repository.CustomerRepository;
import com.investment.metal.infrastructure.security.CustomAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of AuthenticationDomainService.
 * Handles authentication business logic and integrates with existing security components.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationDomainServiceImpl implements AuthenticationDomainService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationProvider authenticationProvider;

    @Override
    public boolean validateCredentials(User user, String password) {
        try {
            // Get the customer entity to access the hashed password
            Customer customer = customerRepository.findById(user.getId().longValue())
                .orElseThrow(() -> new BusinessException(404, "User not found"));
            
            return passwordEncoder.matches(password, customer.getPassword());
        } catch (Exception e) {
            log.warn("Failed to validate credentials for user: {}", user.getUsername(), e);
            return false;
        }
    }

    @Override
    public Token generateToken(User user) {
        return Token.createLoginToken(user.getId(), 7); // 7 days expiration
    }

    @Override
    public Optional<User> validateToken(String tokenValue) {
        try {
            // For now, we'll use a simple validation
            // In a real implementation, you'd validate against stored tokens
            if (tokenValue == null || tokenValue.isEmpty()) {
                return Optional.empty();
            }
            
            // This is a simplified implementation
            // In production, you'd validate the token against a database
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to validate token", e);
            return Optional.empty();
        }
    }

    @Override
    public void recordFailedAttempt(Integer userId, String ipAddress) {
        log.warn("Failed login attempt for user: {} from IP: {}", userId, ipAddress);
        // In a real implementation, you'd store this in a database
        // For now, we'll just log it
    }

    @Override
    public boolean isUserBanned(Integer userId) {
        try {
            Customer customer = customerRepository.findById(userId.longValue()).orElse(null);
            if (customer == null) {
                return true; // User not found is considered banned
            }
            
            // Check if user is active (assuming active field exists)
            // For now, we'll assume all users are not banned
            return false;
        } catch (Exception e) {
            log.warn("Failed to check if user is banned: {}", userId, e);
            return true; // Assume banned on error
        }
    }

    @Override
    public boolean isIpBlocked(String ipAddress) {
        // In a real implementation, you'd check against a blocked IPs table
        // For now, we'll assume no IPs are blocked
        return false;
    }

    @Override
    public int generateValidationCode(boolean strongCode) {
        Random random = new Random();
        if (strongCode) {
            return 100000 + random.nextInt(900000); // 6-digit code
        } else {
            return 1000 + random.nextInt(9000); // 4-digit code
        }
    }

    @Override
    public void validateAccount(User user, int code) throws BusinessException {
        // In a real implementation, you'd validate the code against stored codes
        // For now, we'll just accept any code
        log.info("Account validation for user: {} with code: {}", user.getUsername(), code);
    }

    @Override
    public boolean needsValidation(User user) {
        return !user.isValidated();
    }

    @Override
    public LoginAttempt getLoginAttempts(Integer userId) {
        // In a real implementation, you'd retrieve from database
        return LoginAttempt.builder()
            .userId(userId)
            .ipAddress("unknown")
            .timestamp(LocalDateTime.now())
            .successful(false)
            .build();
    }

    @Override
    public void resetFailedAttempts(Integer userId) {
        log.info("Resetting failed attempts for user: {}", userId);
        // In a real implementation, you'd update the database
    }

    @Override
    public boolean isTokenExpired(Token token) {
        return token.isExpired();
    }

    @Override
    public Token extendToken(Token token) {
        return Token.createLoginToken(token.getUserId(), 7); // Create new token with 7 days
    }
}
