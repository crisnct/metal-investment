package com.investment.metal.domain.service.impl;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.AccountDomainService;
import com.investment.metal.infrastructure.persistence.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of AccountDomainService.
 * Contains business logic for account operations following DDD principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountDomainServiceImpl implements AccountDomainService {
    
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public boolean isValidRegistrationData(String username, String email, String password) {
        if (username == null || email == null || password == null) {
            return false;
        }
        
        // Username validation
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }
        
        // Email validation
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }
        
        // Password validation
        if (password.length() < 6) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        try {
            return customerRepository.findByUsername(username).isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check username availability: {}", username, e);
            return false;
        }
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        try {
            return customerRepository.findByEmail(email).isEmpty();
        } catch (Exception e) {
            log.warn("Failed to check email availability: {}", email, e);
            return false;
        }
    }
    
    @Override
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Check for at least one letter and one number
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        
        return hasLetter && hasNumber;
    }
    
    @Override
    public boolean isAccountActive(User user) {
        return user != null && user.isActive();
    }
    
    @Override
    public boolean isAccountValidated(User user) {
        return user != null && user.isValidated();
    }
    
    @Override
    public boolean canUserRegister(String username, String email) {
        return isUsernameAvailable(username) && isEmailAvailable(email);
    }
    
    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    @Override
    public boolean validatePassword(String password, String hash) {
        return passwordEncoder.matches(password, hash);
    }
}
