package com.investment.metal.service;

import com.investment.metal.database.Customer;
import com.investment.metal.exceptions.BusinessException;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public interface CustomerService {

    String login(String username, String password);

    Optional<User> findByToken(String token);

    Customer findById(Long id);

    Customer registerNewUser(String username, String password, String email) throws BusinessException;

    void validateAccount(Customer user);

}
