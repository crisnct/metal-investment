package com.investment.metal.infrastructure.service;

import com.investment.metal.MessageKey;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.persistence.entity.Alert;
import com.investment.metal.infrastructure.persistence.entity.Purchase;
import com.investment.metal.infrastructure.persistence.entity.Login;
import com.investment.metal.infrastructure.persistence.repository.CustomerRepository;
import com.investment.metal.infrastructure.persistence.repository.AlertRepository;
import com.investment.metal.infrastructure.persistence.repository.PurchaseRepository;
import com.investment.metal.infrastructure.persistence.repository.LoginRepository;
import com.investment.metal.infrastructure.exception.ExceptionService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    private ExceptionService exceptionService;

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

    /**
     * Permanently delete a user account and all associated data.
     * This method removes all user-related data including:
     * - User account (Customer entity)
     * - All alerts created by the user
     * - All purchases made by the user
     * - All login sessions for the user
     * 
     * @param userId the ID of the user to delete
     * @param confirmationCode the confirmation code received via email
     * @throws BusinessException if user is not found or confirmation code is invalid
     */
    @Transactional
    public void deleteUserAccount(Integer userId, int confirmationCode) throws BusinessException {
        // Verify user exists
        Customer user = findById(userId);
        
        // Validate the confirmation code
        Optional<Login> userLogin = loginRepository.findByUserId(userId);
        if (userLogin.isPresent()) {
            Login login = userLogin.get();
            this.exceptionService.check(login.getValidationCode() != confirmationCode, MessageKey.INVALID_REQUEST, "Invalid confirmation code. Please provide the correct code received in your email.");
        } else {
            this.exceptionService.check(true, MessageKey.INVALID_REQUEST, "No confirmation code found. Please request a new deletion preparation email.");
        }
        
        // Delete all alerts for this user
        Optional<List<Alert>> userAlerts = alertRepository.findByUserId(userId);
        if (userAlerts.isPresent()) {
            alertRepository.deleteAll(userAlerts.get());
        }
        
        // Delete all purchases for this user
        Optional<List<Purchase>> userPurchases = purchaseRepository.findByUserId(userId);
        if (userPurchases.isPresent()) {
            purchaseRepository.deleteAll(userPurchases.get());
        }
        
        // Delete all login sessions for this user
        if (userLogin.isPresent()) {
            loginRepository.delete(userLogin.get());
        }
        
        // Finally, delete the user account
        customerRepository.delete(user);
    }

}
