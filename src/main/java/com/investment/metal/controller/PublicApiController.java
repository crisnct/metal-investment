package com.investment.metal.controller;

import com.investment.metal.common.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.dto.ResetPasswordDto;
import com.investment.metal.dto.SimpleMessageDto;
import com.investment.metal.dto.UserLoginDto;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.MessageKey;
import com.investment.metal.service.AccountService;
import com.investment.metal.service.BannedAccountsService;
import com.investment.metal.service.BlockedIpService;
import com.investment.metal.service.LoginService;
import com.investment.metal.service.exception.ExceptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@Tag(name = "Public API", description = "Public endpoints for user registration, login, and account management")
public class PublicApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicApiController.class);

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
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and sends a validation email with a code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> userRegistration(
            @Parameter(description = "Username for the new account", required = true)
            @RequestHeader("username") String username,
            @Parameter(description = "Password for the new account", required = true)
            @RequestHeader("password") String password,
            @Parameter(description = "Email address for the new account", required = true)
            @RequestHeader("email") String email
    ) {
        try {
            this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
            this.blockedIpService.checkBlockedIPGlobal();
            Customer user = this.accountService.registerNewUser(username, this.passwordEncoder.encode(password), email);
            this.bannedAccountsService.checkBanned(user.getId());
            this.loginService.validateAccount(user, false);

            SimpleMessageDto dto = new SimpleMessageDto("An email was sent to %s with a code. Call validation request with that code", email);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (Exception e) {
            SimpleMessageDto dto = new SimpleMessageDto("Error: " + e.getMessage());
            return new ResponseEntity<>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/validateAccount", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Validate user account",
            description = "Validates a user account using the verification code sent via email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account validated successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid verification code",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> validateAccount(
            @Parameter(description = "Username of the account to validate", required = true)
            @RequestHeader("username") final String username,
            @Parameter(description = "Verification code sent via email", required = true)
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
    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token for API access"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = UserLoginDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "403", description = "Account not validated or banned",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<UserLoginDto> login(
            @Parameter(description = "Username for login", required = true)
            @RequestHeader("username") final String username,
            @Parameter(description = "Password for login", required = true)
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
    @Operation(
            summary = "Reset password",
            description = "Sends a password reset email to the user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent",
                    content = @Content(schema = @Schema(implementation = ResetPasswordDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<ResetPasswordDto> resetPassword(
            @Parameter(description = "Email address of the account to reset password for", required = true)
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
    @Operation(
            summary = "Change password",
            description = "Changes user password using a reset token and verification code"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token/code",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> changePassword(
            @Parameter(description = "Verification code sent via email", required = true)
            @RequestHeader("code") final int code,
            @Parameter(description = "New password", required = true)
            @RequestHeader("newPassword") final String newPassword,
            @Parameter(description = "Email address of the account", required = true)
            @RequestHeader("email") final String email,
            @Parameter(description = "Password reset token", required = true)
            @RequestHeader("token") final String token
    ) {
        this.blockedIpService.checkBlockedIPGlobal();
        this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
        final Customer user = this.accountService.findByEmail(email);
        Integer userId = user.getId();
        this.bannedAccountsService.checkBanned(userId);

        this.loginService.verifyCodeAndToken(userId, code, token);
        this.accountService.updatePassword(user, this.passwordEncoder.encode(newPassword));

        return new ResponseEntity<>(new SimpleMessageDto("Password was changed successfully!"), HttpStatus.OK);
    }

}
