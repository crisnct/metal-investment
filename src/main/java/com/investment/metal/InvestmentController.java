package com.investment.metal;


import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.dto.ProfitDto;
import com.investment.metal.dto.SimpleMessageDto;
import com.investment.metal.dto.UserLoginDto;
import com.investment.metal.dto.UserRegistrationDto;
import com.investment.metal.exceptions.NoRollbackBusinessException;
import com.investment.metal.service.AccountService;
import com.investment.metal.service.BannedAccountsService;
import com.investment.metal.service.GalmarleyService;
import com.investment.metal.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
public class InvestmentController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private GalmarleyService service;


    @RequestMapping(value = "/userRegistration", method = RequestMethod.POST)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<UserRegistrationDto> userRegistration(
            HttpServletRequest request,
            @RequestHeader String username,
            @RequestHeader String password,
            @RequestHeader String email,
            HttpServletResponse response) {

        Customer user = this.accountService.registerNewUser(username, password, email);
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
        Customer user = this.accountService.findByUsernameAndPassword(username, password);
        this.bannedAccountsService.checkBanned(user.getId());
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
        Optional<Customer> user = this.accountService.findById(loginEntity.getUserId());
        if (user.isPresent()) {
            SimpleMessageDto dto = new SimpleMessageDto();
            dto.setMessage("The user " + user.get().getUsername() + " has been logged out!");
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/api/profit", method = RequestMethod.GET)
    @Transactional(noRollbackFor = NoRollbackBusinessException.class)
    public ResponseEntity<ProfitDto> getProfit(
            HttpServletRequest request,
            HttpServletResponse response) {
        String token = Util.getTokenFromRequest(request);
        this.loginService.checkToken(token);

        ProfitDto dto = new ProfitDto();
        dto.setProfit(25.212);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


}
