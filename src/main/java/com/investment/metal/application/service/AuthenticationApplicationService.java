package com.investment.metal.application.service;

import com.investment.metal.application.repository.TokenRepository;
import com.investment.metal.application.repository.UserRepository;
import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.AuthenticationDomainService;
import com.investment.metal.domain.valueobject.Token;
import com.investment.metal.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for authentication use cases.
 * Orchestrates domain services and handles application-level concerns.
 * Follows Clean Architecture principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationApplicationService {

    private final AuthenticationDomainService authenticationDomainService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    /**
     * Authenticate user with username and password
     */
    public Token authenticateUser(String username, String password, String ipAddress) throws BusinessException {
        log.info("Authenticating user: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        if (!authenticationDomainService.validateCredentials(user, password)) {
            authenticationDomainService.recordFailedAttempt(user.getId(), ipAddress);
            throw new BusinessException(401, "Invalid credentials");
        }
        
        if (authenticationDomainService.isUserBanned(user.getId())) {
            throw new BusinessException(403, "User account is banned");
        }
        
        if (authenticationDomainService.isIpBlocked(ipAddress)) {
            throw new BusinessException(403, "IP address is blocked");
        }
        
        Token token = authenticationDomainService.generateToken(user);
        tokenRepository.save(token);
        
        log.info("User {} authenticated successfully", username);
        return token;
    }

    /**
     * Validate token and return user
     */
    @Transactional(readOnly = true)
    public User validateToken(String tokenValue) throws BusinessException {
        log.debug("Validating token");
        
        Optional<User> user = authenticationDomainService.validateToken(tokenValue);
        if (user.isEmpty()) {
            throw new BusinessException(401, "Invalid token");
        }
        
        return user.get();
    }

    /**
     * Register new user
     */
    public User registerUser(String username, String email, String password) throws BusinessException {
        log.info("Registering new user: {}", username);
        
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException(409, "Username already exists");
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(409, "Email already exists");
        }
        
        User user = User.builder()
            .username(username)
            .email(email)
            .validated(false)
            .active(true)
            .build();
        
        user = userRepository.save(user);
        
        // Generate validation code
        int validationCode = authenticationDomainService.generateValidationCode(false);
        // TODO: Send validation email with validationCode
        log.debug("Generated validation code: {} for user: {}", validationCode, username);
        
        log.info("User {} registered successfully", username);
        return user;
    }

    /**
     * Validate user account with code
     */
    public void validateAccount(Integer userId, int code) throws BusinessException {
        log.info("Validating account for user: {}", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        authenticationDomainService.validateAccount(user, code);
        
        // Update user as validated
        user = user.toBuilder().validated(true).build();
        userRepository.save(user);
        
        log.info("Account validated for user: {}", userId);
    }

    /**
     * Logout user
     */
    public void logout(String tokenValue) {
        log.info("Logging out user");
        tokenRepository.deleteByValue(tokenValue);
    }

    /**
     * Reset password
     */
    public void resetPassword(String email) throws BusinessException {
        log.info("Resetting password for email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(404, "User not found"));
        
        Token resetToken = Token.createResetPasswordToken(user.getId());
        tokenRepository.save(resetToken);
        
        // TODO: Send reset password email with token
        log.info("Password reset token generated for user: {}", user.getUsername());
    }
}
