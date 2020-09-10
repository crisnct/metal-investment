package com.investment.metal;

import com.investment.metal.common.AlertFrequency;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.MetalInfo;
import com.investment.metal.dto.ProfitDto;
import com.investment.metal.dto.SimpleMessageDto;
import com.investment.metal.exceptions.NoRollbackBusinessException;
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
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProtectedApiController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private PurchaseService purchaseService;

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

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.logout(token);
        Customer user = this.accountService.findById(loginEntity.getUserId());
        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("The user %s has been logged out!", user.getUsername());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/purchase", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> purchase(
            HttpServletRequest request,
            @RequestHeader("metalAmount") final double metalAmount,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("cost") final double cost,
            HttpServletResponse response) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.checkToken(token);

        this.purchaseService.purchase(loginEntity.getUserId(), metalAmount, metalType, cost);

        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Your purchase of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/sell", method = RequestMethod.DELETE)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> sell(
            HttpServletRequest request,
            @RequestHeader("metalAmount") final double metalAmount,
            @RequestHeader("metalSymbol") final String metalSymbol,
            HttpServletResponse response) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.checkToken(token);

        this.purchaseService.sell(loginEntity.getUserId(), metalAmount, metalType);

        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Your sold of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<ProfitDto> getProfit(
            HttpServletRequest request,
            HttpServletResponse response) {
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.checkToken(token);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        final ProfitDto dto = new ProfitDto(user.getUsername());
        List<Purchase> purchases = this.purchaseService.getAllPurchase(loginEntity.getUserId());
        if (!purchases.isEmpty()) {
            for (Purchase purchase : purchases) {
                final MetalInfo info = this.metalPricesService.calculatesUserProfit(purchase);
                dto.addInfo(info);
            }
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/revolutProfit", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> calculateRevolutProfit(
            HttpServletRequest request,
            @RequestHeader("revolutPriceOunce") final double revolutPriceOunce,
            @RequestHeader("metalSymbol") final String metalSymbol,
            HttpServletResponse response) {
        String token = Util.getTokenFromRequest(request);
        this.loginService.checkToken(token);
        MetalType metalType = MetalType.lookup(metalSymbol);
        this.exceptionService.check(metalType == null, MessageKey.INVALID_REQUEST, "metalSymbol header is invalid");

        double priceMetalNowKg = this.metalPricesService.fetchMetalPrice(metalType);
        final double profit = this.revolutService.calculateRevolutProfit(revolutPriceOunce, priceMetalNowKg, metalType);

        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Revolut profit is %.5f%%", profit * 100);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/addAlert", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> addAlert(
            HttpServletRequest request,
            @RequestHeader("expression") final String expression,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("frequency") final String frequency,
            HttpServletResponse response) {
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.checkToken(token);
        Customer user = this.accountService.findById(loginEntity.getUserId());

        AlertFrequency alertFrequency;
        try {
            alertFrequency = AlertFrequency.valueOf(frequency);
        } catch (IllegalArgumentException e) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Frequency header is invalid")
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

        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Alert was added");
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

}
