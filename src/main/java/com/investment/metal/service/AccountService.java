package com.investment.metal.service;

import com.investment.metal.database.Customer;
import com.investment.metal.exceptions.BusinessException;

import java.util.Optional;

public interface AccountService {

    Optional<Customer> findById(Long id);

    Customer registerNewUser(String username, String password, String email) throws BusinessException;

    Customer findByUsername(String username);

    Customer findByUsernameAndPassword(String username, String password);

}
