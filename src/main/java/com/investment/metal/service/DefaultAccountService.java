package com.investment.metal.service;

import com.investment.metal.database.Customer;
import com.investment.metal.database.CustomerRepository;
import com.investment.metal.exceptions.BusinessException;
import com.investment.metal.exceptions.CustomErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("customerService")
public class DefaultAccountService implements AccountService {

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
            throw new BusinessException(CustomErrorCodes.REGISTER_NEW_USER, "There user " + username + " is already registered in the database");
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
                .orElseThrow(() -> new BusinessException(CustomErrorCodes.USER_RETRIEVE, "The username " + username + " doesn't exist in the database"));
    }

    @Override
    public Customer findByUsernameAndPassword(String username, String password) {
        return this.customerRepository
                .findByUsernameAndPassword(username, password)
                .orElseThrow(() -> new BusinessException(CustomErrorCodes.USER_RETRIEVE, "Failed to login for " + username));
    }

    @Override
    public Customer findById(Long id) throws BusinessException {
        return this.customerRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(CustomErrorCodes.USER_RETRIEVE, "Can not find user with id " + id));
    }
}
