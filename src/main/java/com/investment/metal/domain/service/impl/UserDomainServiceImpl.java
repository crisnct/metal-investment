package com.investment.metal.domain.service.impl;

import com.investment.metal.domain.model.User;
import com.investment.metal.domain.service.UserDomainService;
import com.investment.metal.infrastructure.persistence.repository.BannedRepository;
import com.investment.metal.infrastructure.persistence.repository.BanIpRepository;
import com.investment.metal.infrastructure.persistence.repository.LoginRepository;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.persistence.entity.BannedAccount;
import com.investment.metal.infrastructure.persistence.entity.BanIp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserDomainService.
 * Contains business logic for user operations following DDD principles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {
    
    private final PasswordEncoder passwordEncoder;
    private final BannedRepository bannedRepository;
    private final BanIpRepository banIpRepository;
    private final LoginRepository loginRepository;
    
    private static final int MAX_FAILED_ATTEMPTS = 10;
    private static final long BAN_DURATION_MS = 24 * 3600 * 1000; // 24 hours
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 3600 * 1000; // 7 days
    
    @Override
    public boolean validateCredentials(User user, String password) {
        if (user == null || password == null) {
            return false;
        }
        
        // Note: In a real implementation, you'd get the password from the Customer entity
        // For now, we'll assume password validation is handled elsewhere
        // This is a simplified implementation - in practice, you'd need to get the hashed password
        // from the Customer entity and compare it with the provided password
        return true; // Simplified for now - implement proper password validation
    }
    
    @Override
    public boolean canUserPerformActions(User user) {
        if (user == null) {
            return false;
        }
        
        return user.canPerformActions() && 
               !isUserBanned(user) && 
               !hasExceededMaxFailedAttempts(user);
    }
    
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
    public boolean isUserBanned(User user) {
        if (user == null) {
            return false;
        }
        
        Optional<BannedAccount> bannedOpt = bannedRepository.findByUserId(user.getId());
        if (bannedOpt.isEmpty()) {
            return false;
        }
        
        BannedAccount banned = bannedOpt.get();
        boolean isBanned = banned.getBannedUntil().getTime() > System.currentTimeMillis();
        
        // Clean up expired bans
        if (!isBanned) {
            bannedRepository.delete(banned);
        }
        
        return isBanned;
    }
    
    @Override
    public boolean isIpBlockedForUser(User user, String ipAddress) {
        if (user == null || ipAddress == null) {
            return false;
        }
        
        Optional<BanIp> banOpt = banIpRepository.findByUserIdAndIp(user.getId(), ipAddress);
        if (banOpt.isEmpty()) {
            return false;
        }
        
        BanIp ban = banOpt.get();
        boolean isBlocked = ban.getBlockedUntil().getTime() > System.currentTimeMillis();
        
        // Clean up expired bans
        if (!isBlocked) {
            banIpRepository.delete(ban);
        }
        
        return isBlocked;
    }
    
    @Override
    public void recordFailedAttempt(User user, String ipAddress) {
        if (user == null) {
            return;
        }
        
        Optional<Login> loginOpt = loginRepository.findByUserId(user.getId());
        Login login = loginOpt.orElse(new Login());
        
        int attempts = login.getFailedAttempts() + 1;
        login.setUserId(user.getId());
        login.setFailedAttempts(attempts);
        login.setTime(new Timestamp(System.currentTimeMillis()));
        
        loginRepository.save(login);
        
        // Ban user if exceeded max attempts
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            banUser(user.getId(), BAN_DURATION_MS, "Too many failed login attempts");
        }
    }
    
    @Override
    public boolean hasExceededMaxFailedAttempts(User user) {
        if (user == null) {
            return false;
        }
        
        Optional<Login> loginOpt = loginRepository.findByUserId(user.getId());
        if (loginOpt.isEmpty()) {
            return false;
        }
        
        return loginOpt.get().getFailedAttempts() >= MAX_FAILED_ATTEMPTS;
    }
    
    @Override
    public int generateValidationCode(User user) {
        // Generate 6-digit validation code
        return 100000 + (int) (Math.random() * 900000);
    }
    
    @Override
    public boolean validateUserAccount(User user, int code) {
        if (user == null) {
            return false;
        }
        
        Optional<Login> loginOpt = loginRepository.findByUserId(user.getId());
        if (loginOpt.isEmpty()) {
            return false;
        }
        
        Login login = loginOpt.get();
        if (login.getValidationCode() == code) {
            login.setValidated(1);
            login.setFailedAttempts(0);
            loginRepository.save(login);
            return true;
        }
        
        return false;
    }
    
    @Override
    public String generateLoginToken(User user) {
        String token = UUID.randomUUID().toString();
        
        Optional<Login> loginOpt = loginRepository.findByUserId(user.getId());
        Login login = loginOpt.orElse(new Login());
        
        login.setUserId(user.getId());
        login.setLoginToken(token);
        login.setTime(new Timestamp(System.currentTimeMillis()));
        login.setTokenExpireTime(new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRE_TIME));
        login.setLoggedIn(1);
        login.setFailedAttempts(0);
        
        loginRepository.save(login);
        
        return token;
    }
    
    @Override
    public String generatePasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();
        
        Optional<Login> loginOpt = loginRepository.findByUserId(user.getId());
        Login login = loginOpt.orElse(new Login());
        
        login.setUserId(user.getId());
        login.setResetPasswordToken(token);
        login.setTime(new Timestamp(System.currentTimeMillis()));
        
        loginRepository.save(login);
        
        return token;
    }
    
    @Override
    public Optional<User> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<Login> loginOpt = loginRepository.findByLoginToken(token);
        if (loginOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Login login = loginOpt.get();
        
        // Check if token is expired
        if (login.getTokenExpireTime() != null && 
            login.getTokenExpireTime().getTime() < System.currentTimeMillis()) {
            return Optional.empty();
        }
        
        // Check if user is logged in
        if (login.getLoggedIn() == null || login.getLoggedIn() == 0) {
            return Optional.empty();
        }
        
        // Check if user is validated
        if (login.getValidated() == null || login.getValidated() == 0) {
            return Optional.empty();
        }
        
        // Return user (you'd need to fetch user details from database)
        // For now, return empty as we need to implement user lookup
        return Optional.empty();
    }
    
    @Override
    public void logoutUser(User user) {
        if (user == null) {
            return;
        }
        
        Optional<Login> loginOpt = loginRepository.findByUserId(user.getId());
        if (loginOpt.isPresent()) {
            Login login = loginOpt.get();
            login.setLoginToken("");
            login.setResetPasswordToken("");
            login.setLoggedIn(0);
            loginRepository.save(login);
        }
    }
    
    private void banUser(Integer userId, long duration, String reason) {
        BannedAccount banned = new BannedAccount();
        banned.setUserId(userId);
        banned.setBannedUntil(new Timestamp(System.currentTimeMillis() + duration));
        banned.setReason(reason);
        bannedRepository.save(banned);
    }
}
