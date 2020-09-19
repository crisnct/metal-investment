package com.investment.metal;

import com.investment.metal.common.*;
import com.investment.metal.database.*;
import com.investment.metal.dto.*;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.external.MetalFetchPriceBean;
import com.investment.metal.service.*;
import com.investment.metal.service.alerts.AlertService;
import com.investment.metal.service.exception.ExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * For all of those endpoints, the bearer authentication token it's necessary to be provided.</>
 * See {@code SecurityConfiguration.class}
 *
 * @author cristian.tone
 */
@RestController
@RequestMapping("/api")
public class ProtectedApiController {

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
    private MetalPricesService metalPricesService;

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
    private MetalFetchPriceBean metalPriceBean;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/blockIp", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> blockIp(
            @RequestHeader("ip") final String ip,
            @RequestHeader(value = "reason", defaultValue = "unknown reason") final String reason
    ) {
        final Login loginEntity = this.securityCheck(request);
        this.blockedIpService.blockIPForever(loginEntity.getUserId(), ip, reason);

        SimpleMessageDto dto = new SimpleMessageDto("The ip %s was blocked", ip);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/unblockIp", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> unblockIp(
            @RequestHeader("ip") final String ip
    ) {
        final Login loginEntity = this.securityCheck(request);
        this.blockedIpService.unblockIP(loginEntity.getUserId(), ip);

        SimpleMessageDto dto = new SimpleMessageDto("The ip %s was unblocked", ip);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> logout() {
        final Login loginEntity = this.securityCheck(request);
        this.loginService.logout(loginEntity);

        Customer user = this.accountService.findById(loginEntity.getUserId());
        SimpleMessageDto dto = new SimpleMessageDto("The user %s has been logged out!", user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/purchase", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> purchase(
            @RequestHeader("metalAmount") final double metalAmount,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("cost") final double cost
    ) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        final Login loginEntity = this.securityCheck(request);

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

        final Login loginEntity = this.securityCheck(request);

        this.purchaseService.sell(loginEntity.getUserId(), metalAmount, metalType, price);

        SimpleMessageDto dto = new SimpleMessageDto("Your sold of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<ProfitDto> getProfit() {
        final Login loginEntity = this.securityCheck(request);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        final ProfitDto dto = new ProfitDto(user.getUsername());
        List<Purchase> purchases = this.purchaseService.getAllPurchase(loginEntity.getUserId());
        purchases.stream()
                .map(purchase -> this.metalPricesService.calculatesUserProfit(purchase))
                .forEach(dto::addInfo);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/revolutProfit", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> calculateRevolutProfit(
            @RequestHeader("revolutPriceOunce") final double revolutPriceOunce,
            @RequestHeader("metalSymbol") final String metalSymbol
    ) {
        this.securityCheck(request);
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");

        double priceMetalNowKg = this.metalPricesService.fetchMetalPrice(metalType);
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
        final Login loginEntity = this.securityCheck(request);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        AlertFrequency alertFrequency;
        try {
            alertFrequency = AlertFrequency.valueOf(frequency);
        } catch (IllegalArgumentException e) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid frequency header")
                    .build();
        }
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        try {
            if (this.alertService.evaluateExpression(expression, 0)) {
                throw this.exceptionService
                        .createBuilder(MessageKey.INVALID_REQUEST)
                        .setArguments("The expression should be evaluated as FALSE for profit=0")
                        .build();
            }
        } catch (ScriptException e) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid expression")
                    .build();
        }

        this.alertService.addAlert(user.getId(), expression, alertFrequency, metalType);

        SimpleMessageDto dto = new SimpleMessageDto("Alert was added");
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/getAlerts", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<AlertsDto> getAlerts() {
        final Login loginEntity = this.securityCheck(request);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        final List<AlertDto> alerts = this.alertService
                .findAllByUserId(loginEntity.getUserId())
                .stream()
                .map(DtoConversion::toDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new AlertsDto(user.getUsername(), alerts), HttpStatus.OK);
    }

    @RequestMapping(value = "/removeAlert", method = RequestMethod.DELETE)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> removeAlert(
            @RequestHeader("alertId") final long alertId
    ) {
        final Login loginEntity = this.securityCheck(request);
        final boolean matchingUser = this.alertService.findAllByUserId(loginEntity.getUserId())
                .stream()
                .anyMatch(alert -> alert.getId() == alertId);
        if (!matchingUser) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("This alert id is not belonging to this user!")
                    .build();
        }

        this.alertService.removeAlert(alertId);
        Customer user = this.accountService.findById(loginEntity.getUserId());
        return new ResponseEntity<>(new SimpleMessageDto("The alert %s was removed by user %s", alertId, user.getUsername()), HttpStatus.OK);
    }

    @RequestMapping(value = "/notifyUser", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> notifyUser(
            @RequestHeader("username") final String username
    ) {
        this.securityCheck(request);
        Customer user = this.accountService.findByUsername(username);
        this.notificationService.notifyUser(user.getId());
        String message = "The user " + username + " was notified by email about his account status.";
        return new ResponseEntity<>(new SimpleMessageDto(message), HttpStatus.OK);
    }

    @RequestMapping(value = "/setNotificationPeriod", method = RequestMethod.PUT)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> setNotificationPeriod(
            @RequestHeader("period") final int period
    ) {
        Login loginEntity = this.securityCheck(request);
        int millis = period * 1000;
        this.exceptionService.check(millis < NotificationService.MIN_NOTIFICATION_PERIOD && period != 0,
                MessageKey.INVALID_REQUEST, "Invalid period");
        this.notificationService.save(loginEntity.getUserId(), millis);
        return new ResponseEntity<>(new SimpleMessageDto("The notification period was changed to %s seconds", period), HttpStatus.OK);
    }

    @RequestMapping(value = "/getNotificationPeriod", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> getNotificationPeriod() {
        Login loginEntity = this.securityCheck(request);
        final int freq = this.notificationService.getNotificationFrequency(loginEntity.getUserId());
        return new ResponseEntity<>(new SimpleMessageDto("The notification period is %d seconds", freq / 1000), HttpStatus.OK);
    }

    @RequestMapping(value = "/metalInfo", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<AppStatusInfoDto> metalInfo() {
        this.securityCheck(request);
        Currency currency = this.currencyService.findBySymbol(CurrencyType.USD).get();

        AppStatusInfoDto dto = new AppStatusInfoDto();
        dto.setUsdRonRate(currency.getRon());
        dto.setMetalPriceHost(this.metalPriceBean.getClass().getName());
        dto.setMetalCurrencyType(this.metalPriceBean.getCurrencyType());
        for (MetalType metalType : MetalType.values()) {
            MetalPrice price = this.metalPricesService.getMetalPrice(metalType);
            double revProfit = this.revolutService.getRevolutProfitFor(metalType);

            dto.addMetalPrice(metalType, new MetalInfo(metalType.getSymbol(), price.getPrice(), revProfit));
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    private Login securityCheck(HttpServletRequest request) throws BusinessException {
        String token = Util.getTokenFromRequest(request);
        return this.loginService.checkToken(token);
    }

}
