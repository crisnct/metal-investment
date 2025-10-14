package com.investment.metal.infrastructure.security;

import com.investment.metal.domain.service.AccountService;
import com.investment.metal.domain.service.LoginService;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
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

/**
 * Infrastructure security provider for custom authentication.
 * Follows Clean Architecture principles by keeping security infrastructure concerns separate.
 */
@Component
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

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

    private Optional<User> findByToken(String rawToken) {
        Optional<User> result = Optional.empty();
        Optional<Login> login = this.loginService.findByToken(rawToken);
        if (login.isPresent()) {
            Customer customer = this.accountService.findById(login.get().getUserId());
            User user = new User(customer.getUsername(), customer.getPassword(), true, true, true, true,
                    AuthorityUtils.createAuthorityList("USER"));
            result = Optional.of(user);
        }
        return result;
    }
}
