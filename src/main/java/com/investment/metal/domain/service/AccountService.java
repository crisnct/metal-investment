package com.investment.metal.domain.service;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.repository.CustomerRepository;
import com.investment.metal.infrastructure.service.AbstractService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService extends AbstractService {

    @Autowired
    private CustomerRepository customerRepository;

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

    public Customer findById(Integer id) throws BusinessException {
        return this.customerRepository
                .findById((long)id)
                .orElseThrow(() -> this.exceptionService
                        .createBuilder(MessageKey.INEXISTING_USER_ID)
                        .setArguments(id)
                        .build()
                );
    }

}
