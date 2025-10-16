package com.investment.metal.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.persistence.entity.Alert;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import com.investment.metal.infrastructure.persistence.repository.AlertRepository;
import com.investment.metal.infrastructure.persistence.repository.NotificationJpaRepository;
import com.investment.metal.infrastructure.persistence.repository.CustomerRepository;
import com.investment.metal.infrastructure.persistence.repository.LoginRepository;
import com.investment.metal.infrastructure.persistence.repository.PurchaseJpaRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for AccountService.
 * Tests the account deletion functionality and other account operations.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private PurchaseJpaRepository purchaseRepository;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private NotificationJpaRepository notificationRepository;

    @Mock
    private ExceptionService exceptionService;

    @Mock
    private LoginService loginService;

    @InjectMocks
    private AccountService accountService;

    private Customer testUser;
    private Alert testAlert;
    private Purchase testPurchase;
    private Login testLogin;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new Customer();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("hashedpassword");
        testUser.setEmail("test@example.com");

        // Setup test alert
        testAlert = new Alert();
        testAlert.setId(1);
        testAlert.setUserId(1);
        testAlert.setMetalSymbol("GOLD");
        testAlert.setExpression("price > 1000");
        testAlert.setFrequency("DAILY");

        // Setup test purchase
        testPurchase = new Purchase();
        testPurchase.setId(1);
        testPurchase.setUserId(1);
        testPurchase.setMetalSymbol("GOLD");
        testPurchase.setAmount(10.0);
        testPurchase.setCost(1000.0);

        // Setup test login
        testLogin = new Login();
        testLogin.setUserId(1);
        testLogin.setLoggedIn(1);
        testLogin.setValidationCode(123456);
    }

    @Test
    void testDeleteUserAccount_Success() throws BusinessException {
        // Given
        Integer userId = 1;
        List<Alert> userAlerts = Arrays.asList(testAlert);
        List<Purchase> userPurchases = Arrays.asList(testPurchase);

        when(customerRepository.findById((long) userId)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserId(userId)).thenReturn(Optional.of(userAlerts));
        when(purchaseRepository.findByUserId(userId)).thenReturn(Optional.of(userPurchases));
        when(loginRepository.findByUserId(userId)).thenReturn(Optional.of(testLogin));
        
        // Mock LoginService behavior
        doNothing().when(loginService).invalidateAllUserSessions(userId);

        // When
        accountService.deleteUserAccount(userId, 123456);

        // Then
        verify(alertRepository).deleteAll(userAlerts);
        verify(purchaseRepository).deleteAll(userPurchases);
        verify(loginRepository).delete(testLogin);
        verify(notificationRepository).deleteByUserId(userId);
        verify(loginService).invalidateAllUserSessions(userId);
        verify(customerRepository).delete(testUser);
    }

    @Test
    void testDeleteUserAccount_UserNotFound() throws BusinessException {
        // Given
        Integer userId = 999;
        when(customerRepository.findById((long) userId)).thenReturn(Optional.empty());
        com.investment.metal.infrastructure.exception.ExceptionBuilder mockBuilder = mock(com.investment.metal.infrastructure.exception.ExceptionBuilder.class);
        when(exceptionService.createBuilder(any())).thenReturn(mockBuilder);
        when(mockBuilder.setArguments(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(new com.investment.metal.domain.exception.BusinessException(404, "User not found"));

        // When & Then
        assertThrows(BusinessException.class, () -> {
            accountService.deleteUserAccount(userId, 123456);
        });
    }

    @Test
    void testDeleteUserAccount_NoAlerts() throws BusinessException {
        // Given
        Integer userId = 1;
        when(customerRepository.findById((long) userId)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(purchaseRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(loginRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        // Mock LoginService behavior
        doNothing().when(loginService).invalidateAllUserSessions(userId);

        // When
        accountService.deleteUserAccount(userId, 123456);

        // Then
        verify(alertRepository, never()).deleteAll(any());
        verify(purchaseRepository, never()).deleteAll(any());
        verify(loginRepository, never()).delete(any());
        verify(notificationRepository).deleteByUserId(userId);
        verify(loginService).invalidateAllUserSessions(userId);
        verify(customerRepository).delete(testUser);
    }

    @Test
    void testDeleteUserAccount_WithAlertsAndPurchases() throws BusinessException {
        // Given
        Integer userId = 1;
        List<Alert> userAlerts = Arrays.asList(testAlert);
        List<Purchase> userPurchases = Arrays.asList(testPurchase);

        when(customerRepository.findById((long) userId)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserId(userId)).thenReturn(Optional.of(userAlerts));
        when(purchaseRepository.findByUserId(userId)).thenReturn(Optional.of(userPurchases));
        when(loginRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        // Mock LoginService behavior
        doNothing().when(loginService).invalidateAllUserSessions(userId);

        // When
        accountService.deleteUserAccount(userId, 123456);

        // Then
        verify(alertRepository).deleteAll(userAlerts);
        verify(purchaseRepository).deleteAll(userPurchases);
        verify(loginRepository, never()).delete(any());
        verify(notificationRepository).deleteByUserId(userId);
        verify(loginService).invalidateAllUserSessions(userId);
        verify(customerRepository).delete(testUser);
    }

    @Test
    void testDeleteUserAccount_WithLoginSession() throws BusinessException {
        // Given
        Integer userId = 1;
        when(customerRepository.findById((long) userId)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(purchaseRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(loginRepository.findByUserId(userId)).thenReturn(Optional.of(testLogin));
        
        // Mock LoginService behavior
        doNothing().when(loginService).invalidateAllUserSessions(userId);

        // When
        accountService.deleteUserAccount(userId, 123456);

        // Then
        verify(alertRepository, never()).deleteAll(any());
        verify(purchaseRepository, never()).deleteAll(any());
        verify(loginRepository).delete(testLogin);
        verify(notificationRepository).deleteByUserId(userId);
        verify(loginService).invalidateAllUserSessions(userId);
        verify(customerRepository).delete(testUser);
    }

    @Test
    void testDeleteUserAccount_MultipleAlertsAndPurchases() throws BusinessException {
        // Given
        Integer userId = 1;
        Alert alert2 = new Alert();
        alert2.setId(2);
        alert2.setUserId(1);
        alert2.setMetalSymbol("SILVER");
        alert2.setExpression("price < 50");
        alert2.setFrequency("WEEKLY");

        Purchase purchase2 = new Purchase();
        purchase2.setId(2);
        purchase2.setUserId(1);
        purchase2.setMetalSymbol("SILVER");
        purchase2.setAmount(20.0);
        purchase2.setCost(500.0);

        List<Alert> userAlerts = Arrays.asList(testAlert, alert2);
        List<Purchase> userPurchases = Arrays.asList(testPurchase, purchase2);

        when(customerRepository.findById((long) userId)).thenReturn(Optional.of(testUser));
        when(alertRepository.findByUserId(userId)).thenReturn(Optional.of(userAlerts));
        when(purchaseRepository.findByUserId(userId)).thenReturn(Optional.of(userPurchases));
        when(loginRepository.findByUserId(userId)).thenReturn(Optional.of(testLogin));
        
        // Mock LoginService behavior
        doNothing().when(loginService).invalidateAllUserSessions(userId);

        // When
        accountService.deleteUserAccount(userId, 123456);

        // Then
        verify(alertRepository).deleteAll(userAlerts);
        verify(purchaseRepository).deleteAll(userPurchases);
        verify(loginRepository).delete(testLogin);
        verify(notificationRepository).deleteByUserId(userId);
        verify(loginService).invalidateAllUserSessions(userId);
        verify(customerRepository).delete(testUser);
    }
}
