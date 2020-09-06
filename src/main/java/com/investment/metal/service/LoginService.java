package com.investment.metal.service;

import com.investment.metal.database.Customer;
import com.investment.metal.database.Login;
import com.investment.metal.exceptions.BusinessException;

import java.util.Optional;

public interface LoginService {

    void saveAttempt(final long userId, final int validationCode) throws BusinessException;

    String verifyCode(long userId, int code) throws BusinessException;

    Optional<Login> findByToken(String token);

    void validateAccount(Customer user) throws BusinessException;

    String login(Customer user) throws BusinessException;

    Login logout(String token) throws BusinessException;

    void checkToken(String token) throws BusinessException;
}
