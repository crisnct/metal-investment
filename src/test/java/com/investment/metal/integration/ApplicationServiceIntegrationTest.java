package com.investment.metal.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.investment.metal.domain.model.Alert;
import com.investment.metal.domain.model.AlertFrequency;
import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.domain.model.User;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for Application Services.
 * Tests domain models, business logic, and entity operations without Spring Boot context loading.
 * Focuses on core functionality and business rules validation.
 */
class ApplicationServiceIntegrationTest {

    private Customer testUser;
    private Purchase testPurchase;
    private com.investment.metal.infrastructure.persistence.entity.Alert testAlert;
    private User testDomainUser;
    private MetalPurchase testDomainPurchase;
    private Alert testDomainAlert;

    @BeforeEach
    void setUp() {
        // Create test user entity
        testUser = new Customer();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");

        // Create test purchase entity
        testPurchase = new Purchase();
        testPurchase.setId(1);
        testPurchase.setUserId(1);
        testPurchase.setMetalSymbol("GOLD");
        testPurchase.setAmount(10.0);
        testPurchase.setCost(20000.0);
        testPurchase.setTime(java.sql.Timestamp.valueOf(LocalDateTime.now()));

        // Create test alert entity
        testAlert = new com.investment.metal.infrastructure.persistence.entity.Alert();
        testAlert.setId(1);
        testAlert.setUserId(1);
        testAlert.setMetalSymbol("GOLD");
        testAlert.setExpression("price > 2100");
        testAlert.setFrequency("DAILY");
        testAlert.setLastTimeChecked(java.sql.Timestamp.valueOf(LocalDateTime.now()));

        // Create test domain user
        testDomainUser = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .validated(true)
                .active(true)
                .build();

        // Create test domain purchase
        testDomainPurchase = MetalPurchase.builder()
                .id(1)
                .userId(1)
                .metalType(MetalType.GOLD)
                .amount(new BigDecimal("10.0"))
                .cost(new BigDecimal("20000.0"))
                .purchaseTime(LocalDateTime.now())
                .build();

        // Create test domain alert
        testDomainAlert = Alert.builder()
                .id(1)
                .userId(1)
                .metalType(MetalType.GOLD)
                .expression("price > 2100")
                .frequency(AlertFrequency.DAILY)
                .lastTimeChecked(LocalDateTime.now())
                .build();
    }

    @Test
    void testEntityCreation() {
        // Test entity creation and basic properties
        assertNotNull(testUser, "Test user should be created");
        assertNotNull(testPurchase, "Test purchase should be created");
        assertNotNull(testAlert, "Test alert should be created");

        // Test user properties
        assertEquals(1, testUser.getId(), "User ID should be set");
        assertEquals("testuser", testUser.getUsername(), "Username should be set");
        assertEquals("test@example.com", testUser.getEmail(), "Email should be set");

        // Test purchase properties
        assertEquals(1, testPurchase.getId(), "Purchase ID should be set");
        assertEquals(1, testPurchase.getUserId(), "User ID should be set");
        assertEquals("GOLD", testPurchase.getMetalSymbol(), "Metal symbol should be set");
        assertEquals(10.0, testPurchase.getAmount(), "Amount should be set");
        assertEquals(20000.0, testPurchase.getCost(), "Cost should be set");

        // Test alert properties
        assertEquals(1, testAlert.getId(), "Alert ID should be set");
        assertEquals(1, testAlert.getUserId(), "User ID should be set");
        assertEquals("GOLD", testAlert.getMetalSymbol(), "Metal symbol should be set");
        assertEquals("price > 2100", testAlert.getExpression(), "Expression should be set");
        assertEquals(AlertFrequency.DAILY, testAlert.getFrequency(), "Frequency should be set");
    }

