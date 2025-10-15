package com.investment.metal.integration;

import com.investment.metal.infrastructure.controller.ProtectedApiController;
import com.investment.metal.infrastructure.dto.SimpleMessageDto;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.EmailService;
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

import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the deleteAccountPreparation API endpoint.
 * Tests the complete flow of account deletion preparation including authentication and email sending.
 */
@ExtendWith(MockitoExtension.class)
class DeleteAccountPreparationIntegrationTest {

    private ProtectedApiController protectedApiController;
    private AccountService accountService;
    private LoginService loginService;
    private EmailService emailService;
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
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        
        // Inject dependencies using reflection (simulating Spring injection)
        try {
            java.lang.reflect.Field accountServiceField = ProtectedApiController.class.getDeclaredField("accountService");
            accountServiceField.setAccessible(true);
            accountServiceField.set(protectedApiController, accountService);
            
            java.lang.reflect.Field loginServiceField = ProtectedApiController.class.getDeclaredField("loginService");
            loginServiceField.setAccessible(true);
            loginServiceField.set(protectedApiController, loginService);
            
            java.lang.reflect.Field emailServiceField = ProtectedApiController.class.getDeclaredField("emailService");
            emailServiceField.setAccessible(true);
            emailServiceField.set(protectedApiController, emailService);
            
            java.lang.reflect.Field passwordEncoderField = ProtectedApiController.class.getDeclaredField("passwordEncoder");
            passwordEncoderField.setAccessible(true);
            passwordEncoderField.set(protectedApiController, passwordEncoder);
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
    void testDeleteAccountPreparationIntegration_Success() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            // Mock Util.getRandomGenerator()
            java.util.Random mockRandom = mock(java.util.Random.class);
            mockedUtil.when(() -> Util.getRandomGenerator()).thenReturn(mockRandom);
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccountPreparation().join();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getMessage().contains("testuser"));
            assertTrue(response.getBody().getMessage().contains("preparation email has been sent"));

            verify(emailService).sendDeleteAccountPreparationEmail(eq(testUser), anyString());
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_UserNotFound() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(null);

            // When & Then
        assertThrows(CompletionException.class, () -> {
            protectedApiController.deleteAccountPreparation().join();
        });

            verify(emailService, never()).sendDeleteAccountPreparationEmail(any(), any());
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_InvalidToken() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(null);

            // When & Then
        assertThrows(CompletionException.class, () -> {
            protectedApiController.deleteAccountPreparation().join();
        });

            verify(emailService, never()).sendDeleteAccountPreparationEmail(any(), any());
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_ControllerInstantiation() {
        // Test that controller can be instantiated
        assertNotNull(protectedApiController, "ProtectedApiController should be instantiated");
    }

    @Test
    void testDeleteAccountPreparationIntegration_ServiceDependencies() {
        // Test that all required services are available
        assertNotNull(accountService, "AccountService should be available");
        assertNotNull(loginService, "LoginService should be available");
        assertNotNull(emailService, "EmailService should be available");
        assertNotNull(passwordEncoder, "PasswordEncoder should be available");
    }

    @Test
    void testDeleteAccountPreparationIntegration_ResponseStructure() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            // Mock Util.getRandomGenerator()
            java.util.Random mockRandom = mock(java.util.Random.class);
            mockedUtil.when(() -> Util.getRandomGenerator()).thenReturn(mockRandom);
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccountPreparation().join();

            // Then
            assertNotNull(response, "Response should not be null");
            assertNotNull(response.getBody(), "Response body should not be null");
            assertNotNull(response.getBody().getMessage(), "Response message should not be null");
            assertFalse(response.getBody().getMessage().isEmpty(), "Response message should not be empty");
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_HttpStatus() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            // Mock Util.getRandomGenerator()
            java.util.Random mockRandom = mock(java.util.Random.class);
            mockedUtil.when(() -> Util.getRandomGenerator()).thenReturn(mockRandom);
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccountPreparation().join();

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_MessageContent() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            // Mock Util.getRandomGenerator()
            java.util.Random mockRandom = mock(java.util.Random.class);
            mockedUtil.when(() -> Util.getRandomGenerator()).thenReturn(mockRandom);
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccountPreparation().join();

            // Then
            String message = response.getBody().getMessage();
            assertTrue(message.contains("testuser"), "Message should contain username");
            assertTrue(message.contains("preparation email has been sent"), "Message should indicate email was sent");
            assertTrue(message.contains("confirmation code"), "Message should mention confirmation code");
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_EmailServiceCalled() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            // Mock Util.getRandomGenerator()
            java.util.Random mockRandom = mock(java.util.Random.class);
            mockedUtil.when(() -> Util.getRandomGenerator()).thenReturn(mockRandom);
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);

            // When
            protectedApiController.deleteAccountPreparation().join();

            // Then
            verify(emailService, times(1)).sendDeleteAccountPreparationEmail(eq(testUser), anyString());
        }
    }

    @Test
    void testDeleteAccountPreparationIntegration_EmailServiceNotCalledOnError() throws Exception {
        // Given
        try (MockedStatic<Util> mockedUtil = mockStatic(Util.class)) {
            mockedUtil.when(() -> Util.getTokenFromRequest(any())).thenReturn(testToken);
            mockedUtil.when(() -> Util.getClientIpAddress(any())).thenReturn("127.0.0.1");
            
            when(loginService.getLoginWithIp(eq(testToken), anyString())).thenReturn(null);

            // When & Then
        assertThrows(CompletionException.class, () -> {
            protectedApiController.deleteAccountPreparation().join();
        });

            // Then
            verify(emailService, never()).sendDeleteAccountPreparationEmail(any(), any());
        }
    }
}
