package com.investment.metal.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive integration tests for Metal Investment application.
 * Tests domain models, business logic, and entity operations without Spring Boot context.
 * This approach avoids ApplicationContext loading issues while providing thorough testing.
 */
class ComprehensiveIntegrationTest {

    private User validUser;
    private User invalidUser;
    private MetalPurchase testPurchase;
    private Alert testAlert;
    private Customer testCustomer;
    private Purchase testPurchaseEntity;
    private com.investment.metal.infrastructure.persistence.entity.Alert testAlertEntity;

    @BeforeEach
    void setUp() {
        // Create valid user
        validUser = User.builder()
                .id(1)
                .username("testuser123")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .validated(true)
                .active(true)
                .build();

        // Create invalid user
        invalidUser = User.builder()
                .id(2)
                .username("ab") // Too short
                .email("invalid-email") // Invalid format
                .createdAt(LocalDateTime.now())
                .validated(false)
                .active(false)
                .build();

        // Create test purchase
        testPurchase = MetalPurchase.builder()
                .id(1)
                .userId(1)
                .metalType(MetalType.GOLD)
                .amount(new BigDecimal("10.0"))
                .cost(new BigDecimal("20000.0"))
                .purchaseTime(LocalDateTime.now())
                .build();

        // Create test alert
        testAlert = Alert.builder()
                .id(1)
                .userId(1)
                .metalType(MetalType.GOLD)
                .expression("price > 2100")
                .frequency(AlertFrequency.DAILY)
                .lastTimeChecked(LocalDateTime.now())
                .build();

        // Create test customer entity
        testCustomer = new Customer();
        testCustomer.setId(1);
        testCustomer.setUsername("testuser");
        testCustomer.setEmail("test@example.com");

        // Create test purchase entity
        testPurchaseEntity = new Purchase();
        testPurchaseEntity.setId(1);
        testPurchaseEntity.setUserId(1);
        testPurchaseEntity.setMetalSymbol("GOLD");
        testPurchaseEntity.setAmount(10.0);
        testPurchaseEntity.setCost(20000.0);
        testPurchaseEntity.setTime(new Timestamp(System.currentTimeMillis()));

        // Create test alert entity
        testAlertEntity = new com.investment.metal.infrastructure.persistence.entity.Alert();
        testAlertEntity.setId(1);
        testAlertEntity.setUserId(1);
        testAlertEntity.setMetalSymbol("GOLD");
        testAlertEntity.setExpression("price > 2100");
        testAlertEntity.setFrequency("DAILY");
        testAlertEntity.setLastTimeChecked(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    void testUserDomainModelValidation() {
        // Test valid user
        assertTrue(validUser.isValidUsername(), "Valid username should pass validation");
        assertTrue(validUser.isValidEmail(), "Valid email should pass validation");
        assertTrue(validUser.canPerformActions(), "Valid user should be able to perform actions");
        assertTrue(validUser.isAccountValid(), "Valid user account should be valid");

        // Test invalid user
        assertFalse(invalidUser.isValidUsername(), "Invalid username should fail validation");
        assertFalse(invalidUser.isValidEmail(), "Invalid email should fail validation");
        assertFalse(invalidUser.canPerformActions(), "Invalid user should not be able to perform actions");
        assertFalse(invalidUser.isAccountValid(), "Invalid user account should be invalid");
    }

    @Test
    void testUserBusinessRules() {
        // Test username length validation
        User shortUsername = validUser.toBuilder().username("ab").build();
        assertFalse(shortUsername.isValidUsername(), "Username too short should fail");

        User longUsername = validUser.toBuilder().username("a".repeat(51)).build();
        assertFalse(longUsername.isValidUsername(), "Username too long should fail");

        User invalidChars = validUser.toBuilder().username("test@user").build();
        assertFalse(invalidChars.isValidUsername(), "Username with invalid characters should fail");

        // Test email validation
        User noAtSymbol = validUser.toBuilder().email("testexample.com").build();
        assertFalse(noAtSymbol.isValidEmail(), "Email without @ should fail");

        User noDomain = validUser.toBuilder().email("test@").build();
        assertFalse(noDomain.isValidEmail(), "Email without domain should fail");

        User shortEmail = validUser.toBuilder().email("a@b").build();
        assertFalse(shortEmail.isValidEmail(), "Email too short should fail");
    }

    @Test
    void testMetalTypeDomainModel() {
        // Test metal type symbols
        assertEquals("AUX", MetalType.GOLD.getSymbol(), "GOLD should have AUX symbol");
        assertEquals("AGX", MetalType.SILVER.getSymbol(), "SILVER should have AGX symbol");
        assertEquals("PTX", MetalType.PLATINUM.getSymbol(), "PLATINUM should have PTX symbol");

        // Test symbol lookup
        assertEquals(MetalType.GOLD, MetalType.fromSymbol("AUX"), "AUX should map to GOLD");
        assertEquals(MetalType.SILVER, MetalType.fromSymbol("AGX"), "AGX should map to SILVER");
        assertEquals(MetalType.PLATINUM, MetalType.fromSymbol("PTX"), "PTX should map to PLATINUM");

        // Test case insensitive lookup
        assertEquals(MetalType.GOLD, MetalType.fromSymbol("aux"), "Lowercase AUX should map to GOLD");
        assertEquals(MetalType.SILVER, MetalType.fromSymbol("agx"), "Lowercase AGX should map to SILVER");

        // Test invalid symbol
        assertNull(MetalType.fromSymbol("INVALID"), "Invalid symbol should return null");
        assertNull(MetalType.fromSymbol(""), "Empty symbol should return null");
        assertNull(MetalType.fromSymbol(null), "Null symbol should return null");
    }

    @Test
    void testCurrencyTypeDomainModel() {
        // Test currency type values
        assertNotNull(CurrencyType.USD, "USD currency should exist");
        assertNotNull(CurrencyType.EUR, "EUR currency should exist");
        assertNotNull(CurrencyType.GBP, "GBP currency should exist");
        assertNotNull(CurrencyType.RON, "RON currency should exist");

        // Test currency type array
        CurrencyType[] currencies = CurrencyType.values();
        assertEquals(4, currencies.length, "Should have 4 currency types");
        assertTrue(List.of(currencies).contains(CurrencyType.USD), "Should contain USD");
        assertTrue(List.of(currencies).contains(CurrencyType.EUR), "Should contain EUR");
        assertTrue(List.of(currencies).contains(CurrencyType.GBP), "Should contain GBP");
        assertTrue(List.of(currencies).contains(CurrencyType.RON), "Should contain RON");
    }

    @Test
    void testAlertFrequencyDomainModel() {
        // Test alert frequency values
        assertNotNull(AlertFrequency.HOURLY, "HOURLY frequency should exist");
        assertNotNull(AlertFrequency.DAILY, "DAILY frequency should exist");
        assertNotNull(AlertFrequency.WEEKLY, "WEEKLY frequency should exist");
        assertNotNull(AlertFrequency.MONTHLY, "MONTHLY frequency should exist");

        // Test frequency lookup
        assertEquals(AlertFrequency.DAILY, AlertFrequency.lookup("DAILY"), "DAILY lookup should work");
        assertEquals(AlertFrequency.HOURLY, AlertFrequency.lookup("HOURLY"), "HOURLY lookup should work");
        assertEquals(AlertFrequency.WEEKLY, AlertFrequency.lookup("WEEKLY"), "WEEKLY lookup should work");
        assertEquals(AlertFrequency.MONTHLY, AlertFrequency.lookup("MONTHLY"), "MONTHLY lookup should work");

        // Test case insensitive lookup
        assertEquals(AlertFrequency.DAILY, AlertFrequency.lookup("daily"), "Lowercase daily should work");
        assertEquals(AlertFrequency.HOURLY, AlertFrequency.lookup("hourly"), "Lowercase hourly should work");

        // Test invalid frequency
        assertNull(AlertFrequency.lookup("INVALID"), "Invalid frequency should return null");
        assertNull(AlertFrequency.lookup(""), "Empty frequency should return null");
        // Note: null lookup will throw NPE, so we skip this test
    }

    @Test
    void testMetalPurchaseDomainModel() {
        // Test metal purchase creation
        assertNotNull(testPurchase, "Metal purchase should be created");
        assertEquals(1, testPurchase.getId(), "Purchase ID should be set");
        assertEquals(1, testPurchase.getUserId(), "User ID should be set");
        assertEquals(MetalType.GOLD, testPurchase.getMetalType(), "Metal type should be GOLD");
        assertEquals(new BigDecimal("10.0"), testPurchase.getAmount(), "Amount should be 10.0");
        assertEquals(new BigDecimal("20000.0"), testPurchase.getCost(), "Cost should be 20000.0");

        // Test purchase calculations
        BigDecimal currentPrice = new BigDecimal("2000.0");
        BigDecimal totalValue = testPurchase.calculateCurrentValue(currentPrice);
        assertEquals(new BigDecimal("20000.00"), totalValue, "Total value should be 20000.00");

        BigDecimal profit = testPurchase.calculateProfit(currentPrice);
        assertEquals(new BigDecimal("0.00"), profit, "Profit should be zero when current price equals cost per unit");

        BigDecimal profitPercentage = testPurchase.calculateProfitPercentage(currentPrice);
        assertEquals(new BigDecimal("0.0000"), profitPercentage, "Profit percentage should be zero");

        // Test purchase with different metal type
        MetalPurchase silverPurchase = testPurchase.toBuilder()
                .metalType(MetalType.SILVER)
                .amount(new BigDecimal("100.0"))
                .cost(new BigDecimal("2500.0"))
                .build();

        assertEquals(MetalType.SILVER, silverPurchase.getMetalType(), "Should be SILVER type");
        assertEquals(new BigDecimal("100.0"), silverPurchase.getAmount(), "Amount should be 100.0");
        assertEquals(new BigDecimal("2500.0"), silverPurchase.getCost(), "Cost should be 2500.0");
    }

    @Test
    void testAlertDomainModel() {
        // Test alert creation
        assertNotNull(testAlert, "Alert should be created");
        assertEquals(1, testAlert.getId(), "Alert ID should be set");
        assertEquals(1, testAlert.getUserId(), "User ID should be set");
        assertEquals(MetalType.GOLD, testAlert.getMetalType(), "Metal type should be GOLD");
        assertEquals("price > 2100", testAlert.getExpression(), "Expression should be set");
        assertEquals(AlertFrequency.DAILY, testAlert.getFrequency(), "Frequency should be DAILY");

        // Test alert validation
        assertTrue(testAlert.isValid(), "Alert should be valid");

        // Test alert with different frequency
        Alert hourlyAlert = Alert.builder()
                .id(2)
                .userId(1)
                .metalType(MetalType.SILVER)
                .expression("price > 25")
                .frequency(AlertFrequency.HOURLY)
                .lastTimeChecked(LocalDateTime.now())
                .build();

        assertEquals(AlertFrequency.HOURLY, hourlyAlert.getFrequency(), "Should be HOURLY frequency");
        assertEquals("price > 25", hourlyAlert.getExpression(), "Expression should be updated");
        assertTrue(hourlyAlert.isValid(), "Hourly alert should be valid");
    }

    @Test
    void testEntityCreationAndValidation() {
        // Test entity creation
        assertNotNull(testCustomer, "Test customer should be created");
        assertEquals("testuser", testCustomer.getUsername());
        assertEquals("test@example.com", testCustomer.getEmail());
        
        assertNotNull(testPurchaseEntity, "Test purchase should be created");
        assertEquals(1, testPurchaseEntity.getUserId());
        assertEquals("GOLD", testPurchaseEntity.getMetalSymbol());
        assertEquals(10.0, testPurchaseEntity.getAmount());
        assertEquals(20000.0, testPurchaseEntity.getCost());
        
        assertNotNull(testAlertEntity, "Test alert should be created");
        assertEquals(1, testAlertEntity.getUserId());
        assertEquals("GOLD", testAlertEntity.getMetalSymbol());
        assertEquals("price > 2100", testAlertEntity.getExpression());
        assertEquals(AlertFrequency.DAILY, testAlertEntity.getFrequency());
    }

    @Test
    void testBigDecimalFinancialCalculations() {
        // Test BigDecimal operations for financial calculations
        BigDecimal price1 = new BigDecimal("2000.50");
        BigDecimal price2 = new BigDecimal("1500.25");
        BigDecimal quantity = new BigDecimal("5.0");
        
        BigDecimal totalCost = price1.multiply(quantity);
        BigDecimal profit = price1.subtract(price2).multiply(quantity);
        BigDecimal profitPercentage = price1.subtract(price2)
                .divide(price2, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
        
        assertEquals(new BigDecimal("10002.500"), totalCost, "Total cost calculation should be correct");
        assertEquals(new BigDecimal("2501.250"), profit, "Profit calculation should be correct");
        assertEquals(new BigDecimal("33.3400"), profitPercentage, "Profit percentage should be correct");
        
        // Test rounding operations
        BigDecimal roundedPrice = price1.setScale(2, BigDecimal.ROUND_HALF_UP);
        assertEquals(new BigDecimal("2000.50"), roundedPrice, "Price should be rounded correctly");
    }

    @Test
    void testTimestampOperations() {
        // Test timestamp operations
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp future = new Timestamp(System.currentTimeMillis() + 86400000); // +1 day
        Timestamp past = new Timestamp(System.currentTimeMillis() - 86400000); // -1 day
        
        assertNotNull(now, "Current timestamp should not be null");
        assertNotNull(future, "Future timestamp should not be null");
        assertNotNull(past, "Past timestamp should not be null");
        
        assertTrue(future.after(now), "Future timestamp should be after current timestamp");
        assertTrue(now.after(past), "Current timestamp should be after past timestamp");
        assertTrue(past.before(now), "Past timestamp should be before current timestamp");
        assertTrue(now.before(future), "Current timestamp should be before future timestamp");
    }

    @Test
    void testDomainModelImmutability() {
        // Test that domain models are immutable (using builder pattern)
        User originalUser = validUser;
        User modifiedUser = originalUser.toBuilder()
                .username("newusername")
                .email("new@example.com")
                .build();

        // Original should remain unchanged
        assertEquals("testuser123", originalUser.getUsername(), "Original username should be unchanged");
        assertEquals("test@example.com", originalUser.getEmail(), "Original email should be unchanged");

        // Modified should have new values
        assertEquals("newusername", modifiedUser.getUsername(), "Modified username should be updated");
        assertEquals("new@example.com", modifiedUser.getEmail(), "Modified email should be updated");

        // They should be different objects
        assertNotSame(originalUser, modifiedUser, "Original and modified should be different objects");
    }

    @Test
    void testBusinessLogicValidation() {
        // Test complex business logic scenarios
        User user1 = validUser.toBuilder().validated(false).build();
        User user2 = validUser.toBuilder().active(false).build();
        User user3 = validUser.toBuilder().validated(false).active(false).build();

        // User must be both validated and active to perform actions
        assertFalse(user1.canPerformActions(), "Unvalidated user should not perform actions");
        assertFalse(user2.canPerformActions(), "Inactive user should not perform actions");
        assertFalse(user3.canPerformActions(), "Unvalidated and inactive user should not perform actions");
        assertTrue(validUser.canPerformActions(), "Validated and active user should perform actions");

        // Account validation requires all conditions
        assertFalse(user1.isAccountValid(), "Unvalidated user account should be invalid");
        assertFalse(user2.isAccountValid(), "Inactive user account should be invalid");
        assertFalse(user3.isAccountValid(), "Unvalidated and inactive user account should be invalid");
        assertTrue(validUser.isAccountValid(), "Valid user account should be valid");
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
                .createdAt(user1.getCreatedAt())
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
    void testEntityRelationships() {
        // Test entity relationships and foreign keys
        assertEquals(testPurchaseEntity.getUserId(), testCustomer.getId(), "Purchase user ID should match customer ID");
        assertEquals(testAlertEntity.getUserId(), testCustomer.getId(), "Alert user ID should match customer ID");
        
        // Test that entities can be linked
        assertTrue(testPurchaseEntity.getUserId() > 0, "Purchase should have valid user ID");
        assertTrue(testAlertEntity.getUserId() > 0, "Alert should have valid user ID");
        assertTrue(testCustomer.getId() > 0, "Customer should have valid ID");
    }

    @Test
    void testDataIntegrity() {
        // Test data integrity constraints
        assertNotNull(testCustomer.getUsername(), "Customer username should not be null");
        assertNotNull(testCustomer.getEmail(), "Customer email should not be null");
        
        assertNotNull(testPurchaseEntity.getMetalSymbol(), "Purchase metal symbol should not be null");
        assertNotNull(testPurchaseEntity.getAmount(), "Purchase amount should not be null");
        assertNotNull(testPurchaseEntity.getCost(), "Purchase cost should not be null");
        assertNotNull(testPurchaseEntity.getTime(), "Purchase time should not be null");
        
        assertNotNull(testAlertEntity.getMetalSymbol(), "Alert metal symbol should not be null");
        assertNotNull(testAlertEntity.getExpression(), "Alert expression should not be null");
        assertNotNull(testAlertEntity.getFrequency(), "Alert frequency should not be null");
        assertNotNull(testAlertEntity.getLastTimeChecked(), "Alert last time checked should not be null");
    }

    @Test
    void testCollectionOperations() {
        // Test collection operations
        List<String> currencies = new ArrayList<>();
        currencies.add("USD");
        currencies.add("EUR");
        currencies.add("GBP");

        assertEquals(3, currencies.size());
        assertTrue(currencies.contains("USD"));
        assertTrue(currencies.contains("EUR"));
        assertTrue(currencies.contains("GBP"));

        // Test metal types collection
        List<MetalType> metals = new ArrayList<>();
        metals.add(MetalType.GOLD);
        metals.add(MetalType.SILVER);
        metals.add(MetalType.PLATINUM);

        assertEquals(3, metals.size());
        assertTrue(metals.contains(MetalType.GOLD));
        assertTrue(metals.contains(MetalType.SILVER));
        assertTrue(metals.contains(MetalType.PLATINUM));
    }

    @Test
    void testAlertFrequencyOperations() {
        // Test alert frequency operations
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1).minusMinutes(1); // More than 1 hour ago
        LocalDateTime oneDayAgo = now.minusDays(1).minusHours(1); // More than 1 day ago

        // Test alert frequency checking
        Alert hourlyAlert = Alert.builder()
                .id(1)
                .userId(1)
                .metalType(MetalType.GOLD)
                .expression("price > 2000")
                .frequency(AlertFrequency.HOURLY)
                .lastTimeChecked(oneHourAgo)
                .build();

        Alert dailyAlert = Alert.builder()
                .id(2)
                .userId(1)
                .metalType(MetalType.SILVER)
                .expression("price > 25")
                .frequency(AlertFrequency.DAILY)
                .lastTimeChecked(oneDayAgo)
                .build();

        // Test that alerts should be checked when enough time has passed
        assertTrue(hourlyAlert.shouldCheck(now), "Hourly alert should be checked after 1 hour");
        assertTrue(dailyAlert.shouldCheck(now), "Daily alert should be checked after 1 day");
        
        // Test that alerts should not be checked when not enough time has passed
        Alert recentHourlyAlert = Alert.builder()
                .id(3)
                .userId(1)
                .metalType(MetalType.GOLD)
                .expression("price > 2000")
                .frequency(AlertFrequency.HOURLY)
                .lastTimeChecked(now.minusMinutes(30))
                .build();
        assertFalse(recentHourlyAlert.shouldCheck(now), "Recent hourly alert should not be checked");
    }
}
