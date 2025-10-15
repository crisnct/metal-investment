package com.investment.metal.infrastructure.security;

import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.service.LoginService;
import com.investment.metal.infrastructure.exception.ExceptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for verifying authorization security measures.
 * Tests that users can only access their own data and cannot perform unauthorized operations.
 * 
 * @author cristian.tone
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationSecurityTest {

    private AuthorizationService authorizationService;
    private LoginService loginService;
    private ExceptionService exceptionService;
    private Login testLogin1;
    private Login testLogin2;

    @BeforeEach
    void setUp() {
        // Create mocks
        loginService = mock(LoginService.class);
        exceptionService = mock(ExceptionService.class);
        
        // Create AuthorizationService with mocked dependencies
        authorizationService = new AuthorizationService();
        
        // Inject dependencies using reflection
        try {
            java.lang.reflect.Field loginServiceField = AuthorizationService.class.getDeclaredField("loginService");
            loginServiceField.setAccessible(true);
            loginServiceField.set(authorizationService, loginService);

            java.lang.reflect.Field exceptionServiceField = AuthorizationService.class.getDeclaredField("exceptionService");
            exceptionServiceField.setAccessible(true);
            exceptionServiceField.set(authorizationService, exceptionService);
        } catch (Exception e) {
            fail("Failed to inject dependencies: " + e.getMessage());
        }

        // Setup test logins
        testLogin1 = new Login();
        testLogin1.setUserId(1);
        testLogin1.setLoginToken("token1");

        testLogin2 = new Login();
        testLogin2.setUserId(2);
        testLogin2.setLoginToken("token2");
    }

    @Test
    void testAuthorizationService_ValidatesUserAccess_SameUser() {
        // Given
        String token = "valid-token";
        Integer targetUserId = 1; // Same as authenticated user
        
        when(loginService.getLogin(token)).thenReturn(testLogin1);

        // When & Then - Should not throw exception for same user
        assertDoesNotThrow(() -> {
            authorizationService.validateUserAccess(token, targetUserId);
        });
    }

    @Test
    void testAuthorizationService_BlocksAccessToOtherUsers() {
        // Given
        String token = "valid-token";
        Integer targetUserId = 2; // Different user
        
        when(loginService.getLogin(token)).thenReturn(testLogin1);
        when(exceptionService.createBuilder(any())).thenReturn(mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class));
        when(exceptionService.createBuilder(any()).setArguments(anyString())).thenReturn(mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class));
        when(exceptionService.createBuilder(any()).setArguments(anyString()).build()).thenThrow(new BusinessException(400, "Access denied"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            authorizationService.validateUserAccess(token, targetUserId);
        });
    }

    @Test
    void testAuthorizationService_BlocksUsernameAccess() {
        // Given
        String token = "valid-token";
        String targetUsername = "otheruser";
        
        when(loginService.getLogin(token)).thenReturn(testLogin1);
        when(exceptionService.createBuilder(any())).thenReturn(mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class));
        when(exceptionService.createBuilder(any()).setArguments(anyString())).thenReturn(mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class));
        when(exceptionService.createBuilder(any()).setArguments(anyString()).build()).thenThrow(new BusinessException(400, "Access denied"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            authorizationService.validateUserAccessByUsername(token, targetUsername);
        });
    }

    @Test
    void testAuthorizationService_BlocksAdminAccess() {
        // Given
        String token = "valid-token";
        
        when(loginService.getLogin(token)).thenReturn(testLogin1);
        when(exceptionService.createBuilder(any())).thenReturn(mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class));
        when(exceptionService.createBuilder(any()).setArguments(anyString())).thenReturn(mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class));
        when(exceptionService.createBuilder(any()).setArguments(anyString()).build()).thenThrow(new BusinessException(400, "Access denied"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            authorizationService.validateAdminAccess(token);
        });
    }

    @Test
    void testGetAuthenticatedUserId_ReturnsCorrectUserId() {
        // Given
        String token = "valid-token";
        
        when(loginService.getLogin(token)).thenReturn(testLogin1);

        // When
        Integer userId = authorizationService.getAuthenticatedUserId(token);

        // Then
        assertEquals(1, userId);
    }
}
