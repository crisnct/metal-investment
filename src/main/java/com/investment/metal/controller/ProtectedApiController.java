package com.investment.metal.controller;

import com.investment.metal.MessageKey;
import com.investment.metal.common.*;
import com.investment.metal.database.*;
import com.investment.metal.dto.*;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.price.ExternalMetalPriceReader;
import com.investment.metal.service.*;
import com.investment.metal.service.alerts.AlertService;
import com.investment.metal.service.alerts.FunctionInfo;
import com.investment.metal.service.alerts.FunctionParam;
import com.investment.metal.service.exception.ExceptionService;
import java.util.concurrent.TimeUnit;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * For all of those endpoints, the bearer authentication token it's necessary to be provided.</>
 * See {@code SecurityConfiguration.class}
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
    private LoginService loginService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private NotificationService notificationService;

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
    private HttpServletRequest request;

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
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        this.blockedIpService.blockIPForever(loginEntity.getUserId(), ip, reason);

        SimpleMessageDto dto = new SimpleMessageDto(messageService.getMessage("IP_BLOCKED", ip));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/unblockIp", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> unblockIp(
            @RequestHeader("ip") final String ip
    ) {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        this.blockedIpService.unblockIP(loginEntity.getUserId(), ip);

        SimpleMessageDto dto = new SimpleMessageDto(messageService.getMessage("IP_UNBLOCKED", ip));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> logout() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        this.loginService.logout(loginEntity);

        Customer user = this.accountService.findById(loginEntity.getUserId());
        SimpleMessageDto dto = new SimpleMessageDto("The user %s has been logged out!", user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/purchase", method = RequestMethod.POST)
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
    public ResponseEntity<SimpleMessageDto> purchase(
            @Parameter(description = "Amount of metal purchased", required = true)
            @RequestHeader("metalAmount") final double metalAmount,
            @Parameter(description = "Symbol of the metal (e.g., GOLD, SILVER)", required = true)
            @RequestHeader("metalSymbol") final String metalSymbol,
            @Parameter(description = "Total cost of the purchase", required = true)
            @RequestHeader("cost") final double cost
    ) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);

        this.purchaseService.purchase(loginEntity.getUserId(), metalAmount, metalType, cost);

        SimpleMessageDto dto = new SimpleMessageDto("Your purchase of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/sell", method = RequestMethod.DELETE)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> sell(
            @RequestHeader("metalAmount") final double metalAmount,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("price") final double price
    ) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        Objects.requireNonNull(metalType);

        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        this.purchaseService.sell(loginEntity.getUserId(), metalAmount, metalType, price);

        SimpleMessageDto dto = new SimpleMessageDto("Your sold of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
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
        List<Purchase> purchases = this.purchaseService.getAllPurchase(loginEntity.getUserId());
        purchases.stream()
                .map(purchase -> this.metalPriceService.calculatesUserProfit(purchase))
                .forEach(dto::addInfo);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/revolutProfit", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> calculateRevolutProfit(
            @RequestHeader("revolutPriceOunce") final double revolutPriceOunce,
            @RequestHeader("metalSymbol") final String metalSymbol
    ) {
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
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> addAlert(
            @RequestHeader("expression") final String expression,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("frequency") final String frequency
    ) {
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
    public ResponseEntity<AlertsDto> getAlerts() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        final List<AlertDto> alerts = this.alertService
                .findAllByUserId(loginEntity.getUserId())
                .stream()
                .map(DtoConversion::toDto)
                .collect(Collectors.toList());

        Customer user = this.accountService.findById(loginEntity.getUserId());
        return new ResponseEntity<>(new AlertsDto(user.getUsername(), alerts), HttpStatus.OK);
    }

    @RequestMapping(value = "/removeAlert", method = RequestMethod.DELETE)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> removeAlert(
            @RequestHeader("alertId") final Integer alertId
    ) {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Integer userId = loginEntity.getUserId();

        final boolean matchingUser = this.alertService.findAllByUserId(userId)
                .stream()
                .anyMatch(alert -> alert.getId() == alertId);
        if (!matchingUser) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("This alert id is not belonging to this user!")
                    .build();
        }

        this.alertService.removeAlert(alertId);
        Customer user = this.accountService.findById(userId);
        SimpleMessageDto dto = new SimpleMessageDto("The alert %s was removed by user %s", alertId, user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/revolutAlert", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> revolutAlert(
            @RequestHeader("profit") final double profit,
            @RequestHeader("metalSymbol") final String metalSymbol
    ) {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity, "The user is not logged in");

        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(profit < 0, MessageKey.INVALID_REQUEST, "profit can not be negative");
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        Purchase purchase = this.purchaseService.getPurchase(loginEntity.getUserId(), metalSymbol);
        this.exceptionService.check(purchase == null, MessageKey.INVALID_REQUEST, "the user didn't purchase " + metalSymbol);

        double revPrice = this.metalPriceService.calculatesRevolutPrice(purchase, profit);

        SimpleMessageDto dto = new SimpleMessageDto("If you ant to be notified by Revolut when your profit is %.2f for %s then you would need to set an alert in your Revolut account for %.2f RON",
                profit, metalSymbol, revPrice);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/functions", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
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
    public ResponseEntity<SimpleMessageDto> notifyUser(
            @RequestHeader("username") final String username
    ) {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);
        Objects.requireNonNull(loginEntity);

        Customer user = this.accountService.findByUsername(username);
        this.notificationService.notifyUser(user.getId());

        SimpleMessageDto dto = new SimpleMessageDto("The user %s was notified by email about his account status.", username);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/setNotificationPeriod", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> setNotificationPeriod(
            @RequestHeader("period") final int period
    ) {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);

        int millis = (int)TimeUnit.DAYS.toMillis(period);
        this.exceptionService.check(millis < NotificationService.MIN_NOTIFICATION_PERIOD && period != 0,
                MessageKey.INVALID_REQUEST, "Invalid period");
        this.notificationService.save(loginEntity.getUserId(), millis);

        SimpleMessageDto dto = new SimpleMessageDto("The notification period was changed to %s seconds", period);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/getNotificationPeriod", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> getNotificationPeriod() {
        String token = Util.getTokenFromRequest(request);
        final Login loginEntity = this.loginService.getLogin(token);

        final int freq = this.notificationService.getNotificationFrequency(loginEntity.getUserId());

        SimpleMessageDto dto = new SimpleMessageDto("The notification period is %d days", freq);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/metalInfo", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
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
}
