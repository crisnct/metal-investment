package com.investment.metal.service;

import com.investment.metal.database.Customer;
import com.investment.metal.exceptions.BusinessException;

import java.util.List;

public interface AccountService {

    Customer findById(Long id);

    Customer registerNewUser(String username, String password, String email) throws BusinessException;

    Customer findByUsername(String username);

    Customer findByUsernameAndPassword(String username, String password);

    List<Customer> findAll();
}
