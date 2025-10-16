package com.investment.metal.infrastructure.security;

import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.LoginService;
import com.investment.metal.infrastructure.security.JwtService;
import com.investment.metal.domain.exception.BusinessException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Infrastructure security provider for custom authentication.
 * Follows Clean Architecture principles by keeping security infrastructure concerns separate.
 */
@Component
@Slf4j
public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtService jwtService;

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
        Optional<Login> loginOpt = this.loginService.findByToken(rawToken);
        if (loginOpt.isPresent()) {
            return buildUser(loginOpt.get());
        }

        try {
            if (!this.jwtService.isTokenValid(rawToken) || !this.jwtService.isAccessToken(rawToken)) {
                return Optional.empty();
            }
            Integer userId = this.jwtService.extractUserId(rawToken);
            Login loginRecord = this.loginService.findByUserId(userId);
            if (loginRecord != null && loginRecord.getLoggedIn() != null && loginRecord.getLoggedIn() == 1) {
                return buildUser(loginRecord);
            }
            Customer customer = this.accountService.findById(userId);
            if (customer != null) {
                return Optional.of(new User(customer.getUsername(), customer.getPassword(), true, true, true, true,
                        AuthorityUtils.createAuthorityList("USER")));
            }
        } catch (Exception ex) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private Optional<User> buildUser(Login login) {
        try {
            Customer customer = this.accountService.findById(login.getUserId());
            if (customer == null) {
                return Optional.empty();
            }
            User user = new User(customer.getUsername(), customer.getPassword(), true, true, true, true,
                    AuthorityUtils.createAuthorityList("USER"));
            return Optional.of(user);
        } catch (BusinessException ex) {
            log.error("Business exception while building user from login record: {}", ex.getMessage());
            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
