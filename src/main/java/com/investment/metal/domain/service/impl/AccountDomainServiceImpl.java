package com.investment.metal.domain.service.impl;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.AccountDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of AccountDomainService.
 * Contains business logic for account operations following DDD principles.
 */
@Slf4j
@Service
public class AccountDomainServiceImpl implements AccountDomainService {
    
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
        // Domain service should not check data availability - this is application layer concern
        // This method should be moved to application service
        throw new UnsupportedOperationException("Data availability checks should be handled by application layer");
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        // Domain service should not check data availability - this is application layer concern
        // This method should be moved to application service
        throw new UnsupportedOperationException("Data availability checks should be handled by application layer");
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
        // Domain service should not check data availability - this is application layer concern
        // This method should be moved to application service
        throw new UnsupportedOperationException("Data availability checks should be handled by application layer");
    }
    
    @Override
    public String hashPassword(String password) {
        // Password hashing should be handled by infrastructure services
        // Domain services should focus on business rules only
        throw new UnsupportedOperationException("Password hashing should be handled by infrastructure layer");
    }
    
    @Override
    public boolean validatePassword(String password, String hash) {
        // Password validation should be handled by infrastructure services
        // Domain services should focus on business rules only
        throw new UnsupportedOperationException("Password validation should be handled by infrastructure layer");
    }
}
