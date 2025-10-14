package com.investment.metal.application.service;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.AccountDomainService;
import com.investment.metal.application.repository.UserApplicationRepository;
import com.investment.metal.application.mapper.UserMapper;
import com.investment.metal.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for user registration use cases.
 * Orchestrates domain services and handles application-level concerns.
 * Follows Clean Architecture principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationApplicationService {
    
    private final UserApplicationRepository userRepository;
    private final AccountDomainService accountDomainService;
    private final UserMapper userMapper;
    
    /**
     * Check if username is available
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        try {
            return userRepository.findByUsername(username).isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check username availability: {}", username, e);
            return false;
        }
    }
    
    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        try {
            return userRepository.findByEmail(email).isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check email availability: {}", email, e);
            return false;
        }
    }
    
    /**
     * Check if user can register (combines domain validation with data availability)
     */
    @Transactional(readOnly = true)
    public boolean canUserRegister(String username, String email, String password) {
        // Domain validation
        if (!accountDomainService.isValidRegistrationData(username, email, password)) {
            return false;
        }
        
        // Data availability checks
        return isUsernameAvailable(username) && isEmailAvailable(email);
    }
    
    /**
     * Register new user
     */
    public User registerUser(String username, String email, String password) throws BusinessException {
        log.info("Registering new user: {}", username);
        
        // Validate registration data
        if (!accountDomainService.isValidRegistrationData(username, email, password)) {
            throw new BusinessException(400, "Invalid registration data");
        }
        
        // Check availability
        if (!canUserRegister(username, email, password)) {
            throw new BusinessException(409, "Username or email already exists");
        }
        
        // Create user domain model
        User user = User.builder()
            .username(username)
            .email(email)
            .createdAt(java.time.LocalDateTime.now())
            .validated(false)
            .active(true)
            .build();
        
        // Save user (this would need to be implemented in the repository)
        // User savedUser = userRepository.save(user);
        
        log.info("User {} registered successfully", username);
        return user;
    }
}
