package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.database.Customer;
import com.investment.metal.database.CustomerRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService extends AbstractService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private EmailService emailService;

    public Customer registerNewUser(String username, String password, String email) throws BusinessException {
        Optional<Customer> customerOp = this.customerRepository.findByUsername(username);
        if (customerOp.isPresent()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.ALREADY_EXISTING_USER)
                    .setArguments(username)
                    .build();
        }
        Optional<Customer> customerOp2 = this.customerRepository.findByEmail(email);
        if (customerOp2.isPresent()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.ALREADY_EXISTING_EMAIL_ADDRESS)
                    .setArguments(email)
                    .build();
        }

        final Customer user = new Customer();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        return this.customerRepository.save(user);
    }

    public void updatePassword(Customer user, String newPassword) {
        user.setPassword(newPassword);
        this.customerRepository.save(user);
    }

    public Customer findByUsername(String username) throws BusinessException {
        return this.customerRepository
                .findByUsername(username)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.INEXISTING_USER)
                        .setArguments(username)
                        .build()
                );
    }

    public Customer findByEmail(String email) throws BusinessException {
        return this.customerRepository
                .findByEmail(email)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.INVALID_REQUEST)
                        .setArguments("Inexisting email address in the database")
                        .build()
                );
    }

    public List<Customer> findAll() {
        return this.customerRepository.findAll();
    }

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
