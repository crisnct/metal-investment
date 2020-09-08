package com.investment.metal.service.impl;

import com.investment.metal.database.Customer;
import com.investment.metal.database.CustomerRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.MessageKey;
import com.investment.metal.service.AbstractService;
import com.investment.metal.service.AccountService;
import com.investment.metal.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("customerService")
public class DefaultAccountService extends AbstractService implements AccountService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private EmailService emailService;

    @Override
    public Customer registerNewUser(String username, String password, String email) throws BusinessException {
        //TODO check if username is unique

        //TODO validate email and throw exception if case

        Optional<Customer> customerOp = this.customerRepository.findByUsernameAndPassword(username, password);
        if (customerOp.isPresent()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.ALREADY_EXISTING_USER)
                    .setArguments(username)
                    .build();
        }

        final Customer user = new Customer();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return this.customerRepository.save(user);
    }

    @Override
    public Customer findByUsername(String username) throws BusinessException {
        return this.customerRepository
                .findByUsername(username)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.INEXISTING_USER)
                        .setArguments(username)
                        .build()
                );
    }

    @Override
    public Customer findByUsernameAndPassword(String username, String password) {
        return this.customerRepository
                .findByUsernameAndPassword(username, password)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.LOGIN_FAILED)
                        .setArguments(username)
                        .build()
                );
    }

    @Override
    public Customer findById(Long id) throws BusinessException {
        return this.customerRepository
                .findById(id)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.INEXISTING_USER_ID)
                        .setArguments(id)
                        .build()
                );
    }
}
