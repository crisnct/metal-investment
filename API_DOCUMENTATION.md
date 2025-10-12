# Metal Investment API Documentation

## Overview

The Metal Investment API provides endpoints for monitoring precious metals prices and managing investment alerts for Revolut users in Romania. The API is built with Spring Boot and uses JWT authentication for protected endpoints.

## Base URL

- **Production**: `https://metal-investment-635786220311.europe-west1.run.app`
- **Local Development**: `http://localhost:8080`

## Authentication

The API uses JWT (JSON Web Token) authentication for protected endpoints. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. User Registration
- **POST** `/userRegistration`
- **Description**: Creates a new user account and sends a validation email
- **Headers**:
  - `username`: Username for the new account
  - `password`: Password for the new account
  - `email`: Email address for the new account
- **Response**: `SimpleMessageDto` with confirmation message

#### 2. Account Validation
- **POST** `/validateAccount`
- **Description**: Validates a user account using the verification code sent via email
- **Headers**:
  - `username`: Username of the account to validate
  - `code`: Verification code sent via email
- **Response**: `SimpleMessageDto` with validation confirmation

#### 3. User Login
- **POST** `/login`
- **Description**: Authenticates a user and returns a JWT token
- **Headers**:
  - `username`: Username for login
  - `password`: Password for login
- **Response**: `UserLoginDto` containing the JWT token

#### 4. Password Reset
- **POST** `/resetPassword`
- **Description**: Sends a password reset email to the user
- **Headers**:
  - `email`: Email address of the account to reset password for
- **Response**: `ResetPasswordDto` with reset token and confirmation message

#### 5. Change Password
- **PUT** `/changePassword`
- **Description**: Changes user password using a reset token and verification code
- **Headers**:
  - `code`: Verification code sent via email
  - `newPassword`: New password
  - `email`: Email address of the account
  - `token`: Password reset token
- **Response**: `SimpleMessageDto` with confirmation message

### Protected Endpoints (Authentication Required)

All protected endpoints require a valid JWT token in the Authorization header.

#### 1. IP Management

##### Block IP Address
- **POST** `/api/blockIp`
- **Description**: Blocks an IP address permanently with an optional reason
- **Headers**:
  - `ip`: IP address to block
  - `reason`: (Optional) Reason for blocking the IP
- **Response**: `SimpleMessageDto` with confirmation message

##### Unblock IP Address
- **POST** `/api/unblockIp`
- **Description**: Unblocks a previously blocked IP address
- **Headers**:
  - `ip`: IP address to unblock
- **Response**: `SimpleMessageDto` with confirmation message

#### 2. User Session Management

##### Logout
- **POST** `/api/logout`
- **Description**: Logs out the authenticated user
- **Response**: `SimpleMessageDto` with logout confirmation

#### 3. Metal Transactions

##### Record Purchase
- **POST** `/api/purchase`
- **Description**: Records a metal purchase transaction
- **Headers**:
  - `metalAmount`: Amount of metal purchased
  - `metalSymbol`: Symbol of the metal (e.g., GOLD, SILVER)
  - `cost`: Total cost of the purchase
- **Response**: `SimpleMessageDto` with purchase confirmation

##### Record Sale
- **DELETE** `/api/sell`
- **Description**: Records a metal sale transaction
- **Headers**:
  - `metalAmount`: Amount of metal sold
  - `metalSymbol`: Symbol of the metal
  - `price`: Price per unit of metal
- **Response**: `SimpleMessageDto` with sale confirmation

#### 4. Profit Information

##### Get Profit Information
- **GET** `/api/profit`
- **Description**: Retrieves profit information for all metals owned by the user
- **Response**: `ProfitDto` containing detailed profit information

##### Update Revolut Profit
- **PUT** `/api/revolutProfit`
- **Description**: Updates Revolut profit calculations
- **Headers**:
  - `metalSymbol`: Symbol of the metal
  - `revolutPriceOz`: Revolut price per ounce
- **Response**: `SimpleMessageDto` with update confirmation

#### 5. Alert Management

##### Add Alert
- **POST** `/api/addAlert`
- **Description**: Creates a new price alert for a metal
- **Headers**:
  - `metalSymbol`: Symbol of the metal to monitor
  - `expression`: Mathematical expression for the alert condition
  - `frequency`: Alert frequency (e.g., "daily", "hourly")
- **Response**: `SimpleMessageDto` with alert creation confirmation

