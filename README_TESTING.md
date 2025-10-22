# Comprehensive API Testing Documentation

This directory contains comprehensive unit tests and integration tests for the Metal Investment API using JUnit 5 and Mockito.

## Test Structure

### 📁 Test Categories

1. **Controller Tests** (`controller/`)
   - `SimpleApiTest.java` - Unit tests for API controllers
   - Tests all public and protected API endpoints
   - Uses Mockito for mocking dependencies

2. **Service Tests** (`service/`)
   - `SimpleServiceTest.java` - Unit tests for service layer
   - Tests business logic and service interactions
   - Uses Mockito for mocking dependencies

3. **Integration Tests** (`integration/`)
   - `ApiIntegrationTest.java` - Full integration tests
   - Tests complete request/response cycles
   - Uses TestRestTemplate for HTTP testing
   - Tests database interactions

4. **Email Tests** (`email/`)
   - `SimpleEmailTest.java` - Email functionality tests
   - Tests turbo-smtp.com integration
   - Standalone email testing

### 📁 Configuration Files

- `application-test.properties` - Test-specific configuration
- `TestConfig.java` - Mock bean configuration
- `BaseControllerTest.java` - Common test utilities

## Test Coverage

### ✅ API Endpoints Tested

#### Public Endpoints (No Authentication)
- `POST /userRegistration` - User registration
- `POST /validateAccount` - Account validation
- `POST /login` - User login
- `POST /resetPassword` - Password reset
- `PUT /changePassword` - Password change
- `POST /checkUserPendingValidation` - Check validation status
- `POST /resendValidationEmail` - Resend validation email
- `GET /health` - Application and API health check

#### Protected Endpoints (JWT Authentication)
- `POST /api/blockIp` - Block IP address
- `POST /api/unblockIp` - Unblock IP address
- `POST /api/logout` - User logout
- `POST /api/purchase` - Record metal purchase
- `DELETE /api/sell` - Record metal sale
- `GET /api/profit` - Get user profit
- `PUT /api/revolutProfit` - Get Revolut profit
- `POST /api/addAlert` - Add price alert
- `GET /api/getAlerts` - Get user alerts
- `DELETE /api/removeAlert` - Remove alert
- `GET /api/revolutAlert` - Get Revolut alert price
- `GET /api/functions` - Get expression functions
- `POST /api/notifyUser` - Send notification
- `PUT /api/setNotificationPeriod` - Set notification period
- `GET /api/getNotificationPeriod` - Get notification period
- `GET /api/metalInfo` - Get metal information
- `GET /api/appStatus` - Get application status

### ✅ Service Layer Tested

#### Account Service
- User registration
- User authentication
- Password management
- Account validation

#### Login Service
- JWT token creation
- Login management
- Logout functionality

#### Purchase Service
- Metal purchase recording
- Metal sale recording
- Purchase history retrieval
- Profit calculations

#### Alert Service
- Alert creation
- Alert management
- Alert evaluation
- Function management

## Running Tests

### 🚀 Quick Start

```bash
# Run all tests
mvn test

# Run specific test categories
mvn test -Dtest="*ControllerTest"
mvn test -Dtest="*ServiceTest"
mvn test -Dtest="*IntegrationTest"

# Run specific test class
mvn test -Dtest="SimpleApiTest"
```

### 🎯 Test Scripts

#### Windows
```bash
# Comprehensive test runner
run-comprehensive-tests.bat

# Simple test runner
run-all-tests.bat
```

#### Manual Execution
```bash
# Unit tests only
mvn test -Dtest="*ControllerTest,*ServiceTest"

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Email tests only
mvn test -Dtest="*Email*"

# With coverage report
mvn clean test jacoco:report
```

## Test Configuration

### 🔧 Test Profiles

- **`test`** - Default test profile
- Uses H2 in-memory database
- Disables mail auto-configuration
- Enables debug logging

### 🗄️ Database Configuration

