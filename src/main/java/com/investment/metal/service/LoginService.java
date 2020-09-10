package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.common.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.database.LoginRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService extends AbstractService {

    private static final long BANNED_LOGIN_ATTEMPTS = 24 * 3600 * 1000;

    private static final int MAX_LOGIN_ATTEMPTS_FAILED = 10;

    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 3600 * 1000;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private EmailService emailService;

    public void saveAttempt(final long userId, final int validationCode) throws BusinessException {
        final Login loginEntity = this.loginRepository.findByUserId(userId).orElse(new Login());
        loginEntity.setTime(new Timestamp(System.currentTimeMillis()));
        loginEntity.setUserId(userId);
        loginEntity.setValidationCode(validationCode);
        loginEntity.setValidated(false);
        loginEntity.setLoggedIn(false);
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
        try {
            this.emailService.sendMailWithCode(user, codeGenerated);
            this.saveAttempt(user.getId(), codeGenerated);
        } catch (MessagingException e) {
            throw this.exceptionService
                    .createBuilder(MessageKey.CAN_NOT_SEND_MAIL)
                    .setArguments(user.getEmail())
                    .setExceptionCause(e)
                    .build();
        }
    }

    public void verifyCodeAndToken(long userId, int code, String token) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(userId);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (login.getValidationCode() == code && StringUtils.equals(login.getResetPasswordToken(), token)) {
                login.setValidated(true);
                login.setFailedAttempts(0);
                this.loginRepository.save(login);
            } else {
                this.markLoginFailed(userId);
            }
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
    }

    public void verifyCode(long userId, int code) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(userId);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (login.getValidationCode() == code) {
                login.setValidated(true);
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
            if (!login.getValidated()) {
                throw exceptionService.createException(MessageKey.NEEDS_VALIDATION);
            }
            token = generateToken();
            login.setLoginToken(token);
            login.setTime(new Timestamp(System.currentTimeMillis()));
            login.setTokenExpireTime(new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRE_TIME));
            login.setFailedAttempts(0);
            login.setLoggedIn(true);
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
            login.setResetPasswordToken(token);
            this.loginRepository.save(login);
        } else {
            throw exceptionService.createException(MessageKey.USER_NOT_REGISTERED);
        }
        return token;
    }

    public void markLoginFailed(long userId) throws BusinessException {
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

    public Login logout(Login login){
        login.setLoginToken("");
        login.setResetPasswordToken("");
        login.setLoggedIn(false);
        return this.loginRepository.save(login);
    }

    public Login checkToken(String token) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByLoginToken(token);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            this.bannedAccountsService.checkBanned(login.getUserId());
            if (!login.getValidated()) {
                throw exceptionService.createException(MessageKey.NEEDS_VALIDATION);
            }
            if (!login.getLoggedIn()) {
                throw exceptionService.createException(MessageKey.USER_NOT_LOGIN);
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (login.getTokenExpireTime().before(currentTime)) {
                login.setFailedAttempts(0);
                login.setLoggedIn(false);
                login.setValidated(false);
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

    public Optional<Login> findByToken(String token) {
        return this.loginRepository.findByLoginToken(token);
    }

}
