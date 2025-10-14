package com.investment.metal.controller;

import com.investment.metal.application.service.AuthenticationApplicationService;
import com.investment.metal.domain.model.User;
import com.investment.metal.domain.valueobject.Token;
import com.investment.metal.dto.UserLoginDto;
import com.investment.metal.dto.SimpleMessageDto;
import com.investment.metal.exceptions.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for authentication endpoints.
 * Follows Clean Architecture principles by delegating to application services.
 * Handles only HTTP concerns, not business logic.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthenticationController {

    private final AuthenticationApplicationService authenticationService;

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with username and password, returns JWT token along with user details (username and email)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token and user details returned",
            content = @Content(schema = @Schema(implementation = UserLoginDto.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
        @ApiResponse(responseCode = "403", description = "Account banned or IP blocked",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<?> login(
        @Parameter(description = "Username for login", required = true)
        @RequestHeader("username") String username,
        @Parameter(description = "Password for login", required = true)
        @RequestHeader("password") String password,
        HttpServletRequest request) {
        
        try {
            String ipAddress = getClientIpAddress(request);
            Token token = authenticationService.authenticateUser(username, password, ipAddress);
            
            // Get user details for response
            User user = authenticationService.validateToken(token.getValue());
            UserLoginDto response = new UserLoginDto(token.getValue(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            log.warn("Login failed for user: {}, reason: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SimpleMessageDto(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleMessageDto("Internal server error"));
        }
    }

    @PostMapping("/register")
    @Operation(
        summary = "User registration",
        description = "Register new user account"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration data",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
        @ApiResponse(responseCode = "409", description = "Username or email already exists",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> register(
        @Parameter(description = "Username", required = true)
        @RequestHeader("username") String username,
        @Parameter(description = "Email", required = true)
        @RequestHeader("email") String email,
        @Parameter(description = "Password", required = true)
        @RequestHeader("password") String password) {
        
        try {
            authenticationService.registerUser(username, email, password);
            return ResponseEntity.ok(new SimpleMessageDto("User registered successfully. Please check your email for validation."));
            
        } catch (BusinessException e) {
            log.warn("Registration failed for user: {}, reason: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new SimpleMessageDto(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration for user: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleMessageDto("Internal server error"));
        }
    }

    @PostMapping("/validate")
    @Operation(
        summary = "Validate account",
        description = "Validate user account with validation code"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account validated successfully",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid validation code",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> validateAccount(
        @Parameter(description = "User ID", required = true)
        @RequestHeader("userId") Integer userId,
        @Parameter(description = "Validation code", required = true)
        @RequestHeader("code") Integer code) {
        
        try {
            authenticationService.validateAccount(userId, code);
            return ResponseEntity.ok(new SimpleMessageDto("Account validated successfully"));
            
        } catch (BusinessException e) {
            log.warn("Account validation failed for user: {}, reason: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SimpleMessageDto(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during account validation for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleMessageDto("Internal server error"));
        }
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logout user and invalidate token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful",
            content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> logout(
        @Parameter(description = "Authentication token", required = true)
        @RequestHeader("Authorization") String token) {
        
        try {
            String tokenValue = extractTokenFromHeader(token);
            authenticationService.logout(tokenValue);
            return ResponseEntity.ok(new SimpleMessageDto("Logout successful"));
            
        } catch (Exception e) {
            log.error("Unexpected error during logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new SimpleMessageDto("Internal server error"));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}