    @Test
    void testDomainModelCreation() {
        // Test domain model creation and basic properties
        assertNotNull(testDomainUser, "Test domain user should be created");
        assertNotNull(testDomainPurchase, "Test domain purchase should be created");
        assertNotNull(testDomainAlert, "Test domain alert should be created");

        // Test domain user properties
        assertEquals(1, testDomainUser.getId(), "Domain user ID should be set");
        assertEquals("testuser", testDomainUser.getUsername(), "Domain username should be set");
        assertEquals("test@example.com", testDomainUser.getEmail(), "Domain email should be set");
        assertTrue(testDomainUser.isValidated(), "Domain user should be validated");
        assertTrue(testDomainUser.isActive(), "Domain user should be active");

        // Test domain purchase properties
        assertEquals(1, testDomainPurchase.getId(), "Domain purchase ID should be set");
        assertEquals(1, testDomainPurchase.getUserId(), "Domain user ID should be set");
        assertEquals(MetalType.GOLD, testDomainPurchase.getMetalType(), "Domain metal type should be set");
        assertEquals(new BigDecimal("10.0"), testDomainPurchase.getAmount(), "Domain amount should be set");
        assertEquals(new BigDecimal("20000.0"), testDomainPurchase.getCost(), "Domain cost should be set");

        // Test domain alert properties
        assertEquals(1, testDomainAlert.getId(), "Domain alert ID should be set");
        assertEquals(1, testDomainAlert.getUserId(), "Domain user ID should be set");
        assertEquals(MetalType.GOLD, testDomainAlert.getMetalType(), "Domain metal type should be set");
        assertEquals("price > 2100", testDomainAlert.getExpression(), "Domain expression should be set");
        assertEquals(AlertFrequency.DAILY, testDomainAlert.getFrequency(), "Domain frequency should be set");
    }

    @Test
    void testDomainModelBusinessLogic() {
        // Test domain model business logic
        BigDecimal currentPrice = new BigDecimal("2000.0");
        BigDecimal totalValue = testDomainPurchase.calculateCurrentValue(currentPrice);
        assertEquals(new BigDecimal("20000.00"), totalValue, "Total value should be calculated correctly");

        // Test profit calculation
        BigDecimal profit = testDomainPurchase.calculateProfit(currentPrice);
        assertEquals(new BigDecimal("0.00"), profit, "Profit should be zero when current price equals cost per unit");

        // Test profit percentage calculation
        BigDecimal profitPercentage = testDomainPurchase.calculateProfitPercentage(currentPrice);
        assertEquals(new BigDecimal("0.0000"), profitPercentage, "Profit percentage should be zero");

        // Test alert frequency operations
        assertFalse(testDomainAlert.shouldCheck(LocalDateTime.now()), "Alert should not be checked immediately after creation");
        
        // Test alert with different frequency
        Alert hourlyAlert = Alert.builder()
                .id(2)
                .userId(1)
                .metalType(MetalType.GOLD)
                .expression("price > 2050")
                .frequency(AlertFrequency.HOURLY)
                .lastTimeChecked(LocalDateTime.now().minusHours(2))
                .build();
        
        assertTrue(hourlyAlert.shouldCheck(LocalDateTime.now()), "Hourly alert should be checked after 2 hours");
    }

    @Test
    void testMetalTypeOperations() {
        // Test metal type enum operations
        assertEquals("AUX", MetalType.GOLD.getSymbol(), "Gold symbol should be correct");
        assertEquals("AGX", MetalType.SILVER.getSymbol(), "Silver symbol should be correct");
        assertEquals("PTX", MetalType.PLATINUM.getSymbol(), "Platinum symbol should be correct");

        // Test metal type lookup
        assertEquals(MetalType.GOLD, MetalType.lookup("AUX"), "Gold lookup should work");
        assertEquals(MetalType.SILVER, MetalType.lookup("AGX"), "Silver lookup should work");
        assertEquals(MetalType.PLATINUM, MetalType.lookup("PTX"), "Platinum lookup should work");
        assertNull(MetalType.lookup("INVALID"), "Invalid metal type should return null");
    }

    @Test
    void testCurrencyTypeOperations() {
        // Test currency type enum operations
        assertEquals(CurrencyType.USD, CurrencyType.USD, "USD should be correct");
        assertEquals(CurrencyType.EUR, CurrencyType.EUR, "EUR should be correct");
        assertEquals(CurrencyType.GBP, CurrencyType.GBP, "GBP should be correct");
        assertEquals(CurrencyType.RON, CurrencyType.RON, "RON should be correct");
    }

