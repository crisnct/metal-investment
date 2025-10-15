package com.investment.metal.integration;

import com.investment.metal.infrastructure.controller.ProtectedApiController;
import com.investment.metal.infrastructure.dto.SimpleMessageDto;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.LoginService;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
class DeleteAccountIntegrationTest {

    private ProtectedApiController protectedApiController;
    private AccountService accountService;
    private LoginService loginService;
    private PasswordEncoder passwordEncoder;
    private ExceptionService exceptionService;
    private Customer testUser;
    private Login testLogin;

    @BeforeEach
    void setUp() {
        // Initialize controller
        protectedApiController = new ProtectedApiController();

        // Create mocks
        accountService = mock(AccountService.class);
        loginService = mock(LoginService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        exceptionService = mock(ExceptionService.class);

        // Inject dependencies using reflection
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
        testUser.setPassword("encodedpassword");

        testLogin = new Login();
        testLogin.setUserId(1);
        testLogin.setValidationCode(123456);

        // Configure ExceptionService mock to throw exceptions when condition is true
        doAnswer(invocation -> {
            boolean condition = invocation.getArgument(0);
            if (condition) {
                throw new BusinessException(400, "Test exception");
            }
            return null;
        }).when(exceptionService).check(anyBoolean(), any(), any());
    }

    @Test
    void testDeleteAccountIntegration_Success() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
            doNothing().when(accountService).deleteUserAccount(1, 123456);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getMessage().contains("permanently deleted"));
        }
    }

    @Test
    void testDeleteAccountIntegration_InvalidPassword() throws Exception {
        // Given
        String password = "wrongpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(false);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any(), anyInt());
        }
    }

    @Test
    void testDeleteAccountIntegration_EmptyCode() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any(), anyInt());
        }
    }

    @Test
    void testDeleteAccountIntegration_InvalidToken() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("invalid-token");
            when(loginService.getLogin("invalid-token")).thenReturn(null);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any(), anyInt());
        }
    }

    @Test
    void testDeleteAccountIntegration_UserNotFound() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(null);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                protectedApiController.deleteAccount(password, code);
            });

            verify(accountService, never()).deleteUserAccount(any(), anyInt());
        }
    }

    @Test
    void testDeleteAccountIntegration_ResponseStructure() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
            doNothing().when(accountService).deleteUserAccount(1, 123456);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getMessage());
            assertTrue(response.getBody().getMessage().contains("permanently deleted"));
        }
    }

    @Test
    void testDeleteAccountIntegration_HttpStatus() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
            doNothing().when(accountService).deleteUserAccount(1, 123456);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Test
    void testDeleteAccountIntegration_MessageContent() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
            doNothing().when(accountService).deleteUserAccount(1, 123456);

            // When
            ResponseEntity<SimpleMessageDto> response = protectedApiController.deleteAccount(password, code);

            // Then
            assertNotNull(response.getBody());
            String message = response.getBody().getMessage();
            assertTrue(message.contains("permanently deleted"));
            assertTrue(message.contains("testuser"));
        }
    }

    @Test
    void testDeleteAccountIntegration_ServiceDependencies() throws Exception {
        // Given
        String password = "plainpassword";
        String code = "123456";

        try (MockedStatic<Util> utilMock = mockStatic(Util.class)) {
            utilMock.when(() -> Util.getTokenFromRequest(any())).thenReturn("valid-token");
            when(loginService.getLogin("valid-token")).thenReturn(testLogin);
            when(accountService.findById(1)).thenReturn(testUser);
            when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);
            doNothing().when(accountService).deleteUserAccount(1, 123456);

            // When
            protectedApiController.deleteAccount(password, code);

            // Then
            verify(loginService).getLogin("valid-token");
            verify(accountService).findById(1);
            verify(passwordEncoder).matches(password, testUser.getPassword());
            verify(accountService).deleteUserAccount(1, 123456);
        }
    }

    @Test
    void testDeleteAccountIntegration_ControllerInstantiation() {
        // Then
        assertNotNull(protectedApiController);
    }
}
