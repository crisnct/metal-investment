package com.investment.metal;

import com.investment.metal.common.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.dto.ResetPasswordDto;
import com.investment.metal.dto.SimpleMessageDto;
import com.investment.metal.dto.UserLoginDto;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.service.AccountService;
import com.investment.metal.service.BannedAccountsService;
import com.investment.metal.service.BlockedIpService;
import com.investment.metal.service.LoginService;
import com.investment.metal.service.exception.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicApiController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private BlockedIpService blockedIpService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ExceptionService exceptionService;

    @RequestMapping(value = "/userRegistration", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> userRegistration(
            @RequestHeader("username") String username,
            @RequestHeader("password") String password,
            @RequestHeader("email") String email
    ) {
        this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
        this.blockedIpService.checkBlockedIPGlobal();
        Customer user = this.accountService.registerNewUser(username, this.passwordEncoder.encode(password), email);
        this.bannedAccountsService.checkBanned(user.getId());
        this.loginService.validateAccount(user, false);

        SimpleMessageDto dto = new SimpleMessageDto("An email was sent to %s with a code. Call validation request with that code", email);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/validateAccount", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> validateAccount(
            @RequestHeader("username") final String username,
            @RequestHeader("code") final int code
    ) {
        this.blockedIpService.checkBlockedIPGlobal();
        Customer user = this.accountService.findByUsername(username);
        this.bannedAccountsService.checkBanned(user.getId());
        this.loginService.verifyCode(user.getId(), code);

        SimpleMessageDto dto = new SimpleMessageDto("The account was validated. You can log in now.");
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<UserLoginDto> login(
            @RequestHeader("username") final String username,
            @RequestHeader("password") final String password
    ) {
        this.blockedIpService.checkBlockedIPGlobal();
        final Customer user = this.accountService.findByUsername(username);
        this.bannedAccountsService.checkBanned(user.getId());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            this.loginService.markLoginFailed(user.getId());
        }

        String token = this.loginService.login(user);
        UserLoginDto dto = new UserLoginDto(token);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<ResetPasswordDto> resetPassword(
            @RequestHeader("email") final String email
    ) {
        this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
        this.blockedIpService.checkBlockedIPGlobal();
        final Customer user = this.accountService.findByEmail(email);
        this.bannedAccountsService.checkBanned(user.getId());
        this.loginService.validateAccount(user, true);

        String token = this.loginService.generateResetPasswordToken(user);
        String message = "A message with a code was sent to " + email;
        return new ResponseEntity<>(new ResetPasswordDto(token, message), HttpStatus.OK);
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> changePassword(
            @RequestHeader("code") final int code,
            @RequestHeader("newPassword") final String newPassword,
            @RequestHeader("email") final String email,
            @RequestHeader("token") final String token
    ) {
        this.blockedIpService.checkBlockedIPGlobal();
        this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
        final Customer user = this.accountService.findByEmail(email);
        Long userId = user.getId();
        this.bannedAccountsService.checkBanned(userId);

        this.loginService.verifyCodeAndToken(userId, code, token);
        this.accountService.updatePassword(user, this.passwordEncoder.encode(newPassword));

        return new ResponseEntity<>(new SimpleMessageDto("Password was changed successfully!"), HttpStatus.OK);
    }

}
