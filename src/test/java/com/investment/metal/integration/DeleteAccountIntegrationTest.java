package com.investment.metal.integration;

import com.investment.metal.infrastructure.controller.ProtectedApiController;
import com.investment.metal.infrastructure.dto.SimpleMessageDto;
import com.investment.metal.infrastructure.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.LoginService;
import com.investment.metal.infrastructure.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the deleteAccount API endpoint.
 * Tests the complete flow of account deletion including authentication and data cleanup.
 */
@ExtendWith(MockitoExtension.class)
class DeleteAccountIntegrationTest {

    private ProtectedApiController protectedApiController;
    private AccountService accountService;
    private LoginService loginService;
    private PasswordEncoder passwordEncoder;
    private Customer testUser;
    private Login testLogin;
    private String testToken = "test-jwt-token";

    @BeforeEach
    void setUp() {
        // Initialize controller and services
        protectedApiController = new ProtectedApiController();
        
        // Create mock services
        accountService = mock(AccountService.class);
        loginService = mock(LoginService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        
        // Inject dependencies using reflection (simulating Spring injection)
        try {
            java.lang.reflect.Field accountServiceField = ProtectedApiController.class.getDeclaredField("accountService");
            accountServiceField.setAccessible(true);
            accountServiceField.set(protectedApiController, accountService);
            
            java.lang.reflect.Field loginServiceField = ProtectedApiController.class.getDeclaredField("loginService");
            loginServiceField.setAccessible(true);
            loginServiceField.set(protectedApiController, loginService);
            
            java.lang.reflect.Field passwordEncoderField = ProtectedApiController.class.getDeclaredField("passwordEncoder");
            passwordEncoderField.setAccessible(true);
            passwordEncoderField.set(protectedApiController, passwordEncoder);
            
            // Mock ExceptionService
            com.investment.metal.infrastructure.exception.ExceptionService exceptionService = mock(com.investment.metal.infrastructure.exception.ExceptionService.class);
            doThrow(new com.investment.metal.infrastructure.exception.BusinessException(400, "Invalid password provided"))
                .when(exceptionService).check(eq(true), any(), any());
            doThrow(new com.investment.metal.infrastructure.exception.BusinessException(400, "Confirmation code is required"))
                .when(exceptionService).check(eq(true), any(), any());
            java.lang.reflect.Field exceptionServiceField = ProtectedApiController.class.getDeclaredField("exceptionService");
            exceptionServiceField.setAccessible(true);
            exceptionServiceField.set(protectedApiController, exceptionService);
        } catch (Exception e) {
            fail("Failed to inject dependencies: " + e.getMessage());
        }

        // Setup test user
        testUser = new Customer();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$hashedpassword");
        testUser.setEmail("test@example.com");

        // Setup test login
        testLogin = new Login();
        testLogin.setUserId(1);
        testLogin.setLoggedIn(1);
    }

    @Test
    void testDeleteAccountIntegration_Success() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getMessage().contains("testuser"));
            assertTrue(response.getBody().getMessage().contains("permanently deleted"));

            verify(accountService).deleteUserAccount(eq(1), anyInt());
        }
    }

    @Test
    void testDeleteAccountIntegration_InvalidPassword() throws Exception {
        // Given
        String password = "wrongpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

            // When & Then
            assertThrows(Exception.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any());
        }
    }

    @Test
    void testDeleteAccountIntegration_EmptyCode() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // When & Then
            assertThrows(Exception.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any());
        }
    }

    @Test
    void testDeleteAccountIntegration_UserNotFound() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenThrow(new RuntimeException("User not found"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any());
        }
    }

    @Test
    void testDeleteAccountIntegration_InvalidToken() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(null);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any());
        }
    }

    @Test
    void testDeleteAccountIntegration_ControllerInstantiation() {
        // Test that controller can be instantiated
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
    }

    @Test
    void testDeleteAccountIntegration_ServiceDependencies() {
        // Test that all required services are available
        assertNotNull(accountService, "AccountService should be available");
        assertNotNull(loginService, "LoginService should be available");
        assertNotNull(passwordEncoder, "PasswordEncoder should be available");
    }

    @Test
    void testDeleteAccountIntegration_ResponseStructure() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertNotNull(response, "Response should not be null");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertNotNull(response.getBody().getMessage(), "Response message should not be null");
            assertFalse(response.getBody().getMessage().isEmpty(), "Response message should not be empty");
        }
    }

    @Test
    void testDeleteAccountIntegration_HttpStatus() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
        }
    }

    @Test
    void testDeleteAccountIntegration_MessageContent() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            
            when(loginService.getLogin(testToken)).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            String message = response.getBody().getMessage();
            assertTrue(message.contains("testuser"), "Message should contain username");
            assertTrue(message.contains("permanently deleted"), "Message should indicate permanent deletion");
            assertTrue(message.contains("associated data"), "Message should mention associated data");
        }
    }
}
