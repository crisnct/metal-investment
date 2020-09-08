package com.investment.metal;


import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.*;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.CustomErrorCodes;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class InvestmentController {

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

    @RequestMapping(value = "/userRegistration", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<UserRegistrationDto> userRegistration(
            HttpServletRequest request,
            @RequestHeader String username,
            @RequestHeader String password,
            @RequestHeader String email,
            HttpServletResponse response) {

        Customer user = this.accountService.registerNewUser(username, this.passwordEncoder.encode(password), email);
        this.loginService.validateAccount(user);
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setMessage("An email was sent to " + email + " with a code. Call validation request with that code");
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/validateAccount", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<UserLoginDto> validation(
            HttpServletRequest request,
            @RequestHeader("username") final String username,
            @RequestHeader("code") final int code,
            HttpServletResponse response
    ) {
        Customer user = this.accountService.findByUsername(username);
        this.bannedAccountsService.checkBanned(user.getId());
        String token = this.loginService.verifyCode(user.getId(), code);
        UserLoginDto dto = new UserLoginDto();
        dto.setToken(token);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/login", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<UserLoginDto> login(
            HttpServletRequest request,
            @RequestHeader("username") final String username,
            @RequestHeader("password") final String password,
            HttpServletResponse response
    ) {
        final Customer user = this.accountService.findByUsername(username);
        this.bannedAccountsService.checkBanned(user.getId());
        if (!passwordEncoder.matches(password, user.getPassword())){
            this.loginService.markLoginFailed(user);
            throw new BusinessException(CustomErrorCodes.USER_RETRIEVE, "Password doesn't match!");
        }
        String token = this.loginService.login(user);
        UserLoginDto dto = new UserLoginDto();
        dto.setToken(token);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/logout", method = RequestMethod.POST)
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

    @RequestMapping(value = "/api/purchase", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> purchase(
            HttpServletRequest request,
            @RequestHeader("metalAmount") final double metalAmount,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("cost") final double cost,
            HttpServletResponse response) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        Util.check(metalType == null, CustomErrorCodes.PURCHASE, "metalSymbol header is invalid");
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.checkToken(token);

        this.purchaseService.purchase(loginEntity.getUserId(), metalAmount, metalType, cost);

        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Your purchase of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/sell", method = RequestMethod.DELETE)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> sell(
            HttpServletRequest request,
            @RequestHeader("metalAmount") final double metalAmount,
            @RequestHeader("metalSymbol") final String metalSymbol,
            @RequestHeader("value") final double value,
            HttpServletResponse response) {
        MetalType metalType = MetalType.lookup(metalSymbol);
        Util.check(metalType == null, CustomErrorCodes.PURCHASE, "metalSymbol header is invalid");
        String token = Util.getTokenFromRequest(request);
        Login loginEntity = this.loginService.checkToken(token);

        this.purchaseService.sell(loginEntity.getUserId(), metalAmount, metalType, value);

        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Your sold of %.7f %s was recorded in the database", metalAmount, metalType.getSymbol());
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/profit", method = RequestMethod.GET)
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

    @RequestMapping(value = "/api/revolutProfit", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<SimpleMessageDto> calculateRevolutProfit(
            HttpServletRequest request,
            @RequestHeader("revolutPriceOunce") final double revolutPriceOunce,
            @RequestHeader("usdRonRate") final double usdRonRate,
            @RequestHeader("metalSymbol") final String metalSymbol,
            HttpServletResponse response) {
        String token = Util.getTokenFromRequest(request);
        this.loginService.checkToken(token);
        MetalType metalType = MetalType.lookup(metalSymbol);
        Util.check(metalType == null, CustomErrorCodes.VALIDATE_ACCOUNT, "Invalid metalSymbol header");
        final double profit = this.revolutService.calculateRevolutProfit(revolutPriceOunce, usdRonRate, metalType);
        SimpleMessageDto dto = new SimpleMessageDto();
        dto.setMessage("Revolut profit is %.5f%%", profit * 100);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

}
