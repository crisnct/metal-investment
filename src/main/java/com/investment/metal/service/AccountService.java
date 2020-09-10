package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.database.Customer;
import com.investment.metal.database.CustomerRepository;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AccountService extends AbstractService {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BannedAccountsService bannedAccountsService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private EmailService emailService;

    public Customer registerNewUser(String username, String password, String email) throws BusinessException {
        if (!isValidEmailAddress(email)) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("Invalid email address!")
                    .build();
        }

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

    public Customer findByUsername(String username) throws BusinessException {
        return this.customerRepository
                .findByUsername(username)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.INEXISTING_USER)
                        .setArguments(username)
                        .build()
                );
    }

    public Customer findByUsernameAndPassword(String username, String password) {
        return this.customerRepository
                .findByUsernameAndPassword(username, password)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.LOGIN_FAILED)
                        .setArguments(username)
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

    private boolean isValidEmailAddress(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

}
