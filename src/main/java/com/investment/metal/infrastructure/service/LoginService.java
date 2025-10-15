package com.investment.metal.infrastructure.service;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.exception.NoRollbackBusinessException;
import com.investment.metal.infrastructure.encryption.EncryptionService;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.persistence.repository.LoginRepository;
import com.investment.metal.infrastructure.security.JwtService;
import com.investment.metal.infrastructure.util.SecureRandomGenerator;
import java.sql.Timestamp;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing user login and authentication operations.
 * Handles JWT token generation, validation, and user session management.
 * 
 * @author cristian.tone
 */
@Service
public class LoginService extends AbstractService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    private static final long BANNED_LOGIN_ATTEMPTS = 24 * 3600 * 1000;

    private static final int MAX_LOGIN_ATTEMPTS_FAILED = 10;

    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 3600 * 1000;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private BlockedIpService blockedIpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private JwtService jwtService;

    public void saveLoginAttempt(final Integer userId, final int validationCode) throws BusinessException {
        final Login loginEntity = this.loginRepository.findByUserId(userId).orElse(new Login());
        loginEntity.setTime(new Timestamp(System.currentTimeMillis()));
        loginEntity.setUserId(userId);
        loginEntity.setValidationCode(validationCode);
        loginEntity.setValidated(0);
        loginEntity.setLoggedIn(0);
        this.loginRepository.save(loginEntity);
    }

    public void saveDeletionAttempt(final Integer userId, final int validationCode) throws BusinessException {
      final Login loginEntity = this.loginRepository.findByUserId(userId).orElse(new Login());
      loginEntity.setTime(new Timestamp(System.currentTimeMillis()));
      loginEntity.setValidationCode(validationCode);
      this.loginRepository.save(loginEntity);
    }

    public void validateAccount(Customer user, boolean strongCode) throws BusinessException {
        if (strongCode) {
            this.validateAccount(user, 100000000, 899999999);
        } else {
            this.validateAccount(user, 100000, 899999);
        }
    }

    private void validateAccount(Customer user, int minValue, int maxValue) throws BusinessException {
        // SECURITY FIX: Use secure random generation instead of Math.abs(Random.nextInt())
        // This prevents predictable patterns and ensures uniform distribution
        final int codeGenerated = SecureRandomGenerator.nextIntInclusive(minValue, maxValue);
        this.emailService.sendMailWithCode(user, codeGenerated);
        this.saveLoginAttempt(user.getId(), codeGenerated);
    }

    public void verifyCodeAndToken(Integer userId, int code, String rawToken) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(userId);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            String encryptedToken = encryptionService.encrypt(rawToken);
            if (login.getValidationCode() == code && StringUtils.equals(login.getResetPasswordToken(), encryptedToken)) {
                login.setValidated(1);
                login.setFailedAttempts(0);
                this.loginRepository.save(login);
            } else {
                this.markLoginFailed(userId);
            }
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
    }

    public void verifyCode(Integer userId, int code) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(userId);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (login.getValidationCode() == code) {
                login.setValidated(1);
                login.setFailedAttempts(0);
                this.loginRepository.save(login);
            } else {
                this.markLoginFailed(userId);
            }
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
    }

    public String login(Customer user) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(user.getId());
        final String token;
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (login.getValidated() == null || login.getValidated() == 0) {
                throw exceptionService.createException(MessageKey.NEEDS_VALIDATION);
            }
            
            // SECURITY FIX: Generate unique session ID for session rotation
            String sessionId = java.util.UUID.randomUUID().toString();
            
            // Generate secure JWT token with session information
            token = jwtService.generateTokenWithSession(user.getId(), sessionId);
            login.setLoginToken(encryptionService.encrypt(token));
            login.setTime(new Timestamp(System.currentTimeMillis()));
            login.setTokenExpireTime(new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRE_TIME));
            login.setFailedAttempts(0);
            login.setLoggedIn(1);
            this.loginRepository.save(login);
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
        return token;
    }

    public String generateResetPasswordToken(Customer user) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(user.getId());
        final String token;
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            // Generate secure JWT token for password reset
            token = jwtService.generateToken(user.getId());
            login.setResetPasswordToken(encryptionService.encrypt(token));
            this.loginRepository.save(login);
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
        return token;
    }

    public void markLoginFailed(Integer userId) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(userId);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            int attempts = login.getFailedAttempts() + 1;
            login.setFailedAttempts(attempts);

            if (attempts >= MAX_LOGIN_ATTEMPTS_FAILED) {
                this.bannedAccountsService.banUser(userId, BANNED_LOGIN_ATTEMPTS, "Too many failed login attempts!");
                throw exceptionService
                        .createBuilder(MessageKey.WRONG_CODE_ACCOUNT_BANNED)
                        .setException(NoRollbackBusinessException::new)
                        .build();
            } else {
                throw exceptionService.createBuilder(MessageKey.FAILED_LOGIN_VALIDATION)
                        .setArguments(login.getFailedAttempts())
                        .setException(NoRollbackBusinessException::new)
                        .build();
            }
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
    }

    public void logout(Login login) {
        login.setLoginToken("");
        login.setResetPasswordToken("");
        login.setLoggedIn(0);
        this.loginRepository.save(login);
    }

    /**
     * Invalidate all sessions for a specific user.
     * This method should be called when:
     * - User changes password
     * - User account is deleted
     * - Security breach is detected
     * - User requests logout from all devices
     * 
     * @param userId the user ID to invalidate all sessions for
     */
    public void invalidateAllUserSessions(Integer userId) {
        LOGGER.info("Invalidating all sessions for user ID: {}", userId);
        
        Optional<Login> loginOpt = this.loginRepository.findByUserId(userId);
        if (loginOpt.isPresent()) {
            Login login = loginOpt.get();
            login.setLoginToken("");
            login.setResetPasswordToken("");
            login.setLoggedIn(0);
            login.setValidated(0); // Force re-validation
            this.loginRepository.save(login);
            LOGGER.info("All sessions invalidated for user ID: {}", userId);
        }
    }

    /**
     * Invalidate all sessions for a specific user except the current one.
     * This method allows users to logout from all other devices while keeping
     * the current session active.
     * 
     * @param userId the user ID to invalidate sessions for
     * @param currentToken the current valid token to keep active
     */
    public void invalidateAllOtherUserSessions(Integer userId, String currentToken) {
        LOGGER.info("Invalidating all other sessions for user ID: {} (keeping current session)", userId);
        
        Optional<Login> loginOpt = this.loginRepository.findByUserId(userId);
        if (loginOpt.isPresent()) {
            Login login = loginOpt.get();
            // Only invalidate if the current token is different from the one being kept
            String encryptedCurrentToken = this.encryptionService.encrypt(currentToken);
            if (!encryptedCurrentToken.equals(login.getLoginToken())) {
                login.setLoginToken("");
                login.setResetPasswordToken("");
                login.setLoggedIn(0);
                this.loginRepository.save(login);
                LOGGER.info("All other sessions invalidated for user ID: {}", userId);
            }
        }
    }

    /**
     * Force logout a user by invalidating their current session.
     * This method should be called for security purposes or administrative actions.
     * 
     * @param userId the user ID to force logout
     */
    public void forceLogoutUser(Integer userId) {
        LOGGER.info("Force logging out user ID: {}", userId);
        this.invalidateAllUserSessions(userId);
    }

    /**
     * Check if a user has any active sessions.
     * 
     * @param userId the user ID to check
     * @return true if user has active sessions, false otherwise
     */
    public boolean hasActiveSessions(Integer userId) {
        Optional<Login> loginOpt = this.loginRepository.findByUserId(userId);
        if (loginOpt.isPresent()) {
            Login login = loginOpt.get();
            return login.getLoggedIn() != null && login.getLoggedIn() == 1 && 
                   StringUtils.isNotBlank(login.getLoginToken());
        }
        return false;
    }

    /**
     * Get session information for a user.
     * 
     * @param userId the user ID
     * @return Login entity with session information, or null if not found
     */
    public Login getUserSessionInfo(Integer userId) {
        return this.loginRepository.findByUserId(userId).orElse(null);
    }

    public Login getLogin(String token) throws BusinessException {
        Optional<Login> loginOp = this.findByToken(token);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            Integer userId = login.getUserId();
            this.bannedAccountsService.checkBanned(userId);
            this.blockedIpService.checkBlockedIP(userId);

            if (login.getValidated() == null || login.getValidated() == 0) {
                throw exceptionService.createException(MessageKey.NEEDS_VALIDATION);
            }
            if (login.getLoggedIn() == null || login.getLoggedIn() == 0) {
                throw exceptionService.createException(MessageKey.USER_NOT_LOGIN);
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (login.getTokenExpireTime().before(currentTime)) {
                login.setFailedAttempts(0);
                login.setLoggedIn(0);
                login.setValidated(0);
                this.loginRepository.save(login);
                throw exceptionService.createException(MessageKey.EXPIRED_TOKEN);
            }
            
            // SECURITY FIX: Validate JWT token and session information
            if (!jwtService.isTokenValid(token)) {
                throw exceptionService.createException(MessageKey.EXPIRED_TOKEN);
            }
            
            // Check if token is an access token
            if (!jwtService.isAccessToken(token)) {
                throw exceptionService.createException(MessageKey.WRONG_TOKEN);
            }
            
            return login;
        } else {
            throw exceptionService.createException(MessageKey.WRONG_TOKEN);
        }
    }


    public Optional<Login> findByToken(String rawToken) {
        return this.loginRepository.findByLoginToken(encryptionService.encrypt(rawToken));
    }

    public Login findByUserId(Integer userId) {
        return this.loginRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Get login entity by token without validation checks.
     * Used for account deletion where validation status should not matter.
     * 
     * @param rawToken the raw JWT token
     * @return Login entity if found, null otherwise
     */
    public Login getLoginForDeletion(String rawToken) {
        Optional<Login> loginOp = this.loginRepository.findByLoginToken(encryptionService.encrypt(rawToken));
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            // Check if token is expired
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (login.getTokenExpireTime() != null && login.getTokenExpireTime().before(currentTime)) {
                throw exceptionService.createException(MessageKey.EXPIRED_TOKEN);
            }
            return login;
        } else {
            throw exceptionService.createException(MessageKey.WRONG_TOKEN);
        }
    }

    /**
     * Get login entity with pre-captured client IP for async contexts.
     * This method bypasses the request context access for IP checking.
     * 
     * @param rawToken the raw JWT token
     * @param clientIp the pre-captured client IP address
     * @return the login entity
     * @throws BusinessException if token is invalid or user is banned/blocked
     */
    public Login getLoginWithIp(String rawToken, String clientIp) {
        Optional<Login> loginOp = this.findByToken(rawToken);
        if (loginOp.isEmpty()) {
            throw exceptionService.createException(MessageKey.WRONG_TOKEN);
        }
        
        Login login = loginOp.get();
        Integer userId = login.getUserId();
        this.bannedAccountsService.checkBanned(userId);
        this.blockedIpService.checkBlockedIPWithIp(userId, clientIp);

        if (login.getValidated() == null || login.getValidated() == 0) {
            throw exceptionService.createException(MessageKey.NEEDS_VALIDATION);
        }
        if (login.getLoggedIn() == null || login.getLoggedIn() == 0) {
            throw exceptionService.createException(MessageKey.USER_NOT_LOGIN);
        }
        return login;
    }

}