##### Get Alerts
- **GET** `/api/getAlerts`
- **Description**: Retrieves all alerts for the authenticated user
- **Response**: `AlertsDto` containing list of user alerts

##### Remove Alert
- **DELETE** `/api/removeAlert`
- **Description**: Removes a specific alert
- **Headers**:
  - `alertId`: ID of the alert to remove
- **Response**: `SimpleMessageDto` with removal confirmation

##### Get Revolut Alert
- **GET** `/api/revolutAlert`
- **Description**: Retrieves Revolut-specific alert information
- **Headers**:
  - `metalSymbol`: Symbol of the metal
- **Response**: `SimpleMessageDto` with Revolut alert information

#### 6. Function Management

##### Get Functions
- **GET** `/api/functions`
- **Description**: Retrieves available mathematical functions for alert expressions
- **Response**: `ExpressionHelperDto` containing available functions and parameters

#### 7. Notification Management

##### Notify User
- **POST** `/api/notifyUser`
- **Description**: Sends a notification to the user
- **Headers**:
  - `message`: Notification message
- **Response**: `SimpleMessageDto` with notification confirmation

##### Set Notification Period
- **PUT** `/api/setNotificationPeriod`
- **Description**: Sets the notification period for the user
- **Headers**:
  - `period`: Notification period in minutes
- **Response**: `SimpleMessageDto` with period update confirmation

##### Get Notification Period
- **GET** `/api/getNotificationPeriod`
- **Description**: Retrieves the current notification period
- **Response**: `SimpleMessageDto` with current notification period

#### 8. Metal Information

##### Get Metal Info
- **GET** `/api/metalInfo`
- **Description**: Retrieves information about available metals
- **Response**: `MetalInfoDto` containing metal information

## Data Transfer Objects (DTOs)

### SimpleMessageDto
```json
{
  "message": "string"
}
```

### UserLoginDto
```json
{
  "token": "string"
}
```

### ResetPasswordDto
```json
{
  "token": "string",
  "message": "string"
}
```

### ProfitDto
```json
{
  "username": "string",
  "time": "timestamp",
  "metalInfo": [
    {
      "metalSymbol": "string",
      "amount": "number",
      "profit": "number",
      "profitPercentage": "number"
    }
  ]
}
```

### AlertDto
```json
{
  "id": "number",
  "metalSymbol": "string",
  "expression": "string",
  "frequency": "string"
}
```

### AlertsDto
```json
{
  "alerts": [
    {
      "id": "number",
      "metalSymbol": "string",
      "expression": "string",
      "frequency": "string"
    }
  ]
}
```

## Error Responses

All endpoints can return the following error responses:

- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Invalid or missing authentication token
- **403 Forbidden**: Account not validated or banned
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server error

Error responses follow the `SimpleMessageDto` format:

```json
{
  "message": "Error description"
}
```

## Swagger UI

The API documentation is available through Swagger UI at:
- **Production**: `https://metal-investment-635786220311.europe-west1.run.app/swagger-ui.html`
- **Local**: `http://localhost:8080/swagger-ui.html`

## Example Usage

### 1. Register a new user
```bash
curl -X POST "https://metal-investment-635786220311.europe-west1.run.app/userRegistration" \
  -H "username: nelucristian" \
  -H "password: test123" \
  -H "email: nelucristian2005@gmail.com"
```

### 2. Login
```bash
curl -X POST "https://metal-investment-635786220311.europe-west1.run.app/login" \
  -H "username: nelucristian" \
  -H "password: test123"
```

### 3. Get profit information (authenticated)
```bash
curl -X GET "https://metal-investment-635786220311.europe-west1.run.app/api/profit" \
  -H "Authorization: Bearer <your-jwt-token>"
```

### 4. Add an alert (authenticated)
```bash
curl -X POST "https://metal-investment-635786220311.europe-west1.run.app/api/addAlert" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "metalSymbol: GOLD" \
  -H "expression: price > 2000" \
  -H "frequency: daily"
```

## Rate Limiting

The API implements rate limiting to prevent abuse:
- **Global Rate Limit**: 10 requests per minute per IP
- **User Rate Limit**: 100 requests per minute per authenticated user

## Security Features

- JWT-based authentication
- IP blocking capabilities
- Account validation via email
- Password reset functionality
- Rate limiting
- Input validation and sanitization

## Support

For support or questions about the API, please contact:
- Email: support@metalinvestment.com
- Documentation: Available at the Swagger UI endpoint
