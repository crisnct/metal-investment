package com.investment.metal.controller;

import com.investment.metal.common.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Public API", description = "Public endpoints with no authentication")
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
            summary = "Register new user",
            description = "Registers a new user account with username, password, and email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully, validation email sent",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error during registration",
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
            @ApiResponse(responseCode = "400", description = "Invalid verification code or user not found",
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
            description = "Authenticates a user and returns a JWT token along with user details (username and email) for protected endpoints"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token and user details returned",
                    content = @Content(schema = @Schema(implementation = UserLoginDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account banned",
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
        UserLoginDto dto = new UserLoginDto(token, user.getUsername(), user.getEmail());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Reset password",
            description = "Initiates password reset process by sending a reset token to the user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset token generated and sent via email",
                    content = @Content(schema = @Schema(implementation = ResetPasswordDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email address or user not found",
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
            description = "Changes user password using verification code and reset token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid verification code, token, or email",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> changePassword(
            @Parameter(description = "Verification code sent via email", required = true)
            @RequestHeader("code") final int code,
            @Parameter(description = "New password for the account", required = true)
            @RequestHeader("newPassword") final String newPassword,
            @Parameter(description = "Email address of the account", required = true)
            @RequestHeader("email") final String email,
            @Parameter(description = "Reset token received via email", required = true)
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

    @RequestMapping(value = "/checkUserPendingValidation", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Check user validation status",
            description = "Checks if a user exists and is pending email validation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User exists and is pending validation",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "User does not exist or already validated",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> checkUserPendingValidation(
            @Parameter(description = "Username to check", required = true)
            @RequestHeader("username") String username,
            @Parameter(description = "Email address to verify", required = true)
            @RequestHeader("email") String email
    ) {
        try {
            this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
            this.blockedIpService.checkBlockedIPGlobal();
            
            Customer user = this.accountService.findByUsername(username);
            this.bannedAccountsService.checkBanned(user.getId());
            
            // Check if user exists and email matches
            if (user == null) {
                return new ResponseEntity<>(new SimpleMessageDto("User does not exist"), HttpStatus.BAD_REQUEST);
            }
            
            if (!user.getEmail().equals(email)) {
                return new ResponseEntity<>(new SimpleMessageDto("User does not exist"), HttpStatus.BAD_REQUEST);
            }
            
            // Check if user is already validated by checking the Login entity
            Login loginRecord = this.loginService.findByUserId(user.getId());
            if (loginRecord != null && loginRecord.getValidated() != null && loginRecord.getValidated() == 1) {
                return new ResponseEntity<>(new SimpleMessageDto("User already validated"), HttpStatus.BAD_REQUEST);
            }
            
            return new ResponseEntity<>(new SimpleMessageDto("User exists and is pending validation"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new SimpleMessageDto("User does not exist or already validated"), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/resendValidationEmail", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Resend validation email",
            description = "Resends the validation email to a user who is pending account validation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation email sent successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "User already validated or not found",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> resendValidationEmail(
            @Parameter(description = "Username of the account", required = true)
            @RequestHeader("username") String username,
            @Parameter(description = "Email address of the account", required = true)
            @RequestHeader("email") String email
    ) {
        try {
            this.exceptionService.check(!Util.isValidEmailAddress(email), MessageKey.INVALID_REQUEST, "Invalid email address!");
            this.blockedIpService.checkBlockedIPGlobal();
            
            Customer user = this.accountService.findByUsername(username);
            this.bannedAccountsService.checkBanned(user.getId());
            
            // Check if user exists and email matches
            if (user == null || !user.getEmail().equals(email)) {
                return new ResponseEntity<>(new SimpleMessageDto("User not found"), HttpStatus.NOT_FOUND);
            }
            
            // Check if user is already validated by checking the Login entity
            Login loginRecord = this.loginService.findByUserId(user.getId());
            if (loginRecord != null && loginRecord.getValidated() != null && loginRecord.getValidated() == 1) {
                return new ResponseEntity<>(new SimpleMessageDto("User already validated"), HttpStatus.BAD_REQUEST);
            }
            
            // Resend validation email
            this.loginService.validateAccount(user, false);
            
            return new ResponseEntity<>(new SimpleMessageDto("Validation email sent successfully"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new SimpleMessageDto("Error: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/")
    @Operation(
            summary = "Serve React application",
            description = "Serves the main React application HTML page"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "React app served successfully",
                    content = @Content(mediaType = "text/html")),
            @ApiResponse(responseCode = "404", description = "React app not found")
    })
    public ResponseEntity<Resource> serveReactApp() {
      try {
        Resource resource = new ClassPathResource("static/index.html");
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body(resource);
      } catch (Exception e) {
        return ResponseEntity.notFound().build();
      }
    }

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Returns the health status of the application"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Map<String, String> health() {
      Map<String, String> response = new HashMap<>();
      response.put("status", "UP");
      response.put("service", "Metal Investment API");
      response.put("version", "1.0.0");
      return response;
    }

    @GetMapping("/api/health")
    @Operation(
            summary = "API health check",
            description = "Returns the health status of the API with additional information"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API is healthy",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Map<String, String> apiHealth() {
      Map<String, String> response = new HashMap<>();
      response.put("status", "UP");
      response.put("api", "Metal Investment API");
      response.put("swagger", "/swagger-ui.html");
      response.put("docs", "/api-docs");
      return response;
    }
}
