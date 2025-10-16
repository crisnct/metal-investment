package com.investment.metal.infrastructure.controller;

import com.investment.metal.MessageKey;
import com.investment.metal.application.dto.AlertDto;
import com.investment.metal.application.dto.AlertsDto;
import com.investment.metal.application.dto.MetalInfoDto;
import com.investment.metal.application.dto.ProfitDto;
import com.investment.metal.application.service.AlertService;
import com.investment.metal.application.service.FunctionInfo;
import com.investment.metal.application.service.FunctionParam;
import com.investment.metal.application.service.MetalPriceService;
import com.investment.metal.application.service.NotificationService;
import com.investment.metal.application.service.PurchaseService;
import com.investment.metal.domain.dto.ExpressionFunctionDto;
import com.investment.metal.domain.dto.ExpressionFunctionParameterDto;
import com.investment.metal.domain.dto.ExpressionFunctionParameterMinMaxDto;
import com.investment.metal.domain.dto.ExpressionHelperDto;
import com.investment.metal.domain.exception.NoRollbackBusinessException;
import com.investment.metal.domain.model.AlertFrequency;
import com.investment.metal.domain.model.CurrencyType;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.MetalType;
import com.investment.metal.infrastructure.dto.AppStatusInfoDto;
import com.investment.metal.infrastructure.dto.SimpleMessageDto;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.mapper.DtoMapper;
import com.investment.metal.infrastructure.mapper.MetalPurchaseMapper;
import com.investment.metal.infrastructure.persistence.entity.Currency;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.persistence.entity.MetalPrice;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import com.investment.metal.infrastructure.security.AuthorizationService;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.BannedAccountsService;
import com.investment.metal.infrastructure.service.BlockedIpService;
import com.investment.metal.infrastructure.service.CurrencyService;
import com.investment.metal.infrastructure.service.EmailService;
import com.investment.metal.infrastructure.service.LoginService;
import com.investment.metal.infrastructure.service.MessageService;
import com.investment.metal.infrastructure.service.RevolutService;
import com.investment.metal.infrastructure.service.price.ExternalMetalPriceReader;
import com.investment.metal.infrastructure.util.SecureRandomGenerator;
import com.investment.metal.infrastructure.util.Util;
import com.investment.metal.infrastructure.validation.ValidationService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for protected API endpoints that require JWT authentication.
 * Handles authenticated user operations like metal purchases, sales, alerts, and profit calculations.
 * All endpoints require a valid JWT token in the Authorization header.
 * 
 * Security: All endpoints are protected by JWT authentication via SecurityConfiguration.
 * 
 * @author cristian.tone
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Protected API", description = "Protected endpoints requiring JWT authentication")
@SecurityRequirement(name = "bearerAuth")
public class ProtectedApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtectedApiController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private BlockedIpService blockedIpService;

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private LoginService loginService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MetalPurchaseMapper metalPurchaseMapper;

    @Autowired
    private MetalPriceService metalPriceService;

    @Autowired
    private RevolutService revolutService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ExternalMetalPriceReader metalPriceBean;

    @Autowired
    protected MessageService messageService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private HttpServletRequest request;


    /**
     * Block an IP address permanently.
     * This endpoint allows authenticated users to block specific IP addresses
     * with an optional reason for security purposes.
     * 
     * Business Rules:
     * - User must be authenticated with valid JWT token
     * - IP address must be provided
     * - Blocking is permanent until manually unblocked
     * 
     * @param ip the IP address to block
     * @param reason the reason for blocking (optional, defaults to "unknown reason")
     * @return ResponseEntity with success message
     */
    @RequestMapping(value = "/blockIp", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Block IP address",
            description = "Blocks an IP address permanently with an optional reason"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IP blocked successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> blockIp(
            @Parameter(description = "IP address to block", required = true)
            @RequestHeader("ip") final String ip,
            @Parameter(description = "Reason for blocking the IP", required = false)
            @RequestHeader(value = "reason", defaultValue = "unknown reason") final String reason
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateIp(ip);
        this.validationService.validateReason(reason);
        
        // Extract JWT token from request
        String token = Util.getTokenFromRequest(request);
        
        // Get authenticated user from token
        final Login loginEntity = this.loginService.getLogin(token);
        
        // Block the IP address permanently
        this.blockedIpService.blockIPForever(loginEntity.getUserId(), ip, reason);

        SimpleMessageDto dto = new SimpleMessageDto(messageService.getMessage("IP_BLOCKED", ip));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/unblockIp", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Unblock IP address",
            description = "Removes an IP address from the blocked list"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "IP unblocked successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> unblockIp(
            @Parameter(description = "IP address to unblock", required = true)
            @RequestHeader("ip") final String ip
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateIp(ip);
        
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        this.blockedIpService.unblockIP(loginEntity.getUserId(), ip);

        SimpleMessageDto dto = new SimpleMessageDto(messageService.getMessage("IP_UNBLOCKED", ip));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "User logout",
            description = "Logs out the authenticated user and invalidates all sessions for enhanced security"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully from all devices",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> logout() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        
        // SECURITY FIX: Invalidate all sessions for enhanced security
        this.loginService.invalidateAllUserSessions(loginEntity.getUserId());

        Customer user = this.accountService.findById(loginEntity.getUserId());
        SimpleMessageDto dto = new SimpleMessageDto("The user %s has been logged out from all devices! Please login again.", user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    /**
     * Record a metal purchase transaction for the authenticated user.
     * This endpoint allows users to record their precious metal purchases
     * which will be used for profit/loss calculations.
     * 
     * Business Rules:
     * - User must be authenticated with valid JWT token
     * - Metal symbol must be valid (GOLD, SILVER, PLATINUM, etc.)
     * - Amount and cost must be positive numbers
     * - Purchase is accumulated with existing holdings
     * 
     * @param metalAmount the amount of metal purchased
     * @param metalSymbol the symbol of the metal (e.g., GOLD, SILVER)
     * @param cost the total cost of the purchase
     * @return ResponseEntity with success message
     */
    @RequestMapping(value = "/purchase", method = RequestMethod.POST)
    @Bulkhead(name = "api-bulkhead")
    @RateLimiter(name = "api-rate-limiter")
    @TimeLimiter(name = "api-time-limiter")
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Record metal purchase",
            description = "Records a metal purchase transaction for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase recorded successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid metal symbol or parameters",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public CompletableFuture<ResponseEntity<SimpleMessageDto>> purchase(
            @Parameter(description = "Amount of metal purchased", required = true)
            @RequestHeader("metalAmount") final double metalAmount,
            @Parameter(description = "Symbol of the metal (e.g., GOLD, SILVER)", required = true)
            @RequestHeader("metalSymbol") final String metalSymbol,
            @Parameter(description = "Total cost of the purchase", required = true)
            @RequestHeader("cost") final double cost
    ) {
        // Capture ALL request context BEFORE entering async context
        String token = Util.getTokenFromRequest(request);
        String clientIp = Util.getClientIpAddress(request);
        
        return CompletableFuture.supplyAsync(() -> {
            // Validate input parameters to prevent SQL injection
            this.validationService.validateMetalSymbol(metalSymbol);
            this.validationService.validateNumericValue(String.valueOf(metalAmount), "metalAmount");
            this.validationService.validateNumericValue(String.valueOf(cost), "cost");
            
            // Validate metal symbol
            MetalType metalType = MetalType.lookup(metalSymbol);
            this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
            
            // Use captured data instead of accessing request
            final Login loginEntity = this.loginService.getLoginWithIp(token, clientIp);

            // Record the purchase transaction
            this.purchaseService.purchase(loginEntity.getUserId(), metalAmount, metalType, cost);

            SimpleMessageDto dto = new SimpleMessageDto("Your purchase of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/sell", method = RequestMethod.DELETE)
    @Bulkhead(name = "api-bulkhead")
    @RateLimiter(name = "api-rate-limiter")
    @TimeLimiter(name = "api-time-limiter")
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Record metal sale",
            description = "Records a metal sale transaction for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sale recorded successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid metal symbol or parameters",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public CompletableFuture<ResponseEntity<SimpleMessageDto>> sell(
            @Parameter(description = "Amount of metal sold", required = true)
            @RequestHeader("metalAmount") final double metalAmount,
            @Parameter(description = "Symbol of the metal (e.g., GOLD, SILVER)", required = true)
            @RequestHeader("metalSymbol") final String metalSymbol,
            @Parameter(description = "Price per unit of metal", required = true)
            @RequestHeader("price") final double price
    ) {
        // Capture ALL request context BEFORE entering async context
        String token = Util.getTokenFromRequest(request);
        String clientIp = Util.getClientIpAddress(request);
        
        return CompletableFuture.supplyAsync(() -> {
            // Validate input parameters to prevent SQL injection
            this.validationService.validateMetalSymbol(metalSymbol);
            this.validationService.validateNumericValue(String.valueOf(metalAmount), "metalAmount");
            this.validationService.validateNumericValue(String.valueOf(price), "price");
            
            MetalType metalType = MetalType.lookup(metalSymbol);
            this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
            Objects.requireNonNull(metalType, "Metal type must not be null");

            // Use captured data instead of accessing request
            final Login loginEntity = this.loginService.getLoginWithIp(token, clientIp);
            this.purchaseService.sell(loginEntity.getUserId(), metalAmount, metalType, price);

            SimpleMessageDto dto = new SimpleMessageDto("Your sold of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        });
    }

    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Get user profit information",
            description = "Retrieves profit information for all metals owned by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profit information retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProfitDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<ProfitDto> getProfit() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        final ProfitDto dto = new ProfitDto(user.getUsername());
        List<MetalPurchase> metalPurchases = this.purchaseService.getAllPurchase(loginEntity.getUserId());
        metalPurchases.stream()
                .map(metalPurchase -> {
                    // Convert domain model to entity for the service that still expects entities
                    Purchase purchaseEntity = this.metalPurchaseMapper.toEntity(metalPurchase);
                    return this.metalPriceService.calculatesUserProfit(purchaseEntity);
                })
                .forEach(dto::addInfo);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/revolutProfit", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Calculate Revolut profit",
            description = "Calculates profit percentage for Revolut metal prices"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profit calculated successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid metal symbol or parameters",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> calculateRevolutProfit(
            @Parameter(description = "Revolut price per ounce", required = true)
            @RequestHeader("revolutPriceOunce") final double revolutPriceOunce,
            @Parameter(description = "Symbol of the metal (e.g., GOLD, SILVER)", required = true)
            @RequestHeader("metalSymbol") final String metalSymbol
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateMetalSymbol(metalSymbol);
        this.validationService.validateNumericValue(String.valueOf(revolutPriceOunce), "revolutPriceOunce");
        
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        Objects.requireNonNull(metalType);

        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity);

        double priceMetalNowKg = this.metalPriceService.fetchMetalPrice(metalType);
        final double profit = this.revolutService.calculateRevolutProfit(revolutPriceOunce, priceMetalNowKg, metalType);

        SimpleMessageDto dto = new SimpleMessageDto("Revolut profit is %.5f%%", profit * 100);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/addAlert", method = RequestMethod.POST)
    @Bulkhead(name = "api-bulkhead")
    @RateLimiter(name = "api-rate-limiter")
    @TimeLimiter(name = "api-time-limiter")
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Add price alert",
            description = "Creates a new price alert for a specific metal with custom expression"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert added successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid expression, metal symbol, or frequency",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> addAlert(
            @Parameter(description = "Mathematical expression for the alert condition", required = true)
            @RequestHeader("expression") final String expression,
            @Parameter(description = "Symbol of the metal (e.g., GOLD, SILVER)", required = true)
            @RequestHeader("metalSymbol") final String metalSymbol,
            @Parameter(description = "Alert frequency (e.g., DAILY, WEEKLY)", required = true)
            @RequestHeader("frequency") final String frequency
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateExpression(expression);
        this.validationService.validateMetalSymbol(metalSymbol);
        this.validationService.validateFrequency(frequency);
        
        AlertFrequency alertFrequency = AlertFrequency.lookup(frequency);
        this.exceptionService.check(alertFrequency == null, MessageKey.INVALID_REQUEST, "Invalid frequency header");
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        String expInvalidMessage = this.alertService.evaluateExpression(expression).isValid();
        this.exceptionService.check(expInvalidMessage != null, MessageKey.INVALID_REQUEST, expInvalidMessage);

        Objects.requireNonNull(alertFrequency);
        Objects.requireNonNull(metalType);

        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        this.alertService.addAlert(user.getId(), expression, alertFrequency, metalType);

        return new ResponseEntity<>(new SimpleMessageDto("Alert was added"), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAlerts", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Get user alerts",
            description = "Retrieves all price alerts for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AlertsDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<AlertsDto> getAlerts() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        final List<AlertDto> alerts = this.alertService
                .findAllByUserId(loginEntity.getUserId())
                .stream()
                .map(p -> dtoMapper.toDto(p))
                .collect(Collectors.toList());

        Customer user = this.accountService.findById(loginEntity.getUserId());
        return new ResponseEntity<>(new AlertsDto(user.getUsername(), alerts), HttpStatus.OK);
    }

    @RequestMapping(value = "/removeAlert", method = RequestMethod.DELETE)
    @Bulkhead(name = "api-bulkhead")
    @RateLimiter(name = "api-rate-limiter")
    @TimeLimiter(name = "api-time-limiter")
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Remove price alert",
            description = "Removes a specific price alert by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert removed successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Alert not found or not owned by user",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> removeAlert(
            @Parameter(description = "ID of the alert to remove", required = true)
            @RequestHeader("alertId") final Integer alertId
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateIntegerValue(String.valueOf(alertId), "alertId");
        
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Integer userId = loginEntity.getUserId();

        // Enhanced authorization check - verify ownership before any operation
        final boolean matchingUser = this.alertService.findAllByUserId(userId)
                .stream()
                .anyMatch(alert -> alert.getId().equals(alertId));
        
        if (!matchingUser) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Access denied: This alert does not belong to you")
                    .build();
        }

        this.alertService.removeAlert(alertId);
        Customer user = this.accountService.findById(userId);
        SimpleMessageDto dto = new SimpleMessageDto("The alert %s was removed by user %s", alertId, user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/revolutAlert", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Get Revolut alert price",
            description = "Calculates the Revolut price needed to set an alert for a specific profit target"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alert price calculated successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid profit or metal symbol, or user hasn't purchased this metal",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> revolutAlert(
            @Parameter(description = "Target profit percentage", required = true)
            @RequestHeader("profit") final double profit,
            @Parameter(description = "Symbol of the metal (e.g., GOLD, SILVER)", required = true)
            @RequestHeader("metalSymbol") final String metalSymbol
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateMetalSymbol(metalSymbol);
        this.validationService.validateNumericValue(String.valueOf(profit), "profit");
        
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity, "The user is not logged in");

        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(profit < 0, MessageKey.INVALID_REQUEST, "profit can not be negative");
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        MetalPurchase metalPurchase = this.purchaseService.getPurchase(loginEntity.getUserId(), metalSymbol);
        this.exceptionService.check(metalPurchase == null, MessageKey.INVALID_REQUEST, "the user didn't purchase " + metalSymbol);

        // Convert domain model to entity for the service that still expects entities
        Purchase purchaseEntity = this.metalPurchaseMapper.toEntity(metalPurchase);
        double revPrice = this.metalPriceService.calculatesRevolutPrice(purchaseEntity, profit);

        SimpleMessageDto dto = new SimpleMessageDto("If you ant to be notified by Revolut when your profit is %.2f for %s then you would need to set an alert in your Revolut account for %.2f RON",
                profit, metalSymbol, revPrice);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/functions", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Get expression functions",
            description = "Retrieves available mathematical functions for creating alert expressions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Functions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExpressionHelperDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<ExpressionHelperDto> functions() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity);

        ExpressionHelperDto dto = new ExpressionHelperDto();
        for (FunctionInfo function : this.alertService.getExpressionFunctions().values()) {
            ExpressionFunctionDto funcDto = new ExpressionFunctionDto();
            funcDto.setName(function.getName());
            funcDto.setDescription(function.getDescription());
            funcDto.setReturnedType(function.getReturnedType());

            for (FunctionParam param : function.getParameters()) {
                final ExpressionFunctionParameterDto paramDto;
                if (param.getMin() == param.getMax()) {
                    paramDto = new ExpressionFunctionParameterDto();
                } else {
                    ExpressionFunctionParameterMinMaxDto tempParamDto = new ExpressionFunctionParameterMinMaxDto();
                    tempParamDto.setMin(param.getMin());
                    tempParamDto.setMax(param.getMax());
                    paramDto = tempParamDto;
                }
                paramDto.setName(param.getName());
                paramDto.setDescription(param.getDescription());
                funcDto.addParameter(paramDto);
            }

            dto.addFunction(funcDto);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/notifyUser", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Notify user",
            description = "Sends a notification email to the authenticated user about their account status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User notified successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "User not found",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> notifyUser() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity);

        // Users can only notify themselves - no username parameter needed
        Integer userId = loginEntity.getUserId();
        this.notificationService.notifyUser(userId);

        Customer user = this.accountService.findById(userId);
        SimpleMessageDto dto = new SimpleMessageDto("The user %s was notified by email about their account status.", user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/setNotificationPeriod", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Set notification period",
            description = "Sets the notification frequency period for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification period set successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid period value",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> setNotificationPeriod(
            @Parameter(description = "Notification period in days", required = true)
            @RequestHeader("period") final int period
    ) {
        // Validate input parameters to prevent SQL injection
        this.validationService.validateIntegerValue(String.valueOf(period), "period");
        
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);

        int millis = (int)TimeUnit.DAYS.toMillis(period);
        this.exceptionService.check(millis < NotificationService.MIN_NOTIFICATION_PERIOD && period != 0,
                MessageKey.INVALID_REQUEST, "Invalid period");
        this.notificationService.save(loginEntity.getUserId(), millis);

        SimpleMessageDto dto = new SimpleMessageDto("The notification period was changed to %s days", period);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/getNotificationPeriod", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Get notification period",
            description = "Retrieves the current notification frequency period for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification period retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<SimpleMessageDto> getNotificationPeriod() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);

        final int freq = this.notificationService.getNotificationFrequency(loginEntity.getUserId());
        SimpleMessageDto dto = new SimpleMessageDto("The notification period is %d days", TimeUnit.MILLISECONDS.toDays(freq));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/metalInfo", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Get metal information",
            description = "Retrieves comprehensive metal price information including current prices, Revolut rates, and currency conversions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metal information retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AppStatusInfoDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public ResponseEntity<AppStatusInfoDto> metalInfo() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity);

        Currency currency = this.currencyService.findBySymbol(CurrencyType.USD);

        AppStatusInfoDto dto = new AppStatusInfoDto();
        dto.setUsdRonRate(currency.getRon());
        dto.setMetalPriceHost(this.metalPriceBean.getClass().getName());
        dto.setMetalCurrencyType(this.metalPriceBean.getCurrencyType());
        for (MetalType metalType : MetalType.values()) {
            MetalPrice price = this.metalPriceService.getMetalPrice(metalType);
            double price1kg = price.getPrice();
            double ozq = price1kg * Util.OUNCE;
            double revProfit = this.revolutService.getRevolutProfitFor(metalType);
            double ozqRon = ozq * this.currencyService.findBySymbol(CurrencyType.USD).getRon();
            double revPriceOz = ozqRon * (1 + revProfit);
            final MetalInfoDto mp = MetalInfoDto.builder()
                    .symbol(metalType.getSymbol())
                    .price1kg(price1kg)
                    .price1oz(ozq)
                    .price1ozRON(ozqRon)
                    .revolutPriceAdjustment(revProfit)
                    .revolutPrice1oz(revPriceOz)
                    .build();
            dto.addMetalPrice(metalType, mp);
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /**
     * Delete user account and all associated data.
     * This endpoint allows authenticated users to permanently delete their account
     * and all related data including alerts, purchases, and notifications.
     * 
     * Business Rules:
     * - User must be authenticated with valid JWT token
     * - Password must be provided and verified
     * - Confirmation code must be provided (received from deleteAccountPreparation email)
     * - All user data is permanently deleted (alerts, purchases, notifications)
     * - Account deletion is irreversible
     * 
     * @param password the user's password for verification
     * @param code the confirmation code received from the preparation email
     * @return ResponseEntity with success message
     */
    @RequestMapping(value = "/deleteAccount", method = RequestMethod.DELETE)
    @Bulkhead(name = "api-bulkhead")
    @RateLimiter(name = "api-rate-limiter")
    @TimeLimiter(name = "api-time-limiter")
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Delete user account",
            description = "Permanently deletes the authenticated user's account and all associated data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid password or confirmation code",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public CompletableFuture<ResponseEntity<SimpleMessageDto>> deleteAccount(
            @Parameter(description = "User's password for verification", required = true)
            @RequestHeader("password") final String password,
            @Parameter(description = "Confirmation code for account deletion", required = true)
            @RequestHeader("code") final String code
    ) {
        // Capture request context BEFORE entering async context
        String token = Util.getTokenFromRequest(request);
        
        return CompletableFuture.supplyAsync(() -> {
            // Validate input parameters to prevent SQL injection
            this.validationService.validatePassword(password);
            this.validationService.validateString(code, 10, "code");
            
            // Extract authenticated user from JWT token (bypass validation for account deletion)
            final Login loginEntity = this.loginService.getLoginForDeletion(token);
            Objects.requireNonNull(loginEntity, "User must be authenticated");

            // Get user details
            Customer user = this.accountService.findById(loginEntity.getUserId());
            Objects.requireNonNull(user, "User not found");

            // Verify password
            boolean passwordMatches = this.passwordEncoder.matches(password, user.getPassword());
            this.exceptionService.check(!passwordMatches, MessageKey.INVALID_REQUEST, "Invalid password provided");

            // Verify confirmation code (code should be received from email)
            this.exceptionService.check(code == null || code.trim().isEmpty(), MessageKey.INVALID_REQUEST, "Confirmation code is required. Please provide the code received in your email.");

            // Validate the confirmation code
            int codeValue;
            try {
                codeValue = Integer.parseInt(code);
            } catch (NumberFormatException e) {
                this.exceptionService.check(true, MessageKey.INVALID_REQUEST, "Invalid confirmation code format. Please provide a valid numeric code.");
                return null; // This line will never be reached due to the exception above
            }

            // Delete all user-related data with code validation
            this.accountService.deleteUserAccount(loginEntity.getUserId(), codeValue);

            SimpleMessageDto dto = new SimpleMessageDto("Account for user %s has been permanently deleted along with all associated data", user.getUsername());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        });
    }

    /**
     * Send account deletion preparation email to the authenticated user.
     * This endpoint sends an email with a confirmation code that the user
     * will need to provide when actually deleting their account.
     * 
     * Business Rules:
     * - User must be authenticated with valid JWT token
     * - Email is sent to the user's registered email address
     * - Confirmation code is generated and sent via email
     * - This is a preparation step before actual account deletion
     * 
     * @return ResponseEntity with success message
     */
    @RequestMapping(value = "/deleteAccountPreparation", method = RequestMethod.POST)
    @Bulkhead(name = "api-bulkhead")
    @RateLimiter(name = "api-rate-limiter")
    @TimeLimiter(name = "api-time-limiter")
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    @Operation(
            summary = "Send account deletion preparation email",
            description = "Sends an email to the authenticated user with a confirmation code for account deletion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preparation email sent successfully",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token",
                    content = @Content(schema = @Schema(implementation = SimpleMessageDto.class)))
    })
    public CompletableFuture<ResponseEntity<SimpleMessageDto>> deleteAccountPreparation() {
        // Capture ALL request context BEFORE entering async context
        String token = Util.getTokenFromRequest(request);
        String clientIp = Util.getClientIpAddress(request);
        
        return CompletableFuture.supplyAsync(() -> {
            // Extract authenticated user from JWT token
            final Login loginEntity = this.loginService.getLoginWithIp(token, clientIp);
            Objects.requireNonNull(loginEntity, "User must be authenticated");

            // Get user details
            Customer user = this.accountService.findById(loginEntity.getUserId());
            Objects.requireNonNull(user, "User not found");

            // Generate random confirmation code for account deletion
            String confirmationCode = generateConfirmationCode();
            int codeValue = Integer.parseInt(confirmationCode);

            // Store the confirmation code in the database
            this.loginService.saveDeletionAttempt(loginEntity.getUserId(), codeValue);

            // Send preparation email to user
            this.emailService.sendDeleteAccountPreparationEmail(user, confirmationCode);

            SimpleMessageDto dto = new SimpleMessageDto("Account deletion preparation email has been sent to %s. Please check your email for the confirmation code.", user.getUsername());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        });
    }

  /**
     * Generate a secure random confirmation code for account deletion.
     * Uses cryptographically secure random number generation to prevent
     * predictable patterns and ensure proper entropy for security-sensitive operations.
     * 
     * @return a secure random 6-digit confirmation code as String
     */
    private String generateConfirmationCode() {
        // SECURITY FIX: Use secure random generation instead of Math.abs(Random.nextInt())
        // This prevents predictable patterns and ensures uniform distribution
        return SecureRandomGenerator.generateConfirmationCode();
    }
}
