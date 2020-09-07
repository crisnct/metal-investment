package com.investment.metal.service;

import com.investment.metal.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.database.LoginRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.CustomErrorCodes;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultLoginService implements LoginService {

    private static final long BANNED_LOGIN_ATTEMPTS = 24 * 3600 * 1000;

    private static final int MAX_LOGIN_ATTEMPTS_FAILED = 10;

    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 3600 * 1000;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private EmailService emailService;

    @Override
    public void saveAttempt(final long userId, final int validationCode) throws BusinessException {
        final Login loginEntity = this.loginRepository.findByUserId(userId).orElse(new Login());
        loginEntity.setTime(new Timestamp(System.currentTimeMillis()));
        loginEntity.setUserId(userId);
        loginEntity.setValidationCode(validationCode);
        loginEntity.setValidated(false);
        this.loginRepository.save(loginEntity);
    }

    @Override
    @Transactional
    public void validateAccount(Customer user) throws BusinessException {
        final int codeGenerated = 100000 + Util.getRandomGenerator().nextInt(999999);
        try {
            this.emailService.sendMailWithCode(user, codeGenerated);
            this.saveAttempt(user.getId(), codeGenerated);
        } catch (MessagingException e) {
            throw new BusinessException(CustomErrorCodes.MAIL_SERVICE, "Can not send email to " + user.getEmail());
        }
    }

    @Override
    public String verifyCode(long userId, int code) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(userId);
        final String token;
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (login.getValidationCode() == code) {
                token = generateToken();
                login.setToken(token);
                login.setValidated(true);
                login.setFailedAttempts(0);
                this.loginRepository.save(login);
            } else {
                int attempts = login.getFailedAttempts() + 1;
                login.setFailedAttempts(attempts);
                this.loginRepository.save(login);
                if (attempts >= MAX_LOGIN_ATTEMPTS_FAILED) {
                    this.bannedAccountsService.banUser(userId, BANNED_LOGIN_ATTEMPTS, "Too many failed login attempts!");
                    String bannedMessage = "Wrong code. Your account has been banned for 24h";
                    throw new NoRollbackBusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, bannedMessage);
                } else {
                    throw new NoRollbackBusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "Wrong code. Please try again. Number attempts: " + login.getFailedAttempts());
                }
            }
        } else {
            throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "User is not registered");
        }
        return token;
    }

    @Override
    public String login(Customer user) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByUserId(user.getId());
        final String token;
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (!login.getValidated()) {
                throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "The account is not validated!");
            }
            token = generateToken();
            login.setToken(token);
            login.setTime(new Timestamp(System.currentTimeMillis()));
            login.setTokenExpireTime(new Timestamp(System.currentTimeMillis() + TOKEN_EXPIRE_TIME));
            login.setFailedAttempts(0);
            login.setLoggedIn(true);
            this.loginRepository.save(login);
        } else {
            throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "User is not registered");
        }
        return token;
    }

    @Override
    public Login logout(String token) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByToken(token);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            if (!login.getLoggedIn()) {
                throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "The user is not logged in!");
            }
            login.setToken("");
            login.setFailedAttempts(0);
            login.setLoggedIn(false);
            return this.loginRepository.save(login);
        } else {
            throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "Token not found");
        }
    }

    @Override
    public Login checkToken(String token) throws BusinessException {
        Optional<Login> loginOp = this.loginRepository.findByToken(token);
        if (loginOp.isPresent()) {
            Login login = loginOp.get();
            this.bannedAccountsService.checkBanned(login.getUserId());
            if (!login.getValidated()) {
                throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "The account is not validated!");
            }
            if (!login.getLoggedIn()) {
                throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "The user is not logged in!");
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            if (login.getTokenExpireTime().before(currentTime)) {
                login.setFailedAttempts(0);
                login.setLoggedIn(false);
                login.setValidated(false);
                this.loginRepository.save(login);
                throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "The token expired! You need to validate your account");
            }
            return login;
        } else {
            throw new BusinessException(CustomErrorCodes.VALIDATE_ACCOUNT, "Token not found in the database");
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Optional<Login> findByToken(String token) {
        return this.loginRepository.findByToken(token);
    }

}
