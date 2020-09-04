package com.investment.metal;


import com.investment.metal.database.Customer;
import com.investment.metal.dto.ProfitDto;
import com.investment.metal.dto.UserLoginDto;
import com.investment.metal.dto.UserRegistrationDto;
import com.investment.metal.service.CustomerService;
import com.investment.metal.service.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class InvestmentController {

    @Autowired
    private GalmarleyService service;

    @Autowired
    private CustomerService customerService;


    @Transactional
    @RequestMapping(value = "/userRegistration", method = RequestMethod.POST)
    public ResponseEntity<UserRegistrationDto> userRegistration(
            HttpServletRequest request,
            @RequestHeader String username,
            @RequestHeader String password,
            @RequestHeader String email,
            HttpServletResponse response) {

        Customer user = customerService.registerNewUser(username, password, email);
        customerService.validateAccount(user);
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setMessage("An email was sent to " + email + " with a code. Call validation request with that code");
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/login")
    public ResponseEntity<UserLoginDto> login(
            @RequestParam("username") final String username,
            @RequestParam("password") final String password,
            HttpServletResponse response
    ) {
        String token = customerService.login(username, password);
        if (StringUtils.isEmpty(token)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            UserLoginDto dto = new UserLoginDto();
            dto.setToken(token);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }
    }

    @Transactional
    @GetMapping(value = "/api/users/user/{id}", produces = "application/json")
    public Customer getUserDetail(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @Transactional
    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    public ResponseEntity<ProfitDto> getProfit(HttpServletResponse response) {
        ProfitDto dto = new ProfitDto();
        dto.setProfit(25.212);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


}
