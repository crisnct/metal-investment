package com.investment.metal.security;

import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.service.AccountService;
import com.investment.metal.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoginService loginService;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        //
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken authToken) throws AuthenticationException {
        final Object token = authToken.getCredentials();
        return Optional
                .ofNullable(token)
                .map(String::valueOf)
                .flatMap(this::findByToken)
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find user with authentication token=" + token));
    }

    private Optional<User> findByToken(String token) {
        Optional<User> result = Optional.empty();
        final Optional<Login> login = this.loginService.findByToken(token);
        if (login.isPresent()) {
            Optional<Customer> customerOp = this.accountService.findById(login.get().getUserId());
            if (customerOp.isPresent()) {
                Customer customer = customerOp.get();
                User user = new User(customer.getUsername(), customer.getPassword(), true, true, true, true,
                        AuthorityUtils.createAuthorityList("USER"));
                result = Optional.of(user);
            }
        }
        return result;
    }

}