```properties
# H2 in-memory database for testing
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### 🔒 Security Configuration

```properties
# Disable security for easier testing
spring.security.user.name=test
spring.security.user.password=test
```

## Test Data

### 📊 Test Entities

- **Customer** - Test users with various states
- **Login** - JWT tokens and authentication
- **Purchase** - Metal purchase records
- **Alert** - Price alert configurations

### 🏭 Factory Methods

The `BaseControllerTest` class provides factory methods for creating test data:

```java
// Create test user
Customer user = createTestCustomer(1, "testuser", "test@example.com");

// Create test login
Login login = createTestLogin(1, 1, "test-token");

// Create test purchase
Purchase purchase = createTestPurchase(1, 1, "GOLD", 1.0, 1000.0);
```

## Mocking Strategy

### 🎭 Mockito Usage

- **Controllers** - Mock all service dependencies
- **Services** - Mock repository dependencies
- **Integration** - Use real database with test data

### 🔄 Test Isolation

- Each test method is independent
- Database is cleaned between tests
- Mocks are reset between tests

## Assertions

### ✅ Test Assertions

- **HTTP Status Codes** - Verify correct response codes
- **Response Bodies** - Check response content
- **Database State** - Verify data persistence
- **Service Calls** - Verify method invocations

### 📝 Example Assertions

```java
// HTTP response
assertEquals(HttpStatus.OK, response.getStatusCode());
assertNotNull(response.getBody());

// Database verification
Customer savedUser = customerRepository.findByUsername("testuser");
assertNotNull(savedUser);
assertEquals("test@example.com", savedUser.getEmail());

// Service verification
verify(accountService).registerNewUser(username, password, email);
```

## Coverage Reports

### 📊 Coverage Metrics

- **Line Coverage** - Percentage of lines executed
- **Branch Coverage** - Percentage of branches tested
- **Method Coverage** - Percentage of methods called

### 📈 Generating Reports

```bash
# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

## Troubleshooting

### ❌ Common Issues

1. **Compilation Errors**
   - Check import statements
   - Verify method signatures
   - Update mock configurations

2. **Test Failures**
   - Check database state
   - Verify mock setups
   - Review assertion logic

3. **Integration Test Issues**
   - Check test profile configuration
   - Verify database setup
   - Review security configuration

### 🔧 Debug Tips

- Enable debug logging in test properties
- Use `@MockitoSettings(strictness = Strictness.LENIENT)` for flexible mocking
- Add `@Transactional` for database test isolation
- Use `@DirtiesContext` for context refresh

## Best Practices

### ✅ Testing Guidelines

1. **Test Naming** - Use descriptive test method names
2. **Test Structure** - Follow Arrange-Act-Assert pattern
3. **Mock Usage** - Mock external dependencies only
4. **Test Data** - Use realistic test data
5. **Assertions** - Make specific, meaningful assertions

### 🎯 Test Categories

- **Unit Tests** - Fast, isolated, mocked dependencies
- **Integration Tests** - Slower, real dependencies, database
- **End-to-End Tests** - Full system, real external services

## Continuous Integration

### 🔄 CI/CD Integration

```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: mvn clean test

- name: Generate Coverage Report
  run: mvn jacoco:report

- name: Upload Coverage
  uses: codecov/codecov-action@v3
```

### 📊 Quality Gates

- Minimum 80% line coverage
- All tests must pass
- No critical security vulnerabilities
- Code quality checks pass

## Contributing

### 📝 Adding New Tests

1. **Follow Naming Convention** - `*Test.java` for test classes
2. **Use Appropriate Category** - Controller, Service, or Integration
3. **Add Documentation** - Update this README if needed
4. **Test Coverage** - Ensure new code is tested

### 🔄 Test Maintenance

- Update tests when APIs change
- Refactor tests for better maintainability
- Remove obsolete tests
- Keep test data current

## Support

For issues with:
- **Test Configuration** - Check application-test.properties
- **Database Issues** - Verify H2 configuration
- **Mock Problems** - Review Mockito setup
- **Integration Issues** - Check TestRestTemplate configuration