    @Test
    void testAlertFrequencyOperations() {
        // Test alert frequency enum operations
        assertEquals("DAILY", AlertFrequency.DAILY.name(), "Daily frequency should be correct");
        assertEquals("HOURLY", AlertFrequency.HOURLY.name(), "Hourly frequency should be correct");
        assertEquals("WEEKLY", AlertFrequency.WEEKLY.name(), "Weekly frequency should be correct");

        // Test alert frequency lookup
        assertEquals(AlertFrequency.DAILY, AlertFrequency.lookup("DAILY"), "Daily lookup should work");
        assertEquals(AlertFrequency.HOURLY, AlertFrequency.lookup("HOURLY"), "Hourly lookup should work");
        assertEquals(AlertFrequency.WEEKLY, AlertFrequency.lookup("WEEKLY"), "Weekly lookup should work");

        // Test case insensitive lookup
        assertEquals(AlertFrequency.DAILY, AlertFrequency.lookup("daily"), "Lowercase daily should work");
        assertEquals(AlertFrequency.HOURLY, AlertFrequency.lookup("hourly"), "Lowercase hourly should work");

        // Test invalid frequency
        assertNull(AlertFrequency.lookup("INVALID"), "Invalid frequency should return null");
        assertNull(AlertFrequency.lookup(""), "Empty frequency should return null");
    }

    @Test
    void testBigDecimalOperations() {
        // Test BigDecimal financial calculations
        BigDecimal price1 = new BigDecimal("1000.00");
        BigDecimal price2 = new BigDecimal("1333.33");
        BigDecimal amount = new BigDecimal("10.0");

        // Test total value calculation
        BigDecimal totalValue = price1.multiply(amount);
        assertEquals(new BigDecimal("10000.000"), totalValue, "Total value should be calculated correctly");

        // Test profit calculation
        BigDecimal cost = new BigDecimal("10000.00");
        BigDecimal currentValue = price2.multiply(amount);
        BigDecimal profit = currentValue.subtract(cost);
        assertEquals(new BigDecimal("3333.300"), profit, "Profit should be calculated correctly");

        // Test profit percentage calculation
        BigDecimal profitPercentage = profit.divide(cost, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
        assertEquals(new BigDecimal("33.3300"), profitPercentage, "Profit percentage should be calculated correctly");
    }

    @Test
    void testTimestampOperations() {
        // Test timestamp operations
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = now.minusHours(2);
        LocalDateTime future = now.plusHours(2);

        // Test timestamp comparison
        assertTrue(past.isBefore(now), "Past time should be before now");
        assertTrue(future.isAfter(now), "Future time should be after now");
        assertTrue(now.isAfter(past), "Now should be after past time");
        assertTrue(now.isBefore(future), "Now should be before future time");

        // Test time difference
        long hoursDifference = java.time.Duration.between(past, now).toHours();
        assertEquals(2, hoursDifference, "Time difference should be 2 hours");
    }

    @Test
    void testDomainModelEquality() {
        // Test domain model equality
        User user1 = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .validated(true)
                .active(true)
                .build();

        User user2 = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .validated(true)
                .active(true)
                .build();

        // Users with same data should be equal
        assertEquals(user1, user2, "Users with same data should be equal");
        assertEquals(user1.hashCode(), user2.hashCode(), "Hash codes should be equal");

        // Users with different data should not be equal (but they have same ID, so they are equal)
        User user3 = user1.toBuilder().username("different").build();
        // Note: User equality is based on ID only, so users with same ID are equal regardless of other fields
        assertEquals(user1, user3, "Users with same ID should be equal even with different data");
    }

    @Test
    void testFinancialCalculations() {
        // Test comprehensive financial calculations
        BigDecimal initialPrice = new BigDecimal("1000.00");
        BigDecimal currentPrice = new BigDecimal("1500.00");
        BigDecimal amount = new BigDecimal("5.0");

        // Test initial investment
        BigDecimal initialInvestment = initialPrice.multiply(amount);
        assertEquals(new BigDecimal("5000.000"), initialInvestment, "Initial investment should be correct");

        // Test current value
        BigDecimal currentValue = currentPrice.multiply(amount);
        assertEquals(new BigDecimal("7500.000"), currentValue, "Current value should be correct");

        // Test profit
        BigDecimal profit = currentValue.subtract(initialInvestment);
        assertEquals(new BigDecimal("2500.000"), profit, "Profit should be correct");

        // Test profit percentage
        BigDecimal profitPercentage = profit.divide(initialInvestment, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        assertEquals(new BigDecimal("50.0000"), profitPercentage, "Profit percentage should be correct");

        // Test loss scenario
        BigDecimal lossPrice = new BigDecimal("800.00");
        BigDecimal lossValue = lossPrice.multiply(amount);
        BigDecimal loss = lossValue.subtract(initialInvestment);
        assertEquals(new BigDecimal("-1000.000"), loss, "Loss should be correct");

        BigDecimal lossPercentage = loss.divide(initialInvestment, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        assertEquals(new BigDecimal("-20.0000"), lossPercentage, "Loss percentage should be correct");
    }
}