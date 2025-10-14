package com.investment.metal.application.service;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.AccountDomainService;
import com.investment.metal.domain.service.UserDomainService;
import com.investment.metal.application.repository.UserApplicationRepository;
import com.investment.metal.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for user management use cases.
 * Orchestrates domain services and handles application-level concerns.
 * Follows Clean Architecture principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementApplicationService {
    
    private final UserApplicationRepository userRepository;
    private final UserDomainService userDomainService;
    private final AccountDomainService accountDomainService;
    
    /**
     * Register new user
     */
    public User registerUser(String username, String email, String password) throws BusinessException {
        log.info("Registering new user: {}", username);
        
        // Validate registration data
        if (!accountDomainService.isValidRegistrationData(username, email, password)) {
            throw new BusinessException(400, "Invalid registration data");
        }
        
        // Check if user can register
        if (!accountDomainService.canUserRegister(username, email)) {
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
        
        // Save user
        User savedUser = userRepository.save(user);
        
        log.info("User registered successfully: {}", username);
        return savedUser;
    }
    
    /**
     * Update user profile
     */
    public User updateUserProfile(Integer userId, String email) throws BusinessException {
        log.info("Updating user profile: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        // Validate email
        if (!accountDomainService.isEmailAvailable(email)) {
            throw new BusinessException(409, "Email already exists");
        }
        
        // Update user
        User updatedUser = user.toBuilder()
            .email(email)
            .build();
        
        User savedUser = userRepository.save(updatedUser);
        
        log.info("User profile updated successfully: {}", userId);
        return savedUser;
    }
    
    /**
     * Deactivate user account
     */
    public void deactivateUser(Integer userId) throws BusinessException {
        log.info("Deactivating user account: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        User deactivatedUser = user.toBuilder()
            .active(false)
            .build();
        
        userRepository.save(deactivatedUser);
        
        log.info("User account deactivated: {}", userId);
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Integer userId) throws BusinessException {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
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
