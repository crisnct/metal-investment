package com.investment.metal.domain.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.Util;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.persistence.repository.LoginRepository;
import com.investment.metal.domain.service.encryption.EncryptionService;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.infrastructure.service.AbstractService;
import com.investment.metal.infrastructure.service.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

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

    public void saveAttempt(final Integer userId, final int validationCode) throws BusinessException {
        final Login loginEntity = this.loginRepository.findByUserId(userId).orElse(new Login());
        loginEntity.setTime(new Timestamp(System.currentTimeMillis()));
        loginEntity.setUserId(userId);
        loginEntity.setValidationCode(validationCode);
        loginEntity.setValidated(0);
        loginEntity.setLoggedIn(0);
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
        final int diff = maxValue - minValue;
        final int codeGenerated = minValue + Math.abs(Util.getRandomGenerator().nextInt()) % diff;
        this.emailService.sendMailWithCode(user, codeGenerated);
        this.saveAttempt(user.getId(), codeGenerated);
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
            token = generateToken();
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
            token = generateToken();
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
            return login;
        } else {
            throw exceptionService.createException(MessageKey.WRONG_TOKEN);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public Optional<Login> findByToken(String rawToken) {
        return this.loginRepository.findByLoginToken(encryptionService.encrypt(rawToken));
    }

    public Login findByUserId(Integer userId) {
        return this.loginRepository.findByUserId(userId).orElse(null);
    }

}
