package com.investment.metal.application.service;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.UserDomainService;
import com.investment.metal.application.repository.UserApplicationRepository;
import com.investment.metal.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for User use cases.
 * Orchestrates domain services and handles application-level concerns.
 * Follows Clean Architecture principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService {
    
    private final UserApplicationRepository userRepository;
    private final UserDomainService userDomainService;
    
    /**
     * Register new user
     */
    public User registerUser(String username, String email, String password) throws BusinessException {
        log.info("Registering new user: {}", username);
        
        // Validate registration data
        if (!userDomainService.isValidRegistrationData(username, email, password)) {
            throw new BusinessException(400, "Invalid registration data");
        }
        
        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(409, "Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(409, "Email already exists");
        }
        
        // Create user domain model
        User user = User.builder()
            .username(username)
            .email(email)
            .createdAt(java.time.LocalDateTime.now())
            .validated(false)
            .active(true)
            .build();
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Generate validation code
        int validationCode = userDomainService.generateValidationCode(savedUser);
        log.info("Generated validation code for user: {}", username);
        
        return savedUser;
    }
    
    /**
     * Authenticate user
     */
    public String authenticateUser(String username, String password, String ipAddress) throws BusinessException {
        log.info("Authenticating user: {}", username);
        
        // Find user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        // Check if user can perform actions
        if (!userDomainService.canUserPerformActions(user)) {
            throw new BusinessException(403, "User account is restricted");
        }
        
        // Check IP blocking
        if (userDomainService.isIpBlockedForUser(user, ipAddress)) {
            throw new BusinessException(403, "IP address is blocked");
        }
        
        // Validate credentials
        if (!userDomainService.validateCredentials(user, password)) {
            userDomainService.recordFailedAttempt(user, ipAddress);
            throw new BusinessException(401, "Invalid credentials");
        }
        
        // Generate login token
        String token = userDomainService.generateLoginToken(user);
        
        log.info("User {} authenticated successfully", username);
        return token;
    }
    
    /**
     * Validate user account with code
     */
    public void validateAccount(String username, int code) throws BusinessException {
        log.info("Validating account for user: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        if (!userDomainService.validateUserAccount(user, code)) {
            throw new BusinessException(400, "Invalid validation code");
        }
        
        log.info("Account validated successfully for user: {}", username);
    }
    
    /**
     * Reset password
     */
    public String resetPassword(String email) throws BusinessException {
        log.info("Resetting password for email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        String resetToken = userDomainService.generatePasswordResetToken(user);
        
        log.info("Password reset token generated for user: {}", user.getUsername());
        return resetToken;
    }
    
    /**
     * Validate token and return user
     */
    @Transactional(readOnly = true)
    public User validateToken(String token) throws BusinessException {
        log.debug("Validating token");
        
        Optional<User> userOpt = userDomainService.validateToken(token);
        if (userOpt.isEmpty()) {
            throw new BusinessException(401, "Invalid or expired token");
        }
        
        return userOpt.get();
    }
    
    /**
     * Logout user
     */
    public void logoutUser(String username) {
        log.info("Logging out user: {}", username);
        
        userRepository.findByUsername(username).ifPresent(userDomainService::logoutUser);
    }
    
    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) throws BusinessException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
    }
    
    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) throws BusinessException {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
    }
}
